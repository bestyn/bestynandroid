package com.gbksoft.neighbourhood.ui.fragments.audio_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentAudioDetailsBinding
import com.gbksoft.neighbourhood.model.audio.Audio
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.ui.fragments.audio_details.adapter.AudioDetailsListAdapter
import com.gbksoft.neighbourhood.ui.fragments.audio_details.adapter.AudioStoryListAdapter
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.report.ReportContentArgs
import com.gbksoft.neighbourhood.ui.widgets.chat.audio.AudioPlaybackManager
import com.gbksoft.neighbourhood.utils.GridDividerItemDecoration
import com.gbksoft.neighbourhood.utils.LastVisiblePositionChangeListener
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class AudioDetailsFragment : SystemBarsColorizeFragment() {

    private val args by navArgs<AudioDetailsFragmentArgs>()
    private val viewModel by viewModel<AudioDetailsViewModel> {
        parametersOf(args.audio.id)
    }
    private lateinit var layout: FragmentAudioDetailsBinding

    private lateinit var audioAdapter: AudioDetailsListAdapter
    private lateinit var storiesAdapter: AudioStoryListAdapter
    private lateinit var audioPlaybackManager: AudioPlaybackManager

    override fun getStatusBarColor(): Int = R.color.white
    override fun getNavigationBarColor(): Int = R.color.white

    private val lastVisiblePositionListener: RecyclerView.OnScrollListener = object : LastVisiblePositionChangeListener() {
        override fun lastVisiblePositionChanged(lastVisibleItemPosition: Int) {
            viewModel.onVisibleItemChanged(lastVisibleItemPosition)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_audio_details, container, false)

        setupAudio()
        setupStories()
        subscribeToViewModel()
        hideNavigateBar()
        return layout.root
    }

    private fun setupAudio() {
        audioPlaybackManager = AudioPlaybackManager(requireContext())
        audioAdapter = AudioDetailsListAdapter(audioPlaybackManager).apply {
            onStarButtonClickListener = { viewModel.handleStarButtonClick(it) }
            onReportOptionClickListener = { openReportAudio(it) }
        }

        audioAdapter.setData(listOf(args.audio))
        layout.rvAudioDetails.adapter = audioAdapter
        layout.rvAudioDetails.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun setupStories() {
        storiesAdapter = AudioStoryListAdapter().apply {
            onStoryClickListener = { openDynamicStoryList(it) }
        }
        layout.rvStoriesAudioDetails.adapter = storiesAdapter
        layout.rvStoriesAudioDetails.addItemDecoration(getStoriesItemDecoration())
        layout.rvStoriesAudioDetails.addOnScrollListener(lastVisiblePositionListener)
    }

    private fun getStoriesItemDecoration(): RecyclerView.ItemDecoration {
        val spacing = resources.getDimensionPixelSize(R.dimen.business_image_spacing)
        val columnsCount = resources.getInteger(R.integer.business_images_columns_count)
        return GridDividerItemDecoration(spacing, columnsCount)
    }

    private fun subscribeToViewModel() {
        viewModel.stories.observe(viewLifecycleOwner, Observer(this::onStoriesLoaded))
        viewModel.progressBarVisibility.observe(viewLifecycleOwner, Observer(this::onProgressVisibilityChanged))
        viewModel.isAudioFavorite.observe(viewLifecycleOwner, Observer(this::onAudioIsFavoriteChanged))
    }

    private fun onStoriesLoaded(stories: List<FeedPost>) {
        if (stories.isNotEmpty()) {
            storiesAdapter.setData(stories)
        } else {
            showEmptyState()
        }
    }

    private fun onAudioIsFavoriteChanged(isFavorite: Boolean) {
        val audio = args.audio.apply { this.isFavorite = isFavorite }
        audioAdapter.setData(listOf(audio))
    }

    private fun onProgressVisibilityChanged(isVisible: Boolean) {
        if (isVisible) {
            layout.progressBar.visibility = View.VISIBLE
        } else {
            layout.progressBar.visibility = View.GONE
        }
    }

    private fun showEmptyState() {
        layout.rvStoriesAudioDetails.visibility = View.GONE
        layout.ivEmptySearch.visibility = View.VISIBLE
        layout.tvEmptySearch.visibility = View.VISIBLE
    }

    private fun openReportAudio(audio: Audio) {
        val reportContentArgs = ReportContentArgs.fromAudio(audio)
        val direction = AudioDetailsFragmentDirections.toReportPostFragment(reportContentArgs)
        findNavController().navigate(direction)
    }

    private fun openDynamicStoryList(story: FeedPost) {
        val direction = AudioDetailsFragmentDirections.toDynamicStoryList(story.post.id, args.audio.id)
        findNavController().navigate(direction)
    }

    override fun onStop() {
        super.onStop()
        audioPlaybackManager.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlaybackManager.releasePlayer()
    }

}