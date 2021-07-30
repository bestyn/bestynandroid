package com.gbksoft.neighbourhood.ui.fragments.stories.for_unauthorized_users

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentStoryUnauthBinding
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.ui.fragments.base.BaseFragment
import com.gbksoft.neighbourhood.ui.fragments.stories.StoriesViewModel
import com.gbksoft.neighbourhood.ui.fragments.stories.StoryCommentsBottomSheet
import com.gbksoft.neighbourhood.ui.fragments.stories.list.base.BaseStoryListFragment
import com.gbksoft.neighbourhood.utils.InsetUtils
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel
import timber.log.Timber
import java.util.concurrent.TimeUnit

private const val KEY_STORY = "key_story"

class StoryUnauthorizedFragment(private val displaySignInButtonListener: DisplaySignInButton? = null) : BaseFragment() {

    private val storiesViewModel: StoriesViewModel by lazy {
        requireParentFragment().requireParentFragment().getViewModel<StoriesViewModel>()
    }
    private lateinit var layout: FragmentStoryUnauthBinding
    private lateinit var player: SimpleExoPlayer
    private val simpleCache: SimpleCache by inject()
    private var videoProgressDisposable: Disposable? = null
    private val storyLiveData = MutableLiveData<FeedPost>()
    private var authorInfoBottomPadding: Int = 0
    private var topInset: Int = 0
    private var storyCommentsFragment: StoryCommentsBottomSheet? = null

    private val observable = Observable
            .interval(16, TimeUnit.MILLISECONDS)

    companion object {
        fun newInstance(story: FeedPost, displaySignInButtonListener: DisplaySignInButton? = null): StoryUnauthorizedFragment {
            val args = Bundle()
            val storyFragment = StoryUnauthorizedFragment(displaySignInButtonListener)
            args.putParcelable(KEY_STORY, story)
            storyFragment.arguments = args
            return storyFragment
        }
    }

    fun setStory(story: FeedPost) {
        arguments?.run {
            putParcelable(KEY_STORY, story)
        }
        storyLiveData.value = story
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = SimpleExoPlayer.Builder(requireContext()).build()
        storyLiveData.value = arguments?.getParcelable(KEY_STORY)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_story_unauth, container, false)
        subscribeToInsets()
        setClickListeners()
        subscribeStoryLiveData()
        subscribeViewModel()

