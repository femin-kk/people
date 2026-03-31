package com.peopleapp.data.local.dao

import androidx.room.*
import com.peopleapp.data.local.entity.PersonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {

    @Query("SELECT * FROM people WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllPeople(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people WHERE id = :id AND isDeleted = 0")
    suspend fun getPersonById(id: String): PersonEntity?

    @Query("SELECT * FROM people WHERE id = :id AND isDeleted = 0")
    fun getPersonByIdFlow(id: String): Flow<PersonEntity?>

    @Query("""
        SELECT * FROM people 
        WHERE isDeleted = 0 
        AND (
            name LIKE '%' || :query || '%' 
            OR nickname LIKE '%' || :query || '%'
            OR email LIKE '%' || :query || '%'
            OR notes LIKE '%' || :query || '%'
        )
        ORDER BY name ASC
    """)
    fun searchPeople(query: String): Flow<List<PersonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: PersonEntity)

    @Update
    suspend fun updatePerson(person: PersonEntity)

    @Query("UPDATE people SET isDeleted = 1, updatedAt = :time WHERE id = :id")
    suspend fun softDeletePerson(id: String, time: Long = System.currentTimeMillis())

    @Query("SELECT * FROM people WHERE isSynced = 0 AND isDeleted = 0")
    suspend fun getUnsyncedPeople(): List<PersonEntity>

    @Query("UPDATE people SET isSynced = 1, syncedAt = :time WHERE id = :id")
    suspend fun markAsSynced(id: String, time: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM people WHERE isDeleted = 0")
    suspend fun getPeopleCount(): Int
}
