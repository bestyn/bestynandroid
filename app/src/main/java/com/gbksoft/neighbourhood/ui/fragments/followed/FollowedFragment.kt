package com.gbksoft.neighbourhood.ui.fragments.followed

import android.content.Context
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.model.profile.ProfileSearchItem
import com.gbksoft.neighbourhood.model.profile.PublicProfile
import com.gbksoft.neighbourhood.ui.fragments.followers.ProfileListFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_follower_options.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class FollowedProfilesFragment : ProfileListFragment() {

    override fun showBottomSheetDialog(profile: ProfileSearchItem) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_follower_options, null)
        dialogView.btnRemoveFollower.isVisible = false
        dialogView.btnUnfollow.isVisible = false
        dialogView.dividerRemoveFollower.isVisible = false
        dialogView.dividerUnfollow.isVisible = false

        val dialog = BottomSheetDialog(context as Context)
        dialog.setContentView(dialogView)
        dialogView.btnReportUser.setOnClickListener {
            navigateToReportUser(profile)
            dialog.dismiss()
        }
        dialogView.btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override val viewModel by viewModel<FollowedProfilesViewModel>()

    override fun navigateToReportUser(profileSearchItem: ProfileSearchItem) {
        val publicProfile = PublicProfile(
                profileSearchItem.id,
                profileSearchItem.isBusiness,
                profileSearchItem.avatar,
                profileSearchItem.fullName)
        val direction = FollowedProfilesFragmentDirections.toReportUserFragment(publicProfile)
        findNavController().navigate(direction)
    }

    override fun navigateToPublicProfile(profileId: Long) {
        val direction = FollowedProfilesFragmentDirections.toPublicProfileFragment(profileId)
        findNavController().navigate(direction)
    }

    override fun navigateToPublicBusinessProfile(profileId: Long) {
        val direction = FollowedProfilesFragmentDirections.toPublicBusinessProfileFragment(profileId)
        findNavController().navigate(direction)
    }

    override fun onProfilesCountLoaded(profilesCount: Int) {
        layout.tvProfileCount.text = "Following $profilesCount"
    }
}