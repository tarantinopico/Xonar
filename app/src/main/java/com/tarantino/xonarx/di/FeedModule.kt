package com.tarantino.xonarx.di

import com.tarantino.xonarx.data.repository.FeedRepositoryImpl
import com.tarantino.xonarx.domain.repository.FeedRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FeedModule {
    @Binds
    @Singleton
    abstract fun bindFeedRepository(
        impl: FeedRepositoryImpl
    ): FeedRepository
}
