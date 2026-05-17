package com.svwh.tools.core.hook

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class HookStateModule {
    @Binds
    abstract fun bindHookStateRepository(
        impl: HookStateRepositoryImpl,
    ): HookStateRepository
}
