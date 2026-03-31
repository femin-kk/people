package com.peopleapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "events",
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
data class EventEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val personId: String,
    val type: String,      // e.g. "birthday", "met", "last_contact", "important"
    val label: String,     // e.g. "Birthday", "When We Met"
    val date: Long,        // epoch ms
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)
