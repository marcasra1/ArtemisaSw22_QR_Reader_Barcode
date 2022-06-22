package com.artemisaswtt.qrscanner

import androidx.multidex.MultiDexApplication
import com.artemisaswtt.qrscanner.di.settings
import com.artemisaswtt.qrscanner.usecase.Logger
import com.google.firebase.analytics.FirebaseAnalytics
import io.reactivex.plugins.RxJavaPlugins

class App : MultiDexApplication() {

    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    override fun onCreate() {
        handleUnhandledRxJavaErrors()
        applyTheme()
        super.onCreate()

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }

    private fun applyTheme() {
        settings.reapplyTheme()
    }

    private fun handleUnhandledRxJavaErrors() {
        RxJavaPlugins.setErrorHandler { error ->
            Logger.log(error)
        }
    }
}