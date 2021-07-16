package com.example.degram.workManager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.degram.data.DegramRepository
import com.example.degram.util.showNotification
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class UploadScoreWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: DegramRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "com.example.degram.workManager.UploadScoreWorker"
    }

    override suspend fun doWork(): Result {
        //Show the notification
        showNotification(applicationContext)

        try {



        } catch (e : Exception) {
            return Result.retry()
        }
        return Result.success()
    }
}