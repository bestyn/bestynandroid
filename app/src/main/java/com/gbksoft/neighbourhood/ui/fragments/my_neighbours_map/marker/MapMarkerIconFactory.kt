package com.gbksoft.neighbourhood.ui.fragments.my_neighbours_map.marker

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.drawToBitmap
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.LayoutMapMarkerMeBinding
import com.gbksoft.neighbourhood.databinding.LayoutMapMarkerNeighborBinding
import com.gbksoft.neighbourhood.utils.Constants
import timber.log.Timber


class MapMarkerIconFactory(context: Context) {
    private val neighborLayout: LayoutMapMarkerNeighborBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.layout_map_marker_neighbor,
        null,
        false)
    private val meLayout: LayoutMapMarkerMeBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.layout_map_marker_me,
        null,
        false)
    private val clusterLayout: TextView = LayoutInflater.from(context)
        .inflate(R.layout.layout_map_marker_cluster, null) as TextView
    private val titleMaxLength = Constants.MY_NEIGHBORS_MARKER_TITLE_MAX_LENGTH
    private val clusterMaxCount = Constants.MY_NEIGHBORS_CLUSTER_MAX_COUNT

    init {
        clusterLayout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    @Synchronized
    fun createMyMarkerIcon(title: String, avatar: Bitmap?): Bitmap {
        meLayout.avatarView.setImage(avatar)
        meLayout.avatarView.setFullName(title)

        meLayout.tvTitle.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val titleMeasuredWidth = meLayout.tvTitle.measuredWidth
        meLayout.root.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val measuredWidth = if (meLayout.root.measuredWidth > titleMeasuredWidth) {
            meLayout.root.measuredWidth
        } else {
            titleMeasuredWidth
        }
        val measuredHeight = meLayout.root.measuredHeight
        meLayout.root.layout(0, 0, measuredWidth, measuredHeight)
        return meLayout.root.drawToBitmap()
    }

    @Synchronized
    fun createNeighborMarkerIcon(isBusiness: Boolean, title: String, avatar: Bitmap?): Bitmap {
        neighborLayout.tvTitle.text = if (title.length > titleMaxLength) {
            title.substring(0, titleMaxLength) + "…"
        } else {
            title
        }
        neighborLayout.avatarView.reset()
        neighborLayout.avatarView.setBusiness(isBusiness)
        neighborLayout.avatarView.setImage(avatar)
        neighborLayout.avatarView.setFullName(title)

        neighborLayout.tvTitle.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val titleMeasuredWidth = neighborLayout.tvTitle.measuredWidth
        neighborLayout.root.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val measuredWidth = if (meLayout.root.measuredWidth > titleMeasuredWidth) {
            neighborLayout.root.measuredWidth
        } else {
            titleMeasuredWidth
        }
        val measuredHeight = neighborLayout.root.measuredHeight
        neighborLayout.root.layout(0, 0, measuredWidth, measuredHeight)
        Timber.tag("MapTag2").d("title: $title")
        neighborLayout.avatarView.invalidate()
        return neighborLayout.root.drawToBitmap()
    }

    @Synchronized
    fun createClusterIcon(markersCount: Int): Bitmap {
        val text = if (markersCount <= clusterMaxCount) {
            markersCount.toString()
        } else "${clusterMaxCount}+"

        clusterLayout.text = text

        clusterLayout.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val measuredWidth = clusterLayout.measuredWidth
        val measuredHeight = clusterLayout.measuredHeight
        clusterLayout.layout(0, 0, measuredWidth, measuredHeight)
        return clusterLayout.drawToBitmap()
    }

}