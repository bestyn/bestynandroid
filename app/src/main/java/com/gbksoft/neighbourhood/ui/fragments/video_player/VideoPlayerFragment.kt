package com.gbksoft.neighbourhood.ui.fragments.video_player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentVideoPlayerBinding
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.ui.components.DownloadViewModel
import com.gbksoft.neighbourhood.ui.fragments.base.BaseFragment
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class VideoPlayerFragment : BaseFragment() {
    private lateinit var layout: FragmentVideoPlayerBinding
    private val args by navArgs<VideoPlayerFragmentArgs>()
    private val viewModel by viewModel<DownloadViewModel>()
    private val simpleCache: SimpleCache by inject()
    private lateinit var player: SimpleExoPlayer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_video_player, container, false)

        hideNavigateBar()

        player = SimpleExoPlayer.Builder(requireContext())
            .build()

        setupView(player)
        setClickListeners()
        prepareVideo(player, args.video)
        if (savedInstanceState != null) {
            restorePlayerState(savedInstanceState)
        }
        subscribeToViewModel()

        return layout.root
    }

    private fun setupView(player: SimpleExoPlayer) {
        layout.playerView.player = player
        layout.playerView.setControllerVisibilityListener { switchControlsVisibility(it) }
    }

    private fun switchControlsVisibility(visibility: Int) {
        layout.btnClose.visibility = visibility
        layout.btnMute.visibility = visibility
    }

    private fun setClickListeners() {
        layout.btnClose.setOnClickListener { closePlayer() }
        layout.btnMute.setOnClickListener { switchMute() }
        layout.btnDownload.setOnClickListener {
            viewModel.download(args.video.origin)
        }
    }

    private fun closePlayer() {
        player.stop()
        activity?.onBackPressed()
    }

    private fun switchMute() {
        if (player.volume == 0f) {
            player.volume = 1f
        } else {
            player.volume = 0f
        }
        checkIsMuted()
    }

    private fun checkIsMuted() {
        if (player.volume > 0) {
            layout.btnMute.setImageResource(R.drawable.ic_player_unmuted)
        } else {
            layout.btnMute.setImageResource(R.drawable.ic_player_muted)
        }
    }

    private fun prepareVideo(player: SimpleExoPlayer, video: Media.Video) {
        val cacheDataSourceFactory = CacheDataSourceFactory(
                simpleCache,
                DefaultHttpDataSourceFactory(context?.let {
                    Util.getUserAgent(
                            it, getString(
                            R.string.app_name))
                }),
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(video.origin)

        player.prepare(LoopingMediaSource(mediaSource))
    }

    private fun restorePlayerState(savedInstanceState: Bundle) {
        val volume = savedInstanceState.getFloat("volume", -1f)
        if (volume != -1f) {
            player.volume = volume
        }
        checkIsMuted()
        val currentPosition = savedInstanceState.getLong("currentPosition", -1)
        if (currentPosition != -1L) {
            player.seekTo(currentPosition)
        }
        player.playWhenReady = savedInstanceState.getBoolean("isPlaying", false)

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong("currentPosition", player.currentPosition)
        outState.putBoolean("isPlaying", player.isPlaying)
        outState.putFloat("volume", player.volume)
        super.onSaveInstanceState(outState)
    }

    private fun subscribeToViewModel() {
        viewModel.downloading.observe(viewLifecycleOwner, Observer {
            layout.downloadProgressBar.visibility = if (it) View.VISIBLE else View.GONE
            layout.btnDownload.visibility = if (it) View.GONE else View.VISIBLE
        })
        viewModel.downloadComplete.observe(viewLifecycleOwner, Observer {
            ToastUtils.showToastMessage(requireContext(), R.string.video_player_media_downloaded_message)
        })
    }

    override fun onDestroyView() {
        player.stop()
        player.release()
        super.onDestroyView()
    }
}