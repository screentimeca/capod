package com.screentime.airpod.pods.core.apple

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import com.screentime.airpod.common.uShort
import com.screentime.airpod.common.uByte

class BasicSingleApplePodsTest : BaseAirPodsTest() {

    @Test
    fun `test mapping`() = runTest {
        create<SingleApplePods>("07 19 01 05 20 00 F5 0F 01 01 00 6D CE C0 04 22 0A 85 31 2D 82 6B 42 80 01 20 1A") {
            rawPrefix shouldBe uByte(0x01)
            rawDeviceModel shouldBe uShort(0x0520)
            rawStatus shouldBe uByte(0x00)
            rawPodsBattery shouldBe uByte(0xF5)
            rawFlags shouldBe uShort(0x0)
            rawCaseBattery shouldBe uShort(0xF)
            rawCaseLidState shouldBe uByte(0x01)
            rawDeviceColor shouldBe uByte(0x01)
            rawSuffix shouldBe uByte(0x00)
        }
    }

    @Test
    fun `test battery headset percent`() = runTest {
        create<SingleApplePods>("07 19 01 05 20 00 F5 0F 01 01 00 6D CE C0 04 22 0A 85 31 2D 82 6B 42 80 01 20 1A") {
            batteryHeadsetPercent shouldBe 0.5f
        }
    }

}