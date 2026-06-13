package com.example.a216553_praavieenrajj_mrnelson_project_2.Data

import androidx.room.Entity
import androidx.room.PrimaryKey

data class UserProfile(
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val verificationPoints: Int = 0 // PILLAR 4: User impact stored in cloud
)

@Entity(tableName = "budget_envelopes")
data class Envelope(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "",
    val emoji: String = "💰",
    val budgetTotal: Float = 0f,
    val budgetSpent: Float = 0f
)

@Entity(tableName = "favorite_food_banks")
data class FavoriteFoodBank(
    @PrimaryKey val bankId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val visitCount: Int = 0 // PILLAR 3: Local visit tracking
)

data class FoodBankCloudData(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val foodQuantity: Int = 0,
    val isAvailable: Boolean = true
)
