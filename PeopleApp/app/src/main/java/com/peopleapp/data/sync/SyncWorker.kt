package com.peopleapp.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.peopleapp.data.repository.PeopleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: PeopleRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Collect unsynced data
            val unsyncedPeople = repository.getUnsyncedPeople()
            val unsyncedFacts = repository.getUnsyncedFacts()
            val unsyncedEvents = repository.getUnsyncedEvents()
            val unsyncedRelationships = repository.getUnsyncedRelationships()

            // TODO: When server is configured, POST each unsynced item here.
            // On success, call repository.markPersonSynced(person.id) etc.
            // On conflict (HTTP 409), store the conflict payload on the server for manual resolution.

            // For now just log what needs syncing
            val totalUnsynced = unsyncedPeople.size + unsyncedFacts.size +
                    unsyncedEvents.size + unsyncedRelationships.size

            if (totalUnsynced > 0) {
                // Placeholder: actual HTTP calls go here
            }

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "people_sync"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun syncNow(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
