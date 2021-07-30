package com.gbksoft.neighbourhood.ui.fragments.stories.audio.list

import android.app.Activity
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.app.NApplication
import com.gbksoft.neighbourhood.databinding.FragmentAudioListBinding
import com.gbksoft.neighbourhood.model.audio.Audio
import com.gbksoft.neighbourhood.mvvm.result.ResultData
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.report.ReportContentArgs
import com.gbksoft.neighbourhood.ui.fragments.stories.audio.AudioListAdapter
import com.gbksoft.neighbourhood.ui.fragments.stories.audio.add.AddAudioFragment
import com.gbksoft.neighbourhood.ui.widgets.chat.audio.AudioPlaybackManager
import com.gbksoft.neighbourhood.ui.widgets.chat.list.SearchTextWatcher
import com.gbksoft.neighbourhood.utils.LastVisiblePositionChangeListener
import com.gbksoft.neighbourhood.utils.media.MediaUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

private const val GET_AUDIO_REQUEST_CODE = 1

class AudioListFragment : SystemBarsColorizeFragment() {

    private val viewModel by viewModel<AudioListViewModel>()
    private lateinit var layout: FragmentAudioListBinding
    private lateinit var adapter: AudioListAdapter
    private lateinit var audioPlaybackManager: AudioPlaybackManager

    override fun getStatusBarColor() = R.color.screen_foreground_color
    override fun getNavigationBarColor() = R.color.screen_foreground_color
    override fun getFragmentContainerColor() = R.color.screen_foreground_color

    private val lastVisiblePositionListener: RecyclerView.OnScrollListener = object : LastVisiblePositionChangeListener() {
        override fun lastVisiblePositionChanged(lastVisibleItemPosition: Int) {
            viewModel.onVisibleItemChanged(lastVisibleItemPosition)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_audio_list, container, false)
        setupAdapter()
        setClickListeners()
        subscribeViewModel()
        subscribeAdjustStoryResult()
        return layout.root
    }

    private fun subscribeViewModel() {
        viewModel.audioList.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                adapter.setData(it)
                layout.groupEmptySearch.visibility = View.GONE
            } else {
                adapter.setData(listOf())
                layout.groupEmptySearch.visibility = View.VISIBLE
            }
        })
    }

    private fun setupAdapter() {
        audioPlaybackManager = AudioPlaybackManager(requireContext())
        adapter = AudioListAdapter(audioPlaybackManager).apply {
            onStarButtonClickListener = { viewModel.handleStarButtonClick(it) }
            onApplyButtonClickListener = { downloadAudio(it) }
            onReportOptionClickListener = { openReportAudio(it) }
        }
        layout.rvAudio.adapter = adapter
        layout.rvAudio.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        layout.rvAudio.addOnScrollListener(lastVisiblePositionListener)
        layout.rvAudio.setHasFixedSize(true)
    }

    private fun setClickListeners() {
        layout.ivBack.setOnClickListener { popBackStackWithoutResult() }
        layout.btnSearch.setOnClickListener { handleSearchButtonClick() }
        layout.flAddAudio.setOnClickListener { dispatchAudioIntent() }
        layout.etSearch.addTextChangedListener(SearchTextWatcher(::onSearchQueryChanged))
        layout.cgFilters.setOnCheckedChangeListener { _, checkedId ->
            onFilterChanged(checkedId)
        }
    }

    private fun handleSearchButtonClick() {
        if (layout.etSearch.isVisible) {
            hideSearchField()
        } else {
            showSearchField()
        }
    }

    private fun showSearchField() {
        layout.flAddAudio.visibility = View.GONE
        layout.etSearch.visibility = View.VISIBLE
        layout.ivSearch.setBackgroundResource(R.drawable.bg_search_audio_btn_selected)
        layout.ivSearch.setImageResource(R.drawable.ic_search_audio_selected)
    }

    private fun hideSearchField() {
        layout.etSearch.visibility = View.GONE
        layout.flAddAudio.visibility = View.VISIBLE
        layout.ivSearch.setBackgroundResource(R.drawable.bg_search_audio_btn_unselected)
        layout.ivSearch.setImageResource(R.drawable.ic_search_audio_unselected)
    }

    private fun onSearchQueryChanged(searchQuery: CharSequence) {
        viewModel.setCurrentSearch(searchQuery.toString())
    }

    private fun onFilterChanged(checkedId: Int) {
        when (checkedId) {
            R.id.discover -> viewModel.setCurrentTab(AudioTab.DISCOVER)
            R.id.myTracks -> viewModel.setCurrentTab(AudioTab.MY_TRACKS)
            R.id.favorites -> viewModel.setCurrentTab(AudioTab.FAVORITES)
        }
    }

    private fun dispatchAudioIntent() {
        val intent = Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = "audio/*"
        }
        startActivityForResult(intent, GET_AUDIO_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == GET_AUDIO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val audio = intent?.data ?: return
            if (viewModel.validateAddedAudio(audio)) {
                openAddAudio(audio)
            }
        }
    }

    private fun downloadAudio(audio: Audio) {
        layout.progressBar.visibility = View.VISIBLE
        val root = File(NApplication.context.filesDir, "story")
        if (!root.exists()) {
            root.mkdirs()
        }
        val filePath = "$root/${MediaUtils.generateFileName("audio")}.mp3"

        lifecycleScope.launch {
            withContext(Dispatchers.IO) { MediaUtils.downloadAndSaveToFile(audio.url, filePath) }
            audio.fileUri = Uri.parse(filePath)

            val audioMetadataRetriever = MediaMetadataRetriever().apply { setDataSource(requireContext(), audio.fileUri) }
            audio.fileDuration = audioMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toInt()
            popBackStackWithResult(audio)
        }
    }

    private fun subscribeAdjustStoryResult() {
        findNavController()
                .currentBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<Boolean>(AddAudioFragment.AUDIO_CREATED)
                ?.observe(viewLifecycleOwner, Observer { onNewAudioCreated() })
    }

    private fun onNewAudioCreated() {
        layout.cgFilters.check(R.id.myTracks)
    }

    private fun popBackStackWithResult(audio: Audio) {
        layout.progressBar.visibility = View.GONE
        val result = ResultData(audio)
        findNavController()
                .previousBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<ResultData<Audio>>(ADD_AUDIO_RESULT)
                ?.value = result
        findNavController().popBackStack()
    }

    private fun popBackStackWithoutResult() {
        findNavController().popBackStack()
    }

    private fun openReportAudio(audio: Audio) {
        val reportContentArgs = ReportContentArgs.fromAudio(audio)
        val direction = AudioListFragmentDirections.toReportPostFragment(reportContentArgs)
        findNavController().navigate(direction)
    }

    private fun openAddAudio(audio: Uri) {
        val direction = AudioListFragmentDirections.toAddAudio(audio)
        findNavController().navigate(direction)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        audioPlaybackManager.releasePlayer()
    }

    companion object {
        const val ADD_AUDIO_RESULT = "add_audio_result"
    }
}