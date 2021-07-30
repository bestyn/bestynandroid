package com.gbksoft.neighbourhood.ui.fragments.profile.public_view.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentTabPublicInterestsBinding
import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.gbksoft.neighbourhood.model.profile.PublicBasicProfile
import com.gbksoft.neighbourhood.ui.fragments.base.BaseFragment

class PublicInterestsFragment : BaseFragment() {
    private lateinit var layout: FragmentTabPublicInterestsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_tab_public_interests,
            container, false)

        arguments?.let {
            val profile: PublicBasicProfile? = it.getParcelable("profile")
            profile?.let {
                setInterests(it.hashtags)
            }
        }
        return layout.root
    }

    fun setProfile(profile: PublicBasicProfile) {
        if (arguments == null) {
            arguments = Bundle()
        }
        requireArguments().putParcelable("profile", profile)

        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            setInterests(profile.hashtags)
        }
    }

    private fun setInterests(interests: List<Hashtag>) {
        layout.chipGroup.setInterestList(interests)
    }
}