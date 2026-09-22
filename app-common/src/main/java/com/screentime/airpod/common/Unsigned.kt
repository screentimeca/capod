package com.screentime.airpod.common

/**
 * Non-inline wrappers around unsigned conversions.
 *
 * Kotlin 2.2's IR const evaluator crashes on [Int.toUByte]/[Int.toUShort] when the receiver
 * is a compile-time constant (`InterpreterMethodNotFoundError`). Routing through a normal
 * function prevents that lowering from folding the call.
 */
fun uByte(value: Int): UByte = value.toUByte()

fun uShort(value: Int): UShort = value.toUShort()
