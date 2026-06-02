package com.example.a216553_praavieenrajj_mrnelson_project_2.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddEnvelopeScreen(onEnvelopeAdded: (String, Float) -> Unit, onBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Create New Envelope", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Category Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = budget, onValueChange = { if (it.all { char -> char.isDigit() }) budget = it }, label = { Text("Budget Amount (RM)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { if (title.isNotEmpty() && budget.isNotEmpty()) onEnvelopeAdded(title, budget.toFloat()) }, modifier = Modifier.fillMaxWidth()) {
            Text("Save Envelope")
        }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
    }
}