package com.gbksoft.neighbourhood.ui.fragments.create_edit_post.component

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.BottomSheetAudioAttachmentBinding
import com.gbksoft.neighbourhood.ui.dialogs.bottom_sheet.CancelableBottomSheet

class AudioAttachmentBottomSheet : CancelableBottomSheet() {
    private lateinit var layout: BottomSheetAudioAttachmentBinding
    var onSelectFromRecordingsClickListener: (() -> Unit)? = null
    var onRecordVoiceMessageListener: (() -> Unit)? = null
    var onStartAudioLiveStreamClickListener: (() -> Unit)? = null

    override fun createView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_audio_attachment, container, false)
        setClickListeners()
        return layout.root
    }

    private fun setClickListeners() {
        layout.tvSelectFromRecordings.setOnClickListener {
            onSelectFromRecordingsClickListener?.invoke()
            dismiss()
        }
        layout.tvRecordVoiceMessage.setOnClickListener {
            onRecordVoiceMessageListener?.invoke()
            dismiss()
        }
        layout.tvStartAudioLiveStream.setOnClickListener {
            onStartAudioLiveStreamClickListener?.invoke()
            dismiss()
        }
    }

    companion object {
        fun newInstance(): AudioAttachmentBottomSheet {
            return AudioAttachmentBottomSheet()
        }
    }
}