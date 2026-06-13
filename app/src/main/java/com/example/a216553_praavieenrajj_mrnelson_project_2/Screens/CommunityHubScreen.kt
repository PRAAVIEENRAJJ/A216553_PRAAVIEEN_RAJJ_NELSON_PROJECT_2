package com.example.a216553_praavieenrajj_mrnelson_project_2.Screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.a216553_praavieenrajj_mrnelson_project_2.ViewModel.UserViewModel
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityHubScreen(
    userViewModel: UserViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val cloudBanks by userViewModel.cloudFoodBanks.collectAsState()

    var bankName by remember { mutableStateOf("") }
    var foodQty by remember { mutableStateOf("") }
    var locationStatus by remember { mutableStateOf("GPS Sensor: Ready to verify location") }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> if (!isGranted) Toast.makeText(context, "GPS permission needed for SDG 1 hub.", Toast.LENGTH_SHORT).show() }

    @SuppressLint("MissingPermission")
    fun captureAndUpload() {
        if (bankName.isBlank() || foodQty.isBlank()) return
        
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        locationStatus = "Verifying physical coordinates..."
        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                userViewModel.updateLocation(loc)
                val data = hashMapOf(
                    "name" to bankName,
                    "foodQuantity" to (foodQty.toIntOrNull() ?: 0),
                    "latitude" to loc.latitude,
                    "longitude" to loc.longitude,
                    "isAvailable" to true
                )
                db.collection("food_banks").add(data).addOnSuccessListener {
                    bankName = ""; foodQty = ""; locationStatus = "Resource broadcasted to Cloud!"
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Poverty Relief Hub", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            // Integration Pillar 1 & 4: Sensor & Cloud
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Register Aid Location (GPS Verified)", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = bankName, onValueChange = { bankName = it }, label = { Text("Center Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = foodQty, onValueChange = { if(it.all { c -> c.isDigit() }) foodQty = it }, label = { Text("Available Stock Qty") }, modifier = Modifier.fillMaxWidth())
                    Text(locationStatus, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    Button(onClick = { captureAndUpload() }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Icon(Icons.Default.CloudUpload, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verify GPS & Broadcast")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Active Community Aid (Live Cloud Feed)", fontWeight = FontWeight.Black)

            // Integration Pillar 2, 3 & 4: Internet, Room, and Cloud
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                items(cloudBanks) { bank ->
                    val isFav by userViewModel.isFavorite(bank.id).collectAsState(initial = false)
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(bank.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Stock: ${bank.foodQuantity} items", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                // Internet Map API
                                IconButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:${bank.latitude},${bank.longitude}?q=${bank.name}"))) }) {
                                    Icon(Icons.Default.Map, null, tint = MaterialTheme.colorScheme.secondary)
                                }
                                // Room Favorites
                                IconButton(onClick = { userViewModel.toggleFavorite(bank) }) {
                                    Icon(if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = if (isFav) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // ULTIMATE INTEGRATION POINT: Check-in (Sensor + Cloud + Room)
                            Button(
                                onClick = { 
                                    userViewModel.logVisit(bank) { msg -> Toast.makeText(context, msg, Toast.LENGTH_LONG).show() }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Default.HowToReg, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verify Visit & Update Live Stock", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
