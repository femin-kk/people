package com.peopleapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.peopleapp.data.local.dao.*
import com.peopleapp.data.local.entity.*

@Database(
    entities = [
        PersonEntity::class,
        FactEntity::class,
        EventEntity::class,
        RelationshipEntity::class,
        PhotoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PeopleDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun factDao(): FactDao
    abstract fun eventDao(): EventDao
    abstract fun relationshipDao(): RelationshipDao
    abstract fun photoDao(): PhotoDao
}
