package com.artemisaswtt.qrscanner.extension

fun Boolean?.orFalse(): Boolean {
    return this ?: false
}