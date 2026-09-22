package com.screentime.airpod.pods.core.apple.beats

import com.screentime.airpod.pods.core.PodDevice
import com.screentime.airpod.pods.core.apple.BaseAirPodsTest
import com.screentime.airpod.pods.core.apple.HasAppleColor
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import com.screentime.airpod.common.uShort
import com.screentime.airpod.common.uByte

class PowerBeatsProTest : BaseAirPodsTest() {

    // TODO This is handcrafted data, get actual data for tests
    @Test
    fun `test PowerBeatsPro`() = runTest {
        create<PowerBeatsPro>("07 19 01 0B 20 54 AA B5 31 00 00 E0 0C A7 8A 60 4B D3 7D F4 60 4F 2C 73 E9 A7 F4") {
            rawPrefix shouldBe uByte(0x01)
            rawDeviceModel shouldBe uShort(0x0B20)
            rawStatus shouldBe uByte(0x54)
            rawPodsBattery shouldBe uByte(0xAA)
            rawFlags shouldBe uShort(0xB)
            rawCaseBattery shouldBe uShort(0x5)
            rawCaseLidState shouldBe uByte(0x31)
            rawDeviceColor shouldBe uByte(0x00)
            rawSuffix shouldBe uByte(0x00)

            isLeftPodMicrophone shouldBe true
            isRightPodMicrophone shouldBe false

            batteryLeftPodPercent shouldBe 1.0f
            batteryRightPodPercent shouldBe 1.0f

            isCaseCharging shouldBe false
            isRightPodCharging shouldBe true
            isLeftPodCharging shouldBe true
            batteryCasePercent shouldBe 0.5f

            podStyle.identifier shouldBe HasAppleColor.DeviceColor.WHITE.name

            model shouldBe PodDevice.Model.POWERBEATS_PRO
        }
    }
}