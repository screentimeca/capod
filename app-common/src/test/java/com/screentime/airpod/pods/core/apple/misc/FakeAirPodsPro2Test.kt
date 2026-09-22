package com.screentime.airpod.pods.core.apple.misc

import com.screentime.airpod.pods.core.PodDevice
import com.screentime.airpod.pods.core.apple.BaseAirPodsTest
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import com.screentime.airpod.common.uShort
import com.screentime.airpod.common.uByte

class FakeAirPodsPro2Test : BaseAirPodsTest() {

    @Test
    fun `guessed data`() = runTest {
        create<FakeAirPodsPro2>("07 13 01 14 20 75 AA 58 35 00 10 00 E4 E4 26 00 00 00 00 00 00") {
            rawPrefix shouldBe uByte(0x01)
            rawDeviceModel shouldBe uShort(0x1420)
            rawStatus shouldBe uByte(0x75)
            rawPodsBattery shouldBe uByte(0xAA)
            rawFlags shouldBe uShort(0x5)
            rawCaseBattery shouldBe uShort(0x8)
            rawCaseLidState shouldBe uByte(0x35)
            rawDeviceColor shouldBe uByte(0x00)
            rawSuffix shouldBe uByte(0x10)

            batteryLeftPodPercent shouldBe 1.0f
            batteryRightPodPercent shouldBe 1.0f

            isCaseCharging shouldBe true

            batteryCasePercent shouldBe 0.8f

            model shouldBe PodDevice.Model.FAKE_AIRPODS_PRO2
        }
    }

    /**
     * https://discord.com/channels/548521543039189022/927235844127993866/1063027552765087754
     */
    @Test
    fun `user supplied`() = runTest {
        create<FakeAirPodsPro2>("07 13 01 14 20 75 AA 72 39 00 00 6F E4 E4 93 30 00 30 30 30 30") {
            rawPrefix shouldBe uByte(0x01)
            rawDeviceModel shouldBe uShort(0x1420)
            rawStatus shouldBe uByte(0x75)
            rawPodsBattery shouldBe uByte(0xAA)
            rawFlags shouldBe uShort(0x7)
            rawCaseBattery shouldBe uShort(0x2)
            rawCaseLidState shouldBe uByte(0x39)
            rawDeviceColor shouldBe uByte(0x00)
            rawSuffix shouldBe uByte(0x0)

            batteryLeftPodPercent shouldBe 1.0f
            batteryRightPodPercent shouldBe 1.0f

            isCaseCharging shouldBe true

            batteryCasePercent shouldBe 0.2f

            model shouldBe PodDevice.Model.FAKE_AIRPODS_PRO2
        }
    }
}