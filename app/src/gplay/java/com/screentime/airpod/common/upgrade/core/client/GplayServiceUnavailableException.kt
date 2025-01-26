package com.screentime.airpod.common.upgrade.core.client

import android.content.Context
import com.screentime.airpod.R
import com.screentime.airpod.common.error.HasLocalizedError
import com.screentime.airpod.common.error.LocalizedError

class GplayServiceUnavailableException(cause: Throwable) : Exception("Google Play services are unavailable.", cause),
    HasLocalizedError {
    override fun getLocalizedError(context: Context): LocalizedError = LocalizedError(
        throwable = this,
        label = "Google Play Services Unavailable",
        description = context.getString(R.string.upgrades_gplay_unavailable_error)
    )
}