package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Campaign
import com.example.data.model.ContactLead
import com.example.data.model.LeadDraft
import com.example.data.model.LeadParser
import com.example.ui.components.CampaignCard
import com.example.ui.components.DispositionChip
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignsScreen(
    campaigns: List<Campaign>,
    allLeads: List<ContactLead>,
    onStartCampaign: (Campaign) -> Unit,
    onCreateCampaign: (name: String, desc: String, delay: Int, retries: Int, leads: List<ContactLead>) -> Unit,
    onResetCampaign: (Long) -> Unit,
    onDeleteCampaign: (Campaign) -> Unit,
    onAddLeadToCampaign: (campaignId: Long, name: String, phone: String, email: String, location: String, company: String, notes: String) -> Unit,
    onAddLeadsToCampaign: ((campaignId: Long, leads: List<ContactLead>) -> Unit)? = null,
    onDirectDialNumber: (name: String, phone: String, company: String) -> Unit,
    onScheduleCampaign: ((Campaign) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    var showCreateSheet by remember { mutableStateOf(false) }
    var viewingCampaignLeads by remember { mutableStateOf<Campaign?>(null) }

    val filteredCampaigns = when (selectedFilter) {
        "ACTIVE" -> campaigns.filter { it.status == "ACTIVE" }
        "PAUSED" -> campaigns.filter { it.status == "PAUSED" }
        "COMPLETED" -> campaigns.filter { it.status == "COMPLETED" }
        else -> campaigns
    }

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
                            text = "Call Campaigns",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${campaigns.size} automated campaigns configured",
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
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("create_campaign_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Campaign", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Filter Chips Row
            item {
                val filters = listOf("ALL", "ACTIVE", "PAUSED", "COMPLETED")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filters) { filter ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) ElectricCyan else Slate900,
                            modifier = Modifier
                                .border(
                                    1.dp,
                                    if (isSelected) ElectricCyan else Slate800,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedFilter = filter }
                        ) {
                            Text(
                                text = filter,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) Slate950 else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Campaigns List
            if (filteredCampaigns.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                            .border(1.dp, Slate800, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Campaigns in this view",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap 'New Campaign' to configure pacing delay (2s, 3s, 5s) and add leads.",
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
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Create Campaign", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(filteredCampaigns, key = { it.id }) { campaign ->
                    val leadsForCampaign = allLeads.filter { it.campaignId == campaign.id }
                    CampaignCard(
                        campaign = campaign,
                        leads = leadsForCampaign,
                        onStartDialing = onStartCampaign,
                        onViewLeads = { viewingCampaignLeads = campaign },
                        onResetCampaign = onResetCampaign,
                        onDeleteCampaign = onDeleteCampaign,
                        onScheduleCampaign = onScheduleCampaign
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showCreateSheet = true },
            containerColor = ElectricCyan,
            contentColor = Slate950,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_create_campaign")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create Campaign")
        }
    }

    // Modal Sheet: Create New Campaign
    if (showCreateSheet) {
        CreateCampaignSheet(
            onDismiss = { showCreateSheet = false },
            onCreate = { name, desc, delay, retries, leads ->
                onCreateCampaign(name, desc, delay, retries, leads)
                showCreateSheet = false
            }
        )
    }

    // Modal Sheet: View & Manage Campaign Leads
    viewingCampaignLeads?.let { campaign ->
        val campaignLeads = allLeads.filter { it.campaignId == campaign.id }
        CampaignLeadsSheet(
            campaign = campaign,
            leads = campaignLeads,
            onDismiss = { viewingCampaignLeads = null },
            onAddLead = { name, phone, email, location, comp, notes ->
                onAddLeadToCampaign(campaign.id, name, phone, email, location, comp, notes)
            },
            onAddMultipleLeads = { leads ->
                onAddLeadsToCampaign?.invoke(campaign.id, leads)
            },
            onDirectDial = { lead ->
                onDirectDialNumber(lead.name, lead.phoneNumber, lead.company)
            },
            onResetCampaign = {
                onResetCampaign(campaign.id)
            }
        )
    }
}

/**
 * Modern Create Campaign Sheet with:
 * 1. Campaign Name
 * 2. Delay Pacing options (explicit chips for 2s, 3s, 5s, 10s)
 * 3. Add Leads with 3 distinct tabs:
 *    - "One by One" (name, number, email, location)
 *    - "Paste Data" (bulk CSV/TSV paste)
 *    - "Upload File" (CSV/TXT file picker)
 * 4. Staged leads queue manager
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCampaignSheet(
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String, delaySeconds: Int, maxRetries: Int, leads: List<ContactLead>) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var delaySeconds by remember { mutableIntStateOf(3) } // Default 3s
    var maxRetries by remember { mutableIntStateOf(2) }

    // Staged Leads List
    val stagedLeads = remember {
        mutableStateListOf(
            LeadDraft(
                name = "Sophia Turner",
                phoneNumber = "+1 415 555 9012",
                email = "sophia.turner@vertex.io",
                location = "San Francisco, CA",
                company = "Vertex Data"
            ),
            LeadDraft(
                name = "Lucas Sterling",
                phoneNumber = "+1 212 555 3301",
                email = "lsterling@primeglobal.com",
                location = "New York, NY",
                company = "Prime Global"
            ),
            LeadDraft(
                name = "Chloe Zhang",
                phoneNumber = "+1 650 555 7714",
                email = "chloe.z@cloudscale.net",
                location = "Austin, TX",
                company = "CloudScale"
            )
        )
    }

    // Tab state: 0 = One by One, 1 = Paste Data, 2 = Upload File
    var leadTab by remember { mutableIntStateOf(0) }

    // One-by-One form fields
    var oneByName by remember { mutableStateOf("") }
    var oneByPhone by remember { mutableStateOf("") }
    var oneByEmail by remember { mutableStateOf("") }
    var oneByLocation by remember { mutableStateOf("") }

    // Paste Data fields
    var pasteInput by remember {
        mutableStateOf(
            "Michael Reed, +1 312 555 8899, michael@reedcap.com, Chicago IL\n" +
            "Emma Watson, +1 415 555 2341, emma@techflow.io, Seattle WA"
        )
    }

    // Upload File state
    var uploadedFileName by remember { mutableStateOf("") }
    var uploadStatusText by remember { mutableStateOf("") }

    // File picker launcher
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val stream = context.contentResolver.openInputStream(it)
                val content = stream?.bufferedReader()?.use { reader -> reader.readText() } ?: ""
                val parsed = LeadParser.parseText(content)
                if (parsed.isNotEmpty()) {
                    stagedLeads.addAll(parsed)
                    uploadedFileName = it.lastPathSegment ?: "Imported File"
                    uploadStatusText = "Successfully imported ${parsed.size} leads from file"
                    Toast.makeText(context, "Added ${parsed.size} leads", Toast.LENGTH_SHORT).show()
                } else {
                    uploadStatusText = "No valid leads found in file"
                    Toast.makeText(context, "No leads parsed from file", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                uploadStatusText = "Error reading file: ${e.localizedMessage}"
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Slate950,
        modifier = Modifier.testTag("create_campaign_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Create Campaign",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Configure pacing and intake lead contact records",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Campaign Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Campaign Name *") },
                placeholder = { Text("e.g. Q4 Inbound Sales Blitz") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("campaign_name_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = Slate700
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Objective / Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Objective / Description (Optional)") },
                placeholder = { Text("e.g. Schedule product demos with key prospects") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = Slate700
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pacing Delay Section (User requested: "dely sec like 2s , 3s, 5s")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate800, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = Slate900)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Delay Seconds Between Calls",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            color = ElectricCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${delaySeconds}s delay",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ElectricCyan,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset Delay Chips: 2s, 3s, 5s, 10s
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val delayPresets = listOf(
                            Pair(2, "2s (Fast)"),
                            Pair(3, "3s (Optimal)"),
                            Pair(5, "5s (Standard)"),
                            Pair(10, "10s (Relaxed)")
                        )

                        delayPresets.forEach { (sec, label) ->
                            val isSelected = delaySeconds == sec
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { delaySeconds = sec }
                                    .border(
                                        1.dp,
                                        if (isSelected) ElectricCyan else Slate700,
                                        RoundedCornerShape(10.dp)
                                    ),
                                color = if (isSelected) ElectricCyan.copy(alpha = 0.2f) else Slate850
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${sec}s",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = label.substringAfter("(").removeSuffix(")"),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) ElectricCyan.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Slider(
                        value = delaySeconds.toFloat(),
                        onValueChange = { delaySeconds = it.toInt() },
                        valueRange = 1f..20f,
                        steps = 18,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricCyan,
                            activeTrackColor = ElectricCyan,
                            inactiveTrackColor = Slate800
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add Leads Section: Tabbed Intake
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate800, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = Slate900)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Add Leads to Campaign",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Choose how to add contact leads:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3 Intake Tabs: One-by-One, Paste Data, Upload File
                    TabRow(
                        selectedTabIndex = leadTab,
                        containerColor = Slate850,
                        contentColor = ElectricCyan,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[leadTab]),
                                color = ElectricCyan
                            )
                        }
                    ) {
                        Tab(
                            selected = leadTab == 0,
                            onClick = { leadTab = 0 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("1-by-1", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        )
                        Tab(
                            selected = leadTab == 1,
                            onClick = { leadTab = 1 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Paste Data", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        )
                        Tab(
                            selected = leadTab == 2,
                            onClick = { leadTab = 2 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Upload File", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tab Content 0: Add 1-by-1 (Name, Number, Email, Location)
                    if (leadTab == 0) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = oneByName,
                                    onValueChange = { oneByName = it },
                                    label = { Text("Name *") },
                                    placeholder = { Text("John Doe") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = oneByPhone,
                                    onValueChange = { oneByPhone = it },
                                    label = { Text("Number *") },
                                    placeholder = { Text("+1 555-0199") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = oneByEmail,
                                    onValueChange = { oneByEmail = it },
                                    label = { Text("Email") },
                                    placeholder = { Text("john@example.com") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = oneByLocation,
                                    onValueChange = { oneByLocation = it },
                                    label = { Text("Location") },
                                    placeholder = { Text("Dallas, TX") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            Button(
                                onClick = {
                                    if (oneByName.isNotBlank() && oneByPhone.isNotBlank()) {
                                        stagedLeads.add(
                                            LeadDraft(
                                                name = oneByName.trim(),
                                                phoneNumber = oneByPhone.trim(),
                                                email = oneByEmail.trim(),
                                                location = oneByLocation.trim()
                                            )
                                        )
                                        oneByName = ""
                                        oneByPhone = ""
                                        oneByEmail = ""
                                        oneByLocation = ""
                                        Toast.makeText(context, "Lead added to queue", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = oneByName.isNotBlank() && oneByPhone.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Slate800,
                                    contentColor = ElectricCyan
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Lead to List", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Tab Content 1: Paste Data (Bulk CSV/TSV)
                    if (leadTab == 1) {
                        Column {
                            Text(
                                text = "Paste CSV or multi-line data (Format: Name, Number, Email, Location):",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = pasteInput,
                                onValueChange = { pasteInput = it },
                                placeholder = {
                                    Text("Alex Morgan, +1 415 555 1234, alex@domain.com, San Francisco CA\nTaylor Swift, +1 615 555 4321, taylor@music.com, Nashville TN")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricCyan,
                                    unfocusedBorderColor = Slate700
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val parsed = LeadParser.parseText(pasteInput)
                                    if (parsed.isNotEmpty()) {
                                        stagedLeads.addAll(parsed)
                                        pasteInput = ""
                                        Toast.makeText(context, "Added ${parsed.size} leads", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "No valid leads found to paste", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = pasteInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Slate800,
                                    contentColor = ElectricCyan
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Parse & Add Pasted Leads", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Tab Content 2: Upload File (CSV / TXT)
                    if (leadTab == 2) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                                .background(Slate850)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Upload CSV / TXT File",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Supports Name, Number, Email, Location columns",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (uploadStatusText.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Slate900,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = uploadStatusText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldSuccess,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    fileLauncher.launch("*/*")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricCyan,
                                    contentColor = Slate950
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Select File From Device", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Staged Leads Queue Preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate800, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = Slate900)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Staged Leads Queue",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = ElectricCyan.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${stagedLeads.size}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ElectricCyan,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (stagedLeads.isNotEmpty()) {
                            Text(
                                text = "Clear All",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = AmberWarning,
                                modifier = Modifier.clickable { stagedLeads.clear() }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (stagedLeads.isEmpty()) {
                        Text(
                            text = "No leads staged yet. Add leads 1-by-1, paste data, or upload a CSV file above.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            stagedLeads.take(10).forEachIndexed { index, lead ->
                                Surface(
                                    color = Slate850,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = lead.name,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = lead.phoneNumber,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = ElectricCyan
                                                )
                                            }

                                            if (lead.email.isNotBlank() || lead.location.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    if (lead.email.isNotBlank()) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(
                                                                Icons.Default.Email,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                modifier = Modifier.size(11.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(2.dp))
                                                            Text(
                                                                lead.email,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                    if (lead.location.isNotBlank()) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(
                                                                Icons.Default.LocationOn,
                                                                contentDescription = null,
                                                                tint = AmberWarning,
                                                                modifier = Modifier.size(11.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(2.dp))
                                                            Text(
                                                                lead.location,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        IconButton(
                                            onClick = { stagedLeads.remove(lead) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Remove Lead",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (stagedLeads.size > 10) {
                                Text(
                                    text = "+ ${stagedLeads.size - 10} more leads staged for launch",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricCyan
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Submit Button
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val contactLeads = stagedLeads.map { it.toContactLead(0) }
                        onCreate(
                            name.trim(),
                            description.trim(),
                            delaySeconds,
                            maxRetries,
                            contactLeads
                        )
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = Slate950
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_create_campaign")
            ) {
                Text(
                    text = if (stagedLeads.isNotEmpty()) "Launch Campaign (${stagedLeads.size} Leads)" else "Launch Campaign",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignLeadsSheet(
    campaign: Campaign,
    leads: List<ContactLead>,
    onDismiss: () -> Unit,
    onAddLead: (name: String, phone: String, email: String, location: String, comp: String, notes: String) -> Unit,
    onAddMultipleLeads: (List<ContactLead>) -> Unit,
    onDirectDial: (ContactLead) -> Unit,
    onResetCampaign: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAddLeadDialog by remember { mutableStateOf(false) }
    var leadSearch by remember { mutableStateOf("") }

    val filteredLeads = if (leadSearch.isBlank()) leads else {
        leads.filter {
            it.name.contains(leadSearch, ignoreCase = true) ||
            it.phoneNumber.contains(leadSearch, ignoreCase = true) ||
            it.email.contains(leadSearch, ignoreCase = true) ||
            it.location.contains(leadSearch, ignoreCase = true) ||
            it.company.contains(leadSearch, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Slate950
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = campaign.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${leads.size} leads in queue • ${campaign.delaySeconds}s pacing",
                        style = MaterialTheme.typography.bodySmall,
                        color = ElectricCyan
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onResetCampaign) {
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showAddLeadDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Lead", tint = ElectricCyan)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = leadSearch,
                onValueChange = { leadSearch = it },
                placeholder = { Text("Search leads by name, phone, email, location...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Slate700)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = Slate800
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredLeads, key = { it.id }) { lead ->
                    LeadItemRow(lead = lead, onDirectDial = { onDirectDial(lead) })
                }
            }
        }
    }

    if (showAddLeadDialog) {
        AddLeadMultiSheet(
            campaignId = campaign.id,
            onDismiss = { showAddLeadDialog = false },
            onAddSingle = { name, phone, email, location, comp, notes ->
                onAddLead(name, phone, email, location, comp, notes)
                showAddLeadDialog = false
            },
            onAddBulk = { bulkLeads ->
                onAddMultipleLeads(bulkLeads)
                showAddLeadDialog = false
            }
        )
    }
}

@Composable
fun LeadItemRow(
    lead: ContactLead,
    onDirectDial: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = lead.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    DispositionChip(dispositionName = lead.status)
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = lead.phoneNumber,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = ElectricCyan
                    )
                    if (lead.company.isNotBlank()) {
                        Text(
                            text = " • ${lead.company}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (lead.email.isNotBlank() || lead.location.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (lead.email.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = Slate700,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = lead.email,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (lead.location.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = AmberWarning,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = lead.location,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (lead.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = lead.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onDirectDial,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Slate800)
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Direct Call",
                    tint = EmeraldSuccess,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Multi-intake sheet for adding leads to an existing campaign
 * Supports 1-by-1, Paste Data, and Upload File.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLeadMultiSheet(
    campaignId: Long,
    onDismiss: () -> Unit,
    onAddSingle: (name: String, phone: String, email: String, location: String, comp: String, notes: String) -> Unit,
    onAddBulk: (List<ContactLead>) -> Unit
) {
    val context = LocalContext.current
    var activeTab by remember { mutableIntStateOf(0) }

    // 1-by-1 fields
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Paste fields
    var pasteData by remember { mutableStateOf("") }

    // Upload launcher
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val stream = context.contentResolver.openInputStream(it)
                val text = stream?.bufferedReader()?.use { reader -> reader.readText() } ?: ""
                val parsed = LeadParser.parseText(text).map { draft -> draft.toContactLead(campaignId) }
                if (parsed.isNotEmpty()) {
                    onAddBulk(parsed)
                    Toast.makeText(context, "Added ${parsed.size} leads", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "No leads found in file", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Slate950
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Add Contact Leads",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Slate850,
                contentColor = ElectricCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = ElectricCyan
                    )
                }
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("1-by-1", style = MaterialTheme.typography.labelSmall) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Paste Data", style = MaterialTheme.typography.labelSmall) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("Upload File", style = MaterialTheme.typography.labelSmall) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (activeTab == 0) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Lead Full Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company / Organization") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Lead Notes / Context") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank() && phone.isNotBlank()) {
                            onAddSingle(name.trim(), phone.trim(), email.trim(), location.trim(), company.trim(), notes.trim())
                        }
                    },
                    enabled = name.isNotBlank() && phone.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = Slate950
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Lead to Queue", fontWeight = FontWeight.Bold)
                }
            }

            if (activeTab == 1) {
                Text(
                    text = "Paste comma or tab-separated leads (Name, Number, Email, Location):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = pasteData,
                    onValueChange = { pasteData = it },
                    placeholder = { Text("John Doe, +1 555-0100, john@abc.com, Chicago\nJane Smith, +1 555-0101, jane@abc.com, New York") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val parsed = LeadParser.parseText(pasteData).map { it.toContactLead(campaignId) }
                        if (parsed.isNotEmpty()) {
                            onAddBulk(parsed)
                        }
                    },
                    enabled = pasteData.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = Slate950
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Import & Save Leads", fontWeight = FontWeight.Bold)
                }
            }

            if (activeTab == 2) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                        .background(Slate850)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Import from CSV or TXT File",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "File with Name, Number, Email, Location rows",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { fileLauncher.launch("*/*") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = Slate950
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Choose File", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
