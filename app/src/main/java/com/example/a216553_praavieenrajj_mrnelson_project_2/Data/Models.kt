package com.example.a216553_praavieenrajj_mrnelson_project_2.Data

import androidx.room.Entity
import androidx.room.PrimaryKey

data class UserProfile(
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = ""
)

@Entity(tableName = "budget_envelopes")
data class Envelope(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "",
    val emoji: String = "💰",
    val budgetTotal: Float = 0f,
    val budgetSpent: Float = 0f
)