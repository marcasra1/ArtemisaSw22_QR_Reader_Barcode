package com.artemisaswtt.qrscanner.extension

import androidx.appcompat.app.AppCompatActivity
import com.artemisaswtt.qrscanner.feature.common.dialog.ErrorDialogFragment

fun AppCompatActivity.showError(error: Throwable?) {
    val errorDialog =
        ErrorDialogFragment.newInstance(
            this,
            error
        )
    errorDialog.show(supportFragmentManager, "")
}