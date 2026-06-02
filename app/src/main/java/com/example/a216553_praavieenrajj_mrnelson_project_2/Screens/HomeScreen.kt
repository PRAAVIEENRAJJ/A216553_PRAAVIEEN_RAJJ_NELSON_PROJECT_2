package com.example.a216553_praavieenrajj_mrnelson_project_2.Screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a216553_praavieenrajj_mrnelson_project_2.Data.Envelope
import com.example.a216553_praavieenrajj_mrnelson_project_2.Data.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    profile: UserProfile,
    envelopeList: List<Envelope>,
    onNavigateToAdd: () -> Unit,
    onViewProfile: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToInsights: () -> Unit,
    onLogout: () -> Unit
) {
    val displayName = if (profile.fullName.isNotEmpty()) profile.fullName else if (profile.username.isNotEmpty()) profile.username else "User"

    // Keep track of dynamically adjusted savings for each envelope using a map state
    val dynamicSavingsMap = remember { mutableStateMapOf<String, Float>() }

    // Initialize map entries when the database envelope list loads
    LaunchedEffect(envelopeList) {
        envelopeList.forEach { envelope ->
            if (!dynamicSavingsMap.containsKey(envelope.title)) {
                dynamicSavingsMap[envelope.title] = envelope.budgetSpent
            }
        }
    }

    // ==========================================
    // DYNAMIC METRICS WITH INTERACTIVE SLIDERS
    // ==========================================
    val totalAllocated = envelopeList.sumOf { it.budgetTotal.toDouble() }.toFloat()
    val totalSavedInteractive = envelopeList.sumOf { envelope ->
        (dynamicSavingsMap[envelope.title] ?: envelope.budgetSpent).toDouble()
    }.toFloat()

    val remainingBalance = totalAllocated - totalSavedInteractive

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Envelope", modifier = Modifier.size(26.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top App Bar Header Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "A216553 | Smart Budget",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Dashboard",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(
                    onClick = onLogout,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.clip(CircleShape)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = "Logout", modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Premium Gradient Balance Mesh Card
            val gradientBrush = Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.primaryContainer
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(24.dp))
                    .background(brush = gradientBrush, shape = RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Hello, $displayName 👋",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "RM %.2f".format(remainingBalance),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Remaining Balance (Interactive)",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Target Budget", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f))
                            Text("RM %.2f".format(totalAllocated), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Amount Saved", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f))
                            Text("RM %.2f".format(totalSavedInteractive), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile Portal Row Link
            Surface(
                onClick = onViewProfile,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("View My Account Profile", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    Icon(Icons.Default.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.outline)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Advanced Core Functional Modular Grid Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToCommunity() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Relief Hub", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Shared poverty aid maps", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToInsights() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Market Insights", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Live API inflation metrics", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text("Budget Envelopes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(12.dp))

            if (envelopeList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No envelopes created yet. Tap '+' to begin.", color = MaterialTheme.colorScheme.outline, fontSize = 14.sp)
                }
            } else {
                envelopeList.forEach { envelope ->
                    val currentSavings = dynamicSavingsMap[envelope.title] ?: envelope.budgetSpent

                    ExpandableEnvelopeCard(
                        title = envelope.title,
                        emoji = envelope.emoji,
                        budgetTotal = envelope.budgetTotal,
                        currentSavings = currentSavings,
                        onSavingsChanged = { updatedVal ->
                            dynamicSavingsMap[envelope.title] = updatedVal
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ExpandableEnvelopeCard(
    title: String,
    emoji: String,
    budgetTotal: Float,
    currentSavings: Float,
    onSavingsChanged: (Float) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var isEditable by remember { mutableStateOf(false) } // State tracking lock parameters

    val progress = if (budgetTotal > 0) (currentSavings / budgetTotal) else 0f
    val percentage = (progress * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Saved: RM %.2f of RM %.2f".format(currentSavings, budgetTotal),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$percentage%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // ==========================================
            // INTERACTIVE SAVINGS CONTROL PANEL BLOCK
            // ==========================================
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditable) "Dragging to adjust savings..." else "Savings locked. Tap edit to modify.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isEditable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )

                    // The dynamic Save/Edit Toggle Button using crisp system icon geometries
                    IconButton(
                        onClick = { isEditable = !isEditable },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isEditable) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isEditable) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.size(32.dp).clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isEditable) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditable) "Save Changes" else "Edit Savings",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Slider(
                        value = currentSavings,
                        onValueChange = { newValue ->
                            if (isEditable) onSavingsChanged(newValue)
                        },
                        valueRange = 0f..budgetTotal,
                        enabled = isEditable, // Slider is strictly disabled unless explicitly unlocked!
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = if (isEditable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            activeTrackColor = if (isEditable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                        )
                    )
                }
            }
        }
    }
}