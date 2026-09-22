package com.screentime.airpod.pods.core.apple

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import com.screentime.airpod.common.uShort
import com.screentime.airpod.common.uByte

class SingleApplePodsTest : BaseAirPodsTest() {

    @Test
    fun `default bit mapping Max`() = runTest {
        create<SingleApplePods>("07 19 01 0A 20 62 04 80 01 0F 40 0D 70 50 16 F2 40 83 16 BF 10 16 34 9B 74 84 E8") {
            rawPrefix shouldBe uByte(0x01)
            rawDeviceModel shouldBe uShort(0x0A20)
            rawStatus shouldBe uByte(0x62)
            rawPodsBattery shouldBe uByte(0x04)
            rawFlags shouldBe uShort(0x8)
            rawCaseBattery shouldBe uShort(0x0)
            rawCaseLidState shouldBe uByte(0x01)
            rawDeviceColor shouldBe uByte(0x0F)
            rawSuffix shouldBe uByte(0x40)
        }
    }
}