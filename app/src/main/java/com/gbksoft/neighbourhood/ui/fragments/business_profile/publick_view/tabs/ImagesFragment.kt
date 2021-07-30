package com.gbksoft.neighbourhood.ui.fragments.business_profile.publick_view.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentTabBusinessImagesBinding
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.mvvm.VMProvider
import com.gbksoft.neighbourhood.ui.fragments.base.BaseFragment
import com.gbksoft.neighbourhood.ui.fragments.business_profile.publick_view.adapter.ImagesAdapter
import com.gbksoft.neighbourhood.ui.fragments.business_profile.tabs.ShowInAlbumHandler
import com.gbksoft.neighbourhood.utils.GridDividerItemDecoration
import timber.log.Timber

class ImagesFragment : BaseFragment() {
    private lateinit var layout: FragmentTabBusinessImagesBinding
    private lateinit var adapter: ImagesAdapter
    private var viewModel: ImagesViewModel? = null

    fun setProfile(profileId: Long) {
        if (arguments == null) {
            arguments = Bundle()
        }
        viewModel?.init(profileId)
        requireArguments().putLong("profileId", profileId)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.tag("ImagesTag").d("ImagesFragment.onCreate: $this")
        viewModel = VMProvider.create(viewModelStore) { ImagesViewModel() }.get()

        arguments?.let {
            val profileId = it.getLong("profileId")
            viewModel?.init(profileId)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_tab_business_images,
            container, false)

        setupView()
        subscribeToViewModel(viewModel!!)
        return layout.root
    }

    private fun setupView() {
        adapter = ImagesAdapter()
        adapter.onImageClickListener = { image, position -> openImage(image, position) }
        val spacing = resources.getDimensionPixelSize(R.dimen.business_image_spacing)
        val columnsCount = resources.getInteger(R.integer.business_images_columns_count)
        val divider = GridDividerItemDecoration(spacing, columnsCount)
        layout.rvImages.addItemDecoration(divider)
        layout.rvImages.adapter = adapter
    }

    private fun openImage(pic: Media.Picture, position: Int) {
        (parentFragment as? ShowInAlbumHandler)?.let {
            it.showInAlbum(pic, position)
        }
    }

    private fun subscribeToViewModel(viewModel: ImagesViewModel) {
        viewModel.images().observe(viewLifecycleOwner, Observer {
            adapter.setData(it)
        })
    }
}