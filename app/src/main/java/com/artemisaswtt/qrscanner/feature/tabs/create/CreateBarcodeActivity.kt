package com.artemisaswtt.qrscanner.feature.tabs.create

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.artemisaswtt.qrscanner.R
import com.artemisaswtt.qrscanner.di.*
import com.artemisaswtt.qrscanner.extension.applySystemWindowInsets
import com.artemisaswtt.qrscanner.extension.showError
import com.artemisaswtt.qrscanner.extension.toStringId
import com.artemisaswtt.qrscanner.extension.unsafeLazy
import com.artemisaswtt.qrscanner.feature.BaseActivity
import com.artemisaswtt.qrscanner.feature.barcode.BarcodeActivity
import com.artemisaswtt.qrscanner.feature.tabs.create.barcode.*
import com.artemisaswtt.qrscanner.feature.tabs.create.qr.*
import com.artemisaswtt.qrscanner.model.Barcode
import com.artemisaswtt.qrscanner.model.schema.App
import com.artemisaswtt.qrscanner.model.schema.BarcodeSchema
import com.artemisaswtt.qrscanner.model.schema.Schema
import com.artemisaswtt.qrscanner.usecase.Logger
import com.artemisaswtt.qrscanner.usecase.save
import com.google.zxing.BarcodeFormat
import com.marcasralib.systemUI.SystemUIVisibility
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_create_barcode.*

import com.google.ads.consent.*
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.artemisaswtt.qrscanner.utils.Constants
import java.util.*


class CreateBarcodeActivity : BaseActivity(), AppAdapter.Listener {

    companion object {
        private const val BARCODE_FORMAT_KEY = "BARCODE_FORMAT_KEY"
        private const val BARCODE_SCHEMA_KEY = "BARCODE_SCHEMA_KEY"
        private const val DEFAULT_TEXT_KEY = "DEFAULT_TEXT_KEY"

        private const val CHOOSE_PHONE_REQUEST_CODE = 1
        private const val CHOOSE_CONTACT_REQUEST_CODE = 2

        private const val CONTACTS_PERMISSION_REQUEST_CODE = 101
        private val CONTACTS_PERMISSIONS = arrayOf(Manifest.permission.READ_CONTACTS)

        fun start(context: Context, barcodeFormat: BarcodeFormat, barcodeSchema: BarcodeSchema? = null, defaultText: String? = null) {
            val intent = Intent(context, CreateBarcodeActivity::class.java).apply {
                putExtra(BARCODE_FORMAT_KEY, barcodeFormat.ordinal)
                putExtra(BARCODE_SCHEMA_KEY, barcodeSchema?.ordinal ?: -1)
                putExtra(DEFAULT_TEXT_KEY, defaultText)
            }
            context.startActivity(intent)
        }
    }

    private val disposable = CompositeDisposable()

    private val barcodeFormat by unsafeLazy {
        BarcodeFormat.values().getOrNull(intent?.getIntExtra(BARCODE_FORMAT_KEY, -1) ?: -1)
            ?: BarcodeFormat.QR_CODE
    }

    private val barcodeSchema by unsafeLazy {
        BarcodeSchema.values().getOrNull(intent?.getIntExtra(BARCODE_SCHEMA_KEY, -1) ?: -1)
    }

    private val defaultText by unsafeLazy {
        intent?.getStringExtra(DEFAULT_TEXT_KEY).orEmpty()
    }

    var isCreateBarcodeButtonEnabled: Boolean
        get() = false
        set(enabled) {
            val iconId = if (enabled) {
                R.drawable.ic_confirm_enabled
            } else {
                R.drawable.ic_confirm_disabled
            }

            toolbar.menu?.findItem(R.id.item_create_barcode)?.apply {
                icon = ContextCompat.getDrawable(this@CreateBarcodeActivity, iconId)
                isEnabled = enabled
            }
        }

