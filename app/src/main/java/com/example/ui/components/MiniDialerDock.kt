package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactLead
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.viewmodel.SessionState
import java.util.Locale

@Composable
fun MiniDialerDock(
    sessionState: SessionState,
    activeLead: ContactLead?,
    countdownRemaining: Int,
    callTimerSeconds: Int,
    onOpenCockpit: () -> Unit,
    onPauseResume: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVisible = sessionState != SessionState.IDLE && sessionState != SessionState.FINISHED

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(18.dp))
                .border(1.5.dp, ElectricCyan.copy(alpha = 0.8f), RoundedCornerShape(18.dp))
                .clickable { onOpenCockpit() }
                .testTag("mini_dialer_dock"),
            color = Slate900,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Pulse Indicator + Contact Details
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val indicatorColor = when (sessionState) {
                        SessionState.ON_CALL -> EmeraldSuccess
                        SessionState.COUNTDOWN -> ElectricCyan
                        SessionState.PAUSED -> AmberWarning
                        else -> ElectricCyan
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(indicatorColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = indicatorColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = activeLead?.name ?: "Dialer Session",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val statusText = when (sessionState) {
                            SessionState.COUNTDOWN -> "Dialing next in ${countdownRemaining}s..."
                            SessionState.ON_CALL -> {
                                val min = callTimerSeconds / 60
                                val sec = callTimerSeconds % 60
                                String.format(Locale.US, "On call: %02d:%02d", min, sec)
                            }
                            SessionState.PAUSED -> "Dialer Paused"
                            SessionState.DISPOSITION_PENDING -> "Waiting for disposition"
                            else -> "In Session"
                        }

                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = indicatorColor
                        )
                    }
                }

                // Right: Controls (Pause/Play, Skip, Expand)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPauseResume,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (sessionState == SessionState.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Pause/Resume",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onSkip,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Skip",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onOpenCockpit,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExpandLess,
                            contentDescription = "Maximize",
                            tint = ElectricCyan
                        )
                    }
                }
            }
        }
    }
}
