package com.screentime.airpod.pods.core.apple.misc

import com.screentime.airpod.pods.core.PodDevice
import com.screentime.airpod.pods.core.apple.BaseAirPodsTest
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import com.screentime.airpod.common.uShort
import com.screentime.airpod.common.uByte

class FakeAirPodsGen3Test : BaseAirPodsTest() {

    @Test
    fun `charging in case`() = runTest {
        create<FakeAirPodsGen3>("07 13 01 13 20 75 AA 37 34 00 10 00 E4 E4 64 00 00 00 00 00 00") {
            rawPrefix shouldBe uByte(0x01)
            rawDeviceModel shouldBe uShort(0x1320)
            rawStatus shouldBe uByte(0x75)
            rawPodsBattery shouldBe uByte(0xAA)
            rawFlags shouldBe uShort(0x3)
            rawCaseBattery shouldBe uShort(0x7)
            rawCaseLidState shouldBe uByte(0x34)
            rawDeviceColor shouldBe uByte(0x00)
            rawSuffix shouldBe uByte(0x10)

            batteryLeftPodPercent shouldBe 1f
            batteryRightPodPercent shouldBe 1f

            isLeftPodInEar shouldBe false
            isRightPodInEar shouldBe false

            isCaseCharging shouldBe false
            isLeftPodCharging shouldBe true
            isRightPodCharging shouldBe true

            batteryCasePercent shouldBe 0.7f

            model shouldBe PodDevice.Model.FAKE_AIRPODS_GEN3
        }
    }
}