package com.svwh.tools.feature.environment.data

import com.svwh.tools.feature.environment.domain.repository.InstalledAppRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class EnvironmentDataModule {
    @Binds
    abstract fun bindInstalledAppRepository(
        impl: InstalledAppRepositoryImpl,
    ): InstalledAppRepository
}
