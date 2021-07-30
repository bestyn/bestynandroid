package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker

import android.graphics.Rect
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentImageVideoPickerBinding
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.story.creating.ConstructStory
import com.gbksoft.neighbourhood.model.story.creating.VideoSegment
import com.gbksoft.neighbourhood.mvvm.result.ResultData
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker.choose_folder.ChooseFolderBottomSheet
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker.model.PostVideoMediaPickerResult
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker.model.StoryMedia
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker.model.StoryMediaPickerResult
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker.select_media.MediaPickerTab
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker.select_media.SelectedMediaAdapter
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker.select_media.StoryMediaAdapter
import com.gbksoft.neighbourhood.utils.GridDividerItemDecoration
import com.gbksoft.neighbourhood.utils.ToastUtils
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class StoryMediaPickerFragment : SystemBarsColorizeFragment() {
    private val args by navArgs<StoryMediaPickerFragmentArgs>()
    private val viewModel: StoryMediaPickerViewModel by viewModel {
        parametersOf(args.isPost, args.needVideo)
    }
    private lateinit var layout: FragmentImageVideoPickerBinding
    private lateinit var storyMediaAdapter: StoryMediaAdapter
    private lateinit var selectedMediaAdapter: SelectedMediaAdapter
    private var isPost = false

    private val chooseFolderBottomSheet by lazy {
        ChooseFolderBottomSheet().apply { onItemClickListener = ::onFolderSelected }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_image_video_picker, container, false)
        isPost = args.isPost
        if (args.needVideo == 1) layout.filterVideo.performClick()
        setupView()
        setClickListeners()
        subscribeToViewModel()
        return layout.root
    }

    private fun setupView() {
        setupMediaAdapter()
        setupSelectedMediaAdapter()
    }

    private fun setupMediaAdapter() {
        storyMediaAdapter = StoryMediaAdapter(isPost)
        layout.rvMedia.adapter = storyMediaAdapter
        layout.rvMedia.layoutManager = GridLayoutManager(requireContext(), 3)

        val topPadding = resources.getDimensionPixelSize(R.dimen.media_picker_media_list_padding_top)
        val spacing = resources.getDimensionPixelSize(R.dimen.business_image_spacing)
        val columnsCount = resources.getInteger(R.integer.business_images_columns_count)
        val divider = GridDividerItemDecoration(spacing, columnsCount, topPadding)
        layout.rvMedia.addItemDecoration(divider)
    }

    private fun setupSelectedMediaAdapter() {
        selectedMediaAdapter = SelectedMediaAdapter()
        layout.rvSelectedMedia.adapter = selectedMediaAdapter
        layout.rvSelectedMedia.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        val spacing = resources.getDimensionPixelSize(R.dimen.media_picker_selected_media_image_spacing)
        val divider = object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val itemPosition = (view.layoutParams as RecyclerView.LayoutParams).absoluteAdapterPosition
                if (itemPosition > 0) {
                    outRect.left = spacing
                }
            }
        }
        layout.rvSelectedMedia.addItemDecoration(divider)
    }

    private fun subscribeToViewModel() {
        viewModel.storyMedia.observe(viewLifecycleOwner, Observer { onStoryMediaLoaded(it) })
        viewModel.selectedStoryMedia.observe(viewLifecycleOwner, Observer { onSelectedMediaChanged(it) })
        viewModel.folders.observe(viewLifecycleOwner, Observer { onFoldersLoaded(it) })
        viewModel.preparedSelectedVideoSegments.observe(viewLifecycleOwner, Observer { onSelectedVideoSegmentsPrepared(it) })
        viewModel.preparedSelectedPictures.observe(viewLifecycleOwner, Observer { onSelectedPicturesPrepared(it) })
        viewModel.preparedVideoAttachment.observe(viewLifecycleOwner, Observer { onSelectedVideoPrepared(it) })
    }

    private fun setClickListeners() {
        storyMediaAdapter.selectMediaClickListener = { storyMedia ->
            if (args.isPost && viewModel.selectedStoryMedia.value?.firstOrNull { it.mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE } != null && storyMedia.mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                ToastUtils.showToastMessage(R.string.you_can_select_5_images_or_1_video)
            } else  if (args.isPost && viewModel.selectedStoryMedia.value?.firstOrNull { it.mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO } != null && storyMedia.mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE){
                ToastUtils.showToastMessage(R.string.you_can_select_5_images_or_1_video)
            } else if (args.isPost && viewModel.selectedStoryMedia.value?.size == 5 && viewModel.selectedStoryMedia.value!![0].mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                ToastUtils.showToastMessage(R.string.you_have_marked_maximum)
            } else if (args.isPost && viewModel.selectedStoryMedia.value?.size == 1 && viewModel.selectedStoryMedia.value!![0].mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                ToastUtils.showToastMessage(R.string.you_have_marked_maximum)
            } else {
                viewModel.selectMedia(storyMedia)
            }
        }
        storyMediaAdapter.unselectMediaClickListener = {
            val media = it.clone()
            media.number = -1
            viewModel.unselectMedia(media)
        }
        selectedMediaAdapter.onRemoveMediaClickListener = {
            viewModel.unselectMedia(it)
        }
        layout.cgFilters.setOnCheckedChangeListener { _, checkedId ->
            onFilterChanged(checkedId)
        /*    if (args.needVideo == -1) {
                onFilterChanged(checkedId)
            }*/
        }
        layout.tvFolder.setOnClickListener {
            chooseFolderBottomSheet.show(childFragmentManager, "ChooseFolderBottomSheet")
        }
        layout.ivFolderArrow.setOnClickListener {
            chooseFolderBottomSheet.show(childFragmentManager, "ChooseFolderBottomSheet")
        }
        layout.tvDone.setOnClickListener {
            val validationRes = viewModel.prepareSelectedMedia()
            layout.progressBar.visibility = if (validationRes) View.VISIBLE else View.GONE
        }
        layout.tvCancel.setOnClickListener {
            findNavController().popBackStack()
        }

    }

    private fun onStoryMediaLoaded(storyMedia: List<StoryMedia>) {
        storyMediaAdapter.setData(storyMedia)
    }

    private fun onSelectedMediaChanged(selectedStoryMediaList: List<StoryMedia>) {
        selectedMediaAdapter.setData(selectedStoryMediaList)
        layout.rvSelectedMedia.visibility = if (selectedStoryMediaList.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun onFoldersLoaded(folders: List<String>) {
        chooseFolderBottomSheet.folderList = folders
    }

    private fun onFolderSelected(folderPath: String) {
        viewModel.onFolderChanged(folderPath)
        val folderName = folderPath.substring(folderPath.lastIndexOf('/') + 1)
        layout.tvFolder.text = folderName
    }

    private fun onFilterChanged(checkedId: Int) {
        when (checkedId) {
            R.id.filterAll -> viewModel.updateTab(MediaPickerTab.ALL)
            R.id.filterImages -> viewModel.updateTab(MediaPickerTab.IMAGES)
            R.id.filterVideo -> viewModel.updateTab(MediaPickerTab.VIDEOS)
        }
    }

    private fun onSelectedVideoSegmentsPrepared(preparedSelectedMedia: List<VideoSegment>) {
        layout.progressBar.visibility = View.GONE
        val constructStory = ConstructStory.fromGallery(preparedSelectedMedia)
        navigateToAdjustStory(constructStory)
    }

    private fun onSelectedPicturesPrepared(pictures: List<Media.Picture>) {
        layout.progressBar.visibility = View.GONE
        popBackStackWithResult(pictures)
    }

    private fun onSelectedVideoPrepared(video: Media.Video?) {
        layout.progressBar.visibility = View.GONE
        video?.let {
            val result = ResultData(PostVideoMediaPickerResult(video))
            findNavController()
                    .previousBackStackEntry
                    ?.savedStateHandle
                    ?.getLiveData<ResultData<PostVideoMediaPickerResult>>(SELECTED_VIDEO)
                    ?.value = result
            findNavController().popBackStack()
        }
    }

    private fun navigateToAdjustStory(constructStory: ConstructStory) {
        val direction = StoryMediaPickerFragmentDirections.toAdjustStory(constructStory, false)
        findNavController().navigate(direction)
    }

    private fun popBackStackWithResult(media: List<Media.Picture>) {
        val result = ResultData(StoryMediaPickerResult(media))
        findNavController()
                .previousBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<ResultData<StoryMediaPickerResult>>(SELECTED_MEDIA)
                ?.value = result
        findNavController().popBackStack()
    }

    companion object {
        const val SELECTED_MEDIA = "selected_media"
        const val SELECTED_VIDEO = "selected_video"
    }
}