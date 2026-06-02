package com.example.a216553_praavieenrajj_mrnelson_project_2.Data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_envelopes")
data class EnvelopeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val emoji: String,
    val budgetTotal: Float,
    val budgetSpent: Float
)