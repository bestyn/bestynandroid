package com.gbksoft.neighbourhood.ui.dialogs.bottom_sheet

import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheet : BottomSheetDialogFragment() {

    override fun show(manager: FragmentManager, tag: String?) {
        if (isAdded.not()) super.show(manager, tag)
    }
}