package com.svwh.tools.feature.hookconfig.data

import com.svwh.tools.feature.hookconfig.data.user.RoomUserHookConfigRepository
import com.svwh.tools.feature.hookconfig.domain.repository.UserHookConfigRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserHookConfigDataModule {
    @Binds
    @Singleton
    abstract fun bindUserHookConfigRepository(
        impl: RoomUserHookConfigRepository,
    ): UserHookConfigRepository
}
