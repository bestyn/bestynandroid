package com.gbksoft.neighbourhood.ui.fragments.stories.audio.add

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentAddAudioBinding
import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.gbksoft.neighbourhood.ui.dialogs.YesNoDialog
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.HashtagAdapter
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description.component.HashtagTextWatcher
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ClippingMediaSource
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.concurrent.TimeUnit

class AddAudioFragment : SystemBarsColorizeFragment(), HashtagTextWatcher.OnHashtagChangedListener {

    private val args by navArgs<AddAudioFragmentArgs>()
    private val viewModel by viewModel<AddAudioViewModel> {
        parametersOf(args.audio)
    }

    private lateinit var layout: FragmentAddAudioBinding
    private lateinit var player: SimpleExoPlayer

    private var hashtagTextWatcher: HashtagTextWatcher? = null
    private val hashtagAdapter by lazy { HashtagAdapter() }
    private var shouldAppend = false
    private var startTime = 0

    private val progressObservable = Observable.interval(16, TimeUnit.MILLISECONDS)
    private var progressDisposable: Disposable? = null
    private var duration: Int = 0

    override fun getStatusBarColor() = R.color.screen_foreground_color
    override fun getNavigationBarColor() = R.color.screen_foreground_color
    override fun getFragmentContainerColor() = R.color.screen_foreground_color

    override fun onAttach(context: Context) {
        super.onAttach(context)
        player = SimpleExoPlayer.Builder(context).build()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_add_audio, container, false)
        setupView()
        setClickListeners()
        subscribeViewModel()
        return layout.root
    }

    private fun setupView() {
        hashtagTextWatcher = HashtagTextWatcher(layout.etDescription, this).apply {
            setHashtagColor(requireContext(), R.color.post_hashtag_color)
        }
        layout.trimAudioView.setAudioUri(args.audio)
        layout.trimAudioView.onTimeChangedListener = { onAudioTimeThumbChanged(it) }
        layout.etDescription.addTextChangedListener(hashtagTextWatcher)
        layout.rvHashtags.layoutManager = LinearLayoutManager(requireContext())
        layout.rvHashtags.adapter = hashtagAdapter
    }

    private fun subscribeViewModel() {
        viewModel.duration.observe(viewLifecycleOwner, Observer(this::onAudioDurationFetched))
        viewModel.foundHashtags.observe(viewLifecycleOwner, Observer(this::onHashTagsFound))
        viewModel.errorFields.observe(viewLifecycleOwner, Observer(this::handleErrorFields))
        viewModel.audioLoadedLiveEvent.observe(viewLifecycleOwner, Observer { onAudioLoaded() })
    }

    private fun setClickListeners() {
        layout.btnHashtag.setOnClickListener { addHashSign() }
        hashtagAdapter.onItemClickListener = { addHashtag(it) }
        layout.btnPlayPause.setOnClickListener { onPlayPauseButtonClicked() }
        layout.btnSave.setOnClickListener { onSaveButtonClicked() }
        layout.btnClose.setOnClickListener { showCancelChangesDialog() }
    }

    private fun preparePlayer(startTime: Int = 0) {
        val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(requireContext(), getString(R.string.app_name)))
        val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(args.audio)
        val clippingSource = ClippingMediaSource(audioSource, startTime * 1000L, duration * 1000L)
        val loopingSource = LoopingMediaSource(clippingSource)
        player.prepare(loopingSource)
        player.playWhenReady = true
        subscribeAudioProgress()
    }

    private fun subscribeAudioProgress() {
        progressDisposable?.dispose()
        progressDisposable = progressObservable
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val progress = player.currentPosition + startTime
                    layout.trimAudioView.setPlayingProgress(progress)
                }
    }

    private fun onAudioDurationFetched(duration: Int) {
        this.duration = duration
        preparePlayer()
    }

    private fun onAudioTimeThumbChanged(timeInMs: Int) {
        startTime = timeInMs
        preparePlayer(timeInMs)
        player.seekTo(0)
        layout.trimAudioView.skipFirstLevels(timeInMs)
    }


    private fun onHashTagsFound(hashtags: List<Hashtag>) {
        shouldAppend = false
        if (hashtags.isEmpty()) {
            layout.rvHashtags.visibility = View.GONE
        } else {
            layout.rvHashtags.visibility = View.VISIBLE
            hashtagAdapter.setData(hashtags)
        }
    }

    private fun handleErrorFields(errorFieldsModel: ErrorFieldsModel) {
        layout.errors = errorFieldsModel
    }

    private fun onAudioLoaded() {
        ToastUtils.showToastMessage(R.string.audio_successfuly_uploaded_msg)
        popBackStackWithResult()
    }

    private fun addHashSign() {
        hashtagTextWatcher?.addHashSign()
    }

    private fun addHashtag(hashtag: Hashtag) {
        hashtagTextWatcher?.addHashtag(hashtag.name)
    }

    override fun onNewHashtag() {
        viewModel.searchHashtags(null)
    }

    override fun onHashtagChanged(hashtag: String) {
        viewModel.searchHashtags(hashtag)
    }

    override fun onEndHashtag() {
        viewModel.cancelHashtagsSearching()
        layout.rvHashtags.visibility = View.GONE
    }

    private fun onPlayPauseButtonClicked() {
        player.playWhenReady = !player.isPlaying
        if (player.isPlaying) {
            layout.btnPlayPause.setImageResource(R.drawable.ic_pause_audio)
        } else {
            layout.btnPlayPause.setImageResource(R.drawable.ic_play_audio)
        }
    }

    private fun onSaveButtonClicked() {
        val description = layout.etDescription.text.toString()
        viewModel.createAudio(description, startTime)
    }

    private fun showCancelChangesDialog() {
        val builder = YesNoDialog.Builder()
                .setNegativeButton(R.string.add_audio_cancel_changes_cancel_btn, null)
                .setPositiveButton(R.string.add_audio_cancel_changes_ok_btn) { popBackStack() }
                .setMessage(R.string.add_audio_cancel_changes_msg)
                .setCanceledOnTouchOutside(true)

        builder.build().show(childFragmentManager, "DeletePostDialog")
    }

    private fun popBackStack() {
        findNavController().popBackStack()
    }

    private fun popBackStackWithResult() {
        findNavController()
                .previousBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<Boolean>(AUDIO_CREATED)
                ?.value = true
        findNavController().popBackStack()
    }

    override fun onPause() {
        super.onPause()
        player.playWhenReady = false
    }

    override fun onResume() {
        super.onResume()
        player.playWhenReady = true
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }


    companion object {
        const val AUDIO_CREATED = "audio_created"
    }
}