package com.example.a216553_praavieenrajj_mrnelson_project_2.Screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.a216553_praavieenrajj_mrnelson_project_2.Data.Envelope
import com.example.a216553_praavieenrajj_mrnelson_project_2.Data.UserProfile
import com.example.a216553_praavieenrajj_mrnelson_project_2.ViewModel.UserViewModel
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    profile: UserProfile,
    envelopeList: List<Envelope>,
    userViewModel: UserViewModel, // INTEGRATION HUB
    onNavigateToAdd: () -> Unit,
    onViewProfile: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToInsights: () -> Unit,
    onDeleteEnvelope: (Envelope) -> Unit,
    onSaveSavings: (Envelope, Float) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val displayName = if (profile.fullName.isNotEmpty()) profile.fullName else if (profile.username.isNotEmpty()) profile.username else "User"
    val dynamicSavingsMap = remember { mutableStateMapOf<String, Float>() }

    // Pillar Integration States
    val favorites by userViewModel.favoriteFoodBanks.collectAsState()
    val globalInsight by userViewModel.povertyInsight.collectAsState()
    val cloudBanks by userViewModel.cloudFoodBanks.collectAsState()
    val nearestAid by userViewModel.nearestAidCenter.collectAsState()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // PILLAR 1: Sensor (GPS) Permission Logic
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc -> if (loc != null) userViewModel.updateLocation(loc) }
            }
        }
    }

    LaunchedEffect(envelopeList) {
        envelopeList.forEach { if (!dynamicSavingsMap.containsKey(it.title)) dynamicSavingsMap[it.title] = it.budgetSpent }
    }

    val totalAllocated = envelopeList.sumOf { it.budgetTotal.toDouble() }.toFloat()
    val totalSavedInteractive = envelopeList.sumOf { (dynamicSavingsMap[it.title] ?: it.budgetSpent).toDouble() }.toFloat()
    val remainingBalance = totalAllocated - totalSavedInteractive
    val globalProgress = if (totalAllocated > 0) (totalSavedInteractive / totalAllocated) else 0f

    // PILLAR 3 (Room) -> PILLAR 4 (Cloud) LINK: Crisis detection
    val foodEnvelope = envelopeList.find { it.title.lowercase().contains("food") || it.title.lowercase().contains("grocery") }
    val isCrisis = foodEnvelope != null && foodEnvelope.budgetTotal > 0 && (foodEnvelope.budgetSpent / foodEnvelope.budgetTotal) > 0.8f
    
    // Pro-active GPS sensor check if budget is critical
    LaunchedEffect(isCrisis) {
        if (isCrisis) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation.addOnSuccessListener { if (it != null) userViewModel.updateLocation(it) }
            } else {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("A216553 | SDG 1 ADVOCATE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Impact Dashboard", fontSize = 24.sp, fontWeight = FontWeight.Black)
                }
                IconButton(onClick = onLogout) { Icon(Icons.Default.Logout, null, tint = MaterialTheme.colorScheme.error) }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Integrated Header Card (PILLAR 2: World Bank Internet Data)
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(8.dp)) {
                Box(modifier = Modifier.background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer))).padding(24.dp)) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Savings Balance", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                                Text("RM %.2f".format(remainingBalance), style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Black)
                            }
                            // PILLAR 4: User Impact Status
                            Column(horizontalAlignment = Alignment.End) {
                                Icon(Icons.Default.Verified, null, tint = MaterialTheme.colorScheme.onPrimary)
                                Text("${profile.verificationPoints} pts", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        // Internet Data Alert
                        Surface(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Language, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = globalInsight, fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimary, lineHeight = 14.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(progress = { globalProgress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = MaterialTheme.colorScheme.onPrimary, trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                        Text("Resilience Status: ${(globalProgress * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }

            // PILLAR 1+4 INTEGRATION: Pro-active sensor-cloud aid suggestion
            if (isCrisis && nearestAid != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth().animateContentSize(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GpsFixed, null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Nearest Aid center detected (GPS)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                        Text("'${nearestAid!!.first.name}' is physically %.1f KM away with ${nearestAid!!.first.foodQuantity} items.".format(nearestAid!!.second), fontSize = 12.sp)
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // PILLAR 2: External Map API
                            Button(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:${nearestAid!!.first.latitude},${nearestAid!!.first.longitude}?q=${nearestAid!!.first.name}"))) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                                Text("Map Route", fontSize = 11.sp)
                            }
                            // PILLAR 4: Cloud Info Update
                            OutlinedButton(onClick = { userViewModel.logVisit(nearestAid!!.first) { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() } }, modifier = Modifier.weight(1f)) {
                                Text("Log Visit", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardSmallCard("Relief Hub", "Community Aid", Icons.Default.Handshake, onNavigateToCommunity, Modifier.weight(1f))
                DashboardSmallCard("SDG 1 Info", "Live Metrics", Icons.Default.Language, onNavigateToInsights, Modifier.weight(1f))
            }

            // PILLAR 3: Room Database favorite items
            if (favorites.isNotEmpty()) {
                Spacer(modifier = Modifier.height(28.dp))
                Text("Pinned Resources (Room DB Cache)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(favorites) { fav ->
                        val live = cloudBanks.find { it.id == fav.bankId }
                        Card(modifier = Modifier.width(180.dp).clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:${fav.latitude},${fav.longitude}?q=${fav.name}"))) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(fav.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (live != null) {
                                    Text("Stock: ${live.foodQuantity} (Cloud Live)", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                    Text("Visits: ${fav.visitCount} (Room DB)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Text("Syncing Cloud info...", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text("Budget Personal Vaults", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(12.dp))

            envelopeList.forEach { envelope ->
                ExpandableEnvelopeCard(
                    title = envelope.title,
                    emoji = envelope.emoji,
                    budgetTotal = envelope.budgetTotal,
                    currentSavings = dynamicSavingsMap[envelope.title] ?: envelope.budgetSpent,
                    onSavingsChanged = { dynamicSavingsMap[envelope.title] = it },
                    onSave = { onSaveSavings(envelope, it) },
                    onDelete = { onDeleteEnvelope(envelope) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DashboardSmallCard(title: String, desc: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier) {
    Card(modifier = modifier.clickable { onClick() }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(desc, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun ExpandableEnvelopeCard(title: String, emoji: String, budgetTotal: Float, currentSavings: Float, onSavingsChanged: (Float) -> Unit, onSave: (Float) -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var isEditable by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth().animateContentSize().clickable { expanded = !expanded }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 20.sp) }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("RM %.2f / RM %.2f".format(currentSavings, budgetTotal), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Slider(value = currentSavings, onValueChange = { if (isEditable) onSavingsChanged(it) }, valueRange = 0f..budgetTotal, enabled = isEditable, modifier = Modifier.weight(1f))
                    IconButton(onClick = { if (isEditable) onSave(currentSavings); isEditable = !isEditable }) { Icon(if (isEditable) Icons.Default.CheckCircle else Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
}
