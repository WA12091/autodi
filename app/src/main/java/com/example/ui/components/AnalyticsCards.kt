package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallDisposition
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GrayDisposition
import com.example.ui.theme.PurpleVoicemail
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.viewmodel.AnalyticsData
import java.util.Locale

@Composable
fun KpiGrid(
    analytics: AnalyticsData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiMetricCard(
                modifier = Modifier.weight(1f),
                title = "Total Dialed",
                value = "${analytics.totalCalls}",
                subtitle = "${analytics.pendingLeadsCount} leads pending",
                icon = Icons.Default.Call,
                accentColor = ElectricCyan
            )
            KpiMetricCard(
                modifier = Modifier.weight(1f),
                title = "Connected Rate",
                value = String.format(Locale.US, "%.1f%%", analytics.connectionRate),
                subtitle = "${analytics.connectedCalls} connected",
                icon = Icons.Default.CheckCircle,
                accentColor = EmeraldSuccess
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiMetricCard(
                modifier = Modifier.weight(1f),
                title = "Converted / Won",
                value = "${analytics.convertedCalls}",
                subtitle = String.format(Locale.US, "%.1f%% conversion", analytics.conversionRate),
                icon = Icons.Default.TrendingUp,
                accentColor = AmberWarning
            )
            val avgMin = analytics.avgDurationSeconds / 60
            val avgSec = analytics.avgDurationSeconds % 60
            KpiMetricCard(
                modifier = Modifier.weight(1f),
                title = "Avg Call Time",
                value = String.format(Locale.US, "%02d:%02d", avgMin, avgSec),
                subtitle = "Total: ${analytics.totalDurationSeconds / 60}m",
                icon = Icons.Default.Schedule,
                accentColor = RoyalBlue
            )
        }
    }
}

@Composable
fun KpiMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, Slate700.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Slate900
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DispositionBreakdownCard(
    dispositionCounts: Map<String, Int>,
    totalCalls: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Slate700.copy(alpha = 0.6f), RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Slate900
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Real-Time Disposition Analytics",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Live distribution across $totalCalls logged calls",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (totalCalls == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No calls recorded yet. Launch an autodial campaign to see real-time distribution.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Stacked visual progress bar
                DispositionStackedBar(dispositionCounts, totalCalls)

                Spacer(modifier = Modifier.height(16.dp))

                // Detail chips
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DispositionRow(
                        title = "Connected",
                        count = dispositionCounts[CallDisposition.CONNECTED.name] ?: 0,
                        total = totalCalls,
                        color = EmeraldSuccess
                    )
                    DispositionRow(
                        title = "Converted / Sale",
                        count = dispositionCounts[CallDisposition.CONVERTED.name] ?: 0,
                        total = totalCalls,
                        color = ElectricCyan
                    )
                    DispositionRow(
                        title = "Left Voicemail",
                        count = dispositionCounts[CallDisposition.VOICEMAIL.name] ?: 0,
                        total = totalCalls,
                        color = PurpleVoicemail
                    )
                    DispositionRow(
                        title = "Busy Signal",
                        count = dispositionCounts[CallDisposition.BUSY.name] ?: 0,
                        total = totalCalls,
                        color = AmberWarning
                    )
                    DispositionRow(
                        title = "No Answer",
                        count = dispositionCounts[CallDisposition.NO_ANSWER.name] ?: 0,
                        total = totalCalls,
                        color = RoseDanger
                    )
                }
            }
        }
    }
}

@Composable
fun DispositionStackedBar(
    counts: Map<String, Int>,
    total: Int
) {
    val connectedRatio = (counts[CallDisposition.CONNECTED.name] ?: 0).toFloat() / total
    val convertedRatio = (counts[CallDisposition.CONVERTED.name] ?: 0).toFloat() / total
    val voicemailRatio = (counts[CallDisposition.VOICEMAIL.name] ?: 0).toFloat() / total
    val busyRatio = (counts[CallDisposition.BUSY.name] ?: 0).toFloat() / total
    val noAnswerRatio = (counts[CallDisposition.NO_ANSWER.name] ?: 0).toFloat() / total

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Slate800)
    ) {
        if (connectedRatio > 0) {
            Box(
                modifier = Modifier
                    .weight(connectedRatio.coerceAtLeast(0.01f))
                    .fillMaxWidth()
                    .background(EmeraldSuccess)
            )
        }
        if (convertedRatio > 0) {
            Box(
                modifier = Modifier
                    .weight(convertedRatio.coerceAtLeast(0.01f))
                    .fillMaxWidth()
                    .background(ElectricCyan)
            )
        }
        if (voicemailRatio > 0) {
            Box(
                modifier = Modifier
                    .weight(voicemailRatio.coerceAtLeast(0.01f))
                    .fillMaxWidth()
                    .background(PurpleVoicemail)
            )
        }
        if (busyRatio > 0) {
            Box(
                modifier = Modifier
                    .weight(busyRatio.coerceAtLeast(0.01f))
                    .fillMaxWidth()
                    .background(AmberWarning)
            )
        }
        if (noAnswerRatio > 0) {
            Box(
                modifier = Modifier
                    .weight(noAnswerRatio.coerceAtLeast(0.01f))
                    .fillMaxWidth()
                    .background(RoseDanger)
            )
        }
    }
}

@Composable
fun DispositionRow(
    title: String,
    count: Int,
    total: Int,
    color: Color
) {
    val percentage = if (total > 0) (count.toFloat() / total) * 100f else 0f

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = String.format(Locale.US, "(%.1f%%)", percentage),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
