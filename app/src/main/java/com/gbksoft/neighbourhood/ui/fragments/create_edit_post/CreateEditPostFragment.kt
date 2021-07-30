package com.gbksoft.neighbourhood.ui.fragments.create_edit_post

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentCreateEditPostBinding
import com.gbksoft.neighbourhood.domain.utils.textToString
import com.gbksoft.neighbourhood.model.crop.AspectRatio
import com.gbksoft.neighbourhood.model.crop.CropOptions
import com.gbksoft.neighbourhood.model.crop.CropResult
import com.gbksoft.neighbourhood.model.crop.CropSize
import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.profile.ProfileSearchItem
import com.gbksoft.neighbourhood.mvvm.ContextViewModelFactory
import com.gbksoft.neighbourhood.mvvm.result.ResultData
import com.gbksoft.neighbourhood.ui.dialogs.DateTimePicker
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.base.media.MediaProvider
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.MediaPagerAdapter
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.contract.MediaPagerHost
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.adapter.AudioAttachmentAdapter
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.background_publish.CreatePostHandler
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.background_publish.PostConstruct
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.component.AudioAttachmentBottomSheet
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.component.LayoutDelegate
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.component.PostMediaBottomSheet
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.component.RemoveBottomSheet
import com.gbksoft.neighbourhood.ui.fragments.crop.CropFragment
import com.gbksoft.neighbourhood.ui.fragments.neighbourhood.posts.MyNeighbourhoodFeedFragment
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description.component.HashtagTextWatcher
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description.component.MentionTextWatcher
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker.StoryMediaPickerFragment
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker.model.PostVideoMediaPickerResult
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker.model.StoryMediaPickerResult
import com.gbksoft.neighbourhood.ui.widgets.actionbar.ActionBarEvent
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.FileCreator
import com.gbksoft.neighbourhood.utils.KeyboardUtils
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.permission.DexterPermissionListener
import com.gbksoft.neighbourhood.utils.validation.ValidationField
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.tabs.TabLayoutMediator
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.DexterError
import io.reactivex.disposables.CompositeDisposable
import java.io.File

class CreateEditPostFragment : SystemBarsColorizeFragment(), MediaPagerHost, HashtagTextWatcher.OnHashtagChangedListener, MentionTextWatcher.OnMentionChangedListener {
    private val args by navArgs<CreateEditPostFragmentArgs>()
    private lateinit var layout: FragmentCreateEditPostBinding
    private lateinit var viewModel: CreateEditPostViewModel
    private lateinit var mediaAdapter: MediaPagerAdapter
    private lateinit var mediaProvider: MediaProvider
    private lateinit var hashtagTextWatcher: HashtagTextWatcher
    private lateinit var mentionTextWatcher: MentionTextWatcher
    private val hashtagAdapter by lazy { HashtagAdapter() }
    private val mentionAdapter by lazy { MentionAdapter() }

    private val disposables = CompositeDisposable()
    private var isAfterSearchClick = false
    private var shouldAppend = false

    private val allowedAudioFormats = arrayListOf("mp3", "aac", "wav", "m4a")

    private lateinit var player: SimpleExoPlayer

    private lateinit var audioAdapter: AudioAttachmentAdapter

    private val startDateTimePicker by lazy {
        val picker = DateTimePicker(requireContext())
        picker.onDateTimePicked = { viewModel.setStartDateTime(it) }
        picker
    }
    private val endDateTimePicker by lazy {
        val picker = DateTimePicker(requireContext())
        picker.onDateTimePicked = { viewModel.setEndDateTime(it) }
        picker
    }
    private val mediaBottomSheet by lazy {
        val bottomSheet = PostMediaBottomSheet.newInstance()
        bottomSheet.onSelectVideoFromGalleryClickListener = { requestStoragePermission(needVideo = 1) }
        bottomSheet.onSelectImageFromGalleryClickListener = { requestStoragePermission() }
        bottomSheet.onTakePhotoClickListener = { takePhoto() }
        bottomSheet.onMakeVideoClickListener = { makeVideo() }
        bottomSheet
    }

