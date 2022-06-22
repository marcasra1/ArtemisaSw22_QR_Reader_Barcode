package com.artemisaswtt.qrscanner.extension

fun Long?.orZero(): Long {
    return this ?: 0L
}