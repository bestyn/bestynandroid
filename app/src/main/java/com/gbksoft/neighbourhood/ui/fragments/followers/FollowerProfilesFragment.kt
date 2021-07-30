package com.gbksoft.neighbourhood.ui.fragments.followers

import android.content.Context
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.model.profile.ProfileSearchItem
import com.gbksoft.neighbourhood.model.profile.PublicProfile
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_follower_options.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class FollowerProfilesFragment : ProfileListFragment() {

    override val viewModel by viewModel<FollowerProfilesViewModel>()

    override fun showBottomSheetDialog(profile: ProfileSearchItem) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_follower_options, null)
        dialogView.btnUnfollow.isVisible = false

        val dialog = BottomSheetDialog(context as Context)
        dialog.setContentView(dialogView)
        dialogView.btnUnfollow.setOnClickListener {
            viewModel.unfollowProfile(profile.id)
            dialog.dismiss()
        }
        dialogView.btnRemoveFollower.setOnClickListener {
            viewModel.removeFollower(profile.id)
            dialog.dismiss()
        }
        dialogView.btnReportUser.setOnClickListener {
            navigateToReportUser(profile)
            dialog.dismiss()
        }
        dialogView.btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun navigateToReportUser(profileSearchItem: ProfileSearchItem) {
        val publicProfile = PublicProfile(
                profileSearchItem.id,
                profileSearchItem.isBusiness,
                profileSearchItem.avatar,
                profileSearchItem.fullName)
        val direction = FollowerProfilesFragmentDirections.toReportUserFragment(publicProfile)
        findNavController().navigate(direction)
    }

    override fun navigateToPublicProfile(profileId: Long) {
        val direction = FollowerProfilesFragmentDirections.toPublicProfileFragment(profileId)
        findNavController().navigate(direction)
    }

    override fun navigateToPublicBusinessProfile(profileId: Long) {
        val direction = FollowerProfilesFragmentDirections.toPublicBusinessProfileFragment(profileId)
        findNavController().navigate(direction)
    }

    override fun onProfilesCountLoaded(profilesCount: Int) {
        layout.tvProfileCount.text = "Followers $profilesCount"
    }
}