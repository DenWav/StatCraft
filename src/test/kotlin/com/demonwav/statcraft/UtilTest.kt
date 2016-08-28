/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft

import org.junit.Test
import org.junit.Assert.*

class UtilTest {

    @Test
    fun testTransformTime() {
        assertEquals("3 minutes, 39 seconds", transformTime(219))
    }

    @Test
    fun testBiggerTransformTime() {
        assertEquals("12 hours, 9 minutes, 49 seconds", transformTime(43789))
    }

    @Test
    fun testBiggestTransformTime() {
        assertEquals("452 weeks, 5 days, 11 hours, 19 minutes, 54 seconds", transformTime(273842394))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNegativeTransformTime() {
        transformTime(-1)
    }

    @Test
    fun testDistanceUnits0m() {
        assertEquals("0.00 m", distanceUnits(0))
    }

    @Test
    fun testDistanceUnits100point65m() {
        assertEquals("100.65 m", distanceUnits(10065))
    }

    @Test
    fun testDistanceUnits547point23km() {
        assertEquals("547.23 km", distanceUnits(54723000))
    }
}
