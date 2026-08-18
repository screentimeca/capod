package com.screentime.airpod.common.upgrade

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.screentime.airpod.common.upgrade.core.UpgradeControlFoss
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class UpgradeModule {
    @Binds
    @Singleton
    abstract fun control(foss: UpgradeControlFoss): UpgradeRepo

}