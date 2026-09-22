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

class AirPodsGen1Test : BaseAirPodsTest() {

    // Test data from https://github.com/adolfintel/OpenPods/issues/39#issuecomment-557664269
    @Test
    fun `fake airpods`() = runTest {
        create<AirPodsGen1>("07 19 01 02 20 55 AF 56 31 00 00 6F E4 DF 10 AF 10 60 81 03 3B 76 D9 C7 11 22 88") {
            rawPrefix shouldBe uByte(0x01)
            rawDeviceModel shouldBe uShort(0x0220)
            rawStatus shouldBe uByte(0x55)
            rawPodsBattery shouldBe uByte(0xAF)
            rawFlags shouldBe uShort(0x5)
            rawCaseBattery shouldBe uShort(0x6)
            rawCaseLidState shouldBe uByte(0x31)
            rawDeviceColor shouldBe uByte(0x00)
            rawSuffix shouldBe uByte(0x00)

            batteryLeftPodPercent shouldBe 1.0f
            batteryRightPodPercent shouldBe null

            isCaseCharging shouldBe true
            isLeftPodCharging shouldBe false
            isRightPodCharging shouldBe true

            isLeftPodInEar shouldBe false
            isRightPodInEar shouldBe false
            batteryCasePercent shouldBe 0.6f

            caseLidState shouldBe DualApplePods.LidState.OPEN

            podStyle.identifier shouldBe HasAppleColor.DeviceColor.WHITE.name

            model shouldBe PodDevice.Model.AIRPODS_GEN1
        }
    }
}