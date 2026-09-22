package com.screentime.airpod.pods.core.apple.airpods

import com.screentime.airpod.pods.core.PodDevice
import com.screentime.airpod.pods.core.apple.BaseAirPodsTest
import com.screentime.airpod.pods.core.apple.DualApplePods
import com.screentime.airpod.pods.core.apple.HasAppleColor
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import com.screentime.airpod.common.uShort
import com.screentime.airpod.common.uByte

class AirPodsGen3Test : BaseAirPodsTest() {

    @Test
    fun `AirPods Gen3`() = runTest {
        create<AirPodsGen3>("07 19 01 13 20 75 AA B9 31 00 04 67 9A 57 DF BE F6 90 52 B0 04 1F 8D 89 DA F4 9E") {
            rawPrefix shouldBe uByte(0x01)
            rawDeviceModel shouldBe uShort(0x1320)
            rawStatus shouldBe uByte(0x75)
            rawPodsBattery shouldBe uByte(0xAA)
            rawFlags shouldBe uShort(0xB)
            rawCaseBattery shouldBe uShort(0x9)
            rawCaseLidState shouldBe uByte(0x31)
            rawDeviceColor shouldBe uByte(0x00)
            rawSuffix shouldBe uByte(0x04)

            batteryLeftPodPercent shouldBe 1.0f
            batteryRightPodPercent shouldBe 1.0f

            isCaseCharging shouldBe false
            isLeftPodCharging shouldBe true
            isRightPodCharging shouldBe true

            isLeftPodInEar shouldBe false
            isRightPodInEar shouldBe false
            batteryCasePercent shouldBe 0.9f

            caseLidState shouldBe DualApplePods.LidState.OPEN

            state shouldBe HasStateDetectionAirPods.ConnectionState.IDLE

            podStyle.identifier shouldBe HasAppleColor.DeviceColor.WHITE.name

            model shouldBe PodDevice.Model.AIRPODS_GEN3
        }
    }

    @Test
    fun `random guy at bus stop`() = runTest {
        create<AirPodsGen3>("07 19 01 13 20 2B 88 8F 01 00 08 E2 0E 84 37 C5 98 16 D4 B7 37 ED 23 8B 08 EA A1") {
            batteryLeftPodPercent shouldBe 0.8f
            batteryRightPodPercent shouldBe 0.8f

            isCaseCharging shouldBe false
            isLeftPodCharging shouldBe false
            isRightPodCharging shouldBe false

            isLeftPodInEar shouldBe true
            isRightPodInEar shouldBe true
            batteryCasePercent shouldBe null

            caseLidState shouldBe DualApplePods.LidState.NOT_IN_CASE

            state shouldBe HasStateDetectionAirPods.ConnectionState.UNKNOWN

            podStyle.identifier shouldBe HasAppleColor.DeviceColor.WHITE.name
        }
    }
}