package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Campaign
import com.example.data.model.CampaignSchedule
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulesScreen(
    schedules: List<CampaignSchedule>,
    campaigns: List<Campaign>,
    onCreateSchedule: (campaignId: Long, campaignName: String, title: String, timeMillis: Long, repeat: String, notes: String) -> Unit,
    onExecuteNow: (CampaignSchedule) -> Unit,
    onReschedule: (CampaignSchedule, Long) -> Unit,
    onCancelSchedule: (CampaignSchedule) -> Unit,
    onDeleteSchedule: (CampaignSchedule) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    var showCreateSheet by remember { mutableStateOf(false) }
    var scheduleToReschedule by remember { mutableStateOf<CampaignSchedule?>(null) }

    val filteredSchedules = when (selectedFilter) {
        "SCHEDULED" -> schedules.filter { it.status == "SCHEDULED" }
        "COMPLETED" -> schedules.filter { it.status == "COMPLETED" }
        "CANCELLED" -> schedules.filter { it.status == "CANCELLED" }
        else -> schedules
    }

    val pendingCount = schedules.count { it.status == "SCHEDULED" }
    val completedCount = schedules.count { it.status == "COMPLETED" }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Call Schedules",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$pendingCount upcoming • $completedCount completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showCreateSheet = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = Slate950
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("new_schedule_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Schedule Call", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Filter Tabs
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filters = listOf("ALL", "SCHEDULED", "COMPLETED", "CANCELLED")
                    items(filters) { filter ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedFilter = filter }
                                .border(
                                    1.dp,
                                    if (isSelected) ElectricCyan else Slate800,
                                    RoundedCornerShape(12.dp)
                                ),
                            color = if (isSelected) ElectricCyan.copy(alpha = 0.15f) else Slate900
                        ) {
                            Text(
                                text = filter,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            if (filteredSchedules.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp)
                            .border(1.dp, Slate800, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Slate900)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(ElectricCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No Schedules Found",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Set automated campaign run-times so your autodialer executes right on schedule.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showCreateSheet = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricCyan,
                                    contentColor = Slate950
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Create First Schedule", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(filteredSchedules, key = { it.id }) { schedule ->
                    ScheduleCard(
                        schedule = schedule,
                        onExecuteNow = { onExecuteNow(schedule) },
                        onReschedule = { scheduleToReschedule = schedule },
                        onCancel = { onCancelSchedule(schedule) },
                        onDelete = { onDeleteSchedule(schedule) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Modal Sheet: Create Schedule
    if (showCreateSheet) {
        CreateScheduleSheet(
            campaigns = campaigns,
            onDismiss = { showCreateSheet = false },
            onCreate = { cId, cName, title, time, repeat, notes ->
                onCreateSchedule(cId, cName, title, time, repeat, notes)
                showCreateSheet = false
            }
        )
    }

    // Modal Sheet: Reschedule
    scheduleToReschedule?.let { schedule ->
        RescheduleSheet(
            schedule = schedule,
            onDismiss = { scheduleToReschedule = null },
            onSave = { newTime ->
                onReschedule(schedule, newTime)
                scheduleToReschedule = null
            }
        )
    }
}

@Composable
fun ScheduleCard(
    schedule: CampaignSchedule,
    onExecuteNow: () -> Unit,
    onReschedule: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val isPending = schedule.status == "SCHEDULED"
    val diffMillis = schedule.scheduledTimeMillis - now
    val timeLabel = formatScheduledDateTime(schedule.scheduledTimeMillis)
    val relativeLabel = formatRelativeTime(diffMillis, schedule.status)

    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isPending && diffMillis in 0..(30 * 60 * 1000)) ElectricCyan.copy(alpha = 0.8f) else Slate800,
                RoundedCornerShape(18.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Title + Status + Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = schedule.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = schedule.campaignName,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = ElectricCyan
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    ScheduleStatusBadge(status = schedule.status)

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Schedule Options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (isPending) {
                                DropdownMenuItem(
                                    text = { Text("Run Immediately") },
                                    onClick = {
                                        showMenu = false
                                        onExecuteNow()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = ElectricCyan)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Reschedule Time") },
                                    onClick = {
                                        showMenu = false
                                        onReschedule()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.EditCalendar, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Cancel Schedule") },
                                    onClick = {
                                        showMenu = false
                                        onCancel()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = AmberWarning)
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Re-activate / Reschedule") },
                                    onClick = {
                                        showMenu = false
                                        onReschedule()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Schedule, contentDescription = null, tint = ElectricCyan)
                                    }
                                )
                            }

                            DropdownMenuItem(
                                text = { Text("Delete Schedule", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time & Recurrence Info Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate850)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = if (isPending) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = timeLabel,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = relativeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isPending) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Recurrence Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate800)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (schedule.repeatOption) {
                                "DAILY" -> "Daily"
                                "WEEKDAYS" -> "Weekdays"
                                else -> "One-time"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (schedule.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = schedule.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isPending) {
                    OutlinedButton(
                        onClick = onReschedule,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reschedule")
                    }

                    Button(
                        onClick = onExecuteNow,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("execute_schedule_${schedule.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = Slate950
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Run Now", fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }

                    Button(
                        onClick = onReschedule,
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Slate800,
                            contentColor = ElectricCyan
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Re-activate", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleStatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status) {
        "SCHEDULED" -> Triple(ElectricCyan.copy(alpha = 0.15f), ElectricCyan, "SCHEDULED")
        "COMPLETED" -> Triple(EmeraldSuccess.copy(alpha = 0.15f), EmeraldSuccess, "COMPLETED")
        "CANCELLED" -> Triple(Slate700.copy(alpha = 0.4f), MaterialTheme.colorScheme.onSurfaceVariant, "CANCELLED")
        else -> Triple(AmberWarning.copy(alpha = 0.15f), AmberWarning, status)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScheduleSheet(
    campaigns: List<Campaign>,
    onDismiss: () -> Unit,
    onCreate: (campaignId: Long, campaignName: String, title: String, scheduledTimeMillis: Long, repeat: String, notes: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCampaign by remember { mutableStateOf(campaigns.firstOrNull()) }
    var title by remember {
        mutableStateOf(
            if (selectedCampaign != null) "Outbound Sprint - ${selectedCampaign?.name}" else "Automated Campaign Dial"
        )
    }
    var repeatOption by remember { mutableStateOf("ONCE") }
    var notes by remember { mutableStateOf("") }

    val now = System.currentTimeMillis()
    var scheduledTimeMillis by remember { mutableLongStateOf(now + (30 * 60 * 1000)) } // Default in 30 mins
    var selectedPreset by remember { mutableStateOf("30M") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Slate950,
        modifier = Modifier.testTag("create_schedule_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Schedule Automated Calling",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Campaign Selection
            Text(
                text = "Target Campaign",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(campaigns) { camp ->
                    val isSelected = selectedCampaign?.id == camp.id
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedCampaign = camp
                                if (title.isBlank() || title.startsWith("Outbound Sprint")) {
                                    title = "Outbound Sprint - ${camp.name}"
                                }
                            }
                            .border(
                                1.5.dp,
                                if (isSelected) ElectricCyan else Slate800,
                                RoundedCornerShape(12.dp)
                            ),
                        color = if (isSelected) ElectricCyan.copy(alpha = 0.15f) else Slate900
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = camp.name,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Schedule Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Schedule Description / Title") },
                placeholder = { Text("e.g. Morning Executive Outreach") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("schedule_title_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = Slate700
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Execution Time Presets
            Text(
                text = "Execution Time",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            val presets = listOf(
                Pair("15M", "In 15 Mins") to (15 * 60 * 1000L),
                Pair("30M", "In 30 Mins") to (30 * 60 * 1000L),
                Pair("1H", "In 1 Hour") to (60 * 60 * 1000L),
                Pair("2H", "In 2 Hours") to (120 * 60 * 1000L),
                Pair("TMRW_9AM", "Tomorrow 9:00 AM") to calculateTomorrowMillis(9, 0),
                Pair("TMRW_2PM", "Tomorrow 2:00 PM") to calculateTomorrowMillis(14, 0)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.take(3).forEach { (presetInfo, offsetOrTime) ->
                        val (key, label) = presetInfo
                        val isSelected = selectedPreset == key
                        PresetButton(
                            label = label,
                            isSelected = isSelected,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedPreset = key
                                scheduledTimeMillis = now + offsetOrTime
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.drop(3).forEach { (presetInfo, targetTime) ->
                        val (key, label) = presetInfo
                        val isSelected = selectedPreset == key
                        PresetButton(
                            label = label,
                            isSelected = isSelected,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedPreset = key
                                scheduledTimeMillis = if (key.startsWith("TMRW")) targetTime else now + targetTime
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Display current selected time
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Slate800, RoundedCornerShape(12.dp)),
                color = Slate900
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Configured Run Time",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatScheduledDateTime(scheduledTimeMillis),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = ElectricCyan
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = formatRelativeTime(scheduledTimeMillis - now, "SCHEDULED"),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ElectricCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recurrence Options
            Text(
                text = "Recurrence Pattern",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val recurrences = listOf(
                    "ONCE" to "One-Time",
                    "DAILY" to "Daily",
                    "WEEKDAYS" to "Weekdays"
                )
                recurrences.forEach { (option, label) ->
                    val isSelected = repeatOption == option
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { repeatOption = option }
                            .border(
                                1.5.dp,
                                if (isSelected) ElectricCyan else Slate800,
                                RoundedCornerShape(12.dp)
                            ),
                        color = if (isSelected) ElectricCyan.copy(alpha = 0.15f) else Slate900
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Schedule Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Instructions / Notes for Call Run") },
                placeholder = { Text("e.g. Focus on VP persona, pitch enterprise failover tier") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = Slate700
                )
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Submit Button
            Button(
                onClick = {
                    val camp = selectedCampaign
                    if (camp != null && title.isNotBlank()) {
                        onCreate(
                            camp.id,
                            camp.name,
                            title,
                            scheduledTimeMillis,
                            repeatOption,
                            notes
                        )
                    }
                },
                enabled = selectedCampaign != null && title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = Slate950
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_schedule_button")
            ) {
                Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Schedule", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PresetButton(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .border(
                1.dp,
                if (isSelected) ElectricCyan else Slate800,
                RoundedCornerShape(10.dp)
            ),
        color = if (isSelected) ElectricCyan.copy(alpha = 0.2f) else Slate900
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RescheduleSheet(
    schedule: CampaignSchedule,
    onDismiss: () -> Unit,
    onSave: (newTimeMillis: Long) -> Unit
) {
    val now = System.currentTimeMillis()
    var selectedTime by remember { mutableLongStateOf(now + (30 * 60 * 1000)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Slate950
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reschedule Call Run",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Text(
                text = schedule.title,
                style = MaterialTheme.typography.bodyMedium,
                color = ElectricCyan
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Select New Time",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            val options = listOf(
                "+15 Minutes" to (15 * 60 * 1000L),
                "+30 Minutes" to (30 * 60 * 1000L),
                "+1 Hour" to (60 * 60 * 1000L),
                "+2 Hours" to (120 * 60 * 1000L),
                "Tomorrow 9:00 AM" to (calculateTomorrowMillis(9, 0) - now),
                "Tomorrow 2:00 PM" to (calculateTomorrowMillis(14, 0) - now)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.chunked(2).forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowOptions.forEach { (label, delta) ->
                            val targetTime = now + delta
                            val isSelected = selectedTime == targetTime
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedTime = targetTime }
                                    .border(
                                        1.dp,
                                        if (isSelected) ElectricCyan else Slate800,
                                        RoundedCornerShape(12.dp)
                                    ),
                                color = if (isSelected) ElectricCyan.copy(alpha = 0.2f) else Slate900
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "New Execution: ${formatScheduledDateTime(selectedTime)}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = ElectricCyan
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onSave(selectedTime) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = Slate950
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirm Reschedule", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Date & Time Utility Helpers
fun formatScheduledDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatRelativeTime(diffMillis: Long, status: String): String {
    if (status == "COMPLETED") return "Completed"
    if (status == "CANCELLED") return "Cancelled"

    if (diffMillis < 0) {
        val passedMins = (-diffMillis / (60 * 1000)).toInt()
        return if (passedMins < 60) "Due $passedMins mins ago" else "Due earlier"
    }

    val minutes = (diffMillis / (60 * 1000)).toInt()
    val hours = (diffMillis / (3600 * 1000)).toInt()
    val days = (diffMillis / (24 * 3600 * 1000)).toInt()

    return when {
        minutes <= 1 -> "Starts in <1 min"
        minutes < 60 -> "Starts in $minutes mins"
        hours < 24 -> "Starts in $hours hour${if (hours > 1) "s" else ""}"
        else -> "Starts in $days day${if (days > 1) "s" else ""}"
    }
}

private fun calculateTomorrowMillis(hourOfDay: Int, minute: Int): Long {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, 1)
    cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
    cal.set(Calendar.MINUTE, minute)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}
