package com.example.a216553_praavieenrajj_mrnelson_project_2.Data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EnvelopeDao {
    @Query("SELECT * FROM budget_envelopes ORDER BY id DESC")
    fun getAllEnvelopes(): Flow<List<Envelope>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnvelope(envelope: Envelope)

    @Delete
    suspend fun deleteEnvelope(envelope: Envelope)

    @Query("DELETE FROM budget_envelopes")
    suspend fun clearAllEnvelopes()
}