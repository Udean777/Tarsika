package com.ssajudn.tarsika.feature.gallery.data.local

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ssajudn.tarsika.feature.gallery.domain.usecase.PurgeExpiredLocalTrashUseCase
import kotlinx.coroutines.CancellationException
import java.io.File
import java.util.concurrent.TimeUnit

class ExpiredLocalTrashWorker(
    context: Context,
    parameters: WorkerParameters,
) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result =
        try {
            val database = UserAlbumDatabase.get(applicationContext)
            val repository =
                RoomLocalTrashRepository(
                    applicationContext.contentResolver,
                    database.localTrash(),
                    File(applicationContext.filesDir, TRASH_DIRECTORY),
                )
            PurgeExpiredLocalTrashUseCase(repository)()
            Result.success()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            Result.retry()
        }

    companion object {
        private const val WORK_NAME = "purge_expired_local_trash"
        private const val TRASH_DIRECTORY = "trash"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ExpiredLocalTrashWorker>(1, TimeUnit.DAYS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
