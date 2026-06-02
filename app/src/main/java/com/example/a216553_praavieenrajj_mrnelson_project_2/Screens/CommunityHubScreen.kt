package com.example.a216553_praavieenrajj_mrnelson_project_2.Screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.a216553_praavieenrajj_mrnelson_project_2.ViewModel.UserViewModel
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// Data structure representing a crowdsourced poverty-relief resource
data class AidResource(
    val id: String = "",
    val aidTitle: String = "",
    val requirement: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityHubScreen(
    userViewModel: UserViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    // Form Inputs & UI State aligned to SDG 1
    var aidTitle by remember { mutableStateOf("") }
    var aidRequirement by remember { mutableStateOf("") }
    var resourceList by remember { mutableStateOf<List<AidResource>>(emptyList()) }

    // GPS Hardware Coordinates State
    var currentLatitude by remember { mutableDoubleStateOf(0.0) }
    var currentLongitude by remember { mutableDoubleStateOf(0.0) }
    var locationStatus by remember { mutableStateOf("GPS Sensor idle. Location unverified.") }

    // Hardware GPS Sensor Client Provider
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Activity Result Launcher to handle system permission dialogues cleanly
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            locationStatus = "GPS authorized. Ready to acquire lock..."
        } else {
            Toast.makeText(context, "Location hardware access denied.", Toast.LENGTH_SHORT).show()
        }
    }

    // Real-Time Listener streaming mutual-aid locations live from Cloud Firestore
    LaunchedEffect(Unit) {
        db.collection("poverty_relief_aid")
            .orderBy("id", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val resources = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(AidResource::class.java)?.copy(id = doc.id)
                    }
                    resourceList = resources
                }
            }
    }

    // Interrogates the phone's hardware location chip
    @SuppressLint("MissingPermission")
    fun captureSensorLocation(onLocationCaptured: (Location) -> Unit) {
        val finePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        if (finePermission == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                    locationStatus = "GPS Verified: %.4f, %.4f".format(location.latitude, location.longitude)
                    onLocationCaptured(location)
                } else {
                    locationStatus = "Sensor alert: Activate device location services toggle."
                }
            }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Combines form elements with raw sensor coordinate variables into our cloud database collection
    fun uploadAidToCloud() {
        if (aidTitle.isBlank() || aidRequirement.isBlank()) {
            Toast.makeText(context, "Please complete all field blocks.", Toast.LENGTH_SHORT).show()
            return
        }

        captureSensorLocation { location ->
            val resourceMap = hashMapOf(
                "id" to System.currentTimeMillis().toString(),
                "aidTitle" to aidTitle,
                "requirement" to aidRequirement,
                "latitude" to location.latitude,
                "longitude" to location.longitude
            )

            db.collection("poverty_relief_aid").add(resourceMap)
                .addOnSuccessListener {
                    aidTitle = ""
                    aidRequirement = ""
                    Toast.makeText(context, "Relief resource broadcasted to Cloud!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Cloud dispatch synchronization failed.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Poverty Relief Hub", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Form Card Section for Crowdsourcing Support
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Report Poverty Alleviation Program / Aid", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = aidTitle,
                        onValueChange = { aidTitle = it },
                        label = { Text("Aid Name (e.g. Mosque Food Bank, Zakat Hub)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = aidRequirement,
                        onValueChange = { aidRequirement = it },
                        label = { Text("Eligibility / Criteria (e.g. Free for B40 families)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Real-Time Hardware Coordinate Diagnosis Readout
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(locationStatus, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { uploadAidToCloud() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.VolunteerActivism, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verify GPS & Broadcast Program", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Active Relief Programs (Live Cloud Map Feed)", fontWeight = FontWeight.Black, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // Active Cloud Datastream List Feed Container
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(resourceList) { resource ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.0f)) {
                                Text(resource.aidTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Location Pin: %.4f, %.4f".format(resource.latitude, resource.longitude),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = resource.requirement,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}