package com.gbksoft.neighbourhood.ui.fragments.stories.for_unauthorized_users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentStoriesUnauthBinding
import com.gbksoft.neighbourhood.mvvm.result.ResultData
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.CreateEditPostFragment
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.PostResult
import com.gbksoft.neighbourhood.ui.fragments.stories.StoryPagerAdapter
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.InsetUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StoriesForUnauthorizedFragment : SystemBarsColorizeFragment() {

    private lateinit var layout: FragmentStoriesUnauthBinding
    private lateinit var storiesAdapter: StoryPagerAdapter
    private var currentTabPosition = 0
    private var topInset: Int = 0

    private val _insetsLiveData = MutableLiveData<WindowInsets>()

    override fun getNavigationBarColor(): Int = R.color.stories_nav_bar_color
    override fun getStatusBarColor(): Int = R.color.stories_status_bar_color
    override fun getFragmentContainerColor(): Int = R.color.stories_screen_background

    val displaySignInButtonListener = object : DisplaySignInButton {
        override fun displaySignInButton(shouldDisplay: Boolean) {
            showSignInOutButtons()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storiesAdapter = StoryPagerAdapter(this, true, displaySignInButtonListener)
        val args = arguments
        if (args != null) {
            val changedEmail = args.getString(Constants.KEY_CHANGED_EMAIL, null)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_stories_unauth, container, false)
        savedInstanceState?.let { restoreInstanceState(it) }
        setupView()
        subscribeToResult()
        setOnClickListeners()
        return layout.root
    }

    override fun setOnApplyWindowInsetsListener(view: View) {
        view.setOnApplyWindowInsetsListener { v, insets ->
            topInset = insets.systemWindowInsetTop
            layout.storyTabsPositionHelper.updatePadding(top  = insets.systemWindowInsetTop)
            layout.layoutBtnSignInUp.updatePadding(top = insets.systemWindowInsetTop)
            layout.btnSwitchToBestyn.updatePadding(bottom = insets.systemWindowInsetBottom)
            _insetsLiveData.value = insets
            insets.consumeSystemWindowInsets()
        }
        view.requestApplyInsets()
    }
    override fun onResume() {
        super.onResume()
        showNavigateBar()
        InsetUtils.shouldRemovePaddings = false
    }

    private fun restoreInstanceState(bundle: Bundle) {
        currentTabPosition = bundle.getInt("currentTabPosition")
    }

    private fun setupView() {
        layout.storiesViewPager.isSaveEnabled = false
        layout.storiesViewPager.adapter = storiesAdapter
        layout.storiesViewPager.isUserInputEnabled = false
        layout.storiesViewPager.setCurrentItem(currentTabPosition, false)
    }

    private fun setOnClickListeners() {
        layout.btnSwitchToBestyn.setOnClickListener {
            showSignInOutButtons()
        }

        layout.btnGoToSignIn.setOnClickListener {
            goToSignIn()
        }

        layout.btnGoToSignUp.setOnClickListener {
            goToSignUp()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("currentTabPosition", currentTabPosition)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        layout.storiesViewPager.adapter = null
        super.onDestroyView()
    }

    private fun subscribeToResult() {
        findNavController()
                .currentBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<ResultData<PostResult>>(CreateEditPostFragment.RESULT_POST)
                ?.observe(viewLifecycleOwner, Observer {
                    storiesAdapter.setResultData(it)
                })
    }

    private fun showSignInOutButtons() {
        CoroutineScope(Dispatchers.Main).launch {
            if (layout.layoutBtnSignInUp.visibility == View.VISIBLE){
                layout.layoutBtnSignInUp.visibility = View.GONE
                delay(50)
            }
            layout.layoutBtnSignInUp.visibility = View.VISIBLE
            delay(30000)
            layout.layoutBtnSignInUp.visibility = View.GONE
        }
    }

    private fun goToSignIn() {
        val direction = StoriesForUnauthorizedFragmentDirections.toSignIn()
        findNavController().navigate(direction)
    }

    private fun goToSignUp() {
        val direction = StoriesForUnauthorizedFragmentDirections.toSignUp()
        findNavController().navigate(direction)
    }


}