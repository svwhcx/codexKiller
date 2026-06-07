package com.svwh.tools.feature.hookconfig.data

import com.svwh.tools.feature.hookconfig.data.log.ContentProviderHookLogRepository
import com.svwh.tools.feature.hookconfig.data.log.RoomFridaLogRepository
import com.svwh.tools.feature.hookconfig.domain.repository.FridaLogRepository
import com.svwh.tools.feature.hookconfig.domain.repository.HookLogRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class HookLogDataModule {
    @Binds
    abstract fun bindHookLogRepository(
        impl: ContentProviderHookLogRepository,
    ): HookLogRepository

    @Binds
    abstract fun bindFridaLogRepository(
        impl: RoomFridaLogRepository,
    ): FridaLogRepository
}
