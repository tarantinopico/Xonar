package com.tarantino.xonarx.di

import android.content.Context
import com.tarantino.xonarx.domain.usecase.AdBlockerEngine
import com.tarantino.xonarx.domain.usecase.DownloadManagerUseCase
import com.tarantino.xonarx.presentation.browser.BrowserSessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BrowserModule {

    @Provides
    @Singleton
    fun provideBrowserSessionManager(
        @ApplicationContext context: Context,
        adBlockerEngine: AdBlockerEngine,
        downloadManagerUseCase: DownloadManagerUseCase
    ): BrowserSessionManager {
        return BrowserSessionManager(context, adBlockerEngine, downloadManagerUseCase)
    }
}
