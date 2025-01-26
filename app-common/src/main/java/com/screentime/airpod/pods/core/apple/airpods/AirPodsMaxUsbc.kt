package com.screentime.airpod.pods.core.apple.airpods

import com.screentime.airpod.common.bluetooth.BleScanResult
import com.screentime.airpod.common.debug.logging.logTag
import com.screentime.airpod.common.isBitSet
import com.screentime.airpod.pods.core.HasChargeDetection
import com.screentime.airpod.pods.core.HasEarDetection
import com.screentime.airpod.pods.core.PodDevice
import com.screentime.airpod.pods.core.apple.ApplePods
import com.screentime.airpod.pods.core.apple.HasAppleColor
import com.screentime.airpod.pods.core.apple.SingleApplePods
import com.screentime.airpod.pods.core.apple.SingleApplePodsFactory
import com.screentime.airpod.pods.core.apple.protocol.ProximityPairing
import java.time.Instant
import javax.inject.Inject

data class AirPodsMaxUsbc(
    override val identifier: PodDevice.Id = PodDevice.Id(),
    override val seenLastAt: Instant = Instant.now(),
    override val seenFirstAt: Instant = Instant.now(),
    override val seenCounter: Int = 1,
    override val scanResult: BleScanResult,
    override val proximityMessage: ProximityPairing.Message,
    override val reliability: Float = PodDevice.BASE_CONFIDENCE,
    private val rssiAverage: Int? = null,
) : SingleApplePods, HasEarDetection, HasChargeDetection, HasAppleColor {

    override val model: PodDevice.Model = PodDevice.Model.AIRPODS_MAX_USBC

    override val rssi: Int
        get() = rssiAverage ?: super<SingleApplePods>.rssi

    override val isHeadsetBeingCharged: Boolean
        get() = rawFlags.isBitSet(0)

    override val isBeingWorn: Boolean
        get() = rawStatus.isBitSet(5)

    class Factory @Inject constructor() : SingleApplePodsFactory(TAG) {

        override fun isResponsible(message: ProximityPairing.Message): Boolean = message.run {
            getModelInfo().full == DEVICE_CODE && length == ProximityPairing.PAIRING_MESSAGE_LENGTH
        }

        override fun create(scanResult: BleScanResult, message: ProximityPairing.Message): ApplePods {
            var basic = AirPodsMaxUsbc(scanResult = scanResult, proximityMessage = message)
            val result = searchHistory(basic)

            if (result != null) basic = basic.copy(identifier = result.id)
            updateHistory(basic)

            if (result == null) return basic

            return basic.copy(
                identifier = result.id,
                seenFirstAt = result.seenFirstAt,
                seenLastAt = scanResult.receivedAt,
                seenCounter = result.seenCounter,
                reliability = result.reliability,
                rssiAverage = result.rssiSmoothed(basic.rssi),
            )
        }
    }

    companion object {
        private val DEVICE_CODE = 0x1F20.toUShort()
        private val TAG = logTag("PodDevice", "Apple", "AirPods", "Max", "USBC")
    }
}
