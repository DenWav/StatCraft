/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft

import java.nio.ByteBuffer
import java.util.UUID

inline fun <T : AutoCloseable, R> T.use(block: (T) -> R): R {
    var closed = false
    try {
        return block(this)
    } catch (e: Exception) {
        closed = true
        try {
            close()
        } catch (closeException: Exception) {}
        throw e
    } finally {
        if (!closed) {
            close()
        }
    }
}

fun UUID.toByte(): ByteArray {
    val byteBuffer = ByteBuffer.wrap(ByteArray(16))
    byteBuffer.putLong(mostSignificantBits)
    byteBuffer.putLong(leastSignificantBits)
    return byteBuffer.array()
}

fun ByteArray.toUUID(): UUID {
    val buffer = ByteBuffer.wrap(this)
    return UUID(buffer.long, buffer.long)
}
