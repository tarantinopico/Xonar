package com.tarantino.xonarx.domain.usecase

import android.content.Context
import androidx.work.*
import com.tarantino.xonarx.domain.model.DownloadItem
import com.tarantino.xonarx.domain.model.DownloadStatus
import com.tarantino.xonarx.domain.repository.DownloadRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadManagerUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadRepository: DownloadRepository
) {
    fun startDownload(url: String, fileName: String, identityId: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = Data.Builder()
            .putString("URL", url)
            .putString("IDENTITY_ID", identityId)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)

        val item = DownloadItem(
            id = workRequest.id.toString(),
            identityId = identityId,
            url = url,
            fileName = fileName,
            mimeType = "*/*",
            destinationPath = "",
            progress = 0,
            status = DownloadStatus.QUEUED,
            scheduledAt = System.currentTimeMillis(),
            startedAt = null,
            completedAt = null,
            errorMessage = null
        )

        CoroutineScope(Dispatchers.IO).launch {
            downloadRepository.addDownload(item)
        }
    }
}
