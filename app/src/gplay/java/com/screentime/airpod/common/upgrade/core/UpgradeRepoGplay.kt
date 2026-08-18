package com.screentime.airpod.common.upgrade.core

import android.app.Activity
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.screentime.airpod.R
import com.screentime.airpod.common.coroutine.AppScope
import com.screentime.airpod.common.coroutine.DispatcherProvider
import com.screentime.airpod.common.debug.logging.Logging.Priority.VERBOSE
import com.screentime.airpod.common.debug.logging.asLog
import com.screentime.airpod.common.debug.logging.log
import com.screentime.airpod.common.debug.logging.logTag
import com.screentime.airpod.common.error.asErrorDialogBuilder
import com.screentime.airpod.common.flow.replayingShare
import com.screentime.airpod.common.upgrade.UpgradeRepo
import com.screentime.airpod.common.upgrade.core.data.BillingData
import com.screentime.airpod.common.upgrade.core.data.BillingDataRepo
import com.screentime.airpod.common.upgrade.core.data.PurchasedSku
import com.screentime.airpod.common.upgrade.core.data.Sku
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpgradeRepoGplay @Inject constructor(
    @AppScope private val scope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    private val billingDataRepo: BillingDataRepo,
    private val billingCache: BillingCache,
) : UpgradeRepo {

    private var lastProStateAt: Long
        get() = billingCache.lastProStateAt.value
        set(value) = billingCache.lastProStateAt.update { value }

    override val upgradeInfo: Flow<UpgradeRepo.Info> = billingDataRepo.billingData
        .map { data -> // Only relinquish pro state if we haven't had it for a while
            val now = System.currentTimeMillis()
            val proSku = data.getProSku()
            log(TAG) { "now=$now, lastProStateAt=$lastProStateAt, data=${data}" }
            when {
                proSku != null -> {
                    // If we are pro refresh timestamp
                    lastProStateAt = now
                    Info(billingData = data)
                }
                (now - lastProStateAt) < 6 * 60 * 60 * 1000L -> { // 6 hours
                    log(TAG, VERBOSE) { "We are not pro, but were recently, did GPlay try annoy us again?" }
                    Info(gracePeriod = true, billingData = null)
                }
                else -> {
                    Info(billingData = data)
                }
            }
        }
        .catch {
            // Ignore Google Play errors if the last pro state was recent
            val now = System.currentTimeMillis()
            log(TAG) { "now=$now, lastProStateAt=$lastProStateAt, error=$it" }
            if ((now - lastProStateAt) < 24 * 60 * 60 * 1000L) { // 24 hours
                log(TAG, VERBOSE) { "We are not pro, but were recently, and just and an error, what is GPlay doing???" }
                emit(Info(gracePeriod = true, billingData = null))
            } else {
                throw it
            }
        }
        .replayingShare(scope)

    override fun launchBillingFlow(activity: Activity) {
        MaterialAlertDialogBuilder(activity).apply {
            setIcon(com.screentime.airpod.common.R.drawable.ic_heart)
            setTitle(R.string.upgrade_capod_label)
            setMessage(R.string.upgrade_capod_description)
            setPositiveButton(R.string.upgrade_pro_yearly) { _, _ ->
                startPurchase(activity, CapodSku.PRO_YEARLY.sku)
            }
            setNegativeButton(R.string.upgrade_pro_monthly) { _, _ ->
                startPurchase(activity, CapodSku.PRO_MONTHLY.sku)
            }
            setNeutralButton(R.string.general_check_action) { _, _ ->
                restorePurchases(activity)
            }
        }.show()
    }

    private fun startPurchase(activity: Activity, sku: Sku) {
        scope.launch {
            try {
                billingDataRepo.startIapFlow(activity, sku)
            } catch (e: Exception) {
                log(TAG) { "startIapFlow failed:${e.asLog()}" }
                withContext(dispatcherProvider.Main) {
                    e.asErrorDialogBuilder(activity).show()
                }
            }
        }
    }

    private fun restorePurchases(activity: Activity) {
        log(TAG) { "recheck()" }
        scope.launch {
            try {
                val data = billingDataRepo.getIapData()
                log(TAG) { "Recheck successful: $data" }
                withContext(dispatcherProvider.Main) {
                    if (data.purchases.isEmpty()) {
                        Toast.makeText(
                            activity,
                            R.string.upgrades_no_purchases_found_check_account,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                log(TAG) { "Recheck failed:${e.asLog()}" }
                withContext(dispatcherProvider.Main) {
                    e.asErrorDialogBuilder(activity).show()
                }
            }
        }
    }

    data class Info(
        private val gracePeriod: Boolean = false,
        private val billingData: BillingData?,
    ) : UpgradeRepo.Info {

        override val type: UpgradeRepo.Type
            get() = UpgradeRepo.Type.GPLAY

        override val isPro: Boolean
            get() = billingData?.getProSku() != null || gracePeriod

        override val upgradedAt: Instant?
            get() = billingData
                ?.getProSku()
                ?.purchase?.purchaseTime
                ?.let { Instant.ofEpochMilli(it) }
    }

    companion object {
        private fun BillingData.getProSku(): PurchasedSku? = purchasedSkus
            .firstOrNull { it.sku.id in CapodSku.PRO_IDS }

        val TAG: String = logTag("Upgrade", "Gplay", "Control")
    }
}
