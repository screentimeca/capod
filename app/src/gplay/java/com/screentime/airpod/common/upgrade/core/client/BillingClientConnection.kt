package com.screentime.airpod.common.upgrade.core.client

import android.app.Activity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.screentime.airpod.common.debug.logging.Logging.Priority.INFO
import com.screentime.airpod.common.debug.logging.Logging.Priority.WARN
import com.screentime.airpod.common.debug.logging.log
import com.screentime.airpod.common.debug.logging.logTag
import com.screentime.airpod.common.flow.setupCommonEventHandlers
import com.screentime.airpod.common.upgrade.core.data.Sku
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

data class BillingClientConnection(
    private val client: BillingClient,
    private val purchasesGlobal: Flow<Collection<Purchase>>,
) {
    private val purchasesLocal = MutableStateFlow<Collection<Purchase>>(emptySet())
    val purchases: Flow<Collection<Purchase>> = combine(purchasesGlobal, purchasesLocal) { global, local ->
        val combined = mutableSetOf<Purchase>()

        combined.addAll(local)

        global
            .let { purchases -> purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED } }
            .let { combined.addAll(it) }

        combined.sortedByDescending { it.purchaseTime }
    }
        .setupCommonEventHandlers(TAG) { "purchases" }

    suspend fun queryPurchases(): Collection<Purchase> {
        val inapp = queryPurchases(BillingClient.ProductType.INAPP)
        val subs = queryPurchases(BillingClient.ProductType.SUBS)
        val purchases = (inapp + subs)
            .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            .distinctBy { it.purchaseToken }

        purchasesLocal.value = purchases
        return purchases
    }

    private suspend fun queryPurchases(productType: String): Collection<Purchase> {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(productType)
            .build()
        val (result: BillingResult, purchases) = suspendCoroutine<Pair<BillingResult, Collection<Purchase>?>> { continuation ->
            client.queryPurchasesAsync(params) { result, purchases ->
                continuation.resume(result to purchases)
            }
        }

        log(TAG) {
            "queryPurchases(type=$productType): code=${result.responseCode}, message=${result.debugMessage}, purchases=$purchases"
        }

        if (!result.isSuccess) {
            log(TAG, WARN) { "queryPurchases(type=$productType) failed" }
            throw BillingResultException(result)
        }

        return requireNotNull(purchases)
    }

    suspend fun acknowledgePurchase(purchase: Purchase) {
        val ack = AcknowledgePurchaseParams.newBuilder().apply {
            setPurchaseToken(purchase.purchaseToken)
        }.build()

        val result = suspendCoroutine<BillingResult> { continuation ->
            client.acknowledgePurchase(ack) { continuation.resume(it) }
        }

        log(TAG, INFO) { "acknowledgePurchase($purchase): code=${result.responseCode} (${result.debugMessage})" }

        if (!result.isSuccess) throw BillingResultException(result)
    }

    suspend fun querySku(sku: Sku): Sku.Details {
        val productDetails = QueryProductDetailsParams.Product.newBuilder().apply {
            setProductType(sku.type)
            setProductId(sku.id)
        }.build()

        val params = QueryProductDetailsParams.newBuilder().setProductList(listOf(productDetails)).build()

        val (result, details) = suspendCoroutine<Pair<BillingResult, Collection<ProductDetails>?>> { continuation ->
            client.queryProductDetailsAsync(params) { result, queryProductDetailsResult ->
                val unfetched = queryProductDetailsResult.unfetchedProductList
                if (unfetched.isNotEmpty()) {
                    log(TAG, WARN) { "querySku(sku=$sku): unfetched=$unfetched" }
                }
                continuation.resume(result to queryProductDetailsResult.productDetailsList)
            }
        }

        log(TAG) {
            "querySku(sku=$sku): code=${result.responseCode}, debug=${result.debugMessage}), skuDetails=$details"
        }

        if (!result.isSuccess) throw BillingResultException(result)

        if (details.isNullOrEmpty()) throw IllegalStateException("Unknown SKU, no details available.")

        return Sku.Details(sku, details)
    }

    suspend fun launchBillingFlow(activity: Activity, sku: Sku): BillingResult {
        log(TAG) { "launchBillingFlow(activity=$activity, sku=$sku)" }
        val skuDetails = querySku(sku)
        return launchBillingFlow(activity, skuDetails)
    }

    suspend fun launchBillingFlow(activity: Activity, skuDetails: Sku.Details): BillingResult {
        log(TAG) { "launchBillingFlow(activity=$activity, skuDetails=$skuDetails)" }

        val productDetails = skuDetails.details.first()
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder().apply {
            setProductDetails(productDetails)
            if (productDetails.productType == BillingClient.ProductType.SUBS) {
                val offerToken = productDetails.subscriptionOfferDetails
                    ?.firstOrNull { it.offerId.isNullOrEmpty() }
                    ?.offerToken
                    ?: productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
                    ?: throw IllegalStateException("No subscription offer for ${skuDetails.sku.id}")
                setOfferToken(offerToken)
            }
        }.build()

        val billingFlowParams = BillingFlowParams.newBuilder().apply {
            setProductDetailsParamsList(listOf(productParams))
        }.build()

        return client.launchBillingFlow(activity, billingFlowParams)
    }

    companion object {
        val TAG: String = logTag("Upgrade", "Gplay", "Billing", "ClientConnection")
    }
}
