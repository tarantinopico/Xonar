package com.tarantino.xonarx.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class SpeedEtaCalculatorTest {

    private val calculator = SpeedEtaCalculator()

    @Test
    fun `calculateSpeed returns correct bytes per second`() {
        val speed = calculator.calculateSpeed(1024L, 1000L)
        assertEquals(1024L, speed)
        
        val fastSpeed = calculator.calculateSpeed(5120L, 500L)
        assertEquals(10240L, fastSpeed) // 5KB in 0.5s = 10KB/s
    }

    @Test
    fun `calculateSpeed returns zero for invalid time`() {
        val speed = calculator.calculateSpeed(1024L, 0L)
        assertEquals(0L, speed)
    }

    @Test
    fun `calculateEta returns correct remaining seconds`() {
        val eta = calculator.calculateEta(10240L, 1024L)
        assertEquals(10L, eta)
        
        val etaRounding = calculator.calculateEta(10500L, 1000L)
        assertEquals(10L, etaRounding)
    }

    @Test
    fun `calculateEta returns -1 for zero speed`() {
        val eta = calculator.calculateEta(10240L, 0L)
        assertEquals(-1L, eta)
    }

    @Test
    fun `calculateEta returns zero for zero remaining bytes`() {
        val eta = calculator.calculateEta(0L, 1024L)
        assertEquals(0L, eta)
    }
}
