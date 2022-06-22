package com.artemisaswtt.qrscanner.feature.tabs

import android.app.ActionBar
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.google.ads.consent.*
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.marcasralib.systemUI.SystemUIVisibility
import com.artemisaswtt.qrscanner.BuildConfig
import com.artemisaswtt.qrscanner.R
import com.artemisaswtt.qrscanner.extension.applySystemWindowInsets
import com.artemisaswtt.qrscanner.feature.BaseActivity
import com.artemisaswtt.qrscanner.feature.tabs.create.CreateBarcodeFragment
import com.artemisaswtt.qrscanner.feature.tabs.history.BarcodeHistoryFragment
import com.artemisaswtt.qrscanner.feature.tabs.scan.ScanBarcodeFromCameraFragment
import com.artemisaswtt.qrscanner.feature.tabs.settings.SettingsFragment
import com.artemisaswtt.qrscanner.utils.Constants
import com.artemisaswtt.qrscanner.utils.Constants.PRIVACY_POLICY_URL
import com.artemisaswtt.qrscanner.utils.Constants.PUBLISHER_ID
import com.artemisaswtt.qrscanner.utils.UtilMethods
import kotlinx.android.synthetic.main.activity_bottom_tabs.*
import java.net.URL


class BottomTabsActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    //GDPR EU
    private var mShowPersonlAds = true
    private var mAdView: AdView? = null
    private var consentForm: ConsentForm? = null
    private val SHOW_PERSONAL_ADS_KEY = "show.personal.ads.key"
    val TAG = "BottonTabsActivity"

    companion object {
        private const val ACTION_CREATE_BARCODE = "${BuildConfig.APPLICATION_ID}.CREATE_BARCODE"
        private const val ACTION_HISTORY = "${BuildConfig.APPLICATION_ID}.HISTORY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom_tabs)

        supportEdgeToEdge()
        initBottomNavigationView()

        if (savedInstanceState == null) {
            showInitialFragment()
        }



        //Init Admob for non EU

        if (!UtilMethods().isEuUser(this)){
            // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
            MobileAds.initialize(this)
        }

        if (savedInstanceState != null)
            mShowPersonlAds = savedInstanceState.getBoolean(SHOW_PERSONAL_ADS_KEY)
        mAdView = findViewById(R.id.adView_activity_bottom_tabs)

        checkAdConsent()

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId == bottom_navigation_view.selectedItemId) {
            return false
        }
        showFragment(item.itemId)
        return true
    }

    override fun onBackPressed() {
        if (bottom_navigation_view.selectedItemId == R.id.item_scan) {
            super.onBackPressed()
        } else {
            bottom_navigation_view.selectedItemId = R.id.item_scan
        }
    }

    /*override fun onResume() {
        super.onResume()

        SystemUIVisibility(window).hideStatusBar()
    }*/

    private fun supportEdgeToEdge() {
        bottom_navigation_view.applySystemWindowInsets(applyBottom = true)
    }

    private fun initBottomNavigationView() {
        bottom_navigation_view.apply {
            setOnNavigationItemSelectedListener(this@BottomTabsActivity)
        }
    }

    private fun showInitialFragment() {
        when (intent?.action) {
            ACTION_CREATE_BARCODE -> bottom_navigation_view.selectedItemId = R.id.item_create
            ACTION_HISTORY -> bottom_navigation_view.selectedItemId = R.id.item_history
            else -> showFragment(R.id.item_scan)
        }
    }

    private fun showFragment(bottomItemId: Int) {
        val fragment = when (bottomItemId) {
            R.id.item_scan -> ScanBarcodeFromCameraFragment()
            R.id.item_create -> CreateBarcodeFragment()
            R.id.item_history -> BarcodeHistoryFragment()
            R.id.item_settings -> SettingsFragment()
            else -> null
        }
        fragment?.apply(::replaceFragment)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.layout_fragment_container, fragment)
            .setReorderingAllowed(true)
            .commit()
    }

    private fun checkAdConsent() {

        if (UtilMethods().isEuUser(this)){
            val consentInformation = com.google.ads.consent.ConsentInformation.getInstance(this)
            val publisherIds = Array(1, { Constants.PUBLISHER_ID })
            consentInformation.requestConsentInfoUpdate(
                publisherIds,
                object : ConsentInfoUpdateListener {
                    override fun onConsentInfoUpdated(consentStatus: ConsentStatus) {
                        when (consentStatus) {
                            ConsentStatus.PERSONALIZED -> loadAds(true)
                            ConsentStatus.NON_PERSONALIZED -> loadAds(false)
                            ConsentStatus.UNKNOWN -> displayConsentForm()
                        }
                        //Log.d(TAG, "onConsentInfoUpdated, Consent Status = ${consentStatus.name}")
                    }

                    override fun onFailedToUpdateConsentInfo(errorDescription: String) {
                        //Log.d(TAG, "onFailedToUpdateConsentInfo - $errorDescription")
                    }
                })
        }else{
            loadAds(true)
        }

    }

    private fun loadAds(showPersonlAds: Boolean) {


        //Init Adnob SDK for
        if (UtilMethods().isEuUser(this)){

            // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
            MobileAds.initialize(this)
        }


        mShowPersonlAds = showPersonlAds
        val build: AdRequest
        if (mShowPersonlAds)
            build = AdRequest.Builder().build()
        else
            build = AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, getNonPersonalizedAdsBundle())
                .build()


        mAdView!!.adListener = object : AdListener() {
            override fun onAdClosed() {
                //mAdView!!.loadAd(build)
                Log.d(TAG, "onAdClosed Banner Ad")
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                //mAdView!!.loadAd(build)
                Log.d(TAG, "onAdFailedToLoad Banner Ad")
            }
        }
        mAdView!!.loadAd(build)

    }



    private fun getNonPersonalizedAdsBundle(): Bundle {
        val extra = Bundle()
        extra.putString("npa", "1")
        return extra
    }



    private fun displayConsentForm() {
        consentForm = ConsentForm.Builder(this, URL(PRIVACY_POLICY_URL))
            .withListener(object : ConsentFormListener() {
                override fun onConsentFormOpened() {
                    super.onConsentFormOpened()
                    //Log.d(TAG, "Requesting Consent: onConsentFormOpened")
                }

                override fun onConsentFormLoaded() {
                    super.onConsentFormLoaded()
                    //Log.d(TAG, "Requesting Consent: onConsentFormLoaded")
                    consentForm?.show()
                }

                override fun onConsentFormError(reason: String?) {
                    super.onConsentFormError(reason)
                    ConsentInformation.getInstance(this@BottomTabsActivity).consentStatus = ConsentStatus.PERSONALIZED
                    //Log.d(TAG, "Requesting Consent: onConsentFormError. $reason")
                }

                override fun onConsentFormClosed(
                    consentStatus: ConsentStatus?,
                    userPrefersAdFree: Boolean?
                ) {
                    super.onConsentFormClosed(consentStatus, userPrefersAdFree)
                    //Log.d(TAG, "Requesting Consent: onConsentFormClosed")
                    var userWantsAdFree = false
                    if (userPrefersAdFree != null) {
                        userWantsAdFree = userPrefersAdFree
                    }
                    if (userWantsAdFree) {
                        // Buy or Subscribe
                        //Log.d(TAG, "Requesting Consent: User prefers AdFree")
                        // TODO This is where you write your Intent to launch the purchase flow dialog

                    } else {
                        //Log.d(TAG,"Requesting Consent: onConsentFormClosed. Consent Status = $consentStatus")
                        when (consentStatus) {
                            ConsentStatus.PERSONALIZED -> loadAds(true)
                            ConsentStatus.NON_PERSONALIZED -> loadAds(false)
                            ConsentStatus.UNKNOWN -> loadAds(true)
                        }
                    }
                }
            })
            .withPersonalizedAdsOption()
            .withNonPersonalizedAdsOption()
            //          .withAdFreeOption() TODO enable this option if you have created an inApp Purchase to eliminate Ads
            .build()
        consentForm!!.load()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState?.putBoolean(SHOW_PERSONAL_ADS_KEY, mShowPersonlAds)
    }
}