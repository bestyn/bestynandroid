package com.gbksoft.neighbourhood.ui.fragments.business_profile.tabs

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentTabBusinessImagesBinding
import com.gbksoft.neighbourhood.model.crop.AspectRatio
import com.gbksoft.neighbourhood.model.crop.CropOptions
import com.gbksoft.neighbourhood.model.crop.CropResult
import com.gbksoft.neighbourhood.model.crop.CropSize
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.mvvm.ContextViewModelFactory
import com.gbksoft.neighbourhood.mvvm.result.ResultData
import com.gbksoft.neighbourhood.ui.dialogs.YesNoDialog
import com.gbksoft.neighbourhood.ui.fragments.base.BaseFragment
import com.gbksoft.neighbourhood.ui.fragments.base.media.MediaProvider
import com.gbksoft.neighbourhood.ui.fragments.business_profile.adapter.BusinessImagesAdapter
import com.gbksoft.neighbourhood.ui.fragments.business_profile.component.BusinessMediaBottomSheet
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.component.RemoveBottomSheet
import com.gbksoft.neighbourhood.ui.fragments.crop.CropFragment
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.GridDividerItemDecoration
import com.gbksoft.neighbourhood.utils.KeyboardUtils
import com.gbksoft.neighbourhood.utils.LastVisiblePositionChangeListener

class BusinessImagesFragment : BaseFragment() {
    private lateinit var viewModel: BusinessImagesViewModel
    private lateinit var layout: FragmentTabBusinessImagesBinding
    private lateinit var adapter: BusinessImagesAdapter
    private val pictureProvider by lazy { MediaProvider(requireContext(), this) }

    private val mediaBottomSheet by lazy {
        val bottomSheet = BusinessMediaBottomSheet.newInstance()
        bottomSheet.onTakePhotoClickListener = { pictureProvider.requestPictureFromCamera() }
        bottomSheet.onSelectFromGalleryClickListener = { pictureProvider.requestPictureFromGallery() }
        bottomSheet
    }
    private val removeBottomSheet by lazy {
        val bottomSheet = RemoveBottomSheet.newInstance<Media.Picture>()
        bottomSheet.onRemoveClickListener = { removeImage(it) }
        bottomSheet
    }

    private val lastVisiblePositionListener: RecyclerView.OnScrollListener = object : LastVisiblePositionChangeListener() {
        override fun lastVisiblePositionChanged(lastVisibleItemPosition: Int) {
            viewModel.onVisibleItemChanged(lastVisibleItemPosition)
        }
    }

    private val cropAspectRatio = AspectRatio(Constants.PIC_CROP_WIDTH_RATIO, Constants.PIC_CROP_HEIGHT_RATIO)
    private val cropMinSize = CropSize(Constants.PIC_CROP_MIN_WIDTH, Constants.PIC_CROP_MIN_HEIGHT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, ContextViewModelFactory(requireContext()))
            .get(BusinessImagesViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_tab_business_images,
            container, false)

        setupView()
        subscribeToViewModel()
        subscribeToCropResult()
        reloadImages()
        return layout.root
    }

    private fun setupView() {
        adapter = BusinessImagesAdapter()
        adapter.onImageClickListener = { image, position -> openImage(image, position) }
        adapter.onAddImageClickListener = { addImage() }
        adapter.onRemoveImageClickListener = { pic, position -> onRemoveClick(pic) }
        val spacing = resources.getDimensionPixelSize(R.dimen.business_image_spacing)
        val columnsCount = resources.getInteger(R.integer.business_images_columns_count)
        val divider = GridDividerItemDecoration(spacing, columnsCount)
        layout.rvImages.addItemDecoration(divider)
        layout.rvImages.adapter = adapter
        layout.rvImages.addOnScrollListener(lastVisiblePositionListener)
    }

    private fun reloadImages() {
        viewModel.clear()
        viewModel.loadAlbum()
    }

    private fun openImage(pic: Media.Picture, position: Int) {
        (parentFragment as? ShowInAlbumHandler)?.let {
            it.showInAlbum(pic, position)
        }
    }

    private fun addImage() {
        mediaBottomSheet.show(childFragmentManager, "MediaBottomSheet")
    }

    private fun onRemoveClick(pic: Media.Picture) {
        removeBottomSheet.item = pic
        removeBottomSheet.show(childFragmentManager, "RemoveBottomSheet")
    }

    private fun removeImage(pic: Media.Picture) {
        showDeleteImageDialog(pic)
    }

    private fun subscribeToViewModel() {
        viewModel.images().observe(viewLifecycleOwner, Observer {
            adapter.setData(it)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        KeyboardUtils.hideKeyboard(getParentActivity())
        if (resultCode == Activity.RESULT_OK) when (requestCode) {
            MediaProvider.REQUEST_FROM_CAMERA -> {
                cropImage(pictureProvider.fetchCameraFile()?.toUri())
            }
            MediaProvider.REQUEST_FROM_GALLERY -> {
                cropImage(pictureProvider.fetchGalleryUri(data))
            }
        }
    }

    private fun cropImage(uri: Uri?) {
        if (uri == null) return

        val cropOptions = CropOptions(uri, cropAspectRatio, cropMinSize)
        (parentFragment as? OpenCropHandler)?.let {
            it.openCrop(cropOptions)
        }
    }

    private fun subscribeToCropResult() {
        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<ResultData<CropResult>>(CropFragment.CROP_RESULT)
            ?.observe(viewLifecycleOwner, Observer { handleCropResult(it) })
    }

    private fun handleCropResult(resultData: ResultData<CropResult>) {
        if (resultData.notContainsData()) return
        val cropResult = resultData.consumeData() ?: return
        viewModel.uploadMediaPost(cropResult.cropPicture, cropResult.cropArea)
    }

    private fun showDeleteImageDialog(picture: Media.Picture) {
        val builder = YesNoDialog.Builder()
            .setNegativeButton(R.string.delete_image_dialog_no, null)
            .setPositiveButton(R.string.delete_image_dialog_yes) { viewModel.removePicture(picture) }
            .setMessage(R.string.delete_image_dialog_msg)
            .setTitle(R.string.delete_image_dialog_title)
            .setCanceledOnTouchOutside(true)

        builder.build().show(childFragmentManager, "DeletePostDialog")
    }
}