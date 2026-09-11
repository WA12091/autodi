package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AutoDialerDatabase
import com.example.data.model.CallDisposition
import com.example.data.model.CallLog
import com.example.data.model.Campaign
import com.example.data.model.CampaignSchedule
import com.example.data.model.ContactLead
import com.example.data.repository.AutoDialerRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab(val label: String) {
    DASHBOARD("Dashboard"),
    CAMPAIGNS("Campaigns"),
    SCHEDULES("Schedules"),
    SETTINGS("Settings")
}

enum class SessionState {
    IDLE,
    COUNTDOWN,
    DIALING,
    ON_CALL,
    DISPOSITION_PENDING,
    PAUSED,
    FINISHED
}

data class AnalyticsData(
    val totalCalls: Int = 0,
    val connectedCalls: Int = 0,
    val convertedCalls: Int = 0,
    val connectionRate: Float = 0f,
    val conversionRate: Float = 0f,
    val avgDurationSeconds: Int = 0,
    val totalDurationSeconds: Int = 0,
    val dispositionCounts: Map<String, Int> = emptyMap(),
    val activeCampaignsCount: Int = 0,
    val totalLeadsCount: Int = 0,
    val pendingLeadsCount: Int = 0
)

class AutoDialerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AutoDialerRepository

    // Scheduled Execution Alert Event
    private val _scheduleAlert = MutableStateFlow<String?>(null)
    val scheduleAlert: StateFlow<String?> = _scheduleAlert.asStateFlow()

    fun dismissScheduleAlert() {
        _scheduleAlert.value = null
    }

    init {
        val db = AutoDialerDatabase.getDatabase(application)
        repository = AutoDialerRepository(
            db.campaignDao(),
            db.contactLeadDao(),
            db.callLogDao(),
            db.campaignScheduleDao()
        )
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
        startScheduleMonitor()
    }

    // UI Tab State
    private val _selectedTab = MutableStateFlow(AppTab.DASHBOARD)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    // Repository Flows
    val allCampaigns = repository.allCampaigns.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allCallLogs = repository.allCallLogs.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allLeads = repository.allLeads.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allSchedules = repository.allSchedules.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val pendingSchedules = repository.pendingSchedules.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Analytics Calculation Flow
    val analytics: StateFlow<AnalyticsData> = combine(
        allCallLogs,
        allCampaigns,
        allLeads
    ) { logs, campaigns, leads ->
        val total = logs.size
        val connected = logs.count { it.disposition == CallDisposition.CONNECTED.name || it.disposition == CallDisposition.CONVERTED.name }
        val converted = logs.count { it.disposition == CallDisposition.CONVERTED.name }
        val totalDuration = logs.sumOf { it.durationSeconds }
        val avgDuration = if (total > 0) totalDuration / total else 0
        val connRate = if (total > 0) (connected.toFloat() / total) * 100f else 0f
        val convRate = if (total > 0) (converted.toFloat() / total) * 100f else 0f

        val dispCounts = mutableMapOf<String, Int>()
        CallDisposition.entries.forEach { disp ->
            dispCounts[disp.name] = logs.count { it.disposition == disp.name }
        }

        val activeCamps = campaigns.count { it.status == "ACTIVE" }
        val pendingLeads = leads.count { it.status == "PENDING" }

        AnalyticsData(
            totalCalls = total,
            connectedCalls = connected,
            convertedCalls = converted,
            connectionRate = connRate,
            conversionRate = convRate,
            avgDurationSeconds = avgDuration,
            totalDurationSeconds = totalDuration,
            dispositionCounts = dispCounts,
            activeCampaignsCount = activeCamps,
            totalLeadsCount = leads.size,
            pendingLeadsCount = pendingLeads
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsData())

    // Settings State
    val callDelaySeconds = MutableStateFlow(5)
    val autoAdvance = MutableStateFlow(true)
    val directDialMode = MutableStateFlow(false) // false: ACTION_DIAL (safe everywhere), true: ACTION_CALL
    val vibrateOnDial = MutableStateFlow(true)
    val maxRetries = MutableStateFlow(2)

    // Active Dialing Session State
    private val _sessionState = MutableStateFlow(SessionState.IDLE)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _activeCampaign = MutableStateFlow<Campaign?>(null)
    val activeCampaign: StateFlow<Campaign?> = _activeCampaign.asStateFlow()

    private val _activeLead = MutableStateFlow<ContactLead?>(null)
    val activeLead: StateFlow<ContactLead?> = _activeLead.asStateFlow()

    private val _currentQueue = MutableStateFlow<List<ContactLead>>(emptyList())
    val currentQueue: StateFlow<List<ContactLead>> = _currentQueue.asStateFlow()

    private val _queueIndex = MutableStateFlow(0)
    val queueIndex: StateFlow<Int> = _queueIndex.asStateFlow()

    private val _countdownRemaining = MutableStateFlow(5)
    val countdownRemaining: StateFlow<Int> = _countdownRemaining.asStateFlow()

    private val _callTimerSeconds = MutableStateFlow(0)
    val callTimerSeconds: StateFlow<Int> = _callTimerSeconds.asStateFlow()

    private val _isCockpitOpen = MutableStateFlow(false)
    val isCockpitOpen: StateFlow<Boolean> = _isCockpitOpen.asStateFlow()

    private var countdownJob: Job? = null
    private var callTimerJob: Job? = null

    // Call History Search & Filter State
    val historySearchQuery = MutableStateFlow("")
    val historySelectedDisposition = MutableStateFlow("ALL")

    fun openCockpit() {
        _isCockpitOpen.value = true
    }

    fun closeCockpit() {
        _isCockpitOpen.value = false
    }

    // Start Campaign Dialing Session
    fun startCampaignDialer(campaign: Campaign) {
        viewModelScope.launch {
            val leads = repository.getDialableLeads(campaign.id, campaign.maxRetries)
            if (leads.isEmpty()) {
                // If all dialed, reset and retry or inform
                repository.resetCampaignLeads(campaign.id)
                val refreshed = repository.getDialableLeads(campaign.id, campaign.maxRetries)
                if (refreshed.isEmpty()) return@launch
                _currentQueue.value = refreshed
            } else {
                _currentQueue.value = leads
            }

            _activeCampaign.value = campaign
            _queueIndex.value = 0
            _activeLead.value = _currentQueue.value.getOrNull(0)
            _isCockpitOpen.value = true

            startCountdownForLead()
        }
    }

    private fun startCountdownForLead() {
        countdownJob?.cancel()
        callTimerJob?.cancel()
        _callTimerSeconds.value = 0

        val lead = _activeLead.value
        if (lead == null) {
            _sessionState.value = SessionState.FINISHED
            return
        }

        _sessionState.value = SessionState.COUNTDOWN
        val delaySec = _activeCampaign.value?.delaySeconds ?: callDelaySeconds.value
        _countdownRemaining.value = delaySec

        countdownJob = viewModelScope.launch {
            for (i in delaySec downTo 1) {
                _countdownRemaining.value = i
                delay(1000)
            }
            _countdownRemaining.value = 0
            dialCurrentLead()
        }
    }

    fun dialNow() {
        countdownJob?.cancel()
        dialCurrentLead()
    }

    fun skipCurrentLead() {
        countdownJob?.cancel()
        callTimerJob?.cancel()
        viewModelScope.launch {
            val lead = _activeLead.value
            if (lead != null) {
                repository.updateLead(lead.copy(status = "SKIPPED"))
                val campaign = _activeCampaign.value
                repository.insertCallLog(
                    CallLog(
                        campaignId = campaign?.id ?: 0,
                        campaignName = campaign?.name ?: "Ad-hoc Call",
                        leadId = lead.id,
                        contactName = lead.name,
                        phoneNumber = lead.phoneNumber,
                        company = lead.company,
                        timestamp = System.currentTimeMillis(),
                        durationSeconds = 0,
                        disposition = "SKIPPED",
                        notes = "Skipped by agent"
                    )
                )
            }
            advanceToNextLead()
        }
    }

    fun pauseSession() {
        countdownJob?.cancel()
        _sessionState.value = SessionState.PAUSED
    }

    fun resumeSession() {
        if (_sessionState.value == SessionState.PAUSED) {
            startCountdownForLead()
        }
    }

    fun stopSession() {
        countdownJob?.cancel()
        callTimerJob?.cancel()
        _sessionState.value = SessionState.IDLE
        _activeCampaign.value = null
        _activeLead.value = null
        _isCockpitOpen.value = false
    }

    private fun dialCurrentLead() {
        val lead = _activeLead.value ?: return
        triggerVibration()

        _sessionState.value = SessionState.ON_CALL
        viewModelScope.launch {
            repository.updateLead(
                lead.copy(
                    status = "CALLING",
                    callAttempts = lead.callAttempts + 1,
                    lastDialedAt = System.currentTimeMillis()
                )
            )
        }

        // Start call duration timer
        callTimerJob?.cancel()
        _callTimerSeconds.value = 0
        callTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _callTimerSeconds.value += 1
            }
        }

        // Dispatch call Intent to Android system
        val app = getApplication<Application>()
        val phoneUri = Uri.parse("tel:${lead.phoneNumber.replace(" ", "")}")
        val action = if (directDialMode.value) Intent.ACTION_CALL else Intent.ACTION_DIAL
        val intent = Intent(action, phoneUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            app.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to ACTION_DIAL if ACTION_CALL permission or telephony isn't available
            val fallbackIntent = Intent(Intent.ACTION_DIAL, phoneUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                app.startActivity(fallbackIntent)
            } catch (_: Exception) {}
        }
    }

    fun completeCallWithDisposition(disposition: CallDisposition, notes: String = "") {
        callTimerJob?.cancel()
        val duration = _callTimerSeconds.value
        val lead = _activeLead.value
        val campaign = _activeCampaign.value

        viewModelScope.launch {
            if (lead != null && campaign != null) {
                // Update Lead
                repository.updateLead(
                    lead.copy(
                        status = disposition.name,
                        lastDisposition = disposition.name,
                        notes = if (notes.isNotBlank()) notes else lead.notes
                    )
                )

                // Record Call Log
                repository.insertCallLog(
                    CallLog(
                        campaignId = campaign.id,
                        campaignName = campaign.name,
                        leadId = lead.id,
                        contactName = lead.name,
                        phoneNumber = lead.phoneNumber,
                        company = lead.company,
                        timestamp = System.currentTimeMillis(),
                        durationSeconds = duration,
                        disposition = disposition.name,
                        notes = notes
                    )
                )
            }

            if (autoAdvance.value) {
                advanceToNextLead()
            } else {
                _sessionState.value = SessionState.DISPOSITION_PENDING
            }
        }
    }

    fun advanceToNextLead() {
        val nextIdx = _queueIndex.value + 1
        if (nextIdx < _currentQueue.value.size) {
            _queueIndex.value = nextIdx
            _activeLead.value = _currentQueue.value[nextIdx]
            startCountdownForLead()
        } else {
            _sessionState.value = SessionState.FINISHED
            callTimerJob?.cancel()
            countdownJob?.cancel()
        }
    }

    private fun triggerVibration() {
        if (!vibrateOnDial.value) return
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(100)
            }
        } catch (_: Exception) {}
    }

    // Direct Call from History or Contact List
    fun directCallNumber(name: String, phoneNumber: String, company: String = "") {
        val app = getApplication<Application>()
        val phoneUri = Uri.parse("tel:${phoneNumber.replace(" ", "")}")
        val intent = Intent(Intent.ACTION_DIAL, phoneUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            app.startActivity(intent)
            // Log this single direct call
            viewModelScope.launch {
                repository.insertCallLog(
                    CallLog(
                        campaignId = 0,
                        campaignName = "Direct Dial",
                        leadId = 0,
                        contactName = name,
                        phoneNumber = phoneNumber,
                        company = company,
                        timestamp = System.currentTimeMillis(),
                        durationSeconds = 0,
                        disposition = "CONNECTED",
                        notes = "Manual single call initiated"
                    )
                )
            }
        } catch (_: Exception) {}
    }

    // Campaign CRUD Actions
    fun createCampaignWithLeads(
        name: String,
        description: String,
        delaySeconds: Int,
        maxRetries: Int,
        leads: List<ContactLead>
    ) {
        viewModelScope.launch {
            val campaignId = repository.insertCampaign(
                Campaign(
                    name = name,
                    description = description,
                    delaySeconds = delaySeconds,
                    maxRetries = maxRetries,
                    status = "ACTIVE"
                )
            )

            if (leads.isNotEmpty()) {
                val prepared = leads.map { it.copy(campaignId = campaignId) }
                repository.insertLeads(prepared)
            }
        }
    }

    fun createCampaign(name: String, description: String, delaySeconds: Int, maxRetries: Int, leadsInput: String) {
        viewModelScope.launch {
            val campaignId = repository.insertCampaign(
                Campaign(
                    name = name,
                    description = description,
                    delaySeconds = delaySeconds,
                    maxRetries = maxRetries,
                    status = "ACTIVE"
                )
            )

            val parsedLeads = com.example.data.model.LeadParser.parseText(leadsInput).map {
                it.toContactLead(campaignId)
            }

            if (parsedLeads.isNotEmpty()) {
                repository.insertLeads(parsedLeads)
            }
        }
    }

    fun deleteCampaign(campaign: Campaign) {
        viewModelScope.launch {
            if (_activeCampaign.value?.id == campaign.id) {
                stopSession()
            }
            repository.deleteCampaign(campaign)
        }
    }

    fun resetCampaign(campaignId: Long) {
        viewModelScope.launch {
            repository.resetCampaignLeads(campaignId)
        }
    }

    fun addLeadToCampaign(
        campaignId: Long,
        name: String,
        phone: String,
        email: String = "",
        location: String = "",
        company: String = "",
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.insertLead(
                ContactLead(
                    campaignId = campaignId,
                    name = name,
                    phoneNumber = phone,
                    email = email,
                    location = location,
                    company = company,
                    notes = notes,
                    status = "PENDING"
                )
            )
        }
    }

    fun addLeadsToCampaign(
        campaignId: Long,
        leads: List<ContactLead>
    ) {
        viewModelScope.launch {
            val prepared = leads.map { it.copy(campaignId = campaignId) }
            repository.insertLeads(prepared)
        }
    }

    fun updateCallLogNotes(callLog: CallLog, updatedNotes: String, updatedDisposition: String) {
        viewModelScope.launch {
            repository.updateCallLog(
                callLog.copy(
                    notes = updatedNotes,
                    disposition = updatedDisposition
                )
            )
        }
    }

    fun clearCallLogs() {
        viewModelScope.launch {
            repository.clearAllLogs()
        }
    }

    fun reloadStarterData() {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    // Call Scheduling Management & Automation Engine
    private fun startScheduleMonitor() {
        viewModelScope.launch {
            while (true) {
                delay(6000) // Check every 6 seconds
                checkDueSchedules()
            }
        }
    }

    private suspend fun checkDueSchedules() {
        // Do not interrupt an active call in progress
        if (_sessionState.value == SessionState.ON_CALL || _sessionState.value == SessionState.DIALING) {
            return
        }

        val now = System.currentTimeMillis()
        val dueSchedules = repository.getDueSchedulesSync(now)
        for (schedule in dueSchedules) {
            val campaign = repository.getCampaignByIdSync(schedule.campaignId)
            if (campaign != null) {
                // Update schedule state based on repeat option
                when (schedule.repeatOption) {
                    "DAILY" -> {
                        val nextTime = schedule.scheduledTimeMillis + (24 * 3600 * 1000)
                        repository.updateSchedule(
                            schedule.copy(
                                scheduledTimeMillis = nextTime,
                                lastExecutedAt = now
                            )
                        )
                    }
                    "WEEKDAYS" -> {
                        val cal = java.util.Calendar.getInstance()
                        cal.timeInMillis = schedule.scheduledTimeMillis + (24 * 3600 * 1000)
                        while (cal.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SATURDAY ||
                            cal.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SUNDAY) {
                            cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
                        }
                        repository.updateSchedule(
                            schedule.copy(
                                scheduledTimeMillis = cal.timeInMillis,
                                lastExecutedAt = now
                            )
                        )
                    }
                    else -> {
                        repository.updateSchedule(
                            schedule.copy(
                                status = "COMPLETED",
                                lastExecutedAt = now
                            )
                        )
                    }
                }

                _scheduleAlert.value = "Scheduled trigger: '${schedule.title}' (${campaign.name})"

                // Launch the campaign automated dialer
                startCampaignDialer(campaign)
                break // Launch one campaign at a time
            } else {
                repository.updateScheduleStatus(schedule.id, "CANCELLED")
            }
        }
    }

    fun createSchedule(
        campaignId: Long,
        campaignName: String,
        title: String,
        scheduledTimeMillis: Long,
        repeatOption: String = "ONCE",
        notes: String = "",
        autoStartCockpit: Boolean = true
    ) {
        viewModelScope.launch {
            repository.insertSchedule(
                CampaignSchedule(
                    campaignId = campaignId,
                    campaignName = campaignName,
                    title = title,
                    scheduledTimeMillis = scheduledTimeMillis,
                    repeatOption = repeatOption,
                    status = "SCHEDULED",
                    notes = notes,
                    autoStartCockpit = autoStartCockpit
                )
            )
        }
    }

    fun executeScheduleNow(schedule: CampaignSchedule) {
        viewModelScope.launch {
            val campaign = repository.getCampaignByIdSync(schedule.campaignId)
            val now = System.currentTimeMillis()
            if (campaign != null) {
                if (schedule.repeatOption == "DAILY") {
                    repository.updateSchedule(
                        schedule.copy(
                            scheduledTimeMillis = now + (24 * 3600 * 1000),
                            lastExecutedAt = now
                        )
                    )
                } else if (schedule.repeatOption == "WEEKDAYS") {
                    val cal = java.util.Calendar.getInstance()
                    cal.timeInMillis = now + (24 * 3600 * 1000)
                    while (cal.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SATURDAY ||
                        cal.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SUNDAY) {
                        cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
                    }
                    repository.updateSchedule(
                        schedule.copy(
                            scheduledTimeMillis = cal.timeInMillis,
                            lastExecutedAt = now
                        )
                    )
                } else {
                    repository.updateSchedule(
                        schedule.copy(
                            status = "COMPLETED",
                            lastExecutedAt = now
                        )
                    )
                }
                _scheduleAlert.value = "Execution launched: '${schedule.title}'"
                startCampaignDialer(campaign)
            }
        }
    }

    fun reschedule(schedule: CampaignSchedule, newTimeMillis: Long) {
        viewModelScope.launch {
            repository.updateSchedule(
                schedule.copy(
                    scheduledTimeMillis = newTimeMillis,
                    status = "SCHEDULED"
                )
            )
        }
    }

    fun cancelSchedule(schedule: CampaignSchedule) {
        viewModelScope.launch {
            repository.updateSchedule(
                schedule.copy(status = "CANCELLED")
            )
        }
    }

    fun deleteSchedule(schedule: CampaignSchedule) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
        }
    }
}
