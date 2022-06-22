package com.artemisaswtt.qrscanner.feature.tabs.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.artemisaswtt.qrscanner.BuildConfig
import com.artemisaswtt.qrscanner.R
import com.artemisaswtt.qrscanner.di.barcodeDatabase
import com.artemisaswtt.qrscanner.di.settings
import com.artemisaswtt.qrscanner.extension.applySystemWindowInsets
import com.artemisaswtt.qrscanner.extension.packageManager
import com.artemisaswtt.qrscanner.extension.showError
import com.artemisaswtt.qrscanner.feature.common.dialog.DeleteConfirmationDialogFragment
import com.artemisaswtt.qrscanner.feature.tabs.settings.camera.ChooseCameraActivity
import com.artemisaswtt.qrscanner.feature.tabs.settings.formats.SupportedFormatsActivity
import com.artemisaswtt.qrscanner.feature.tabs.settings.permissions.AllPermissionsActivity
import com.artemisaswtt.qrscanner.feature.tabs.settings.search.ChooseSearchEngineActivity
import com.artemisaswtt.qrscanner.feature.tabs.settings.theme.ChooseThemeActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_settings.*


class SettingsFragment : Fragment(), DeleteConfirmationDialogFragment.Listener {
    private val disposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        supportEdgeToEdge()
    }

    override fun onResume() {
        super.onResume()
        handleButtonCheckedChanged()
        handleButtonClicks()
        showSettings()
        showAppVersion()
    }

    override fun onDeleteConfirmed() {
        clearHistory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.clear()
    }

    fun supportEdgeToEdge() {
        app_bar_layout.applySystemWindowInsets(applyTop = true)
    }

    private fun handleButtonCheckedChanged() {
        button_inverse_barcode_colors_in_dark_theme.setCheckedChangedListener { settings.areBarcodeColorsInversed = it
            if (it) {
                button_inverse_barcode_colors_in_dark_theme.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue1))
                //Toast.makeText(activity, "Cambia color", Toast.LENGTH_LONG).show()
            }else{
                button_inverse_barcode_colors_in_dark_theme.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
        }
        button_open_links_automatically.setCheckedChangedListener { settings.openLinksAutomatically = it
            if (it) {
                button_open_links_automatically.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue1))
            }else{
                button_open_links_automatically.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
        }
        button_copy_to_clipboard.setCheckedChangedListener { settings.copyToClipboard = it
            if (it) {
                button_copy_to_clipboard.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue1))
            }else{
                button_copy_to_clipboard.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
        }
        button_simple_auto_focus.setCheckedChangedListener { settings.simpleAutoFocus = it
            if (it) {
                button_simple_auto_focus.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue1))
            }else{
                button_simple_auto_focus.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
        }
        button_flashlight.setCheckedChangedListener { settings.flash = it
            if (it) {
                button_flashlight.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue1))
            }else{
                button_flashlight.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
        }
        button_vibrate.setCheckedChangedListener { settings.vibrate = it
            if (it) {
                button_vibrate.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue1))
            }else{
                button_vibrate.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }}
        button_continuous_scanning.setCheckedChangedListener { settings.continuousScanning = it
            if (it) {
                button_continuous_scanning.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue1))
            }else{
                button_continuous_scanning.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }}
        button_confirm_scans_manually.setCheckedChangedListener { settings.confirmScansManually = it
            if (it) {
                button_confirm_scans_manually.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue1))
            }else{
                button_confirm_scans_manually.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }}
        button_save_scanned_barcodes.setCheckedChangedListener { settings.saveScannedBarcodesToHistory = it
            if (it) {
                button_save_scanned_barcodes.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue1))
            }else{
                button_save_scanned_barcodes.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }}
        button_save_created_barcodes.setCheckedChangedListener { settings.saveCreatedBarcodesToHistory = it
            if (it) {
                button_save_created_barcodes.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue1))
            }else{
                button_save_created_barcodes.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }}
        button_do_not_save_duplicates.setCheckedChangedListener { settings.doNotSaveDuplicates = it
            if (it) {
                button_do_not_save_duplicates.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue1))
            }else{
                button_do_not_save_duplicates.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }}
        button_enable_error_reports.setCheckedChangedListener { settings.areErrorReportsEnabled = it
            if (it) {
                button_enable_error_reports.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue1))
            }else{
                button_enable_error_reports.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }}
    }

    private fun handleButtonClicks() {
        button_choose_theme.setOnClickListener { ChooseThemeActivity.start(requireActivity()) }
        button_choose_camera.setOnClickListener { ChooseCameraActivity.start(requireActivity()) }
        button_select_supported_formats.setOnClickListener { SupportedFormatsActivity.start(requireActivity()) }
        button_clear_history.setOnClickListener { showDeleteHistoryConfirmationDialog() }
        button_choose_search_engine.setOnClickListener { ChooseSearchEngineActivity.start(requireContext()) }
        button_permissions.setOnClickListener { AllPermissionsActivity.start(requireActivity()) }
        button_check_updates.setOnClickListener { showAppInMarket() }
        button_source_code.setOnClickListener { showSourceCode() }
        button_rate_and_comment.setOnClickListener { showAppInMarket() }
        button_share.setOnClickListener { shareApp() }
    }

    private fun clearHistory() {
        button_clear_history.isEnabled = false

        barcodeDatabase.deleteAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    button_clear_history.isEnabled = true
                },
                { error ->
                    button_clear_history.isEnabled = true
                    showError(error)
                }
            )
            .addTo(disposable)
    }

    private fun showSettings() {
        settings.apply {
            button_inverse_barcode_colors_in_dark_theme.isChecked = areBarcodeColorsInversed
            button_open_links_automatically.isChecked = openLinksAutomatically
            button_copy_to_clipboard.isChecked = copyToClipboard
            button_simple_auto_focus.isChecked = simpleAutoFocus
            button_flashlight.isChecked = flash
            button_vibrate.isChecked = vibrate
            button_continuous_scanning.isChecked = continuousScanning
            button_confirm_scans_manually.isChecked = confirmScansManually
            button_save_scanned_barcodes.isChecked = saveScannedBarcodesToHistory
            button_save_created_barcodes.isChecked = saveCreatedBarcodesToHistory
            button_do_not_save_duplicates.isChecked = doNotSaveDuplicates
            button_enable_error_reports.isChecked = areErrorReportsEnabled
        }
    }

    private fun showDeleteHistoryConfirmationDialog() {
        val dialog = DeleteConfirmationDialogFragment.newInstance(R.string.dialog_delete_clear_history_message)
        dialog.show(childFragmentManager, "")
    }

    private fun showAppInMarket() {
        /*val uri = Uri.parse("market://details?id=" + requireContext().packageName)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        }
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        }*/

        // Do something in response to button_comments click
        //Samsung
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("samsungapps://ProductDetail/com.artemisaswtt.qrscanner")
        )
        //Amazon
        //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("amzn://apps/android?p=com.artemisaswtt.qrscanner"));
        //PlayStore
        /*/val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=com.artemisaswtt.qrscanner")
        )*/
        //Huawei
        //final String HUAWEI_APP_ID = "104839471";
        //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://appgallery.cloud.huawei.com/marketshare/app/C" + HUAWEI_APP_ID));
        startActivity(browserIntent)
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"

        //PlayStore: https://play.google.com/store/apps/details?id=<package_name>
        /*shareIntent.putExtra(Intent.EXTRA_SUBJECT,  resources.getString(R.string.app_name))
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            " https://play.google.com/store/apps/details?id="+ requireContext().getPackageName()
        )*/

        //Amazon
        //shareIntent.putExtra(Intent.EXTRA_TEXT, " amzn://apps/android?p=com.artemisaswtt.qrscanner");

        //samsung: https://apps.samsung.com/appquery/appDetail.as?appId=<package_name>
        shareIntent.putExtra(Intent.EXTRA_SUBJECT,  resources.getString(R.string.app_name))
        shareIntent.putExtra(Intent.EXTRA_TEXT, " https://galaxystore.samsung.com/detail/com.artemisaswtt.qrscanner")

        //Huawei
        //final String HUAWEI_APP_ID = "104839471";
        //shareIntent.putExtra(Intent.EXTRA_TEXT, " https://appgallery.cloud.huawei.com/marketshare/app/C" + HUAWEI_APP_ID);

        startActivity(Intent.createChooser(shareIntent, ""))
    }

    private fun showSourceCode() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/wewewe718/QrAndBarcodeScanner"))
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun showAppVersion() {
        button_app_version.hint = BuildConfig.VERSION_NAME
    }
}