package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallDisposition
import com.example.data.model.Campaign
import com.example.data.model.ContactLead
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GrayDisposition
import com.example.ui.theme.PurpleVoicemail
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.viewmodel.SessionState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveDialerCockpit(
    isOpen: Boolean,
    sessionState: SessionState,
    campaign: Campaign?,
    lead: ContactLead?,
    queue: List<ContactLead>,
    currentIndex: Int,
    countdownRemaining: Int,
    callTimerSeconds: Int,
    onClose: () -> Unit,
    onDialNow: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSkip: () -> Unit,
    onStopSession: () -> Unit,
    onDispositionSelected: (CallDisposition, String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var notesText by remember(lead?.id) { mutableStateOf("") }

    // Pulsing animation for glowing caller avatar
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = Slate950,
        dragHandle = null,
        modifier = modifier.testTag("active_dialer_cockpit_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar: Campaign & Minimize
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Minimize",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = campaign?.name ?: "Autodialer Session",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (queue.isNotEmpty()) {
                        Text(
                            text = "Lead ${currentIndex + 1} of ${queue.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan
                        )
                    }
                }

                IconButton(onClick = onStopSession) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "End Session",
                        tint = RoseDanger
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (sessionState == SessionState.FINISHED) {
                // Session Finished Card
                DialerSessionFinishedCard(
                    totalLeads = queue.size,
                    onClose = onClose,
                    onStop = onStopSession
                )
            } else if (lead != null) {
                // Active Caller Display
                val ringColor = when (sessionState) {
                    SessionState.ON_CALL -> EmeraldSuccess
                    SessionState.COUNTDOWN -> ElectricCyan
                    SessionState.PAUSED -> AmberWarning
                    else -> ElectricCyan
                }

                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(if (sessionState == SessionState.ON_CALL || sessionState == SessionState.COUNTDOWN) pulseScale else 1f)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(ringColor.copy(alpha = 0.25f), Color.Transparent)
                            )
                        )
                        .border(3.dp, ringColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Slate900),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = ringColor,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Lead Details
                Text(
                    text = lead.name,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                if (lead.company.isNotBlank()) {
                    Text(
                        text = lead.company,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = lead.phoneNumber,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = ElectricCyan
                )

                if (lead.email.isNotBlank() || lead.location.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (lead.email.isNotBlank()) {
                            Surface(
                                color = Slate800,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "Email",
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = lead.email,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        if (lead.location.isNotBlank()) {
                            Surface(
                                color = Slate800,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Location",
                                        tint = AmberWarning,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = lead.location,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                if (lead.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Note: ${lead.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Live Timer / Countdown Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate800, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (sessionState) {
                            SessionState.COUNTDOWN -> {
                                Text(
                                    text = "Auto-dialing in",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${countdownRemaining}s",
                                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = ElectricCyan
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                val maxDelay = (campaign?.delaySeconds ?: 5).coerceAtLeast(1)
                                val progress = (countdownRemaining.toFloat() / maxDelay).coerceIn(0f, 1f)
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = ElectricCyan,
                                    trackColor = Slate800
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = onDialNow,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElectricCyan,
                                        contentColor = Slate950
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("dial_now_button")
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Dial Immediately", fontWeight = FontWeight.Bold)
                                }
                            }
                            SessionState.ON_CALL -> {
                                Text(
                                    text = "Call in progress",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EmeraldSuccess
                                )
                                val min = callTimerSeconds / 60
                                val sec = callTimerSeconds % 60
                                Text(
                                    text = String.format(Locale.US, "%02d:%02d", min, sec),
                                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = EmeraldSuccess
                                )
                            }
                            SessionState.PAUSED -> {
                                Text(
                                    text = "Dialer Paused",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = AmberWarning
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onResume,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AmberWarning,
                                        contentColor = Slate950
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Resume Autodialer", fontWeight = FontWeight.Bold)
                                }
                            }
                            else -> {
                                Text(
                                    text = "Ready to record disposition",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Call Notes Input
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Call Notes & Outcomes") },
                    placeholder = { Text("e.g., Demo confirmed, requested pricing by email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cockpit_call_notes_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = Slate700,
                        focusedContainerColor = Slate900,
                        unfocusedContainerColor = Slate900
                    ),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dispositions Grid: 1-touch logging
                Text(
                    text = "Select Call Outcome / Disposition:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DispositionButton(
                            title = "Connected",
                            color = EmeraldSuccess,
                            modifier = Modifier.weight(1f),
                            onClick = { onDispositionSelected(CallDisposition.CONNECTED, notesText) }
                        )
                        DispositionButton(
                            title = "Won / Converted",
                            color = ElectricCyan,
                            modifier = Modifier.weight(1f),
                            onClick = { onDispositionSelected(CallDisposition.CONVERTED, notesText) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DispositionButton(
                            title = "Left Voicemail",
                            color = PurpleVoicemail,
                            modifier = Modifier.weight(1f),
                            onClick = { onDispositionSelected(CallDisposition.VOICEMAIL, notesText) }
                        )
                        DispositionButton(
                            title = "Busy Signal",
                            color = AmberWarning,
                            modifier = Modifier.weight(1f),
                            onClick = { onDispositionSelected(CallDisposition.BUSY, notesText) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DispositionButton(
                            title = "No Answer",
                            color = RoseDanger,
                            modifier = Modifier.weight(1f),
                            onClick = { onDispositionSelected(CallDisposition.NO_ANSWER, notesText) }
                        )
                        DispositionButton(
                            title = "Callback Req",
                            color = AmberWarning,
                            modifier = Modifier.weight(1f),
                            onClick = { onDispositionSelected(CallDisposition.CALLBACK, notesText) }
                        )
                        DispositionButton(
                            title = "Wrong No.",
                            color = GrayDisposition,
                            modifier = Modifier.weight(1f),
                            onClick = { onDispositionSelected(CallDisposition.WRONG_NUMBER, notesText) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Session Control Buttons (Pause, Skip, Hangup)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = if (sessionState == SessionState.PAUSED) onResume else onPause,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Slate900)
                            .border(1.dp, Slate700, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (sessionState == SessionState.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Pause/Resume",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onSkip,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Slate900)
                            .border(1.dp, Slate700, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Skip Lead",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onStopSession,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(RoseDanger.copy(alpha = 0.2f))
                            .border(1.dp, RoseDanger, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = RoseDanger
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DispositionButton(
    title: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("disposition_$title"),
        color = Slate900,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = color
            )
        }
    }
}

@Composable
fun DialerSessionFinishedCard(
    totalLeads: Int,
    onClose: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(ElectricCyan.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Campaign Dialing Completed!",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "All $totalLeads contact leads in this queue have been dialed. Check your Dashboard and Call History for updated real-time analytics.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    onStop()
                    onClose()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = Slate950
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Return to Dashboard", fontWeight = FontWeight.Bold)
            }
        }
    }
}
