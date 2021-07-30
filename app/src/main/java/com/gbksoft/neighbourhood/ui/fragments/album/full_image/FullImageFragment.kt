package com.gbksoft.neighbourhood.ui.fragments.album.full_image

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentImagePreviewBinding
import com.gbksoft.neighbourhood.ui.components.DownloadViewModel
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.utils.ToastUtils
import org.koin.androidx.viewmodel.ext.android.viewModel


class FullImageFragment : SystemBarsColorizeFragment() {
    private lateinit var layout: FragmentImagePreviewBinding
    private val args by navArgs<FullImageFragmentArgs>()
    private val viewModel by viewModel<DownloadViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_image_preview, container, false)

        hideNavigateBar()
        loadImage()
        initListeners()
        subscribeToViewModel()

        return layout.root
    }

    private fun loadImage() {
        layout.progressBar.visibility = View.VISIBLE
        Glide.with(layout.ivPreview)
            .load(args.image.origin)
            .addListener(requestListener())
            .into(layout.ivPreview)
    }

    private fun initListeners() {
        layout.btnDownload.setOnClickListener {
            viewModel.download(args.image.origin)
        }
        layout.btnClose.setOnClickListener { findNavController().popBackStack() }
    }

    private fun requestListener(): RequestListener<Drawable> {
        return object : RequestListener<Drawable> {
            override fun onLoadFailed(e: GlideException?, model: Any?,
                                      target: Target<Drawable>?,
                                      isFirstResource: Boolean): Boolean {
                layout.progressBar.visibility = View.GONE
                return false
            }

            override fun onResourceReady(resource: Drawable?,
                                         model: Any?, target: Target<Drawable>?,
                                         dataSource: DataSource?,
                                         isFirstResource: Boolean): Boolean {
                layout.progressBar.visibility = View.GONE
                return false
            }
        }
    }

    private fun subscribeToViewModel() {
        viewModel.downloading.observe(viewLifecycleOwner, Observer {
            layout.downloadProgressBar.visibility = if (it) View.VISIBLE else View.GONE
            layout.btnDownload.visibility = if (it) View.GONE else View.VISIBLE
        })
        viewModel.downloadComplete.observe(viewLifecycleOwner, Observer {
            ToastUtils.showToastMessage(requireContext(), R.string.full_image_media_downloaded_message)
        })
    }

    override fun getNavigationBarColor(): Int {
        return R.color.screen_background_color
    }
}