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

class AirPodsGen4Test : BaseAirPodsTest() {

    @Test
    fun `AirPods Gen4 via log from #225`() = runTest {
        create<AirPodsGen4>("07 19 01 19 20 2B 33 8F 11 00 04 59 D4 57 20 0F 1C 13 38 B2 00 74 E9 DD 70 D7 A5") {
            rawPrefix shouldBe uByte(0x01)
            rawDeviceModel shouldBe uShort(0x1920)
            rawStatus shouldBe uByte(0x2B)
            rawPodsBattery shouldBe uByte(0x33)
            rawFlags shouldBe uShort(0x8)
            rawCaseBattery shouldBe uShort(0xF)
            rawCaseLidState shouldBe uByte(0x11)
            rawDeviceColor shouldBe uByte(0x00)
            rawSuffix shouldBe uByte(0x04)

            batteryLeftPodPercent shouldBe 0.3f
            batteryRightPodPercent shouldBe 0.3f

            isCaseCharging shouldBe false
            isLeftPodCharging shouldBe false
            isRightPodCharging shouldBe false

            isLeftPodInEar shouldBe true
            isRightPodInEar shouldBe true
            batteryCasePercent shouldBe null

            caseLidState shouldBe DualApplePods.LidState.UNKNOWN

            state shouldBe HasStateDetectionAirPods.ConnectionState.IDLE

            podStyle.identifier shouldBe HasAppleColor.DeviceColor.WHITE.name

            model shouldBe PodDevice.Model.AIRPODS_GEN4
        }
    }
}