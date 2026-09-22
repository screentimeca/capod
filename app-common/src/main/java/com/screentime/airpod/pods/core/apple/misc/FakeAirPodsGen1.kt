package com.screentime.airpod.pods.core.apple.misc

import com.screentime.airpod.common.bluetooth.BleScanResult
import com.screentime.airpod.common.debug.logging.log
import com.screentime.airpod.common.debug.logging.logTag
import com.screentime.airpod.common.isBitSet
import com.screentime.airpod.common.lowerNibble
import com.screentime.airpod.common.upperNibble
import com.screentime.airpod.pods.core.*
import com.screentime.airpod.pods.core.apple.ApplePods
import com.screentime.airpod.pods.core.apple.ApplePodsFactory
import com.screentime.airpod.pods.core.apple.protocol.ProximityPairing
import java.time.Instant
import javax.inject.Inject
import com.screentime.airpod.common.uShort

/**
 * Basically an AirPods GEN1 clone
 * Similar data structure but a lot of placeholder values or hardcoded values
 * Marketed as TWS i99999
 */
data class FakeAirPodsGen1 constructor(
    override val identifier: PodDevice.Id = PodDevice.Id(),
    override val seenLastAt: Instant = Instant.now(),
    override val seenFirstAt: Instant = Instant.now(),
    override val seenCounter: Int = 1,
    override val scanResult: BleScanResult,
    override val proximityMessage: ProximityPairing.Message,
    override val reliability: Float = PodDevice.BASE_CONFIDENCE,
    private val rssiAverage: Int? = null,
    private val cachedBatteryPercentage: Float? = null,
) : ApplePods, DualPodDevice, HasEarDetectionDual, HasChargeDetectionDual, HasCase {

    override val model: PodDevice.Model = PodDevice.Model.FAKE_AIRPODS_GEN1

    override val rssi: Int
        get() = rssiAverage ?: super<ApplePods>.rssi

    /**
     * Normally values for the left pod are in the lower nibbles, if the left pod is primary (microphone)
     * If the right pod is the primary, the values are flipped.
     */
    val areValuesFlipped: Boolean
        get() = !rawStatus.isBitSet(5)

    override val batteryLeftPodPercent: Float?
        get() {
            val value = when (areValuesFlipped) {
                true -> rawPodsBattery.upperNibble.toInt()
                false -> rawPodsBattery.lowerNibble.toInt()
            }
            return when (value) {
                15 -> null
                else -> if (value > 10) {
                    log { "Left pod: Above 100% battery: $value" }
                    1.0f
                } else {
                    (value / 10f)
                }
            }
        }

    override val batteryRightPodPercent: Float?
        get() {
            val value = when (areValuesFlipped) {
                true -> rawPodsBattery.lowerNibble.toInt()
                false -> rawPodsBattery.upperNibble.toInt()
            }
            return when (value) {
                15 -> null
                else -> if (value > 10) {
                    log { "Right pod: Above 100% battery: $value" }
                    1.0f
                } else {
                    value / 10f
                }
            }
        }

    private val isThisPodInThecase: Boolean
        get() = rawStatus.isBitSet(6)

    override val isLeftPodInEar: Boolean
        get() = when (areValuesFlipped xor isThisPodInThecase) {
            true -> rawStatus.isBitSet(3)
            false -> rawStatus.isBitSet(1)
        }

    override val isRightPodInEar: Boolean
        get() = when (areValuesFlipped xor isThisPodInThecase) {
            true -> rawStatus.isBitSet(1)
            false -> rawStatus.isBitSet(3)
        }

    override val isLeftPodCharging: Boolean
        get() = when (areValuesFlipped) {
            false -> rawFlags.isBitSet(0)
            true -> rawFlags.isBitSet(1)
        }

    override val isRightPodCharging: Boolean
        get() = when (areValuesFlipped) {
            false -> rawFlags.isBitSet(1)
            true -> rawFlags.isBitSet(0)
        }

    override val batteryCasePercent: Float?
        get() = when (val value = rawCaseBattery.toInt()) {
            15 -> cachedBatteryPercentage
            else -> if (value > 10) {
                log { "Case: Above 100% battery: $value" }
                1.0f
            } else {
                value / 10f
            }
        }

    override val isCaseCharging: Boolean
        get() = rawFlags.isBitSet(2)

    class Factory @Inject constructor() : ApplePodsFactory<FakeAirPodsGen1>(TAG) {

        override fun isResponsible(message: ProximityPairing.Message): Boolean = message.run {
            // Official message length is 19HEX, i.e. binary 25, did they copy this wrong?
            getModelInfo().full == DEVICE_CODE && length == 19
        }

        override fun create(scanResult: BleScanResult, message: ProximityPairing.Message): ApplePods {
            var basic = FakeAirPodsGen1(scanResult = scanResult, proximityMessage = message)
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
                cachedBatteryPercentage = result.getLatestCaseBattery(),
                rssiAverage = result.rssiSmoothed(basic.rssi),
            )
        }
    }

    companion object {
        private val DEVICE_CODE = uShort(0x0220)
        private val TAG = logTag("PodDevice", "Apple", "Fake", "AirPods", "Gen1")
    }
}