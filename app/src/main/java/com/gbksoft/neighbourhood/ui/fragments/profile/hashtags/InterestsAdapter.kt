package com.gbksoft.neighbourhood.ui.fragments.profile.hashtags

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.google.android.material.chip.Chip

class InterestsAdapter : RecyclerView.Adapter<InterestsAdapter.InterestsViewHolder>() {

    private val interests = mutableListOf<Hashtag>()
    var onInterestClickListener: ((Hashtag) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterestsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.adapter_interest_list, parent, false) as Chip
        return InterestsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return interests.size
    }

    override fun onBindViewHolder(holder: InterestsViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemId(position: Int): Long {
        return interests[position].id
    }

    fun setData(data: List<Hashtag>) {
        val result = DiffUtil.calculateDiff(HashtagDiffUtil(interests, data))
        interests.clear()
        interests.addAll(data)
        result.dispatchUpdatesTo(this)
    }

    inner class InterestsViewHolder(val chip: Chip) : RecyclerView.ViewHolder(chip) {
        lateinit var interest: Hashtag

        fun bind(position: Int) {
            interest = interests[position]
            setupView()
            initListeners()
        }

        private fun setupView() {
            chip.text = interest.name
            chip.isChecked = interest.isSelected
        }

        private fun initListeners() {
            chip.setOnClickListener {
                onInterestClickListener?.invoke(interest)
            }
        }
    }
}
