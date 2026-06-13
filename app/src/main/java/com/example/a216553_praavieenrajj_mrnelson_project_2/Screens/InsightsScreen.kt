package com.example.a216553_praavieenrajj_mrnelson_project_2.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a216553_praavieenrajj_mrnelson_project_2.Data.RetrofitClient
import com.example.a216553_praavieenrajj_mrnelson_project_2.Data.WorldBankDataPoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()

    var insightText by remember { mutableStateOf("Fetching latest poverty statistics from World Bank...") }
    var isLoading by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    fun fetchSDG1Data() {
        coroutineScope.launch {
            isLoading = true
            isError = false
            try {
                val response = RetrofitClient.apiInstance.getPovertyData()
                // World Bank API returns a JSON array: [0] = Metadata object, [1] = Data array
                if (response.size > 1) {
                    val dataListElement = response[1]
                    val itemType = object : TypeToken<List<WorldBankDataPoint>>() {}.type
                    val dataPoints: List<WorldBankDataPoint> = Gson().fromJson(dataListElement, itemType)
                    
                    // Find the first data point that has a value (most recent)
                    val validPoint = dataPoints.firstOrNull { it.value != null }
                    if (validPoint != null) {
                        insightText = "According to World Bank data (${validPoint.date}), the poverty headcount ratio in Malaysia is ${"%.2f".format(validPoint.value)}% of the population (at $2.15 a day). This metric helps monitor progress for SDG 1: No Poverty."
                    } else {
                        insightText = "No recent data points found for poverty headcount ratio in Malaysia."
                    }
                } else {
                    insightText = "Unable to parse SDG 1 data from the service provider."
                }
            } catch (e: Exception) {
                isError = true
                insightText = "Network connection failed. Please check your internet settings to load SDG 1 insights."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchSDG1Data()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SDG 1 Insights", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "SDG 1: NO POVERTY",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Global Goal Monitoring for Malaysia",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        Text(
                            text = insightText,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 28.sp,
                            textAlign = TextAlign.Center,
                            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { fetchSDG1Data() },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Refresh Global Metrics", fontWeight = FontWeight.Bold)
            }
        }
    }
}
