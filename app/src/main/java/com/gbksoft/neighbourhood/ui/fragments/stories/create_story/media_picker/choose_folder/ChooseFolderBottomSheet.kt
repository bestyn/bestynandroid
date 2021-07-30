package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker.choose_folder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.BottomSheetChooseFolderBinding
import com.gbksoft.neighbourhood.ui.dialogs.bottom_sheet.BaseBottomSheet

class ChooseFolderBottomSheet : BaseBottomSheet() {

    private lateinit var layout: BottomSheetChooseFolderBinding
    private lateinit var adapter: ChooseFolderAdapter
    var onItemClickListener: ((String) -> Unit)? = null
    var folderList: List<String>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        layout = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_choose_folder, container, false)
        setup()
        return layout.root
    }

    private fun setup() {
        adapter = ChooseFolderAdapter()
        adapter.onItemClickListener = {
            onItemClickListener?.invoke(it)
            dismiss()
        }
        folderList?.let {
            adapter.setData(it)
        }

        layout.rvFolders.adapter = adapter
        layout.rvFolders.layoutManager = LinearLayoutManager(requireContext())
        layout.rvFolders.addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
    }

    companion object {
        const val ALL_PHOTOS = "All Photos"
    }
}