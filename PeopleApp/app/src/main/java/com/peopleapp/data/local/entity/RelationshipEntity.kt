package com.peopleapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "relationships",
    foreignKeys = [
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["fromPersonId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["toPersonId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("fromPersonId"),
        Index("toPersonId"),
        Index(value = ["fromPersonId", "toPersonId"], unique = true)
    ]
)
data class RelationshipEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val fromPersonId: String,
    val toPersonId: String,
    val type: String,       // e.g. "friend", "spouse", "colleague", "parent", "introduced_by"
    val label: String,      // e.g. "Friend", "Spouse"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)
