package com.gbksoft.neighbourhood.ui.fragments.crop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentCropBinding
import com.gbksoft.neighbourhood.model.crop.AspectRatio
import com.gbksoft.neighbourhood.model.crop.CropResult
import com.gbksoft.neighbourhood.model.crop.CropSize
import com.gbksoft.neighbourhood.mvvm.result.ResultData
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import timber.log.Timber


class CropFragment : SystemBarsColorizeFragment() {
    companion object {
        const val CROP_RESULT = "crop_result"
    }

    private val args by navArgs<CropFragmentArgs>()
    private lateinit var layout: FragmentCropBinding
    private lateinit var imageCropper: ImageCropper

    override fun getStatusBarColor(): Int = R.color.image_cropper_system_bars_color
    override fun getNavigationBarColor(): Int = R.color.image_cropper_system_bars_color

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_crop, container, false)

        val uri = args.cropOptions.source
        setupImageView(args.cropOptions.aspectRatio, args.cropOptions.minCropSize)
        setClickListeners()
        imageCropper = ImageCropper(layout.cropImageView)
        imageCropper.loadImage(requireContext(), uri)

        return layout.root
    }

    override fun onStart() {
        super.onStart()
        hideNavigateBar()
    }

    private fun setupImageView(aspectRatio: AspectRatio?, minCropSize: CropSize?) {
        aspectRatio?.let {
            layout.cropImageView.setAspectRatio(it.aspectRatioX, it.aspectRatioY)
        }
        minCropSize?.let {
            layout.cropImageView.setMinCropResultSize(it.width, it.height)
        }
    }

    private fun setClickListeners() {
        layout.ivCancel.setOnClickListener {
            findNavController().popBackStack()
        }
        layout.ivApply.setOnClickListener {
            putResult()
            findNavController().popBackStack()
        }
    }

    private fun putResult() {
        val cropRect = imageCropper.getCropRect()

        Timber.tag("CropTag").d("cropResult: $cropRect")
        val cropResult = CropResult(args.cropOptions.source, cropRect)
        findNavController().previousBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<ResultData<CropResult>>(CROP_RESULT)
            ?.value = ResultData(cropResult)
    }
}