package com.screentime.airpod.pods.core.apple.misc

import com.screentime.airpod.pods.core.PodDevice
import com.screentime.airpod.pods.core.apple.BaseAirPodsTest
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import com.screentime.airpod.common.uShort
import com.screentime.airpod.common.uByte

class FakeAirPodsGen1Test : BaseAirPodsTest() {

    @Test
    fun `charging in box`() = runTest {
        create<FakeAirPodsGen2>("07 13 01 0F 20 71 AA 37 32 00 10 00 64 64 FF 00 00 00 00 00 00") {
            rawPrefix shouldBe uByte(0x01)
            rawDeviceModel shouldBe uShort(0x0F20)
            rawStatus shouldBe uByte(0x71)
            rawPodsBattery shouldBe uByte(0xAA)
            rawFlags shouldBe uShort(0x3)
            rawCaseBattery shouldBe uShort(0x7)
            rawCaseLidState shouldBe uByte(0x32)
            rawDeviceColor shouldBe uByte(0x00)
            rawSuffix shouldBe uByte(0x10)

            batteryLeftPodPercent shouldBe 1.0f
            batteryRightPodPercent shouldBe 1.0f

            isLeftPodInEar shouldBe false
            isRightPodInEar shouldBe false

            isLeftPodCharging shouldBe true
            isRightPodCharging shouldBe true

            isCaseCharging shouldBe false

            batteryCasePercent shouldBe 0.7f

            model shouldBe PodDevice.Model.FAKE_AIRPODS_GEN2
        }
    }
}