package com.peopleapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "facts",
    foreignKeys = [
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("personId")]
)
data class FactEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val personId: String,
    val category: String,  // e.g. "occupation", "hobby", "where_met", "custom"
    val label: String,     // e.g. "Occupation", "Hobby"
    val value: String,     // e.g. "Software Engineer"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)