        return layout.root
    }

    private fun subscribeToInsets() {
        authorInfoBottomPadding = layout.authorInfo.paddingBottom
        (parentFragment as? BaseStoryListFragment)?.getInsetsLiveData()?.observe(viewLifecycleOwner,
                Observer { setWindowInsets(it) })
    }

    private fun setWindowInsets(insets: WindowInsets) {
        topInset = insets.systemWindowInsetTop
        layout.storyMenu.updatePadding(top = insets.systemWindowInsetTop)
        layout.authorInfo.updatePadding(bottom = authorInfoBottomPadding + insets.systemWindowInsetBottom)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnApplyWindowInsetsListener(view)
    }

    private fun setOnApplyWindowInsetsListener(view: View) {
        val authorInfoBottomPadding = layout.authorInfo.paddingBottom
        view.setOnApplyWindowInsetsListener { v, insets ->
            layout.storyMenu.updatePadding(top = insets.systemWindowInsetTop)
            layout.btnSearch.updatePadding(top = insets.systemWindowInsetTop)
            layout.authorInfo.updatePadding(bottom = authorInfoBottomPadding + insets.systemWindowInsetBottom)
            insets.consumeSystemWindowInsets()
        }
        view.requestApplyInsets()
    }

    override fun onDestroyView() {
        player.release()
        storyCommentsFragment = null
        layout.playerView.player = null
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        startPlaying()
        subscribeVideoProgress()
        InsetUtils.shouldRemovePaddings = false
    }

    override fun onPause() {
        super.onPause()
        stopPlaying()
        unsubscribeVideoProgress()
    }

    private fun subscribeStoryLiveData() {
        storyLiveData.observe(viewLifecycleOwner, Observer {
            Timber.tag("StoryTag").d("storyLiveData updated")
            setupView(it)
            prepareVideo(it)
        })
    }

    private fun setupView(story: FeedPost) {
        layout.model = story
        layout.tvDescription.setCollapsedText(story.getDescription())
        layout.tvDescription.visibility = if (TextUtils.isEmpty(story.getDescription())) View.GONE else View.VISIBLE
        layout.tvAddress.visibility = if (TextUtils.isEmpty(story.getAddress())) View.GONE else View.VISIBLE
        layout.playerView.player = player
        layout.audioStory.visibility = if (story.audio == null || story.audio?.url.isNullOrEmpty()) View.GONE else View.VISIBLE
        layout.tvAudioStory.text = story.audio?.description
        storiesViewModel.saveUnAuthorizedStoryId(story.post.id.toInt())
    }

    private var currentVideoUri: Uri? = null

    private fun prepareVideo(story: FeedPost) {
        val videoUri = story.post.media.first().origin
        if (videoUri == currentVideoUri) return

        currentVideoUri = videoUri
        val cacheDataSourceFactory = CacheDataSourceFactory(
                simpleCache,
                DefaultHttpDataSourceFactory(context?.let {
                    Util.getUserAgent(
                            it, getString(
                            R.string.app_name))
                }),
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(videoUri)

        player.prepare(LoopingMediaSource(mediaSource))
    }

    private fun subscribeVideoProgress() {
        videoProgressDisposable = observable
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .map { player.currentPosition }
                .subscribe {
                    val progressVideo = it.toFloat() / player.duration.toFloat() * 100
                    layout.storyProgressBar.setProgress(progressVideo)
                }
    }

    private fun unsubscribeVideoProgress() {
        videoProgressDisposable?.dispose()
        videoProgressDisposable = null
    }

    private fun setClickListeners() {
        layout.playerView.controllerAutoShow = false
        layout.playerView.useController = false

        layout.playerView.videoSurfaceView?.setOnClickListener { switchPlaying() }
        layout.btnPlay.setOnClickListener { startPlaying() }
        layout.btnMute.setOnClickListener { toggleAudio() }

        val showSignInOutButtonsListeners = View.OnClickListener {
            showSignInOutButtons()
        }

        layout.btnSearch.setOnClickListener(showSignInOutButtonsListeners)
        layout.avatar.setOnClickListener(showSignInOutButtonsListeners)
        layout.tvAuthorName.setOnClickListener(showSignInOutButtonsListeners)
        layout.tvDescription.setOnClickListener(showSignInOutButtonsListeners)
        layout.ivLike.setOnClickListener(showSignInOutButtonsListeners)
        layout.btnCreateStory.setOnClickListener(showSignInOutButtonsListeners)
        layout.ivFollowers.setOnClickListener(showSignInOutButtonsListeners)
        layout.ivComments.setOnClickListener(showSignInOutButtonsListeners)
        layout.btnMenu.setOnClickListener(showSignInOutButtonsListeners)
        layout.imageAudioStory.setOnClickListener(showSignInOutButtonsListeners)
        layout.tvAudioStory.setOnClickListener(showSignInOutButtonsListeners)

        layout.tvDescription.setOnClickListener { showSignInOutButtons() }
    }

    private fun showSignInOutButtons() {
        displaySignInButtonListener?.displaySignInButton(true)
    }



    private fun switchPlaying() {
        if (player.isPlaying) stopPlaying()
        else startPlaying()
    }

    private fun startPlaying() {
        player.playWhenReady = true
        layout.btnPlay.visibility = View.GONE
    }

    private fun stopPlaying() {
        player.playWhenReady = false
        layout.btnPlay.visibility = View.VISIBLE
    }

    private fun toggleAudio() {
        storiesViewModel.toggleAudio()
    }

    private fun subscribeViewModel() {
        storiesViewModel.isAudioEnabled.observe(viewLifecycleOwner, Observer { isEnabled ->
            if (isEnabled) {
                layout.btnMute.setImageResource(R.drawable.ic_unmuted)
                player.volume = 100f
            } else {
                layout.btnMute.setImageResource(R.drawable.ic_muted)
                player.volume = 0f
            }
        })
    }

}