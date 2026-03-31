package com.peopleapp.data.local.dao

import androidx.room.*
import com.peopleapp.data.local.entity.RelationshipEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RelationshipDao {

    @Query("""
        SELECT * FROM relationships 
        WHERE (fromPersonId = :personId OR toPersonId = :personId) 
        AND isDeleted = 0
        ORDER BY type
    """)
    fun getRelationshipsForPerson(personId: String): Flow<List<RelationshipEntity>>

    @Query("""
        SELECT r.* FROM relationships r
        INNER JOIN people p1 ON r.fromPersonId = p1.id
        INNER JOIN people p2 ON r.toPersonId = p2.id
        WHERE r.isDeleted = 0 AND p1.isDeleted = 0 AND p2.isDeleted = 0
        AND (r.type LIKE '%' || :query || '%' OR r.label LIKE '%' || :query || '%' OR r.notes LIKE '%' || :query || '%')
    """)
    fun searchRelationships(query: String): Flow<List<RelationshipEntity>>

    @Query("""
        SELECT * FROM relationships 
        WHERE fromPersonId = :fromId AND toPersonId = :toId AND isDeleted = 0
        LIMIT 1
    """)
    suspend fun getRelationshipBetween(fromId: String, toId: String): RelationshipEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationship(relationship: RelationshipEntity)

    @Update
    suspend fun updateRelationship(relationship: RelationshipEntity)

    @Query("UPDATE relationships SET isDeleted = 1, updatedAt = :time WHERE id = :id")
    suspend fun softDeleteRelationship(id: String, time: Long = System.currentTimeMillis())

    @Query("SELECT * FROM relationships WHERE isSynced = 0 AND isDeleted = 0")
    suspend fun getUnsyncedRelationships(): List<RelationshipEntity>
}