    private val removeBottomSheet by lazy {
        val bottomSheet = RemoveBottomSheet.newInstance<Media>()
        bottomSheet.onRemoveClickListener = { viewModel.removeMedia(it) }
        bottomSheet
    }

    private val audioAttachmentBottomSheet by lazy {
        val bottomSheet = AudioAttachmentBottomSheet.newInstance()
        bottomSheet.onSelectFromRecordingsClickListener = { selectAudioFromRecordings() }
        bottomSheet.onRecordVoiceMessageListener = { recordVoiceMessage() }
        bottomSheet.onStartAudioLiveStreamClickListener = { startAudioLiveStream() }
        bottomSheet
    }

    private val cropAspectRatio = AspectRatio(Constants.PIC_CROP_WIDTH_RATIO, Constants.PIC_CROP_HEIGHT_RATIO)
    private val cropMinSize = CropSize(Constants.PIC_CROP_MIN_WIDTH, Constants.PIC_CROP_MIN_HEIGHT)


    override fun getStatusBarColor(): Int = R.color.screen_foreground_color
    override fun getNavigationBarColor(): Int = R.color.screen_foreground_color

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(viewModelStore, ContextViewModelFactory(requireContext()))
                .get(CreateEditPostViewModel::class.java)
        viewModel.initByPost(args.post)

        CreateEditPostFragment.postConstruct?.let {
            viewModel.restorePostConstruct(it)
            CreateEditPostFragment.postConstruct = null
        }

        player = SimpleExoPlayer.Builder(requireContext()).build()

