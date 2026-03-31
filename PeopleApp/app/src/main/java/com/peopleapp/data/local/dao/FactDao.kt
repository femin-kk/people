package com.peopleapp.data.local.dao

import androidx.room.*
import com.peopleapp.data.local.entity.FactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FactDao {

    @Query("SELECT * FROM facts WHERE personId = :personId AND isDeleted = 0 ORDER BY category, label")
    fun getFactsForPerson(personId: String): Flow<List<FactEntity>>

    @Query("""
        SELECT f.* FROM facts f
        INNER JOIN people p ON f.personId = p.id
        WHERE p.isDeleted = 0 AND f.isDeleted = 0
        AND (f.value LIKE '%' || :query || '%' OR f.label LIKE '%' || :query || '%' OR f.category LIKE '%' || :query || '%')
    """)
    fun searchFacts(query: String): Flow<List<FactEntity>>

    @Query("""
        SELECT f.* FROM facts f
        INNER JOIN people p ON f.personId = p.id
        WHERE p.isDeleted = 0 AND f.isDeleted = 0
        AND f.personId = :personId
        AND (f.value LIKE '%' || :query || '%' OR f.label LIKE '%' || :query || '%')
    """)
    fun searchFactsForPerson(personId: String, query: String): Flow<List<FactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFact(fact: FactEntity)

    @Update
    suspend fun updateFact(fact: FactEntity)

    @Query("UPDATE facts SET isDeleted = 1, updatedAt = :time WHERE id = :id")
    suspend fun softDeleteFact(id: String, time: Long = System.currentTimeMillis())

    @Query("SELECT * FROM facts WHERE isSynced = 0 AND isDeleted = 0")
    suspend fun getUnsyncedFacts(): List<FactEntity>
}
