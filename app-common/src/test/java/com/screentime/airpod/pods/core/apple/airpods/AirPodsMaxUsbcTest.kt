package com.screentime.airpod.pods.core.apple.airpods

import com.screentime.airpod.pods.core.PodDevice
import com.screentime.airpod.pods.core.apple.BaseAirPodsTest
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import com.screentime.airpod.common.uShort
import com.screentime.airpod.common.uByte

class AirPodsMaxUsbcTest : BaseAirPodsTest() {

    // Test data from https://github.com/d4rken-org/capod/issues/236
    @Test
    fun `default AirPods Max`() = runTest {
        create<AirPodsMaxUsbc>("07 19 01 1F 20 2B 05 80 03 12 C5 2E 8B F9 9A 7E 19 7B 63 0F 30 6E D7 3B E2 EC 32") {
            rawPrefix shouldBe uByte(0x01)
            rawDeviceModel shouldBe uShort(0x1F20)
            rawStatus shouldBe uByte(0x2B)
            rawPodsBattery shouldBe uByte(0x05)
            rawFlags shouldBe uShort(0x8)
            rawCaseBattery shouldBe uShort(0x0)
            rawCaseLidState shouldBe uByte(0x03)
            rawDeviceColor shouldBe uByte(0x12)
            rawSuffix shouldBe uByte(0xC5)

            batteryHeadsetPercent shouldBe 0.5f

            isHeadsetBeingCharged shouldBe false

            model shouldBe PodDevice.Model.AIRPODS_MAX_USBC
        }
    }
}