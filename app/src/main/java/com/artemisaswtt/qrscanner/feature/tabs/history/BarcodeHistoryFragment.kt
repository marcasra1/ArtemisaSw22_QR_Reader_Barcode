package com.artemisaswtt.qrscanner.feature.tabs.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.artemisaswtt.qrscanner.R
import com.artemisaswtt.qrscanner.di.barcodeDatabase
import com.artemisaswtt.qrscanner.extension.applySystemWindowInsets
import com.artemisaswtt.qrscanner.extension.showError
import com.artemisaswtt.qrscanner.feature.common.dialog.DeleteConfirmationDialogFragment
import com.artemisaswtt.qrscanner.feature.tabs.history.export.ExportHistoryActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_barcode_history.*


class BarcodeHistoryFragment : Fragment(), DeleteConfirmationDialogFragment.Listener {
    private val disposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_barcode_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        supportEdgeToEdge()
        initTabs()
        handleMenuClicked()

    }


    override fun onDeleteConfirmed() {
        clearHistory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.clear()
    }

    private fun supportEdgeToEdge() {
        app_bar_layout.applySystemWindowInsets(applyTop = true)
    }

    private fun initTabs() {
        view_pager.adapter = BarcodeHistoryViewPagerAdapter(requireContext(), childFragmentManager)
        tab_layout.setupWithViewPager(view_pager)
    }

    private fun handleMenuClicked() {
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_export_history -> navigateToExportHistoryScreen()
                R.id.item_clear_history -> showDeleteHistoryConfirmationDialog()
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun navigateToExportHistoryScreen() {
        ExportHistoryActivity.start(requireActivity())
    }

    private fun showDeleteHistoryConfirmationDialog() {
        val dialog = DeleteConfirmationDialogFragment.newInstance(R.string.dialog_delete_clear_history_message)
        dialog.show(childFragmentManager, "")
    }

    private fun clearHistory() {
        barcodeDatabase.deleteAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { },
                ::showError
            )
            .addTo(disposable)
    }
}