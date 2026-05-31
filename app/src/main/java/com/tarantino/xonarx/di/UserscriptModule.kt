package com.tarantino.xonarx.di

import com.tarantino.xonarx.data.repository.UserscriptRepositoryImpl
import com.tarantino.xonarx.domain.repository.UserscriptRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserscriptModule {
    @Binds
    @Singleton
    abstract fun bindUserscriptRepository(
        impl: UserscriptRepositoryImpl
    ): UserscriptRepository
}
