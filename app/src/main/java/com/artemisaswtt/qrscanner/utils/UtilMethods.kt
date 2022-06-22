package com.artemisaswtt.qrscanner.utils

import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.telephony.TelephonyManager
import java.util.*

public class UtilMethods() {


    fun isEuUser(context: Context): Boolean {
        val tm = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        var country: String? = tm?.networkCountryIso
        country = if (country != null) country else Locale.getDefault().country
        val euCountries = arrayOf(
            "BE",
            "EL",
            "LT",
            "PT",
            "BG",
            "ES",
            "LU",
            "RO",
            "CZ",
            "FR",
            "HU",
            "SI",
            "DK",
            "HR",
            "MT",
            "SK",
            "DE",
            "IT",
            "NL",
            "FI",
            "EE",
            "CY",
            "AT",
            "SE",
            "IE",
            "LV",
            "PL",
            //"UK", //Out of EU
            "CH",
            "NO",
            "IS",
            "LI"
        )
        return Arrays.asList(*euCountries).contains(country!!.toUpperCase())
    }


}