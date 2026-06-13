package com.example.a216553_praavieenrajj_mrnelson_project_2.Data

import com.google.gson.JsonElement
import retrofit2.http.GET

interface ApiService {
    // Fetches Malaysia's poverty headcount ratio from the World Bank API (SDG 1 related)
    @GET("v2/country/MY/indicator/SI.POV.DDAY?format=json")
    suspend fun getPovertyData(): List<JsonElement>
}
