package com.peopledb.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "people")
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    // Days since epoch (LocalDate.toEpochDay()), null if unknown
    val birthdayEpochDay: Long? = null,
    // Whether the birth YEAR is known. If false, birthdayEpochDay still stores a
    // placeholder year (e.g. 2000) but only month/day should be shown.
    val birthdayYearKnown: Boolean = true,
    val primaryPhotoPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isPlace: Boolean = false
)

@Entity(
    tableName = "person_tags",
    foreignKeys = [
        ForeignKey(entity = Person::class, parentColumns = ["id"], childColumns = ["personId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Tag::class, parentColumns = ["id"], childColumns = ["tagId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("personId"), Index("tagId")]
)
data class PersonTag(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personId: Long,
    val tagId: Long
)

// A place attachment to a person, with an optional year range for when they were there.
@Entity(
    tableName = "person_places",
    foreignKeys = [
        ForeignKey(entity = Person::class, parentColumns = ["id"], childColumns = ["personId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Tag::class, parentColumns = ["id"], childColumns = ["placeTagId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("personId"), Index("placeTagId")]
)
data class PersonPlace(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personId: Long,
    val placeTagId: Long,
    val fromYear: Int? = null,
    val toYear: Int? = null
)

@Entity(
    tableName = "relationships",
    foreignKeys = [
        ForeignKey(entity = Person::class, parentColumns = ["id"], childColumns = ["personId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Person::class, parentColumns = ["id"], childColumns = ["relatedPersonId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("personId"), Index("relatedPersonId")]
)
data class Relationship(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personId: Long,
    val relatedPersonId: Long,
    val type: String
)

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(entity = Person::class, parentColumns = ["id"], childColumns = ["personId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("personId")]
)
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personId: Long,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(entity = Person::class, parentColumns = ["id"], childColumns = ["personId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("personId")]
)
data class Photo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personId: Long,
    val filePath: String,
    val addedAt: Long = System.currentTimeMillis()
)
