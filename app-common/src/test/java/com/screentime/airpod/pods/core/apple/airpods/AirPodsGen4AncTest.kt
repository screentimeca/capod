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

class AirPodsGen4AncTest : BaseAirPodsTest() {

    @Test
    fun `AirPods Gen4 with ANC via log from #226`() = runTest {
        create<AirPodsGen4Anc>("07 19 01 1B 20 0B 9A 8F 10 00 04 43 DF EC 1D D3 F1 C3 F4 A1 9B 29 26 B9 E7 3A A0") {
            rawPrefix shouldBe uByte(0x01)
            rawDeviceModel shouldBe uShort(0x1B20)
            rawStatus shouldBe uByte(0x0b)
            rawPodsBattery shouldBe uByte(0x9A)
            rawFlags shouldBe uShort(0x8)
            rawCaseBattery shouldBe uShort(0xF)
            rawCaseLidState shouldBe uByte(0x10)
            rawDeviceColor shouldBe uByte(0x00)
            rawSuffix shouldBe uByte(0x04)

            batteryLeftPodPercent shouldBe 0.9f
            batteryRightPodPercent shouldBe 1.0f

            isCaseCharging shouldBe false
            isLeftPodCharging shouldBe false
            isRightPodCharging shouldBe false

            isLeftPodInEar shouldBe true
            isRightPodInEar shouldBe true
            batteryCasePercent shouldBe null

            caseLidState shouldBe DualApplePods.LidState.UNKNOWN

            state shouldBe HasStateDetectionAirPods.ConnectionState.IDLE

            podStyle.identifier shouldBe HasAppleColor.DeviceColor.WHITE.name

            model shouldBe PodDevice.Model.AIRPODS_GEN4_ANC
        }
    }
}