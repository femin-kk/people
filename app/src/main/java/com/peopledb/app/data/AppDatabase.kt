package com.peopledb.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Person::class, Tag::class, PersonTag::class, PersonPlace::class, Relationship::class, Note::class, Photo::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun tagDao(): TagDao
    abstract fun personTagDao(): PersonTagDao
    abstract fun personPlaceDao(): PersonPlaceDao
    abstract fun relationshipDao(): RelationshipDao
    abstract fun noteDao(): NoteDao
    abstract fun photoDao(): PhotoDao

    companion object {
        const val DB_NAME = "peopledb.db"

        // Adds the optional "eventAt" column to notes (custom date/time for a note,
        // distinct from when it was actually created).
        private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN eventAt INTEGER")
            }
        }

        // Adds "gender" to people (for resolving gendered relationship labels) and "role"
        // to relationships (the structured relationship kind, e.g. CHILD, PARENT, SIBLING).
        private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE people ADD COLUMN gender TEXT")
                db.execSQL("ALTER TABLE relationships ADD COLUMN role TEXT")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build().also { INSTANCE = it }
            }
        }

        /** Closes and clears the singleton so the DB file can be safely replaced (used by restore). */
        fun closeInstance() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }
    }
}
