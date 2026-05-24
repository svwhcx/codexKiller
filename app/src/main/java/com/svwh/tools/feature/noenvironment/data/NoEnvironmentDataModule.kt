package com.svwh.tools.feature.noenvironment.data

import com.svwh.tools.feature.noenvironment.data.repack.RepackProgressControllerImpl
import com.svwh.tools.feature.noenvironment.data.repack.WorkManagerRepackWorkflowRunner
import com.svwh.tools.feature.noenvironment.domain.repack.RepackProgressController
import com.svwh.tools.feature.noenvironment.domain.repack.RepackWorkflowRunner
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NoEnvironmentDataModule {
    @Binds
    abstract fun bindRepackProgressController(
        impl: RepackProgressControllerImpl,
    ): RepackProgressController

    @Binds
    abstract fun bindRepackWorkflowRunner(
        runner: WorkManagerRepackWorkflowRunner,
    ): RepackWorkflowRunner
}
