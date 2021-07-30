package com.gbksoft.neighbourhood.ui.fragments.post_details.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R

class PostEmptyCommentsAdapter : RecyclerView.Adapter<PostEmptyCommentsAdapter.PostEmptyCommentsViewHolder>() {
    private var isVisible = false

    fun setVisibility(isVisible: Boolean) {
        if (this.isVisible == isVisible) return
        this.isVisible = isVisible
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostEmptyCommentsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.adapter_post_empty_comments, parent, false)
        return PostEmptyCommentsViewHolder(view)
    }

    override fun getItemCount(): Int = if (isVisible) 1 else 0


    override fun onBindViewHolder(holder: PostEmptyCommentsViewHolder, position: Int) {

    }


    class PostEmptyCommentsViewHolder(val root: View) : RecyclerView.ViewHolder(root)
}