        audioAdapter = AudioAttachmentAdapter(requireContext(), player)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        hideNavigateBar()
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_create_edit_post, container, false)
        mediaProvider = MediaProvider(requireContext(), this)

        val layoutDelegate = LayoutDelegate(layout)
        layoutDelegate.setup(args.post)

        if (args.post.description.isNotEmpty()) {
            isAfterSearchClick = true
        }

        layout.root.forbidBackPress({
            val navOptionsBuilder = NavOptions.Builder()
            //navOptionsBuilder.setPopUpTo(R.id.main_graph, false)
            findNavController().navigate(R.id.myNeighbourhoodFragment, null, navOptionsBuilder.build())
        })
        setupView()
        setClickListeners()
        subscribeToViewModel()
        subscribeToFragmentResult()
        subscribeToImageVideoPickerResult()
        subscribeToCropResult()

        return layout.root
    }

    override fun getHideKeyboardOnTouchViews(): List<View> {
        return listOf(layout.scrollView)
    }

    override fun onKeyboardVisibilityChanged(isVisible: Boolean) {
        if (isVisible) {
            layout.buttonPanel.visibility = View.GONE
            layout.panelShadow.visibility = View.GONE
        } else {
            layout.buttonPanel.visibility = View.VISIBLE
            layout.panelShadow.visibility = View.VISIBLE
        }
    }

    private fun setupView() {
        layout.actionBar.setEventHandler { onActionBarEvent(it) }
        mediaAdapter = MediaPagerAdapter(requireContext(), true, isCreated = false)
        layout.addedMediaPager.adapter = mediaAdapter
        layout.addedMediaPager.isSaveEnabled = false
        TabLayoutMediator(layout.addedMediaDots, layout.addedMediaPager) { _, _ -> }.attach()
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

        val linearLayoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        layout.rwAudioAttach.layoutManager = linearLayoutManager

        audioAdapter.onHideAttachButton = { hideAudioAttachButton ->
            if (hideAudioAttachButton) {
                layout.btnAddAudioRecord.visibility = View.GONE
            } else {
                layout.btnAddAudioRecord.visibility = View.VISIBLE
            }
        }

        audioAdapter.onAudioRemoved = {
            viewModel.removeAudioMedia(it)
        }
    }

    private fun onActionBarEvent(event: ActionBarEvent) {
        when (event) {
            ActionBarEvent.CANCEL -> {
                //findNavController().popBackStack()
                val navOptionsBuilder = NavOptions.Builder()
                //navOptionsBuilder.setPopUpTo(R.id.main_graph, false)
                findNavController().navigate(R.id.myNeighbourhoodFragment, null, navOptionsBuilder.build())
            }
        }
    }

    private fun subscribeToViewModel() {
        viewModel.getControlState().observe(viewLifecycleOwner, Observer { updateControlsState(it) })
        viewModel.errorFields().observe(viewLifecycleOwner, Observer(layout::setErrors))
        viewModel.hashtags().observe(viewLifecycleOwner, Observer { onHashTagsFound(it) })
        viewModel.mentions().observe(viewLifecycleOwner, Observer { onMentionsFound(it) })
        viewModel.postMediaList().observe(viewLifecycleOwner, Observer { showMediaList(it) })
        viewModel.postCreate().observe(viewLifecycleOwner, Observer {
            val direction = CreateEditPostFragmentDirections.toMyNeighbourhoodFragment()
            findNavController().navigate(direction)
        })
        viewModel.postEdit().observe(viewLifecycleOwner, Observer {
            popBackStackWithResult(ResultData(PostResult.onEdited(it)))
        })
        viewModel.createPostLiveData.observe(viewLifecycleOwner, Observer {
            (getParentActivity() as? CreatePostHandler)?.createPost(it)
            MyNeighbourhoodFeedFragment.lastPostDescription = it.postModel.preparedDescription
            val direction = CreateEditPostFragmentDirections.toMyNeighbourhoodFragment()
            findNavController().navigate(direction)
            ToastUtils.showToastMessageLong(R.string.please_wait_your_post_is_publishing)
        })
        viewModel.editPostLiveData.observe(viewLifecycleOwner, Observer {
            (getParentActivity() as? CreatePostHandler)?.editPost(it)
            MyNeighbourhoodFeedFragment.lastPostDescription = it.postModel.preparedDescription
            val direction = CreateEditPostFragmentDirections.toMyNeighbourhoodFragment()
            findNavController().navigate(direction)
            ToastUtils.showToastMessageLong(R.string.please_wait_your_post_is_publishing)
        })

        viewModel.scrollToError.observe(viewLifecycleOwner, Observer { scrollToError(it) })
        layout.model = viewModel.postConstruct.postModel
    }

    private fun subscribeToFragmentResult() {

        setFragmentResultListener("audioRecordAttachment") { key, bundle ->
            val result = bundle.getString("audioRecordAttachment") ?: ""
            if (result.isNotEmpty() && audioAdapter.list.map { it.origin.toString() }.contains(result).not()) {

                val fileSize = File(result).length() / (1024L * 1024L)
                val sizeIsCorrect = fileSize <= 350

                if (sizeIsCorrect) {
                    val audio = Media.Audio.local(File(result))
                    audioAdapter.addItem(audio)
                    layout.rwAudioAttach.adapter = audioAdapter
                    viewModel.addAudioAttachment(audio)
                    CreateEditPostFragment.audioAttachmentCount++
              /*      val tempFile = FileCreator.fileFromContentUri(requireContext(), result.toUri())
                    viewModel.addAudioAttachmentFile()*/
                } else {
                    ToastUtils.showToastMessage(getString(R.string.audio_size_too_big))
                }
            }
        }
    }

    private fun subscribeToImageVideoPickerResult() {
        findNavController()
                .currentBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<ResultData<StoryMediaPickerResult>>(StoryMediaPickerFragment.SELECTED_MEDIA)
                ?.observe(viewLifecycleOwner, Observer(this::handleStoryMediaPickerResult))


        findNavController()
                .currentBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<ResultData<PostVideoMediaPickerResult>>(StoryMediaPickerFragment.SELECTED_VIDEO)
                ?.observe(viewLifecycleOwner, Observer(this::handlePostVideoMediaPickerResult))
    }

    private fun handleStoryMediaPickerResult(storyMediaPickerResult: ResultData<StoryMediaPickerResult>) {
        val selectedMedia = storyMediaPickerResult.consumeData()?.media ?: return
        selectedMedia.forEach {
            viewModel.addPictureAttachment(it.origin, it.previewArea!!)
        }
    }

    private fun handlePostVideoMediaPickerResult(storyMediaPickerResult: ResultData<PostVideoMediaPickerResult>) {
        val selectedMedia = storyMediaPickerResult.consumeData()?.video ?: return
        val uri = selectedMedia.origin
        viewModel.addVideoAttachment(uri)
    }


    private fun requestStoragePermission(needVideo: Int = -1) {
        val permissionListener = DexterPermissionListener()
        permissionListener.onPermissionGranted = {
            openImageMultipleImagesPicker(needVideo)
        }
        permissionListener.onPermissionToken = { it.continuePermissionRequest() }
        Dexter.withContext(context)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(permissionListener)
                .withErrorListener { error: DexterError ->
                    ToastUtils.showToastMessage(requireContext(), "Error occurred: $error")
                }
                .onSameThread()
                .check()
    }

    private var isMediaActionsEnabled = true

    @SuppressLint("ClickableViewAccessibility")
    private fun updateControlsState(stateMap: Map<Int, List<Boolean>>) {
        layout.btnPost.isClickable = controlStateIsActive(R.id.btnPost, stateMap)
        layout.addMedia.isClickable = controlStateIsActive(R.id.addMedia, stateMap)
        isMediaActionsEnabled = controlStateIsActive(R.id.addedMediaPager, stateMap)
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
            layout.searchDivider.visibility = View.GONE
        } else {
            layout.tvMentionsEmptyState.visibility = View.GONE
            layout.rvMentions.visibility = View.VISIBLE
            layout.llMentions.visibility = View.VISIBLE
            layout.searchDivider.visibility = View.VISIBLE
        }
    }

    private fun addMention(mention: ProfileSearchItem) {
        mentionTextWatcher.addMention(mention)
    }

    private fun showMediaList(mediaList: List<Media>) {
        mediaList.filter { it !is Media.Audio }.let { list ->
            mediaAdapter.setData(list.map { Pair(it, false) })
            val lastPosition = viewModel.currentMediaPage
            val lastCount = viewModel.lastMediaCount
            if (lastPosition == lastCount - 1) {
                layout.addedMediaPager.postDelayed({
                    layout.addedMediaPager.setCurrentItem(list.size - 1, false)
                }, 10)
            } else {
                layout.addedMediaPager.postDelayed({
                    layout.addedMediaPager.setCurrentItem(lastPosition, false)
                }, 10)
            }
            viewModel.lastMediaCount = list.size
            if (list.size == 1 && list[0] is Media.Video) {
                layout.addedMediaDots.visibility = View.INVISIBLE
            } else {
                layout.addedMediaDots.visibility = View.VISIBLE
            }
        }

        mediaList.filter { it is Media.Audio }.let { list ->
            if (list.isNotEmpty()) {
                viewModel.removeAllAudioFiles()
            }
            audioAdapter.clear()
            audioAdapter.setData(ArrayList(list as List<Media.Audio>))
            layout.rwAudioAttach.adapter = audioAdapter
            audioAdapter.list.forEach { fileUri ->
                viewModel.addAudioAttachment(fileUri)
            }
        }
    }

    override fun onStop() {
        viewModel.currentMediaPage = layout.addedMediaPager.currentItem
        player.playWhenReady = false
        super.onStop()
    }

    private fun popBackStackWithResult(result: ResultData<PostResult>) {
        findNavController()
                .previousBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<ResultData<PostResult>>(RESULT_POST)
                ?.value = result
        findNavController().popBackStack()
    }

    private fun scrollToError(error: ValidationField?) {
        val view = when (error) {
            ValidationField.NAME -> layout.tilName
            ValidationField.ADDRESS -> layout.tilAddress
            ValidationField.PRICE ->
                layout.tilPrice
            ValidationField.START_DATE_TIME -> layout.tilStartDateTime
            ValidationField.END_DATE_TIME -> layout.tilEndDateTime
            ValidationField.DESCRIPTION -> layout.tilDescription
            else -> return
        }
        layout.scrollView.smoothScrollTo(view.x.toInt(), view.y.toInt())
    }

    private fun setClickListeners() {
        layout.btnAddMedia.setOnClickListener { addMedia(viewModel.getAvailableMediaType()) }
        layout.addMedia.setOnClickListener { addMedia(viewModel.getAvailableMediaType()) }
        layout.btnHashtag.setOnClickListener { addHashSign() }
        layout.btnMention.setOnClickListener { addMentionSign() }
        hashtagAdapter.onItemClickListener = { addHashtag(it) }
        mentionAdapter.onProfileClickListener = { addMention(it) }
        layout.etAddress.setOnClickListener {
            KeyboardUtils.hideKeyboardWithClearFocus(layout.root, layout.addressTitle)
            openAddressPicker()
        }
        layout.etStartDateTime.setOnClickListener {
            startDateTimePicker.show(childFragmentManager, viewModel.getStartDateTime())
        }
        layout.etEndDateTime.setOnClickListener {
            endDateTimePicker.show(childFragmentManager, viewModel.getEndDateTime())
        }
        layout.btnPost.setOnClickListener {
            KeyboardUtils.hideKeyboard(it)

            val preparedDescription = mentionTextWatcher.prepareMentionsText()
            viewModel.setPreparedDescription(preparedDescription)
            viewModel.createEditPost()

            CreateEditPostFragment.postConstruct = null

        }
        layout.btnAddAudioRecord.setOnClickListener {
            addAudio()
        }
    }

    private fun addHashSign() {
        hashtagTextWatcher.addHashSign()
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
        layout.searchDivider.visibility = View.GONE
    }

    private fun addMentionSign() {
        mentionTextWatcher.addMentionSign()
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
        layout.searchDivider.visibility = View.GONE
    }

    private fun addMedia(availableMediaType: MediaProvider.Type?) {
        if (!isMediaActionsEnabled) return
        mediaBottomSheet.setAvailableMediaType(availableMediaType)
        mediaBottomSheet.show(childFragmentManager, "MediaBottomSheet")
    }

    override fun removeMedia(postMedia: Media) {
        if (!isMediaActionsEnabled) return
        removeBottomSheet.item = postMedia
        removeBottomSheet.show(childFragmentManager, "removeBottomSheet")
    }

    private fun addAudio() {
        audioAttachmentBottomSheet.show(childFragmentManager, "audioAttachmentBottomSheet")
    }

    override fun cropMedia(postMedia: Media) {
        val uri = (postMedia as? Media.Picture)?.origin
        cropImage(uri)
    }

    override fun onMediaClick(postMedia: Media) {
        if (!isMediaActionsEnabled) return
        when (postMedia) {
            is Media.Picture -> {
                val direction =
                        CreateEditPostFragmentDirections.toImagePreviewFragment(postMedia)
                findNavController().navigate(direction)
            }
            is Media.Video -> {
                val direction =
                        CreateEditPostFragmentDirections.toVideoPlayer(postMedia)
                findNavController().navigate(direction)
            }
        }
    }

    private fun selectMediaFromGallery(mediaType: MediaProvider.Type?) {
        when (mediaType) {
            MediaProvider.Type.VIDEO -> mediaProvider.requestVideoFromGallery()
            MediaProvider.Type.PICTURE -> mediaProvider.requestPictureFromGallery()
            else -> mediaProvider.requestPictureOrVideoFromGallery()
        }
    }

    private fun takePhoto() {
        mediaProvider.requestPictureFromCamera()
    }

    private fun makeVideo() {
        mediaProvider.requestVideoFromCamera()
    }

    private fun openAddressPicker() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME,
                Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS)
        val currentAddress = layout.etAddress.textToString()
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
            MediaProvider.REQUEST_FROM_CAMERA -> {
                handleFileAttachmentResponse(resultCode)
            }
            MediaProvider.REQUEST_FROM_GALLERY -> {
                handleUriAttachmentResponse(resultCode, data)
            }
            MediaProvider.REQUEST_FROM_SOUNDS -> {
                val data1 = data?.data
                if (data1 != null) {
                    val result = data.data.toString()
                    val type = requireContext().getFileExtension(result.toUri())
                    val fileSize = getFileSize(result.toUri())!! / (1024L * 1024L)
                    val sizeIsCorrect = fileSize <= 350
                    val typeIsCorrect = allowedAudioFormats.contains(type) || type == null
                    when {
                        typeIsCorrect && sizeIsCorrect -> {

                            val tempFile = FileCreator.fileFromContentUri(requireContext(), result.toUri())
                            val audio = Media.Audio.local(tempFile)
                            audioAdapter.addItem(audio)
                            layout.rwAudioAttach.adapter = audioAdapter
                            viewModel.addAudioAttachment(audio)
                        }
                        typeIsCorrect.not() -> {
                            ToastUtils.showToastMessage(getString(R.string.allowed_audio_files))
                        }
                        sizeIsCorrect.not() -> {
                            ToastUtils.showToastMessage(getString(R.string.audio_size_too_big))
                        }
                    }
                } else {
                    val clipData = data?.clipData

                    if (clipData != null) {
                        val count = clipData.itemCount
                        val list = arrayListOf<String>()
                        for (i in 0 until count) {
                            list.add(clipData.getItemAt(i).uri.toString())
                        }
                        var allTypesIsCorrect = true
                        var allSizesAreCorrect = true
                        list.forEach {
                            val type = requireContext().getFileExtension(it.toUri())

                            val fileSize = getFileSize(it.toUri())!! / (1024L * 1024L)
                            val sizeIsCorrect = fileSize <= 350
                            val typeIsCorrect = allowedAudioFormats.contains(type) || type == null

                            when {
                                typeIsCorrect && sizeIsCorrect -> {
                                    val file = FileCreator.fileFromContentUri(requireContext(), it.toUri())
                                    val audio = Media.Audio.local(file)
                                    audioAdapter.addItem(audio)
                                    viewModel.addAudioAttachment(audio)
                                }
                                typeIsCorrect.not() -> {
                                    allTypesIsCorrect = false
                                }
                                sizeIsCorrect.not() -> {
                                    allSizesAreCorrect = false
                                }
                            }
                        }
                        layout.rwAudioAttach.adapter = audioAdapter
                        if (allTypesIsCorrect.not()) {
                            ToastUtils.showToastMessage(getString(R.string.allowed_audio_files))
                        }

                        if (allSizesAreCorrect.not()) {
                            ToastUtils.showToastMessage(getString(R.string.audio_size_too_big))
                        }

                    }
                }
            }

        }
    }

    private val MEGABYTE = 1024L * 1024L

    fun bytesToMeg(bytes: Long): Long {
        return bytes / MEGABYTE
    }

    private fun handleFileAttachmentResponse(resultCode: Int) {
        if (resultCode != Activity.RESULT_OK) return
        if (mediaProvider.fetchFileContentType() == MediaProvider.Type.VIDEO) {
            val file = mediaProvider.fetchCameraFile() ?: return
            viewModel.addVideoAttachment(file.toUri())
        } else {
            cropImage(mediaProvider.fetchCameraFile()?.toUri())
        }
    }

    private fun handleUriAttachmentResponse(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return
        if (mediaProvider.fetchUriContentType(data) == MediaProvider.Type.VIDEO) {
            val uri = mediaProvider.fetchGalleryUri(data) ?: return
            viewModel.addVideoAttachment(uri)
        } else {
            cropImage(mediaProvider.fetchGalleryUri(data))
        }
    }

    private fun cropImage(uri: Uri?) {
        if (uri == null) return

        val cropOptions = CropOptions(uri, cropAspectRatio, cropMinSize)
        val direction = CreateEditPostFragmentDirections.toCropImage(cropOptions)

        findNavController().navigate(direction)
    }

    private fun subscribeToCropResult() {
        findNavController().currentBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<ResultData<CropResult>>(CropFragment.CROP_RESULT)
                ?.observe(viewLifecycleOwner, Observer { handleCropResult(it) })
    }

    private fun handleCropResult(resultData: ResultData<CropResult>) {
        if (resultData.notContainsData())
            return
        val cropResult = resultData.consumeData() ?: return
        viewModel.addPictureAttachment(cropResult.cropPicture, cropResult.cropArea)
    }

    private fun openImageMultipleImagesPicker(needVideoAttach: Int = 0) {
        val direction = CreateEditPostFragmentDirections.toImageVideoPicker(true).apply { needVideo = needVideoAttach }
        findNavController().navigate(direction)
    }

    private fun selectAudioFromRecordings() {
        mediaProvider.requestAudioFromRecords()
    }

    private fun recordVoiceMessage() {
        val preparedDescription = mentionTextWatcher.prepareMentionsText()
        val postConstruct = viewModel.postConstruct
        postConstruct.postModel.preparedDescription = preparedDescription

        Log.d("recordrere", viewModel.createPostLiveData.value.toString())
        Log.d("recordrere", postConstruct.postMediaList.size.toString())

        CreateEditPostFragment.postConstruct = postConstruct
        val direction =
                CreateEditPostFragmentDirections.toAudioRecordFragment()
        findNavController().navigate(direction)
    }

    private fun startAudioLiveStream() {}

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }

    fun Context.getFileExtension(uri: Uri): String? = when (uri.scheme) {
        // get file extension
        ContentResolver.SCHEME_FILE -> File(uri.path!!).extension
        // get actual name of file
        //ContentResolver.SCHEME_FILE -> File(uri.path!!).name
        ContentResolver.SCHEME_CONTENT -> getCursorContent(uri)
        else -> null
    }

    private fun Context.getCursorContent(uri: Uri): String? = try {
        contentResolver.query(uri, null, null, null, null)?.let { cursor ->
            cursor.run {
                val mimeTypeMap: MimeTypeMap = MimeTypeMap.getSingleton()
                if (moveToFirst()) mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
                // case for get actual name of file
                //if (moveToFirst()) getString(getColumnIndex(OpenableColumns.DISPLAY_NAME))
                else null
            }.also { cursor.close() }
        }
    } catch (e: Exception) {
        null
    }

    private fun getFileSize(fileUri: Uri): Long? {
        val returnCursor = requireContext().getContentResolver().query(fileUri, null, null, null, null)
        val sizeIndex = returnCursor?.getColumnIndex(OpenableColumns.SIZE)
        returnCursor?.moveToFirst()
        return returnCursor?.getLong(sizeIndex!!)
    }

    companion object {
        private const val AUTOCOMPLETE_REQUEST_CODE = 1
        const val RESULT_POST = "result_post"

        var audioAttachmentCount: Int = 0
        var postConstruct: PostConstruct? = null
        var filesMap = hashMapOf<String, String>()
    }
}

fun View.forbidBackPress(callback: () -> Unit) {
    isFocusableInTouchMode = true
    requestFocus()

    setOnKeyListener(object : View.OnKeyListener {
        override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    callback.invoke()
                    return true
                }
            }
            return false
        }
    })

}