    //GDPR EU
    private var mShowPersonlAds = true
    private var mAdView: AdView? = null
    private var mInterstitialAd: InterstitialAd? = null
    private var consentForm: ConsentForm? = null
    private val SHOW_PERSONAL_ADS_KEY = "show.personal.ads.key"
    val TAG = "CreateBarcodeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (createBarcodeImmediatelyIfNeeded()) {
            return
        }

        setContentView(R.layout.activity_create_barcode)
        supportEdgeToEdge()
        handleToolbarBackClicked()
        handleToolbarMenuItemClicked()
        showToolbarTitle()
        showToolbarMenu()
        showFragment()

        //mInterstitialAd = InterstitialAd(this)
        //mInterstitialAd!!.adUnitId = "ca-app-pub-4087941191153559/6200718448"

        //mAdView = findViewById(R.id.adView_BrightDisplayActivity)
        //val adRequest = AdRequest.Builder().build()
        //mAdView.loadAd(adRequest)
        if (savedInstanceState != null)
            mShowPersonlAds = savedInstanceState.getBoolean(SHOW_PERSONAL_ADS_KEY)
        //mAdView = findViewById(R.id.adView_BrightDisplayActivity)
        checkAdConsent()

    }

    /*override fun onResume() {
        super.onResume()

        SystemUIVisibility(this.window).hideStatusBar()
    }*/


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            CHOOSE_PHONE_REQUEST_CODE -> showChosenPhone(data)
            CHOOSE_CONTACT_REQUEST_CODE -> showChosenContact(data)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CONTACTS_PERMISSION_REQUEST_CODE && permissionsHelper.areAllPermissionsGranted(grantResults)) {
            chooseContact()
        }
    }

    override fun onAppClicked(packageName: String) {
        createBarcode(App.fromPackage(packageName))
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    override fun onBackPressed() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
        }
        super.onBackPressed()
    }

    private fun supportEdgeToEdge() {
        root_view.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }

    private fun createBarcodeImmediatelyIfNeeded(): Boolean {
        if (intent?.action != Intent.ACTION_SEND) {
            return false
        }

        return when (intent?.type) {
            "text/plain" -> {
                createBarcodeForPlainText()
                true
            }
            "text/x-vcard" -> {
                createBarcodeForVCard()
                true
            }
            else -> false
        }
    }

    private fun createBarcodeForPlainText() {
        val text = intent?.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
        val schema = barcodeParser.parseSchema(barcodeFormat, text)
        createBarcode(schema, true)
    }

    private fun createBarcodeForVCard() {
        val uri = intent?.extras?.get(Intent.EXTRA_STREAM) as? Uri ?: return
        val text = readDataFromVCardUri(uri).orEmpty()
        val schema = barcodeParser.parseSchema(barcodeFormat, text)
        createBarcode(schema, true)
    }

    private fun readDataFromVCardUri(uri: Uri): String? {
        val stream = try {
            contentResolver.openInputStream(uri) ?: return null
        } catch (e: Exception) {
            Logger.log(e)
            return null
        }

        val fileContent = StringBuilder("")

        var ch: Int
        try {
            while (stream.read().also { ch = it } != -1) {
                fileContent.append(ch.toChar())
            }
        } catch (e: Exception) {
            Logger.log(e)
        }
        stream.close()

        return fileContent.toString()
    }

    private fun handleToolbarBackClicked() {
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun handleToolbarMenuItemClicked() {
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_phone -> choosePhone()
                R.id.item_contacts -> requestContactsPermissions()
                R.id.item_create_barcode -> createBarcode()
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun showToolbarTitle() {
        val titleId = barcodeSchema?.toStringId() ?: barcodeFormat.toStringId()
        toolbar.setTitle(titleId)
    }

    private fun showToolbarMenu() {
        val menuId = when (barcodeSchema) {
            BarcodeSchema.APP -> return
            BarcodeSchema.PHONE, BarcodeSchema.SMS, BarcodeSchema.MMS -> R.menu.menu_create_qr_code_phone
            BarcodeSchema.VCARD, BarcodeSchema.MECARD -> R.menu.menu_create_qr_code_contacts
            else -> R.menu.menu_create_barcode
        }
        toolbar.inflateMenu(menuId)
    }

    private fun showFragment() {
        val fragment = when {
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.OTHER -> CreateQrCodeTextFragment.newInstance(defaultText)
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.URL -> CreateQrCodeUrlFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.BOOKMARK -> CreateQrCodeBookmarkFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.PHONE -> CreateQrCodePhoneFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.WIFI -> CreateQrCodeWifiFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.EMAIL -> CreateQrCodeEmailFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.SMS -> CreateQrCodeSmsFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.MMS -> CreateQrCodeMmsFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.CRYPTOCURRENCY -> CreateQrCodeCryptocurrencyFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.GEO -> CreateQrCodeLocationFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.APP -> CreateQrCodeAppFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.OTP_AUTH -> CreateQrCodeOtpFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.VEVENT -> CreateQrCodeEventFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.VCARD -> CreateQrCodeVCardFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.MECARD -> CreateQrCodeMeCardFragment()
            barcodeFormat == BarcodeFormat.DATA_MATRIX -> CreateDataMatrixFragment()
            barcodeFormat == BarcodeFormat.AZTEC -> CreateAztecFragment()
            barcodeFormat == BarcodeFormat.PDF_417 -> CreatePdf417Fragment()
            barcodeFormat == BarcodeFormat.CODABAR -> CreateCodabarFragment()
            barcodeFormat == BarcodeFormat.CODE_39 -> CreateCode39Fragment()
            barcodeFormat == BarcodeFormat.CODE_93 -> CreateCode93Fragment()
            barcodeFormat == BarcodeFormat.CODE_128 -> CreateCode128Fragment()
            barcodeFormat == BarcodeFormat.EAN_8 -> CreateEan8Fragment()
            barcodeFormat == BarcodeFormat.EAN_13 -> CreateEan13Fragment()
            barcodeFormat == BarcodeFormat.ITF -> CreateItf14Fragment()
            barcodeFormat == BarcodeFormat.UPC_A -> CreateUpcAFragment()
            barcodeFormat == BarcodeFormat.UPC_E -> CreateUpcEFragment()
            else -> return
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    private fun choosePhone() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        }
        startActivityForResultIfExists(intent, CHOOSE_PHONE_REQUEST_CODE)
    }

    private fun showChosenPhone(data: Intent?) {
        val phone = contactHelper.getPhone(this, data) ?: return
        getCurrentFragment().showPhone(phone)
    }

    private fun requestContactsPermissions() {
        permissionsHelper.requestPermissions(this, CONTACTS_PERMISSIONS, CONTACTS_PERMISSION_REQUEST_CODE)
    }

    private fun chooseContact() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        startActivityForResultIfExists(intent, CHOOSE_CONTACT_REQUEST_CODE)
    }

    private fun showChosenContact(data: Intent?) {
        val contact = contactHelper.getContact(this, data) ?: return
        getCurrentFragment().showContact(contact)
    }

    private fun startActivityForResultIfExists(intent: Intent, requestCode: Int) {
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, requestCode)
        } else {
            Toast.makeText(this, R.string.activity_barcode_no_app, Toast.LENGTH_SHORT).show()
        }
    }

    private fun createBarcode() {
        val schema = getCurrentFragment().getBarcodeSchema()
        createBarcode(schema)
    }

    private fun createBarcode(schema: Schema, finish: Boolean = false) {
        val barcode = Barcode(
            text = schema.toBarcodeText(),
            formattedText = schema.toFormattedText(),
            format = barcodeFormat,
            schema = schema.schema,
            date = System.currentTimeMillis(),
            isGenerated = true
        )

        if (settings.saveCreatedBarcodesToHistory.not()) {
            navigateToBarcodeScreen(barcode, finish)
            return
        }

        barcodeDatabase.save(barcode, settings.doNotSaveDuplicates)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { id ->
                    navigateToBarcodeScreen(barcode.copy(id = id), finish)
                },
                ::showError
            )
            .addTo(disposable)
    }

    private fun getCurrentFragment(): BaseCreateBarcodeFragment {
        return supportFragmentManager.findFragmentById(R.id.container) as BaseCreateBarcodeFragment
    }

    private fun navigateToBarcodeScreen(barcode: Barcode, finish: Boolean) {
        BarcodeActivity.start(this, barcode, true)

        if (finish) {
            finish()
        }
    }


    //Admob --------------------------------------------------
    private fun checkAdConsent() {

        if (isEuUser(this)){
            val consentInformation = ConsentInformation.getInstance(this)
            val publisherIds = Array(1, { Constants.PUBLISHER_ID })
            consentInformation.requestConsentInfoUpdate(publisherIds, object : ConsentInfoUpdateListener {
                override fun onConsentInfoUpdated(consentStatus: ConsentStatus) {
                    when (consentStatus) {
                        ConsentStatus.PERSONALIZED -> loadAds(true)
                        ConsentStatus.NON_PERSONALIZED -> loadAds(false)
                        ConsentStatus.UNKNOWN -> loadAds(false)
                    }
                    Log.d(TAG, "onConsentInfoUpdated, Consent Status = ${consentStatus.name}")
                }

                override fun onFailedToUpdateConsentInfo(errorDescription: String) {
                    Log.d(TAG, "onFailedToUpdateConsentInfo - $errorDescription")
                }
            })
        }else{
            loadAds(true)
        }

    }

    private fun loadAds(showPersonlAds: Boolean) {
        mShowPersonlAds = showPersonlAds
        val adRequest: AdRequest
        if (mShowPersonlAds)
            adRequest = AdRequest.Builder().build()
        else
            adRequest = AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, getNonPersonalizedAdsBundle())
                .build()

        InterstitialAd.load(this, Constants.INTERSTITIAL_ADS_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError.message)
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
            }
        })
    }

    private fun getNonPersonalizedAdsBundle(): Bundle {
        val extra = Bundle()
        extra.putString("npa", "1")
        return extra
    }


    fun isEuUser(context: Context): Boolean {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        var country: String? = tm?.networkCountryIso
        country = if (country != null) country else Locale.getDefault().country
        val euCountries = arrayOf("BE", "EL", "LT", "PT", "BG", "ES", "LU", "RO", "CZ", "FR", "HU", "SI", "DK", "HR", "MT", "SK", "DE", "IT", "NL", "FI", "EE", "CY", "AT", "SE", "IE", "LV", "PL", "UK", "CH", "NO", "IS", "LI")
        return Arrays.asList(*euCountries).contains(country!!.toUpperCase())
    }
}