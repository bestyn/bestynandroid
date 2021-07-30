package com.gbksoft.neighbourhood.ui.widgets.reaction.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentPostReactionsBinding
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.reaction.PostReaction
import com.gbksoft.neighbourhood.ui.fragments.base.BaseFragment
import com.gbksoft.neighbourhood.utils.LastVisiblePositionChangeListener

abstract class BasePostReactionsFragment : BaseFragment() {
    var onProfileAvatarClickListener: ((PostReaction) -> Unit)? = null
    var onChatClickListener: ((PostReaction) -> Unit)? = null

    private lateinit var viewModel: BasePostReactionsViewModel
    private lateinit var layout: FragmentPostReactionsBinding
    private lateinit var adapter: PostReactionsAdapter

    protected abstract fun provideViewModel(): BasePostReactionsViewModel

    private val lastVisiblePositionListener: RecyclerView.OnScrollListener = object : LastVisiblePositionChangeListener() {
        override fun lastVisiblePositionChanged(lastVisibleItemPosition: Int) {
            viewModel.onVisibleItemChanged(lastVisibleItemPosition)
        }
    }

    private fun setupView() {
        adapter = PostReactionsAdapter().apply {
            this.onProfileAvatarClickListener = this@BasePostReactionsFragment.onProfileAvatarClickListener
            this.onChatClickListener = this@BasePostReactionsFragment.onChatClickListener
        }
        layout.rvReactionsList.adapter = adapter
        layout.rvReactionsList.layoutManager = LinearLayoutManager(requireContext())
        layout.rvReactionsList.addOnScrollListener(lastVisiblePositionListener)
    }

    private fun initViewModelObservers() {
        viewModel.postReactions.observe(viewLifecycleOwner, Observer {
            adapter.setData(it)
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = provideViewModel()

        val feedPost = arguments?.getParcelable<FeedPost>(KEY_FEED_POST)
        feedPost?.let {
            viewModel.postId = it.post.id
            viewModel.loadPostReactions()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_post_reactions, container, false)
        setupView()
        return layout.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModelObservers()
    }
}