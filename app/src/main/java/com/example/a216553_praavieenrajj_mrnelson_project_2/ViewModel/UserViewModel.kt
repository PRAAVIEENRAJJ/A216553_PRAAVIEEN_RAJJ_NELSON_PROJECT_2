package com.example.a216553_praavieenrajj_mrnelson_project_2.ViewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.a216553_praavieenrajj_mrnelson_project_2.Data.AppDatabase
import com.example.a216553_praavieenrajj_mrnelson_project_2.Data.Envelope
import com.example.a216553_praavieenrajj_mrnelson_project_2.Data.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val envelopeDao = AppDatabase.getDatabase(application).envelopeDao()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(UserProfile())
    val uiState: StateFlow<UserProfile> = _uiState.asStateFlow()

    val envelopes: StateFlow<List<Envelope>> = envelopeDao.getAllEnvelopes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun registerUser(fullName: String, username: String, email: String, phone: String, onResult: (Boolean) -> Unit) {
        val userMap = hashMapOf(
            "fullName" to fullName,
            "username" to username,
            "email" to email,
            "phone" to phone
        )

        db.collection("users").document(username).set(userMap)
            .addOnSuccessListener {
                _uiState.update { UserProfile(fullName, username, email, phone) }
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun loginUser(username: String, onResult: (Boolean) -> Unit) {
        db.collection("users").document(username).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val profile = document.toObject(UserProfile::class.java)
                    if (profile != null) {
                        _uiState.value = profile
                        
                        // Sync envelopes from Firestore to local Room
                        viewModelScope.launch {
                            syncEnvelopesFromFirestore(username)
                        }
                        
                        onResult(true)
                    } else {
                        onResult(false)
                    }
                } else {
                    onResult(false)
                }
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    private suspend fun syncEnvelopesFromFirestore(username: String) {
        try {
            val snapshot = db.collection("users")
                .document(username)
                .collection("envelopes")
                .get()
                .await()
            
            val cloudEnvelopes = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Envelope::class.java)
            }
            
            // Wipe local for this user and replace with cloud data
            envelopeDao.clearAllEnvelopes()
            cloudEnvelopes.forEach { 
                // Reset ID to 0 so Room auto-generates a new local ID
                envelopeDao.insertEnvelope(it.copy(id = 0)) 
            }
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error syncing envelopes: ${e.message}")
        }
    }

    fun updateFromLogin(username: String) {
        _uiState.update { it.copy(username = username) }
    }

    fun updateFromRegister(fullName: String, username: String, email: String, phone: String) {
        _uiState.update { UserProfile(fullName, username, email, phone) }
    }

    fun addEnvelope(title: String, budget: Float) {
        val currentUsername = _uiState.value.username
        
        viewModelScope.launch {
            val newEnv = Envelope(
                title = title,
                emoji = "💰",
                budgetTotal = budget,
                budgetSpent = 0f
            )
            
            // 1. Save to Local Room
            envelopeDao.insertEnvelope(newEnv)
            
            // 2. Save to Firestore under User's collection if logged in
            if (currentUsername.isNotEmpty()) {
                db.collection("users")
                    .document(currentUsername)
                    .collection("envelopes")
                    .add(newEnv)
                    .addOnFailureListener { e ->
                        Log.e("UserViewModel", "Failed to save envelope to cloud: ${e.message}")
                    }
            }
        }
    }

    fun clear() {
        _uiState.value = UserProfile()
        viewModelScope.launch {
            envelopeDao.clearAllEnvelopes()
        }
    }
}