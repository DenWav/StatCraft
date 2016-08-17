/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft

import org.junit.Before
import org.junit.Test
import java.util.UUID

import org.junit.Assert.*
import java.util.Arrays

class ExtFunctionsTest {

    private lateinit var uuid: UUID
    private lateinit var array: ByteArray

    @Before
    fun before() {
        uuid = UUID.fromString("c43d2930-22aa-40f4-aca6-82e6540044cc");
        array = byteArrayOf(-60, 61, 41, 48, 34, -86, 64, -12, -84, -90, -126, -26, 84, 0, 68, -52)
    }

    @Test
    fun testUuidToByte() {
        assertTrue(Arrays.equals(array, uuid.toByte()))
    }

    @Test
    fun testByteToUuid() {
        assertTrue(uuid == array.toUUID())
    }
}
