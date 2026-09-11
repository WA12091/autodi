package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.MiniDialerDock
import com.example.ui.screens.ActiveDialerCockpit
import com.example.ui.screens.CampaignsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SchedulesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.viewmodel.AppTab
import com.example.viewmodel.AutoDialerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AutoDialerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                AutoDialerApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AutoDialerApp(viewModel: AutoDialerViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val analytics by viewModel.analytics.collectAsStateWithLifecycle()
    val campaigns by viewModel.allCampaigns.collectAsStateWithLifecycle()
    val allLeads by viewModel.allLeads.collectAsStateWithLifecycle()
    val schedules by viewModel.allSchedules.collectAsStateWithLifecycle()
    val scheduleAlert by viewModel.scheduleAlert.collectAsStateWithLifecycle()

    // Dialing Session State
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    val activeCampaign by viewModel.activeCampaign.collectAsStateWithLifecycle()
    val activeLead by viewModel.activeLead.collectAsStateWithLifecycle()
    val queue by viewModel.currentQueue.collectAsStateWithLifecycle()
    val queueIndex by viewModel.queueIndex.collectAsStateWithLifecycle()
    val countdownRemaining by viewModel.countdownRemaining.collectAsStateWithLifecycle()
    val callTimerSeconds by viewModel.callTimerSeconds.collectAsStateWithLifecycle()
    val isCockpitOpen by viewModel.isCockpitOpen.collectAsStateWithLifecycle()

    // Settings
    val callDelaySeconds by viewModel.callDelaySeconds.collectAsStateWithLifecycle()
    val autoAdvance by viewModel.autoAdvance.collectAsStateWithLifecycle()
    val directDialMode by viewModel.directDialMode.collectAsStateWithLifecycle()
    val vibrateOnDial by viewModel.vibrateOnDial.collectAsStateWithLifecycle()
    val maxRetries by viewModel.maxRetries.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Slate950,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            Column {
                // Persistent Mini Dialer Dock when minimized
                if (!isCockpitOpen) {
                    MiniDialerDock(
                        sessionState = sessionState,
                        activeLead = activeLead,
                        countdownRemaining = countdownRemaining,
                        callTimerSeconds = callTimerSeconds,
                        onOpenCockpit = { viewModel.openCockpit() },
                        onPauseResume = {
                            if (sessionState == com.example.viewmodel.SessionState.PAUSED) {
                                viewModel.resumeSession()
                            } else {
                                viewModel.pauseSession()
                            }
                        },
                        onSkip = { viewModel.skipCurrentLead() }
                    )
                }

                // Standard M3 Bottom Navigation Bar
                NavigationBar(
                    containerColor = Slate900,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("main_bottom_nav")
                ) {
                    val tabs = listOf(
                        Triple(AppTab.DASHBOARD, Icons.Default.Dashboard, "Dashboard"),
                        Triple(AppTab.CAMPAIGNS, Icons.Default.Campaign, "Campaigns"),
                        Triple(AppTab.SCHEDULES, Icons.Default.Schedule, "Schedules"),
                        Triple(AppTab.SETTINGS, Icons.Default.Settings, "Settings")
                    )

                    tabs.forEach { (tab, icon, label) ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(tab) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Slate800
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate950)
        ) {
            when (selectedTab) {
                AppTab.DASHBOARD -> {
                    DashboardScreen(
                        analytics = analytics,
                        campaigns = campaigns,
                        schedules = schedules,
                        sessionState = sessionState,
                        activeCampaign = activeCampaign,
                        onStartCampaign = { campaign ->
                            viewModel.startCampaignDialer(campaign)
                        },
                        onOpenCockpit = { viewModel.openCockpit() },
                        onNavigateToCampaigns = { viewModel.selectTab(AppTab.CAMPAIGNS) },
                        onNavigateToSchedules = { viewModel.selectTab(AppTab.SCHEDULES) },
                        onExecuteScheduleNow = { schedule ->
                            viewModel.executeScheduleNow(schedule)
                        }
                    )
                }

                AppTab.CAMPAIGNS -> {
                    CampaignsScreen(
                        campaigns = campaigns,
                        allLeads = allLeads,
                        onStartCampaign = { campaign ->
                            viewModel.startCampaignDialer(campaign)
                        },
                        onCreateCampaign = { name, desc, delay, retries, leads ->
                            viewModel.createCampaignWithLeads(name, desc, delay, retries, leads)
                        },
                        onResetCampaign = { campaignId ->
                            viewModel.resetCampaign(campaignId)
                        },
                        onDeleteCampaign = { campaign ->
                            viewModel.deleteCampaign(campaign)
                        },
                        onAddLeadToCampaign = { cId, name, phone, email, loc, comp, notes ->
                            viewModel.addLeadToCampaign(cId, name, phone, email, loc, comp, notes)
                        },
                        onAddLeadsToCampaign = { cId, leads ->
                            viewModel.addLeadsToCampaign(cId, leads)
                        },
                        onDirectDialNumber = { name, phone, comp ->
                            viewModel.directCallNumber(name, phone, comp)
                        },
                        onScheduleCampaign = { campaign ->
                            viewModel.selectTab(AppTab.SCHEDULES)
                        }
                    )
                }

                AppTab.SCHEDULES -> {
                    SchedulesScreen(
                        schedules = schedules,
                        campaigns = campaigns,
                        onCreateSchedule = { cId, cName, title, time, repeat, notes ->
                            viewModel.createSchedule(cId, cName, title, time, repeat, notes)
                        },
                        onExecuteNow = { schedule ->
                            viewModel.executeScheduleNow(schedule)
                        },
                        onReschedule = { schedule, newTime ->
                            viewModel.reschedule(schedule, newTime)
                        },
                        onCancelSchedule = { schedule ->
                            viewModel.cancelSchedule(schedule)
                        },
                        onDeleteSchedule = { schedule ->
                            viewModel.deleteSchedule(schedule)
                        }
                    )
                }

                AppTab.SETTINGS -> {
                    SettingsScreen(
                        callDelaySeconds = callDelaySeconds,
                        onCallDelayChange = { viewModel.callDelaySeconds.value = it },
                        autoAdvance = autoAdvance,
                        onAutoAdvanceChange = { viewModel.autoAdvance.value = it },
                        directDialMode = directDialMode,
                        onDirectDialModeChange = { viewModel.directDialMode.value = it },
                        vibrateOnDial = vibrateOnDial,
                        onVibrateChange = { viewModel.vibrateOnDial.value = it },
                        maxRetries = maxRetries,
                        onMaxRetriesChange = { viewModel.maxRetries.value = it },
                        onReloadStarterData = { viewModel.reloadStarterData() },
                        onClearCallLogs = { viewModel.clearCallLogs() }
                    )
                }
            }

            // Fullscreen Active Dialer Cockpit Sheet
            ActiveDialerCockpit(
                isOpen = isCockpitOpen,
                sessionState = sessionState,
                campaign = activeCampaign,
                lead = activeLead,
                queue = queue,
                currentIndex = queueIndex,
                countdownRemaining = countdownRemaining,
                callTimerSeconds = callTimerSeconds,
                onClose = { viewModel.closeCockpit() },
                onDialNow = { viewModel.dialNow() },
                onPause = { viewModel.pauseSession() },
                onResume = { viewModel.resumeSession() },
                onSkip = { viewModel.skipCurrentLead() },
                onStopSession = { viewModel.stopSession() },
                onDispositionSelected = { disposition, notes ->
                    viewModel.completeCallWithDisposition(disposition, notes)
                }
            )

            // Automated Schedule Execution Notification Banner
            scheduleAlert?.let { alertMsg ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .align(Alignment.TopCenter)
                        .testTag("schedule_alert_banner"),
                    color = Slate900,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = alertMsg,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = { viewModel.dismissScheduleAlert() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss Alert",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
