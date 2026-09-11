package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhoneForwarded
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.ContactLead
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun CampaignCard(
    campaign: Campaign,
    leads: List<ContactLead>,
    onStartDialing: (Campaign) -> Unit,
    onViewLeads: (Campaign) -> Unit,
    onResetCampaign: (Long) -> Unit,
    onDeleteCampaign: (Campaign) -> Unit,
    onScheduleCampaign: ((Campaign) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val totalLeads = leads.size
    val dialedLeads = leads.count { it.status != "PENDING" }
    val convertedLeads = leads.count { it.status == "CONVERTED" }
    val connectedLeads = leads.count { it.status == "CONNECTED" }
    val progress = if (totalLeads > 0) dialedLeads.toFloat() / totalLeads else 0f

    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Slate700.copy(alpha = 0.6f), RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row: Title + Status + More menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = campaign.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (campaign.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = campaign.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    CampaignStatusBadge(status = campaign.status)

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (onScheduleCampaign != null) {
                                DropdownMenuItem(
                                    text = { Text("Schedule Auto-Dial") },
                                    onClick = {
                                        showMenu = false
                                        onScheduleCampaign(campaign)
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Alarm, contentDescription = null, tint = ElectricCyan)
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("View & Manage Leads") },
                                onClick = {
                                    showMenu = false
                                    onViewLeads(campaign)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.People, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Reset Leads to Pending") },
                                onClick = {
                                    showMenu = false
                                    onResetCampaign(campaign.id)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Campaign", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    onDeleteCampaign(campaign)
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

            // Metadata Row (Delay, Max Retries)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = ElectricCyan
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${campaign.delaySeconds}s delay",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PhoneForwarded,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = AmberWarning
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Max ${campaign.maxRetries} retries",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = EmeraldSuccess
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$totalLeads leads",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar & Stats
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progress: $dialedLeads of $totalLeads dialed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ElectricCyan
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = ElectricCyan,
                    trackColor = Slate800,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Converted / Connected summary chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LeadStatPill(
                    label = "Connected",
                    count = connectedLeads,
                    color = EmeraldSuccess
                )
                LeadStatPill(
                    label = "Won / Converted",
                    count = convertedLeads,
                    color = ElectricCyan
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onViewLeads(campaign) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("view_leads_${campaign.id}"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Leads ($totalLeads)")
                }

                Button(
                    onClick = { onStartDialing(campaign) },
                    modifier = Modifier
                        .weight(1.4f)
                        .testTag("start_dialing_${campaign.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = Slate950
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Start Autodialer",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun LeadStatPill(
    label: String,
    count: Int,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$label: $count",
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
fun CampaignStatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status) {
        "ACTIVE" -> Triple(EmeraldSuccess.copy(alpha = 0.15f), EmeraldSuccess, "ACTIVE")
        "PAUSED" -> Triple(AmberWarning.copy(alpha = 0.15f), AmberWarning, "PAUSED")
        else -> Triple(ElectricCyan.copy(alpha = 0.15f), ElectricCyan, "COMPLETED")
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
