package com.screentime.airpod.debug.autoreport

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.screentime.airpod.common.debug.autoreport.AutomaticBugReporter
import com.screentime.airpod.debug.autoreport.FossAutoReporting
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class AutoReportingModule {
    @Binds
    @Singleton
    abstract fun autoreporting(foss: FossAutoReporting): AutomaticBugReporter
}