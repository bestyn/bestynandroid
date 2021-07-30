package com.gbksoft.neighbourhood.ui.fragments.create_edit_post

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterHashtagListBinding
import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.gbksoft.neighbourhood.ui.fragments.base.SimpleDiffUtilCallback

class HashtagAdapter : RecyclerView.Adapter<HashtagAdapter.HashtagViewHolder>() {

    private val hashtags = mutableListOf<Hashtag>()
    var onItemClickListener: ((Hashtag) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HashtagViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val textView = DataBindingUtil.inflate<AdapterHashtagListBinding>(
                inflater,
                R.layout.adapter_hashtag_list,
                parent,
                false).root as TextView
        return HashtagViewHolder(textView)
    }

    override fun getItemCount(): Int {
        return hashtags.size
    }

    override fun onBindViewHolder(holder: HashtagViewHolder, position: Int) {
        holder.bind(hashtags[position])
    }

    fun setData(data: List<Hashtag>) {
        val diffResult = DiffUtil.calculateDiff(HashtagDiffutil(hashtags, data))
        hashtags.clear()
        hashtags.addAll(data)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class HashtagViewHolder(private val textView: TextView) : RecyclerView.ViewHolder(textView) {

        fun bind(hashtag: Hashtag) {
            textView.text = String.format("#%s", hashtag.name)
            textView.setOnClickListener { onItemClickListener?.invoke(hashtag) }
        }
    }
}

class HashtagDiffutil(oldData: List<Hashtag>, newData: List<Hashtag>) : SimpleDiffUtilCallback<Hashtag>(oldData, newData) {
    override fun areItemsTheSame(oldItem: Hashtag, newItem: Hashtag) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Hashtag, newItem: Hashtag) = oldItem == newItem
}