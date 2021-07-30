package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.app.NApplication
import com.gbksoft.neighbourhood.databinding.FragmentStoryDescriptionBinding
import com.gbksoft.neighbourhood.domain.utils.not
import com.gbksoft.neighbourhood.domain.utils.textToString
import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.profile.ProfileSearchItem
import com.gbksoft.neighbourhood.model.story.creating.StorySource
import com.gbksoft.neighbourhood.model.story.creating.VideoSegment
import com.gbksoft.neighbourhood.mvvm.result.ResultData
import com.gbksoft.neighbourhood.mvvm.result.ResultInt
import com.gbksoft.neighbourhood.ui.dialogs.YesNoDialog
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.CreateEditPostFragment
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.HashtagAdapter
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.MentionAdapter
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.PostResult
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.StoryBuilder
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.StoryBuilderUtil
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.adjust_story.VideoSegmentsManager
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description.component.HashtagTextWatcher
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description.component.MentionTextWatcher
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.select_cover.SelectCoverFragment
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.KeyboardUtils
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.io.File


class StoryDescriptionFragment : SystemBarsColorizeFragment(), HashtagTextWatcher.OnHashtagChangedListener, MentionTextWatcher.OnMentionChangedListener {

    private lateinit var layout: FragmentStoryDescriptionBinding
    private val viewModel by viewModel<StoryDescriptionViewModel> {
        parametersOf(args.story)
    }
    private val args by navArgs<StoryDescriptionFragmentArgs>()
    private val coverImageRequestOptions: RequestOptions by lazy {
        val radius = resources.getDimensionPixelSize(R.dimen.story_cover_image_corner)
        RequestOptions().transform(CenterCrop(), RoundedCorners(radius))
    }
    private lateinit var hashtagTextWatcher: HashtagTextWatcher
    private lateinit var mentionTextWatcher: MentionTextWatcher
    private val hashtagAdapter by lazy { HashtagAdapter() }
    private val mentionAdapter by lazy { MentionAdapter() }
    private var shouldAppend = false

