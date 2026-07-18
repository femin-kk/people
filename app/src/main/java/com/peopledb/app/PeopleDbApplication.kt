package com.peopledb.app

import android.app.Application
import com.peopledb.app.data.AppDatabase
import com.peopledb.app.data.Repository

class PeopleDbApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: Repository
        private set

    override fun onCreate() {
        super.onCreate()
        initDatabase()
    }

    /** (Re)creates the database + repository. Called on startup and after a restore. */
    fun initDatabase() {
        database = AppDatabase.getInstance(this)
        repository = Repository(database)
    }
}
