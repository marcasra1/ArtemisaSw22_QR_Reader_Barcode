package com.artemisaswtt.qrscanner.extension

import com.artemisaswtt.qrscanner.model.Barcode
import com.google.zxing.Result

fun Result.equalTo(barcode: Barcode?): Boolean {
    return barcodeFormat == barcode?.format && text == barcode?.text
}