    override fun getStatusBarColor() = R.color.screen_foreground_color
    override fun getNavigationBarColor() = R.color.screen_foreground_color

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_story_description, container, false)
        hideNavigateBar()

        setupView()
        setClickListeners()
        subscribeToResult()
        subscribeToViewModel()
        return layout.root
    }

    private fun setupView() {
        layout.model = viewModel.storyModel
        layout.actionBar.setTitle(if (viewModel.isCreationMode) {
            R.string.title_add_story_description
        } else {
            R.string.title_edit_story_description
        })
        if (viewModel.not { isCreationMode }) {
            layout.btnSelectCover.visibility = View.GONE
        }
        hashtagTextWatcher = HashtagTextWatcher(layout.etDescription, this).apply {
            setHashtagColor(requireContext(), R.color.post_hashtag_color)
        }
        mentionTextWatcher = MentionTextWatcher(layout.etDescription, this).apply {
            setMentionColor(requireContext(), R.color.post_hashtag_color)
        }
        layout.etDescription.addTextChangedListener(hashtagTextWatcher)
        layout.etDescription.addTextChangedListener(mentionTextWatcher)
        layout.rvHashtags.layoutManager = LinearLayoutManager(requireContext())
        layout.rvHashtags.adapter = hashtagAdapter
        layout.rvMentions.layoutManager = LinearLayoutManager(requireContext())
        layout.rvMentions.adapter = mentionAdapter

        showStoryCover()
        viewModel.storyModel.posterTimestamp.addOnPropertyChangedCallback(
                object : Observable.OnPropertyChangedCallback() {
                    override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                        showStoryCover()
                    }
                })
    }

    private fun showStoryCover() {
        if (viewModel.storyModel.posterUrl.get() != null) {
            loadCoverImage(viewModel.storyModel.posterUrl.get())
            return
        }
        val posterFrameTimestamp = viewModel.storyModel.posterTimestamp.get() ?: return
        when (args.story.source) {
            StorySource.FROM_CAMERA,
            StorySource.FROM_GALLERY -> {
                val videoSegments = args.story.videoSegments ?: return
                loadCoverImage(videoSegments, posterFrameTimestamp)
            }
            StorySource.FROM_TEXT_STORY -> {
                val backgroundResId = args.story.background?.backgroundResId ?: return
                loadCoverImage(backgroundResId, posterFrameTimestamp)
            }
            StorySource.FROM_DUET -> {
                val originalVideoUri = args.story.duetOriginalVideoUri ?: return
                val cameraVideoSegments = args.story.videoSegments ?: return
                loadCoverImage(originalVideoUri, cameraVideoSegments, posterFrameTimestamp)
            }
        }
    }

    private fun loadCoverImage(poster: Uri?) {
        Glide.with(layout.ivStoryCover)
                .load(poster)
                .apply(coverImageRequestOptions)
                .into(layout.ivStoryCover)
    }

    private fun loadCoverImage(videoSegments: List<VideoSegment>, posterFrameTimestamp: Long) {
        val videoSegmentsManager = VideoSegmentsManager().apply {
            setVideoSegments(videoSegments)
        }

        val pos = videoSegmentsManager.getVideoSegmentPositionByTotalTime(posterFrameTimestamp.toInt())
        val time = videoSegmentsManager.getTimeInVideoSegmentByTotalTime(pos, posterFrameTimestamp.toInt())
        val videoSegment = videoSegments[pos]
        Glide.with(layout.ivStoryCover)
                .load(videoSegment.uri.toString())
                .thumbnail()
                .apply(coverImageRequestOptions.frame(time * 1000L))
                .into(layout.ivStoryCover)
    }

    private fun loadCoverImage(backgroundResId: Int, posterFrameTimestamp: Long) {
        val storyBuilder = StoryBuilder(NApplication.context, args.story)
        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.Default) {
                val bitmap = BitmapFactory.decodeResource(context?.resources, backgroundResId)
                storyBuilder.getBitmapFromTextStory(bitmap, posterFrameTimestamp.toInt())
            }
            Glide.with(layout.ivStoryCover)
                    .load(bitmap)
                    .apply(coverImageRequestOptions)
                    .into(layout.ivStoryCover)
        }
    }

    private fun loadCoverImage(originalVideoUri: Uri, cameraVideoSegments: List<VideoSegment>, posterFrameTimestamp: Long) {
        val videoSegmentsManager = VideoSegmentsManager().apply {
            setVideoSegments(cameraVideoSegments)
        }

        val pos = videoSegmentsManager.getVideoSegmentPositionByTotalTime(posterFrameTimestamp.toInt())
        val time = videoSegmentsManager.getTimeInVideoSegmentByTotalTime(pos, posterFrameTimestamp.toInt())
        val videoSegment = cameraVideoSegments[pos]

        lifecycleScope.launch {
            val originalVideoBitmap = withContext(Dispatchers.Default) { StoryBuilderUtil.getFrameFromVideo(originalVideoUri, posterFrameTimestamp) }
            val cameraVideoBitmap = withContext(Dispatchers.Default) { StoryBuilderUtil.getFrameFromVideo(videoSegment.uri, time.toLong()) }
            val scaledOriginalVideoBitmap = Bitmap.createScaledBitmap(originalVideoBitmap, 270, 480, false)
            val scaledCameraVideoBitmap = Bitmap.createScaledBitmap(cameraVideoBitmap, 270, 480, false)
            val duetBitmap = StoryBuilderUtil.combineDuetBitmaps(scaledOriginalVideoBitmap, scaledCameraVideoBitmap)

            Glide.with(layout.ivStoryCover)
                    .load(duetBitmap)
                    .apply(coverImageRequestOptions)
                    .into(layout.ivStoryCover)
        }

    }

    private fun setClickListeners() {
        layout.etLocation.setOnClickListener {
            KeyboardUtils.hideKeyboardWithClearFocus(layout.root, layout.labelLocation)
            openAddressPicker()
        }
        layout.btnHashtag.setOnClickListener { addHashSign() }
        layout.btnMention.setOnClickListener { addMentionSign() }
        hashtagAdapter.onItemClickListener = { addHashtag(it) }
        mentionAdapter.onProfileClickListener = { addMention(it) }
        layout.btnSelectCover.setOnClickListener { selectCover() }
        layout.btnPost.setOnClickListener { postStory() }
    }

    override fun handleOnBackPressed(): Boolean {
        return if (!viewModel.isCreationMode && viewModel.hasEditingChanges()) {
            showConfirmCancelEditingDialog()
            true
        } else {
            super.handleOnBackPressed()
        }
    }

    private fun showConfirmCancelEditingDialog() {
        val builder = YesNoDialog.Builder()
                .setMessage(R.string.cancel_story_editing_dialog)
                .setNegativeButton(R.string.cancel_story_editing_dialog_cancel, null)
                .setPositiveButton(R.string.cancel_story_editing_dialog_ok) {
                    findNavController().popBackStack()
                }
                .setCanceledOnTouchOutside(true)
        builder.build().show(childFragmentManager, "ConfirmCancelEditingDialog")
    }

    private fun openAddressPicker() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME,
                Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS)
        val currentAddress = layout.etLocation.textToString()
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setInitialQuery(currentAddress)
                .build(requireContext())
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        KeyboardUtils.hideKeyboard(getParentActivity())
        when (requestCode) {
            AUTOCOMPLETE_REQUEST_CODE -> {
                viewModel.handleAddressResponse(resultCode, data)
            }
        }
    }

    private fun addHashSign() {
        hashtagTextWatcher?.addHashSign()
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
        layout.llHashtags.visibility = View.GONE
    }

    private fun addMentionSign() {
        mentionTextWatcher?.addMentionSign()
    }

    override fun onNewMention() {
        viewModel.searchMentions(null)
    }

    override fun onMentionChanged(mention: String) {
        viewModel.searchMentions(mention)
    }

    override fun onMentionEnd() {
        viewModel.cancelMentionsSearching()
        layout.rvMentions.visibility = View.GONE
        layout.llMentions.visibility = View.GONE
    }

    private fun selectCover() {
        val currentCoverTimestamp = viewModel.storyModel.posterTimestamp.get()
                ?: Constants.STORY_DEFAULT_COVER_TIMESTAMP
        val direction = StoryDescriptionFragmentDirections.toSelectCover(args.story, currentCoverTimestamp)
        findNavController().navigate(direction)
    }

    private fun postStory() {
        KeyboardUtils.hideKeyboard(layout.root)
        layout.switchAllowDuet.isClickable = false
        layout.btnHashtag.isClickable = false
        layout.btnPost.isClickable = false

        val preparedDescription = mentionTextWatcher.prepareMentionsText()
        viewModel.setPreparedDescription(preparedDescription)
        viewModel.postStory()
    }

    private fun subscribeToResult() {
        findNavController().currentBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<ResultInt>(SelectCoverFragment.RESULT_COVER_TIMESTAMP)
                ?.observe(viewLifecycleOwner, Observer { handleCoverTimestampResult(it) })
    }

    private fun handleCoverTimestampResult(result: ResultInt?) {
        result ?: return
        val timestamp = result.consumeData() ?: return
        viewModel.storyModel.posterTimestamp.set(timestamp.toLong())
    }

    private fun subscribeToViewModel() {
        viewModel.foundHashtags.observe(viewLifecycleOwner, Observer { onHashTagsFound(it) })
        viewModel.foundMentions.observe(viewLifecycleOwner, Observer { onMentionsFound(it) })
        viewModel.errorFields.observe(this.viewLifecycleOwner, Observer { handleErrorFields(it) })
        viewModel.storyUpdated.observe(viewLifecycleOwner, Observer { onStoryUpdated(it) })
        viewModel.editingCanceled.observe(viewLifecycleOwner, Observer {
            popBackStackWithoutResult()
        })
        viewModel.createStory.observe(viewLifecycleOwner, Observer {
            (getParentActivity() as? CreateStoryHandler)?.createStory(args.story, it)
            ToastUtils.showToastMessageLong(R.string.your_story_is_publishing)
            nevigateToStories()
        })
    }

    private fun popBackStackWithoutResult() {
        findNavController().popBackStack()
    }

    private fun onHashTagsFound(hashtags: List<Hashtag>) {
        shouldAppend = false
        if (hashtags.isEmpty()) {
            layout.rvHashtags.visibility = View.GONE
            layout.llHashtags.visibility = View.GONE
        } else {
            layout.rvHashtags.visibility = View.VISIBLE
            layout.llHashtags.visibility = View.VISIBLE
            hashtagAdapter.setData(hashtags)
        }
    }

    private fun addHashtag(hashtag: Hashtag) {
        hashtagTextWatcher.addHashtag(hashtag.name)
    }

    private fun onMentionsFound(mentions: List<ProfileSearchItem>) {
        mentionAdapter.setData(mentions)
        if (mentions.isEmpty()) {
            layout.tvMentionsEmptyState.visibility = View.VISIBLE
            layout.rvMentions.visibility = View.GONE
            layout.llMentions.visibility = View.GONE
        } else {
            layout.tvMentionsEmptyState.visibility = View.GONE
            layout.rvMentions.visibility = View.VISIBLE
            layout.llMentions.visibility = View.VISIBLE
        }
    }

    private fun addMention(mention: ProfileSearchItem) {
        mentionTextWatcher.addMention(mention)
    }

    private fun handleErrorFields(errorFieldsModel: ErrorFieldsModel) {
        layout.errors = errorFieldsModel
    }

    private fun nevigateToStories() {
        val direction = StoryDescriptionFragmentDirections.toStoryList()
        findNavController().navigate(direction)
    }

    private fun onStoryUpdated(feedPost: FeedPost) {
        ToastUtils.showToastMessage(R.string.toast_story_updated)
        popBackStackWithResult(ResultData(PostResult.onEdited(feedPost)))
    }

    private fun popBackStackWithResult(resultData: ResultData<PostResult>) {
        findNavController()
                .previousBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<ResultData<PostResult>>(CreateEditPostFragment.RESULT_POST)
                ?.value = resultData
        findNavController().popBackStack()
    }

    companion object {
        private const val AUTOCOMPLETE_REQUEST_CODE = 1
    }
}