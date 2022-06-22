package com.marcasralib.systemUI

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity

class SystemUIVisibility(window: Window) : AppCompatActivity() {

    var myWindow : Window = window

    /**
     * Hides the system bars and makes the Activity "fullscreen". If this should be the default
     * state it should be called from [Activity.onWindowFocusChanged] if hasFocus is true.
     * It is also recommended to take care of cutout areas. The default behavior is that the app shows
     * in the cutout area in portrait mode if not in fullscreen mode. This can cause "jumping" if the
     * user swipes a system bar to show it. It is recommended to set [WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER],
     * call [showBelowCutout] from [Activity.onCreate]
     * (see [Android Developers article about cutouts](https://developer.android.com/guide/topics/display-cutout#never_render_content_in_the_display_cutout_area)).
     * @see showSystemUI
     * @see addSystemUIVisibilityListener
     */
    fun hideStatusBar() {
        // R = SdkVersion 30
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Log.i("systemUIVisibility", "on Build.VERSION_CODES >= R")
            myWindow.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            Log.i("systemUIVisibility", "on Build.VERSION_CODES < R")
            // Enables regular immersive mode.
            // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
            // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            @Suppress("DEPRECATION")
            myWindow.decorView.systemUiVisibility = (
                    // Do not let system steal touches for showing the navigation bar
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                    //or View.SYSTEM_UI_FLAG_IMMERSIVE
                            // Hide the nav bar and status bar
                            //or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

                            // Keep the app content behind the bars even if user swipes them up
                            //or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            //or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
            // make navbar translucent - do this already in hideSystemUI() so that the bar
            // is translucent if user swipes it up
            //@Suppress("DEPRECATION")
            //window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
    }

    /**
     * Shows the system bars and returns back from fullscreen.
     * @see hideSystemUI
     * @see addSystemUIVisibilityListener
     */
    fun showStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // show app content in fullscreen, i. e. behind the bars when they are shown (alternative to
            // deprecated View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION and View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            myWindow.setDecorFitsSystemWindows(false)
            // finally, show the system bars
            myWindow.insetsController?.show(WindowInsets.Type.systemBars())
        } else {
            // Shows the system bars by removing all the flags
            // except for the ones that make the content appear under the system bars.
            @Suppress("DEPRECATION")
            myWindow.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_VISIBLE)
        }
    }



}