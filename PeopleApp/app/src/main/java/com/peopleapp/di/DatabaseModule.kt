package com.peopleapp.di

import android.content.Context
import androidx.room.Room
import com.peopleapp.data.local.PeopleDatabase
import com.peopleapp.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PeopleDatabase =
        Room.databaseBuilder(context, PeopleDatabase::class.java, "people_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun providePersonDao(db: PeopleDatabase): PersonDao = db.personDao()
    @Provides fun provideFactDao(db: PeopleDatabase): FactDao = db.factDao()
    @Provides fun provideEventDao(db: PeopleDatabase): EventDao = db.eventDao()
    @Provides fun provideRelationshipDao(db: PeopleDatabase): RelationshipDao = db.relationshipDao()
    @Provides fun providePhotoDao(db: PeopleDatabase): PhotoDao = db.photoDao()
}
