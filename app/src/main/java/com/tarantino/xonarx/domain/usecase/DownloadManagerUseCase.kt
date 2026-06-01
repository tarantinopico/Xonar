package com.tarantino.xonarx.domain.usecase

import android.content.Context
import androidx.work.*
import com.tarantino.xonarx.domain.download.XonarDownloadManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadManagerUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadManager: XonarDownloadManager
) {
    fun startDownload(url: String, fileName: String, identityId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            downloadManager.enqueueDownload(
                url = url,
                fileName = fileName,
                mimeType = "*/*",
                identityId = identityId
            )
        }
    }
}
