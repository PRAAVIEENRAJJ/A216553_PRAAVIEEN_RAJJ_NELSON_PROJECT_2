package com.example.a216553_praavieenrajj_mrnelson_project_2.Data

import retrofit2.http.GET

interface ApiService {
    // Fetches a random public text payload asynchronously from the server
    @GET("api/v2/facts/random?language=en")
    suspend fun getRandomFact(): FactResponse
}