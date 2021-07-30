package com.gbksoft.neighbourhood.ui.fragments.base.posts_feed

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentMediaPagerContentBinding
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.ui.fragments.base.BaseFragment
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.contract.MediaPagerHost
import com.gbksoft.neighbourhood.utils.glide.RectCenterCrop
import com.gbksoft.neighbourhood.utils.media.PlaceholderProvider
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


class MediaPagerContentFragment : BaseFragment() {
    private lateinit var layout: FragmentMediaPagerContentBinding
    private var postMedia: Media? = null
    private var removable: Boolean = true
    private lateinit var defaultRequestOptions: RequestOptions
    private lateinit var roundedCornersTransform: RoundedCorners
    private lateinit var centerCropTransform: CenterCrop
    private val mediaMetadataRetriever = MediaMetadataRetriever()

    companion object {
        const val ARG_POST_MEDIA = "post_media"
        const val ARG_REMOVABLE = "removable"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            postMedia = it.getParcelable(ARG_POST_MEDIA)
            removable = it.getBoolean(ARG_REMOVABLE, true)
        }

        val radius = resources.getDimensionPixelSize(R.dimen.add_post_media_stroke_corner)
        roundedCornersTransform = RoundedCorners(radius)
        centerCropTransform = CenterCrop()
        defaultRequestOptions = RequestOptions()
            .transform(centerCropTransform, roundedCornersTransform)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_media_pager_content, container, false)

        when (val media = postMedia) {
            is Media.Picture -> {
                layout.ivPlay.visibility = View.GONE
                layout.ivPreview.visibility = View.VISIBLE
                loadPicturePreview(media)
            }
            is Media.Video -> {
                layout.ivPlay.visibility = View.VISIBLE

                /*if (media.isLocal()) {
                    layout.ivLocalVideoPreview.visibility = View.VISIBLE
                    layout.ivPreview.visibility = View.GONE
                } else {*/
                layout.ivPreview.visibility = View.VISIBLE
                loadVideoPreview(media)
                //}
            }
        }

        layout.btnRemove.visibility = if (removable) View.VISIBLE else View.GONE

        layout.btnRemove.setOnClickListener { removeMedia() }
        layout.ivPreview.setOnClickListener { onClickMedia() }
        layout.ivPlay.setOnClickListener { onClickMedia() }

        return layout.root
    }


    private fun loadPicturePreview(picture: Media.Picture) {
        val previewArea = picture.previewArea
        val requestOptions = if (picture.isLocal() && previewArea != null) {
            RequestOptions().downsample(DownsampleStrategy.NONE)
                .transform(RectCenterCrop(previewArea), roundedCornersTransform)
        } else {
            defaultRequestOptions
        }

        Glide.with(layout.ivPreview)
            .asBitmap()
            .load(picture.preview)
            .placeholder(PlaceholderProvider.getPicturePlaceholder(requireContext()))
            .apply(requestOptions)
            .into(layout.ivPreview)
    }

    private var videoPreviewDisposable: Disposable? = null
    private fun loadVideoPreview(video: Media.Video) {
        if (video.preview != video.origin) {
            loadVideoPreviewUri(video.preview)
        } else {
            if (video.isLocal()) {
                loadVideoPreviewUri(video.origin)
            } else {
                loadRemoteVideoFrame(video.origin.toString())
            }
        }
    }

    private fun loadVideoPreviewUri(uri: Uri) {
        Glide.with(layout.ivPreview)
            .load(uri)
            .placeholder(PlaceholderProvider.getVideoPlaceholder(requireContext()))
            .apply(defaultRequestOptions)
            .into(layout.ivPreview)
    }

    private fun loadRemoteVideoFrame(url: String) {
        videoPreviewDisposable?.dispose()
        videoPreviewDisposable = Single.fromCallable {
            mediaMetadataRetriever.setDataSource(url, mutableMapOf<String, String>())
            return@fromCallable mediaMetadataRetriever.getFrameAtTime(100)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { frame ->
                Glide.with(layout.ivPreview)
                    .load(frame)
                    .placeholder(PlaceholderProvider.getVideoPlaceholder(requireContext()))
                    .apply(defaultRequestOptions)
                    .into(layout.ivPreview)
            }
    }

    private fun removeMedia() {
        val hostFragment = parentFragment
        if (hostFragment is MediaPagerHost) {
            postMedia?.let { hostFragment.removeMedia(it) }
        }
    }

    private fun onClickMedia() {
        val hostFragment = parentFragment
        if (hostFragment is MediaPagerHost) {
            postMedia?.let { hostFragment.onMediaClick(it) }
        }
    }
}