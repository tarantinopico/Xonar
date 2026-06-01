package com.tarantino.xonarx.domain.usecase

import android.content.Context
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QrScannerUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun scanQrCode(): String? {
        val scanner = GmsBarcodeScanning.getClient(context)
        
        return try {
            val ensureModule = ModuleInstall.getClient(context)
            val request = ModuleInstallRequest.newBuilder()
                .addApi(scanner)
                .build()
            ensureModule.installModules(request).await()
            
            val result = scanner.startScan().await()
            result.rawValue
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
