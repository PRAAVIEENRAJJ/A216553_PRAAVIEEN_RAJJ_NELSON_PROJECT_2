package com.example.a216553_praavieenrajj_mrnelson_project_2.ViewModel

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.a216553_praavieenrajj_mrnelson_project_2.Data.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    private val appDatabase = AppDatabase.getDatabase(application)
    private val envelopeDao = appDatabase.envelopeDao()
    private val foodBankDao = appDatabase.foodBankDao()

    private val _uiState = MutableStateFlow(UserProfile())
    val uiState: StateFlow<UserProfile> = _uiState.asStateFlow()

    private val _povertyInsight = MutableStateFlow("SDG 1: Monitoring progress to end poverty.")
    val povertyInsight: StateFlow<String> = _povertyInsight.asStateFlow()

    // Pillar 1: Sensor Data State
    private val _userLocation = MutableStateFlow<Location?>(null)
    
    // Pillar 4: Cloud Data State
    private val _cloudFoodBanks = MutableStateFlow<List<FoodBankCloudData>>(emptyList())
    val cloudFoodBanks: StateFlow<List<FoodBankCloudData>> = _cloudFoodBanks.asStateFlow()

    // THE INTEGRATION LINK: Reactive stream for physically nearest aid center
    val nearestAidCenter: StateFlow<Pair<FoodBankCloudData, Float>?> = 
        combine(_userLocation, _cloudFoodBanks) { loc, banks ->
            if (loc == null || banks.isEmpty()) null
            else banks.map { bank ->
                val bankLoc = Location("").apply { latitude = bank.latitude; longitude = bank.longitude }
                bank to loc.distanceTo(bankLoc) / 1000f // Return bank and distance in KM
            }.minByOrNull { it.second }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val favoriteFoodBanks: StateFlow<List<FavoriteFoodBank>> = foodBankDao.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val envelopes: StateFlow<List<Envelope>> = envelopeDao.getAllEnvelopes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        fetchFoodBanksFromCloud()
        fetchGlobalInsight()
    }

    fun updateLocation(location: Location) { _userLocation.value = location }

    // Pillar 4 Action: App updates cloud information based on user interaction
    fun logVisit(bank: FoodBankCloudData, onResult: (String) -> Unit) {
        val user = _uiState.value.username
        val loc = _userLocation.value
        if (loc == null) { onResult("GPS Lock required to verify visit."); return }
        
        val bankLoc = Location("").apply { latitude = bank.latitude; longitude = bank.longitude }
        if (loc.distanceTo(bankLoc) > 500) {
            onResult("Too far! You must visit the center to update stock.")
            return
        }

        viewModelScope.launch {
            try {
                // Update Cloud Information
                val newQty = if (bank.foodQuantity > 0) bank.foodQuantity - 1 else 0
                db.collection("food_banks").document(bank.id).update("foodQuantity", newQty, "isAvailable", newQty > 0).await()
                
                // Update Local Room Impact
                val favs = foodBankDao.getAllFavorites().first()
                favs.find { it.bankId == bank.id }?.let {
                    foodBankDao.addFavorite(it.copy(visitCount = it.visitCount + 1))
                }
                onResult("Verified! Cloud stock updated. Impact logged in Room.")
            } catch (e: Exception) {
                onResult("Visit logged locally, but cloud update failed.")
            }
        }
    }

    private fun fetchFoodBanksFromCloud() {
        db.collection("food_banks").addSnapshotListener { snapshot, _ ->
            val banks = snapshot?.documents?.mapNotNull { it.toObject(FoodBankCloudData::class.java)?.copy(id = it.id) }
            _cloudFoodBanks.value = banks ?: emptyList()
        }
    }

    private fun fetchGlobalInsight() {
        viewModelScope.launch {
            try {
                val res = RetrofitClient.apiInstance.getPovertyData()
                if (res.size > 1) {
                    val data: List<WorldBankDataPoint> = Gson().fromJson(res[1], object : TypeToken<List<WorldBankDataPoint>>() {}.type)
                    data.firstOrNull { it.value != null }?.let {
                        _povertyInsight.value = "SDG 1 Update: ${"%.2f".format(it.value)}% poverty rate in Malaysia (${it.date})."
                    }
                }
            } catch (e: Exception) { }
        }
    }

    fun toggleFavorite(bank: FoodBankCloudData) {
        viewModelScope.launch {
            if (foodBankDao.isFavorite(bank.id).first()) {
                foodBankDao.removeFavorite(FavoriteFoodBank(bank.id, bank.name, bank.latitude, bank.longitude, bank.address))
            } else {
                foodBankDao.addFavorite(FavoriteFoodBank(bank.id, bank.name, bank.latitude, bank.longitude, bank.address))
            }
        }
    }

    fun isFavorite(id: String): Flow<Boolean> = foodBankDao.isFavorite(id)

    // User Isolation & Reset Logic
    fun registerUser(f: String, u: String, e: String, p: String, onResult: (Boolean) -> Unit) {
        val userMap = hashMapOf("fullName" to f, "username" to u, "email" to e, "phone" to p)
        db.collection("users").document(u).set(userMap).addOnSuccessListener {
            viewModelScope.launch {
                envelopeDao.clearAllEnvelopes()
                foodBankDao.clearAllFavorites()
                _uiState.value = UserProfile(f, u, e, p)
                onResult(true)
            }
        }.addOnFailureListener { onResult(false) }
    }

    fun loginUser(u: String, onResult: (Boolean) -> Unit) {
        db.collection("users").document(u).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val profile = doc.toObject(UserProfile::class.java) ?: UserProfile()
                // IMPORTANT: Ensure username is set in state even if missing from doc body
                _uiState.value = if (profile.username.isEmpty()) profile.copy(username = u) else profile
                
                viewModelScope.launch {
                    envelopeDao.clearAllEnvelopes()
                    foodBankDao.clearAllFavorites()
                    syncEnvelopesFromFirestore(u)
                    onResult(true)
                }
            } else onResult(false)
        }.addOnFailureListener { onResult(false) }
    }

    private suspend fun syncEnvelopesFromFirestore(u: String) {
        try {
            val snapshot = db.collection("users").document(u).collection("envelopes").get().await()
            val cloudEnvelopes = snapshot.documents.mapNotNull { it.toObject(Envelope::class.java) }
            cloudEnvelopes.forEach { envelopeDao.insertEnvelope(it.copy(id = 0)) }
        } catch (e: Exception) {
            Log.e("UserViewModel", "Sync Error: ${e.message}")
        }
    }

    fun addEnvelope(title: String, budget: Float) {
        val user = _uiState.value.username
        val trimmedTitle = title.trim()
        viewModelScope.launch {
            try {
                val env = Envelope(title = trimmedTitle, budgetTotal = budget)
                envelopeDao.insertEnvelope(env)
                if (user.isNotEmpty()) {
                    db.collection("users").document(user)
                        .collection("envelopes").document(trimmedTitle)
                        .set(env).await()
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error adding envelope: ${e.message}")
            }
        }
    }

    fun updateEnvelopeSavings(envelope: Envelope, newSavings: Float) {
        val user = _uiState.value.username
        viewModelScope.launch {
            try {
                envelopeDao.insertEnvelope(envelope.copy(budgetSpent = newSavings))
                if (user.isNotEmpty()) {
                    db.collection("users").document(user)
                        .collection("envelopes").document(envelope.title)
                        .update("budgetSpent", newSavings).await()
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error updating savings: ${e.message}")
            }
        }
    }

    fun deleteEnvelope(envelope: Envelope) {
        val user = _uiState.value.username
        val titleToDelete = envelope.title
        viewModelScope.launch {
            try {
                // 1. Delete from local Room database immediately for UI responsiveness
                envelopeDao.deleteEnvelope(envelope)
                
                // 2. Delete from Firestore
                if (user.isNotEmpty() && titleToDelete.isNotEmpty()) {
                    db.collection("users").document(user)
                        .collection("envelopes").document(titleToDelete)
                        .delete().await()
                    Log.d("UserViewModel", "Successfully deleted $titleToDelete from Firestore")
                } else {
                    Log.w("UserViewModel", "Could not delete from Firestore: user=$user, title=$titleToDelete")
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error deleting envelope from Firestore: ${e.message}")
            }
        }
    }

    fun clear() {
        _uiState.value = UserProfile()
        viewModelScope.launch {
            envelopeDao.clearAllEnvelopes()
            foodBankDao.clearAllFavorites()
        }
    }
}
