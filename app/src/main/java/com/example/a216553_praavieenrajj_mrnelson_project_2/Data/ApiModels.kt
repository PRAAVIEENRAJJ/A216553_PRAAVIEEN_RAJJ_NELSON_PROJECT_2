package com.example.a216553_praavieenrajj_mrnelson_project_2.Data

import com.google.gson.annotations.SerializedName

data class FactResponse(
    @SerializedName("id") val id: String,
    @SerializedName("text") val factText: String,
    @SerializedName("source") val source: String
)