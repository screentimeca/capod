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

class AirPodsGen2Test : BaseAirPodsTest() {

    @Test
    fun `random Neighbor AirPodsGen2`() = runTest {
        create<AirPodsGen2>("07 19 01 0F 20 02 F9 8F 01 00 05 F2 7E 14 E0 54 0A 53 69 5B 7D F2 15 1F D7 B1 12") {
            rawPrefix shouldBe uByte(0x01)
            rawDeviceModel shouldBe uShort(0x0F20)
            rawStatus shouldBe uByte(0x02)
            rawPodsBattery shouldBe uByte(0xF9)
            rawFlags shouldBe uShort(0x8)
            rawCaseBattery shouldBe uShort(0xF)
            rawCaseLidState shouldBe uByte(0x01)
            rawDeviceColor shouldBe uByte(0x00)
            rawSuffix shouldBe uByte(0x05)

            batteryLeftPodPercent shouldBe null
            batteryRightPodPercent shouldBe 0.9f

            isCaseCharging shouldBe false
            isLeftPodCharging shouldBe false
            isRightPodCharging shouldBe false

            isLeftPodInEar shouldBe false
            isRightPodInEar shouldBe true
            batteryCasePercent shouldBe null

            caseLidState shouldBe DualApplePods.LidState.NOT_IN_CASE

            state shouldBe HasStateDetectionAirPods.ConnectionState.MUSIC

            podStyle.identifier shouldBe HasAppleColor.DeviceColor.WHITE.name

            model shouldBe PodDevice.Model.AIRPODS_GEN2
        }
    }
}