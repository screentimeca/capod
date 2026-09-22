package com.screentime.airpod.pods.core.apple.airpods

import com.screentime.airpod.common.isBitSet
import com.screentime.airpod.pods.core.PodDevice
import com.screentime.airpod.pods.core.apple.BaseAirPodsTest
import com.screentime.airpod.pods.core.apple.HasAppleColor
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import com.screentime.airpod.common.uShort
import com.screentime.airpod.common.uByte

class AirPodsMaxTest : BaseAirPodsTest() {

    // Test data from https://github.com/adolfintel/OpenPods/issues/124
    @Test
    fun `default AirPods Max`() = runTest {
        create<AirPodsMax>("07 19 01 0A 20 62 04 80 01 0F 40 0D 70 50 16 F2 40 83 16 BF 10 16 34 9B 74 84 E8") {
            rawPrefix shouldBe uByte(0x01)
            rawDeviceModel shouldBe uShort(0x0A20)
            rawStatus shouldBe uByte(0x62)
            rawPodsBattery shouldBe uByte(0x04)
            rawFlags shouldBe uShort(0x8)
            rawCaseBattery shouldBe uShort(0x0)
            rawCaseLidState shouldBe uByte(0x01)
            rawDeviceColor shouldBe uByte(0x0F)
            rawSuffix shouldBe uByte(0x40)

            batteryHeadsetPercent shouldBe 0.4f

            isHeadsetBeingCharged shouldBe false

            model shouldBe PodDevice.Model.AIRPODS_MAX
        }
    }

    // Test data from https://github.com/adolfintel/OpenPods/issues/124
    @Test
    fun `default AirPods Max  flipped values`() = runTest {
        create<AirPodsMax>("07 19 01 0A 20 02 05 80 04 0F 44 A7 60 9B F8 3C FD B1 D8 1C 61 EA 82 60 A3 2C 4E") {
            rawPrefix shouldBe uByte(0x01)
            rawDeviceModel shouldBe uShort(0x0A20)
            rawStatus shouldBe uByte(0x02)
            rawPodsBattery shouldBe uByte(0x05)
            rawFlags shouldBe uShort(0x8)
            rawCaseBattery shouldBe uShort(0x0)
            rawCaseLidState shouldBe uByte(0x04)
            rawDeviceColor shouldBe uByte(0x0F)
            rawSuffix shouldBe uByte(0x44)

            batteryHeadsetPercent shouldBe 0.5f

            isHeadsetBeingCharged shouldBe false
        }
    }

    @Test
    fun `some dude at the gym`() = runTest {
        create<AirPodsMax>("07 19 01 0A 20 23 07 80 03 03 65 1F 28 32 D0 D9 71 43 00 9A 40 E7 6B EA 6C 2C FB") {
            rawPrefix shouldBe uByte(0x01)
            rawDeviceModel shouldBe uShort(0x0A20)
            rawStatus shouldBe uByte(0x23)
            rawPodsBattery shouldBe uByte(0x07)
            rawFlags shouldBe uShort(0x8)
            rawCaseBattery shouldBe uShort(0x0)
            rawCaseLidState shouldBe uByte(0x03)
            rawDeviceColor shouldBe uByte(0x03)
            rawSuffix shouldBe uByte(0x65)

            batteryHeadsetPercent shouldBe 0.7f

            isHeadsetBeingCharged shouldBe false

            rawStatus.isBitSet(5) shouldBe true

            podStyle shouldBe HasAppleColor.DeviceColor.BLUE
        }
    }

    @Test
    fun `wear status`() = runTest {
        create<AirPodsMax>("07 19 01 0A 20 03 07 80 03 03 65 1F 28 32 D0 D9 71 43 00 9A 40 E7 6B EA 6C 2C FB") {
            rawStatus shouldBe uByte(0x03)

            rawStatus.isBitSet(5) shouldBe false
            isBeingWorn shouldBe false
        }
        create<AirPodsMax>("07 19 01 0A 20 23 07 80 03 03 65 1F 28 32 D0 D9 71 43 00 9A 40 E7 6B EA 6C 2C FB") {
            rawStatus shouldBe uByte(0x23)

            rawStatus.isBitSet(5) shouldBe true
            isBeingWorn shouldBe true
        }
    }
}