package com.example.data.repository

import com.example.data.local.CallLogDao
import com.example.data.local.CampaignDao
import com.example.data.local.CampaignScheduleDao
import com.example.data.local.ContactLeadDao
import com.example.data.model.CallLog
import com.example.data.model.Campaign
import com.example.data.model.CampaignSchedule
import com.example.data.model.ContactLead
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class AutoDialerRepository(
    private val campaignDao: CampaignDao,
    private val contactLeadDao: ContactLeadDao,
    private val callLogDao: CallLogDao,
    private val campaignScheduleDao: CampaignScheduleDao
) {
    val allCampaigns: Flow<List<Campaign>> = campaignDao.getAllCampaigns()
    val allCallLogs: Flow<List<CallLog>> = callLogDao.getAllCallLogs()
    val allLeads: Flow<List<ContactLead>> = contactLeadDao.getAllLeads()
    val allSchedules: Flow<List<CampaignSchedule>> = campaignScheduleDao.getAllSchedules()
    val pendingSchedules: Flow<List<CampaignSchedule>> = campaignScheduleDao.getPendingSchedules()

    fun getCampaignById(id: Long): Flow<Campaign?> = campaignDao.getCampaignById(id)
    suspend fun getCampaignByIdSync(id: Long): Campaign? = campaignDao.getCampaignByIdSync(id)

    fun getLeadsForCampaign(campaignId: Long): Flow<List<ContactLead>> =
        contactLeadDao.getLeadsForCampaign(campaignId)

    suspend fun getDialableLeads(campaignId: Long, maxAttempts: Int): List<ContactLead> =
        contactLeadDao.getDialableLeadsSync(campaignId, maxAttempts)

    suspend fun insertCampaign(campaign: Campaign): Long = campaignDao.insertCampaign(campaign)
    suspend fun updateCampaign(campaign: Campaign) = campaignDao.updateCampaign(campaign)
    suspend fun deleteCampaign(campaign: Campaign) = campaignDao.deleteCampaign(campaign)
    suspend fun deleteCampaignById(id: Long) = campaignDao.deleteCampaignById(id)
    suspend fun updateCampaignStatus(id: Long, status: String) = campaignDao.updateCampaignStatus(id, status)

    suspend fun insertLead(lead: ContactLead): Long = contactLeadDao.insertLead(lead)
    suspend fun insertLeads(leads: List<ContactLead>) = contactLeadDao.insertLeads(leads)
    suspend fun updateLead(lead: ContactLead) = contactLeadDao.updateLead(lead)
    suspend fun deleteLead(lead: ContactLead) = contactLeadDao.deleteLead(lead)
    suspend fun resetCampaignLeads(campaignId: Long) = contactLeadDao.resetCampaignLeads(campaignId)

    suspend fun insertCallLog(callLog: CallLog): Long = callLogDao.insertCallLog(callLog)
    suspend fun updateCallLog(callLog: CallLog) = callLogDao.updateCallLog(callLog)
    suspend fun deleteCallLog(callLog: CallLog) = callLogDao.deleteCallLog(callLog)
    suspend fun clearAllLogs() = callLogDao.clearAllLogs()

    // Schedule operations
    suspend fun insertSchedule(schedule: CampaignSchedule): Long = campaignScheduleDao.insertSchedule(schedule)
    suspend fun updateSchedule(schedule: CampaignSchedule) = campaignScheduleDao.updateSchedule(schedule)
    suspend fun deleteSchedule(schedule: CampaignSchedule) = campaignScheduleDao.deleteSchedule(schedule)
    suspend fun deleteScheduleById(id: Long) = campaignScheduleDao.deleteScheduleById(id)
    suspend fun updateScheduleStatus(id: Long, status: String) = campaignScheduleDao.updateScheduleStatus(id, status)
    suspend fun getDueSchedulesSync(currentTimeMillis: Long): List<CampaignSchedule> =
        campaignScheduleDao.getDueSchedulesSync(currentTimeMillis)
    suspend fun getScheduleByIdSync(id: Long): CampaignSchedule? = campaignScheduleDao.getScheduleByIdSync(id)

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val existing = campaignDao.getAllCampaigns().first()
        if (existing.isEmpty()) {
            // Seed Campaign 1: Enterprise Tech Outbound
            val c1Id = campaignDao.insertCampaign(
                Campaign(
                    name = "Q3 Enterprise Tech Leads",
                    description = "Outbound executive prospecting for SaaS infrastructure rollout",
                    delaySeconds = 4,
                    maxRetries = 3,
                    status = "ACTIVE"
                )
            )

            val leadsC1 = listOf(
                ContactLead(
                    campaignId = c1Id,
                    name = "Marcus Vance",
                    phoneNumber = "+1 415 890 2341",
                    company = "Apex Cloud Systems",
                    status = "PENDING",
                    notes = "CTO - Interested in automated failover"
                ),
                ContactLead(
                    campaignId = c1Id,
                    name = "Elena Rostova",
                    phoneNumber = "+1 206 555 0192",
                    company = "Horizon Analytics",
                    status = "CONNECTED",
                    callAttempts = 1,
                    lastDisposition = "CONNECTED",
                    notes = "Confirmed 30m demo next Tuesday at 2 PM"
                ),
                ContactLead(
                    campaignId = c1Id,
                    name = "David Chen",
                    phoneNumber = "+1 650 334 8820",
                    company = "OmniSec Cybersecurity",
                    status = "CONVERTED",
                    callAttempts = 2,
                    lastDisposition = "CONVERTED",
                    notes = "Signed NDA, sent pilot contract proposal"
                ),
                ContactLead(
                    campaignId = c1Id,
                    name = "Sarah Jenkins",
                    phoneNumber = "+1 312 901 7744",
                    company = "Midwest FinTech",
                    status = "VOICEMAIL",
                    callAttempts = 1,
                    lastDisposition = "VOICEMAIL",
                    notes = "Left message regarding SLA pricing"
                ),
                ContactLead(
                    campaignId = c1Id,
                    name = "Anthony Ross",
                    phoneNumber = "+1 737 444 8921",
                    company = "Vanguard Logistics",
                    status = "PENDING",
                    notes = "VP of Operations"
                ),
                ContactLead(
                    campaignId = c1Id,
                    name = "Sophia Miller",
                    phoneNumber = "+1 408 772 3190",
                    company = "NextGen AI Labs",
                    status = "PENDING",
                    notes = "Founder & Lead Architect"
                )
            )
            contactLeadDao.insertLeads(leadsC1)

            // Seed Campaign 2: Customer Retention & VIP
            val c2Id = campaignDao.insertCampaign(
                Campaign(
                    name = "VIP Client Quarterly Review",
                    description = "Account renewal, NPS follow-up & executive check-in calls",
                    delaySeconds = 5,
                    maxRetries = 2,
                    status = "ACTIVE"
                )
            )

            val leadsC2 = listOf(
                ContactLead(
                    campaignId = c2Id,
                    name = "Victoria Sterling",
                    phoneNumber = "+1 212 555 4910",
                    company = "Sterling & Partners Capital",
                    status = "CONVERTED",
                    callAttempts = 1,
                    lastDisposition = "CONVERTED",
                    notes = "Renewed 12-month tier-1 agreement"
                ),
                ContactLead(
                    campaignId = c2Id,
                    name = "Liam O'Connor",
                    phoneNumber = "+1 617 899 3021",
                    company = "Boston BioHealth",
                    status = "PENDING",
                    notes = "Requested feature overview for Q4 expansion"
                ),
                ContactLead(
                    campaignId = c2Id,
                    name = "Rachel Adams",
                    phoneNumber = "+1 303 400 8119",
                    company = "Summit Media Group",
                    status = "BUSY",
                    callAttempts = 1,
                    lastDisposition = "BUSY",
                    notes = "Line engaged, auto-retry scheduled"
                ),
                ContactLead(
                    campaignId = c2Id,
                    name = "Brandon Hall",
                    phoneNumber = "+1 404 662 1099",
                    company = "PeachState Logistics",
                    status = "PENDING",
                    notes = "Director of Procurement"
                )
            )
            contactLeadDao.insertLeads(leadsC2)

            // Seed realistic initial Call Logs for Real-time Analytics
            val now = System.currentTimeMillis()
            val sampleLogs = listOf(
                CallLog(
                    campaignId = c1Id,
                    campaignName = "Q3 Enterprise Tech Leads",
                    leadId = 2,
                    contactName = "Elena Rostova",
                    phoneNumber = "+1 206 555 0192",
                    company = "Horizon Analytics",
                    timestamp = now - (18 * 60 * 1000), // 18 mins ago
                    durationSeconds = 184, // 3m 04s
                    disposition = "CONNECTED",
                    notes = "Confirmed demo next Tuesday at 2 PM with engineering lead"
                ),
                CallLog(
                    campaignId = c1Id,
                    campaignName = "Q3 Enterprise Tech Leads",
                    leadId = 3,
                    contactName = "David Chen",
                    phoneNumber = "+1 650 334 8820",
                    company = "OmniSec Cybersecurity",
                    timestamp = now - (55 * 60 * 1000),
                    durationSeconds = 312, // 5m 12s
                    disposition = "CONVERTED",
                    notes = "Deal closed! Moving forward with pilot rollout"
                ),
                CallLog(
                    campaignId = c1Id,
                    campaignName = "Q3 Enterprise Tech Leads",
                    leadId = 4,
                    contactName = "Sarah Jenkins",
                    phoneNumber = "+1 312 901 7744",
                    company = "Midwest FinTech",
                    timestamp = now - (95 * 60 * 1000),
                    durationSeconds = 42,
                    disposition = "VOICEMAIL",
                    notes = "Left customized elevator pitch message"
                ),
                CallLog(
                    campaignId = c2Id,
                    campaignName = "VIP Client Quarterly Review",
                    leadId = 7,
                    contactName = "Victoria Sterling",
                    phoneNumber = "+1 212 555 4910",
                    company = "Sterling & Partners Capital",
                    timestamp = now - (3 * 3600 * 1000),
                    durationSeconds = 420, // 7m 00s
                    disposition = "CONVERTED",
                    notes = "Executive renewal signed, praised 24/7 uptime"
                ),
                CallLog(
                    campaignId = c2Id,
                    campaignName = "VIP Client Quarterly Review",
                    leadId = 9,
                    contactName = "Rachel Adams",
                    phoneNumber = "+1 303 400 8119",
                    company = "Summit Media Group",
                    timestamp = now - (5 * 3600 * 1000),
                    durationSeconds = 12,
                    disposition = "BUSY",
                    notes = "Call disconnected / busy signal"
                ),
                CallLog(
                    campaignId = c1Id,
                    campaignName = "Q3 Enterprise Tech Leads",
                    leadId = 1,
                    contactName = "Jonathan Pierce",
                    phoneNumber = "+1 214 770 1289",
                    company = "Texas Cloud Dynamics",
                    timestamp = now - (14 * 3600 * 1000),
                    durationSeconds = 150,
                    disposition = "CONNECTED",
                    notes = "Informed pricing plans, requested email summary"
                ),
                CallLog(
                    campaignId = c1Id,
                    campaignName = "Q3 Enterprise Tech Leads",
                    leadId = 5,
                    contactName = "Gregory Bell",
                    phoneNumber = "+1 512 883 0019",
                    company = "Austin Tech Core",
                    timestamp = now - (22 * 3600 * 1000),
                    durationSeconds = 25,
                    disposition = "NO_ANSWER",
                    notes = "Ranged 5 times, no answer"
                )
            )

            sampleLogs.forEach { callLogDao.insertCallLog(it) }
        }

        val existingSchedules = campaignScheduleDao.getAllSchedules().first()
        if (existingSchedules.isEmpty()) {
            val allCamps = campaignDao.getAllCampaigns().first()
            val c1 = allCamps.getOrNull(0)
            val c2 = allCamps.getOrNull(1) ?: c1
            val now = System.currentTimeMillis()

            if (c1 != null) {
                // Schedule 1: Upcoming in 30 minutes
                campaignScheduleDao.insertSchedule(
                    CampaignSchedule(
                        campaignId = c1.id,
                        campaignName = c1.name,
                        title = "Outbound Executive Blitz",
                        scheduledTimeMillis = now + (30 * 60 * 1000), // In 30 mins
                        repeatOption = "DAILY",
                        status = "SCHEDULED",
                        notes = "Target CTOs and VPs for Q3 platform rollout demos",
                        autoStartCockpit = true
                    )
                )

                // Schedule 2: Tomorrow morning at 9:30 AM
                campaignScheduleDao.insertSchedule(
                    CampaignSchedule(
                        campaignId = c2?.id ?: c1.id,
                        campaignName = c2?.name ?: c1.name,
                        title = "VIP Account Executive Follow-Up",
                        scheduledTimeMillis = now + (24 * 3600 * 1000) - (2 * 3600 * 1000), // ~Tomorrow morning
                        repeatOption = "WEEKDAYS",
                        status = "SCHEDULED",
                        notes = "Priority renewals and NPS check-in calls",
                        autoStartCockpit = true
                    )
                )

                // Schedule 3: Completed schedule earlier today
                campaignScheduleDao.insertSchedule(
                    CampaignSchedule(
                        campaignId = c1.id,
                        campaignName = c1.name,
                        title = "Morning Pipeline Warm-Up",
                        scheduledTimeMillis = now - (4 * 3600 * 1000),
                        repeatOption = "ONCE",
                        status = "COMPLETED",
                        notes = "Completed initial batch outreach",
                        lastExecutedAt = now - (4 * 3600 * 1000)
                    )
                )
            }
        }
    }
}
