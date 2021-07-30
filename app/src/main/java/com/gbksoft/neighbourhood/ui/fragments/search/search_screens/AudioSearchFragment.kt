package com.gbksoft.neighbourhood.ui.fragments.search.search_screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentSearchListBinding
import com.gbksoft.neighbourhood.model.audio.Audio
import com.gbksoft.neighbourhood.ui.fragments.base.BaseFragment
import com.gbksoft.neighbourhood.ui.fragments.search.GlobalSearchFragmentDirections
import com.gbksoft.neighbourhood.ui.fragments.search.adapter.AudioSearchListAdapter
import com.gbksoft.neighbourhood.ui.widgets.chat.audio.AudioPlaybackManager
import com.gbksoft.neighbourhood.utils.LastVisiblePositionChangeListener
import org.koin.androidx.viewmodel.ext.android.viewModel

class AudioSearchFragment : BaseFragment() {
    private lateinit var layout: FragmentSearchListBinding
    private lateinit var adapter: AudioSearchListAdapter
    private val viewModel by viewModel<AudioSearchViewModel>()
    private lateinit var audioPlaybackManager: AudioPlaybackManager
    private var globalSearchScreen: GlobalSearchScreen? = null
    private var currentSearchQuery: SearchQuery? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initAdapter()
    }

    private fun initAdapter() {
        audioPlaybackManager = AudioPlaybackManager(requireContext())
        adapter = AudioSearchListAdapter(audioPlaybackManager).apply {
            onStarButtonClickListener = { viewModel.handleStarButtonClick(it) }
            onAudioDetailsClickListener = { openAudioDetails(it) }
            onReportOptionClickListener = { openReportAudio(it) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_search_list, container, false)

        setupView()
        subscribeToViewModel()

        return layout.root
    }

    override fun onStart() {
        super.onStart()
        globalSearchScreen = fetchGlobalSearchScreen()
        globalSearchScreen?.searchQuery()?.observe(viewLifecycleOwner, Observer { onSearchQuery(it) })
    }

    override fun onStop() {
        super.onStop()
        globalSearchScreen = null
        audioPlaybackManager.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlaybackManager.stop()
    }


    private fun fetchGlobalSearchScreen(): GlobalSearchScreen? {
        val host = parentFragment
        if (host is GlobalSearchScreen) {
            return host
        }
        return null
    }

    private fun navigator() = globalSearchScreen?.navigator()

    private fun onSearchQuery(searchQuery: SearchQuery?) {
        if (currentSearchQuery != searchQuery) {
            currentSearchQuery = searchQuery?.copy()
            // adapter.clearData()
            layout.rvSearchResult.scrollToPosition(0)
            if (searchQuery == null || searchQuery.isEmpty()) viewModel.cancelSearch()
            else viewModel.findProfiles(searchQuery.value)
        }
    }

    private fun setupView() {
        layout.rvSearchResult.adapter = adapter
        layout.rvSearchResult.layoutManager = LinearLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL, false)
        AppCompatResources.getDrawable(requireContext(), R.drawable.divider_profiles_search_result)?.let { divider ->
            val dividerDecoration = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
            dividerDecoration.setDrawable(divider)
            dividerDecoration
        }?.let { divider ->
            layout.rvSearchResult.addItemDecoration(divider)
        }
        layout.rvSearchResult.addOnScrollListener(object : LastVisiblePositionChangeListener() {
            override fun lastVisiblePositionChanged(lastVisibleItemPosition: Int) {
                viewModel.onVisibleItemChanged(lastVisibleItemPosition)
            }
        })
    }

    override fun onDestroyView() {
        layout.rvSearchResult.adapter = null
        super.onDestroyView()
    }

    private fun subscribeToViewModel() {
        viewModel.searchResult.observe(viewLifecycleOwner, Observer { showSearchResult(it) })
        viewModel.progressBarVisibility.observe(viewLifecycleOwner, Observer { setProgressBarVisibility(it) })
    }

    private fun showSearchResult(profiles: List<Audio>) {
        if (profiles.isEmpty()) {
            layout.rvSearchResult.visibility = View.GONE
            layout.groupEmptySearch.visibility = View.VISIBLE
        } else {
            layout.groupEmptySearch.visibility = View.GONE
            layout.rvSearchResult.visibility = View.VISIBLE
        }
           adapter.setData(profiles)
    }

    private fun setProgressBarVisibility(isVisible: Boolean) {
        if (isVisible) {
            layout.groupEmptySearch.visibility = View.GONE
            layout.rvSearchResult.visibility = View.GONE
            layout.progressBar.visibility = View.VISIBLE
        } else {
            layout.progressBar.visibility = View.GONE
        }
    }



    private fun openReportAudio(audio: Audio) {}

    private fun openAudioDetails(audio: Audio) {
        val direction = GlobalSearchFragmentDirections.toAudioDetailsFragment(audio)
        findNavController().navigate(direction)
    }

}