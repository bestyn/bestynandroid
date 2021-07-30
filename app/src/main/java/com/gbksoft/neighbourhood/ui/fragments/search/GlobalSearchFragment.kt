package com.gbksoft.neighbourhood.ui.fragments.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentGlobalSearchBinding
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.contract.MediaPagerHost
import com.gbksoft.neighbourhood.ui.fragments.search.navigation.GlobalSearchNavigator
import com.gbksoft.neighbourhood.ui.fragments.search.search_screens.*
import com.gbksoft.neighbourhood.utils.KeyboardUtils
import org.koin.androidx.viewmodel.ext.android.viewModel

class GlobalSearchFragment : SystemBarsColorizeFragment(), MediaPagerHost, GlobalSearchScreen {
    private lateinit var layout: FragmentGlobalSearchBinding

    private val viewModel by viewModel<GlobalSearchViewModel>()
    private val currentSearchQuery = MutableLiveData<SearchQuery?>()

    private val postsSearchFragment = PostsSearchFragment()
    private val profilesSearchFragment = ProfilesSearchFragment()
    private val audioSearchFragment = AudioSearchFragment()

    private var currentSearchFragment: Fragment? = null
    private var isRecentSearchesEmpty = true

    private val navigator by lazy { GlobalSearchNavigator(this) }

    override fun getStatusBarColor(): Int {
        return R.color.screen_foreground_color
    }

    override fun searchQuery(): LiveData<SearchQuery?> = currentSearchQuery
    override fun navigator(): GlobalSearchNavigator = navigator

    override fun onStart() {
        super.onStart()
        hideNavigateBar()
        if (layout.searchView.getCurrentQuery().isEmpty()) {
            layout.searchView.focus()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_global_search, container, false)

        setClickListeners()
        subscribeToViewModel()

        return layout.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (layout.searchTabs.checkedChipId) {
            R.id.searchTabPosts -> setCurrentFragment(postsSearchFragment)
            R.id.searchTabProfiles -> setCurrentFragment(profilesSearchFragment)
            R.id.searchTabAudios -> setCurrentFragment(audioSearchFragment)
        }
    }

    private fun setCurrentFragment(fragment: Fragment) {
        currentSearchFragment = fragment
        childFragmentManager
            .beginTransaction()
            .replace(R.id.searchScreenContainer, fragment)
            .commit()
    }

    private fun setClickListeners() {
        layout.recentSearches.recentSearchClickListener = ::onRecentSearchClick
        layout.recentSearches.recentSearchRemoveClickListener = ::removeRecentSearch
        layout.btnBack.setOnClickListener { navigator.popBackStack() }
        layout.searchView.onSearchClickListener = ::onSearchClick
        layout.searchTabs.setOnCheckedChangeListener { _, checkedId ->
            onSearchTabChecked(checkedId)
        }
    }

    private fun onRecentSearchClick(recentSearch: String) {
        layout.searchView.setQuery(recentSearch)
        onSearchClick(recentSearch)
    }

    private fun removeRecentSearch(recentSearch: String) {
        viewModel.removeRecentSearch(recentSearch)
    }

    private fun onSearchClick(query: CharSequence) {
        currentSearchQuery.value = SearchQuery(System.currentTimeMillis(), query.toString())
        if (query.isNotEmpty()) {
            hideKeyboard()
        }
        onSearchTabChecked(layout.searchTabs.checkedChipId)
        resolveViewVisibility()
    }

    private fun hideKeyboard() {
        layout.searchView.clearFocus()
        KeyboardUtils.hideKeyboard(layout.searchView)
    }

    private fun onSearchTabChecked(checkedId: Int) {
        val fragment: Fragment = when (checkedId) {
            R.id.searchTabPosts -> postsSearchFragment
            R.id.searchTabProfiles -> profilesSearchFragment
            R.id.searchTabAudios -> audioSearchFragment
            else -> return
        }
        if (currentSearchFragment != fragment) {
            setCurrentFragment(fragment)
        }
    }

    private fun subscribeToViewModel() {
        viewModel.recentSearches.observe(viewLifecycleOwner, Observer { setRecentSearches(it) })
    }

    private fun setRecentSearches(recentSearches: List<String>) {
        isRecentSearchesEmpty = recentSearches.isEmpty()
        layout.recentSearches.setItems(recentSearches)
        resolveViewVisibility()
    }

    private fun resolveViewVisibility() {
        if (currentSearchQuery.value.isNullOrEmpty()) {
            layout.searchScreenContainer.visibility = View.GONE
            if (isRecentSearchesEmpty) {
                layout.groupRecentSearches.visibility = View.GONE
                layout.groupStartSearch.visibility = View.VISIBLE
            } else {
                layout.groupStartSearch.visibility = View.GONE
                layout.groupRecentSearches.visibility = View.VISIBLE
            }
        } else {
            layout.groupStartSearch.visibility = View.GONE
            layout.groupRecentSearches.visibility = View.GONE
            layout.searchScreenContainer.visibility = View.VISIBLE
        }
    }

    override fun onMediaClick(postMedia: Media) {
        when (postMedia) {
            is Media.Picture -> {
                navigator.toFullImage(postMedia)
            }
            is Media.Video -> {
                navigator.toVideoPlayer(postMedia)
            }
        }
    }


}