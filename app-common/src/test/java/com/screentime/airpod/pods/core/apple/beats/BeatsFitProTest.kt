package com.screentime.airpod.pods.core.apple.beats

import com.screentime.airpod.pods.core.PodDevice
import com.screentime.airpod.pods.core.apple.BaseAirPodsTest
import com.screentime.airpod.pods.core.apple.HasAppleColor
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import com.screentime.airpod.common.uShort
import com.screentime.airpod.common.uByte

class BeatsFitProTest : BaseAirPodsTest() {

    /**
     * From https://github.com/d4rken-org/capod/issues/33#issuecomment-1256235651
     */
    @Test
    fun `test basics`() = runTest {
        create<BeatsFitPro>("07 19 01 12 20 20 FA 8F 01 11 24 9B 9B 23 52 60 5A 8C 32 1C A5 C2 81 51 82 AF C8") {
            rawPrefix shouldBe uByte(0x01)
            rawDeviceModel shouldBe uShort(0x1220)
            rawStatus shouldBe uByte(0x20)
            rawPodsBattery shouldBe uByte(0xFA)
            rawFlags shouldBe uShort(0x8)
            rawCaseBattery shouldBe uShort(0xF)
            rawCaseLidState shouldBe uByte(0x01)
            rawDeviceColor shouldBe uByte(0x11)
            rawSuffix shouldBe uByte(0x24)

            isLeftPodMicrophone shouldBe true
            isRightPodMicrophone shouldBe false

            batteryLeftPodPercent shouldBe 1.0f
            batteryRightPodPercent shouldBe null

            isCaseCharging shouldBe false
            isRightPodCharging shouldBe false
            isLeftPodCharging shouldBe false
            batteryCasePercent shouldBe null
            podStyle.identifier shouldBe HasAppleColor.DeviceColor.UNKNOWN.name

            model shouldBe PodDevice.Model.BEATS_FIT_PRO
        }
    }

    @Test
    fun `extra rl test case`() = runTest {
        create<BeatsFitPro>("07 19 01 12 20 04 FA 92 54 11 24 CE B1 DF 9D 8D F5 E3 37 60 B1 23 8B 90 3B 63 3F") {
            rawPrefix shouldBe uByte(0x01)
            rawDeviceModel shouldBe uShort(0x1220)
            rawStatus shouldBe uByte(0x04)
            rawPodsBattery shouldBe uByte(0xFA)
            rawFlags shouldBe uShort(0x9)
            rawCaseBattery shouldBe uShort(0x2)
            rawCaseLidState shouldBe uByte(0x54)
            rawDeviceColor shouldBe uByte(0x11)
            rawSuffix shouldBe uByte(0x24)

            isLeftPodMicrophone shouldBe false
            isRightPodMicrophone shouldBe true

            batteryLeftPodPercent shouldBe null
            batteryRightPodPercent shouldBe 1.0f

            isCaseCharging shouldBe false
            isRightPodCharging shouldBe true
            isLeftPodCharging shouldBe false
            batteryCasePercent shouldBe 0.2f
            podStyle.identifier shouldBe HasAppleColor.DeviceColor.UNKNOWN.name

            model shouldBe PodDevice.Model.BEATS_FIT_PRO
        }
    }
}