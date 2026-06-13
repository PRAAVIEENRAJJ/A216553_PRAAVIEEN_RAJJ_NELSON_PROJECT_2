package com.example.a216553_praavieenrajj_mrnelson_project_2.Data

import com.google.gson.annotations.SerializedName

data class WorldBankIndicator(
    val id: String = "",
    val value: String = ""
)

data class WorldBankCountry(
    val id: String = "",
    val value: String = ""
)

data class WorldBankDataPoint(
    val indicator: WorldBankIndicator? = null,
    val country: WorldBankCountry? = null,
    val date: String? = "",
    val value: Double? = null
)
