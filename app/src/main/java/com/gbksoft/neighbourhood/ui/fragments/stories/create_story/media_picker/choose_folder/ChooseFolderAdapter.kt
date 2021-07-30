package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker.choose_folder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterMediaFolderBinding

class ChooseFolderAdapter : RecyclerView.Adapter<ChooseFolderAdapter.MediaFolderViewHolder>() {

    private val folderPathList = mutableListOf<String>()
    var onItemClickListener: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaFolderViewHolder {
        val layout = DataBindingUtil.inflate<AdapterMediaFolderBinding>(
                LayoutInflater.from(parent.context),
                R.layout.adapter_media_folder,
                parent,
                false)
        return MediaFolderViewHolder(layout, onItemClickListener)
    }

    override fun onBindViewHolder(holder: MediaFolderViewHolder, position: Int) {
        val folderPath = folderPathList[position]
        holder.bind(folderPath)
    }

    override fun getItemCount(): Int {
        return folderPathList.size
    }

    fun setData(data: List<String>) {
        folderPathList.clear()
        folderPathList.add(ChooseFolderBottomSheet.ALL_PHOTOS)
        folderPathList.addAll(data)
        notifyDataSetChanged()
    }

    class MediaFolderViewHolder(
        private val layout: AdapterMediaFolderBinding,
        private val onItemClickListener: ((String) -> Unit)?) : RecyclerView.ViewHolder(layout.root) {

        fun bind(folderPath: String) {
            val folderName = folderPath.substring(folderPath.lastIndexOf('/') + 1)
            layout.tvFolder.text = folderName
            layout.tvFolder.setOnClickListener { onItemClickListener?.invoke(folderPath) }
        }
    }
}