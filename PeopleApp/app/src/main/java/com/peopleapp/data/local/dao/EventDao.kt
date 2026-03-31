package com.peopleapp.data.local.dao

import androidx.room.*
import com.peopleapp.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Query("SELECT * FROM events WHERE personId = :personId AND isDeleted = 0 ORDER BY date DESC")
    fun getEventsForPerson(personId: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Query("UPDATE events SET isDeleted = 1, updatedAt = :time WHERE id = :id")
    suspend fun softDeleteEvent(id: String, time: Long = System.currentTimeMillis())

    @Query("SELECT * FROM events WHERE isSynced = 0 AND isDeleted = 0")
    suspend fun getUnsyncedEvents(): List<EventEntity>
}
