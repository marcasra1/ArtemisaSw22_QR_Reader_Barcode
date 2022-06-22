package com.artemisaswtt.qrscanner.extension

fun Int?.orZero(): Int {
    return this ?: 0
}