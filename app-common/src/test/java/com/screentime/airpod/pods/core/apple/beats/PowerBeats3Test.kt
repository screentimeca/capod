package com.screentime.airpod.pods.core.apple.beats

import com.screentime.airpod.pods.core.PodDevice
import com.screentime.airpod.pods.core.apple.BaseAirPodsTest
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import com.screentime.airpod.common.uShort
import com.screentime.airpod.common.uByte

class PowerBeats3Test : BaseAirPodsTest() {

    // TODO This is handcrafted data, get actual data for tests
    @Test
    fun `default PowerBeats3`() = runTest {
        create<PowerBeats3>("07 19 01 03 20 62 04 80 01 0F 40 0D 70 50 16 F2 40 83 16 BF 10 16 34 9B 74 84 E8") {
            rawPrefix shouldBe uByte(0x01)
            rawDeviceModel shouldBe uShort(0x0320)
            rawStatus shouldBe uByte(0x62)
            rawPodsBattery shouldBe uByte(0x04)
            rawFlags shouldBe uShort(0x8)
            rawCaseBattery shouldBe uShort(0x0)
            rawCaseLidState shouldBe uByte(0x01)
            rawDeviceColor shouldBe uByte(0x0F)
            rawSuffix shouldBe uByte(0x40)

            batteryHeadsetPercent shouldBe 0.4f

            model shouldBe PodDevice.Model.POWERBEATS_3
        }
    }
}