package com.artemisaswtt.qrscanner.feature.tabs.create.qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.artemisaswtt.qrscanner.R
import com.artemisaswtt.qrscanner.extension.textString
import com.artemisaswtt.qrscanner.feature.tabs.create.BaseCreateBarcodeFragment
import com.artemisaswtt.qrscanner.model.schema.Schema
import com.artemisaswtt.qrscanner.model.schema.VEvent
import kotlinx.android.synthetic.main.fragment_create_qr_code_vevent.*

class CreateQrCodeEventFragment : BaseCreateBarcodeFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_qr_code_vevent, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edit_text_title.requestFocus()
        parentActivity.isCreateBarcodeButtonEnabled = true
    }

    override fun getBarcodeSchema(): Schema {
        return VEvent(
            uid = edit_text_title.textString,
            organizer = edit_text_organizer.textString,
            summary = edit_text_summary.textString,
            startDate = button_date_time_start.dateTime,
            endDate = button_date_time_end.dateTime
        )
    }
}