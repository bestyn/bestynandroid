package com.gbksoft.neighbourhood.mappers.profile

import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.app.NApplication
import com.gbksoft.neighbourhood.data.models.response.search.ProfileSearchModel
import com.gbksoft.neighbourhood.data.models.response.user.BusinessProfileModel
import com.gbksoft.neighbourhood.data.models.response.user.ProfileModel
import com.gbksoft.neighbourhood.data.models.response.user.UserModel
import com.gbksoft.neighbourhood.data.shared_prefs.CurrentProfileModel
import com.gbksoft.neighbourhood.mappers.HashtagMapper.toHashtagList
import com.gbksoft.neighbourhood.mappers.base.TimestampMapper
import com.gbksoft.neighbourhood.mappers.media.MediaMapper.toPictureList
import com.gbksoft.neighbourhood.model.business_profile.BusinessProfile
import com.gbksoft.neighbourhood.model.business_profile.PublicBusinessProfile
import com.gbksoft.neighbourhood.model.map.Coordinates
import com.gbksoft.neighbourhood.model.profile.*
import com.gbksoft.neighbourhood.model.profile_data.*
import com.gbksoft.neighbourhood.ui.fragments.profile.model.FollowType
import java.util.*

object ProfileMapper {
    private val resources = NApplication.context.resources

    @JvmStatic
    fun toProfile(userModel: UserModel): BasicProfile {
        val profileModel = userModel.profile
        val address = Address(profileModel.address, profileModel.latitude, profileModel.longitude)
        val gender = convertGender(profileModel.gender)
        val hashtags = toHashtagList(profileModel.hashtags)
        val avatar = AvatarMapper.toAvatar(profileModel.avatar)
        val birthday = profileModel.birthday?.let { Birthday(TimestampMapper.toAppTimestamp(it)) }
        return BasicProfile(
                profileModel.id,
                Email(userModel.email),
                profileModel.fullName,
                address,
                avatar,
                birthday,
                gender,
                hashtags,
                profileModel.seeBusinessPosts == 1
        )
    }

    @JvmStatic
    fun toPublicProfile(profileModel: ProfileModel): PublicBasicProfile {
        val address = Address(profileModel.address, profileModel.latitude, profileModel.longitude)
        val gender = convertGender(profileModel.gender)
        val avatar = AvatarMapper.toAvatar(profileModel.avatar)
        val hashtags = toHashtagList(profileModel.hashtags)
        val birthday = profileModel.birthday?.let { Birthday(TimestampMapper.toAppTimestamp(it)) }
        val followType = getFollowType(profileModel.isFollowed, profileModel.isFollower)
        return PublicBasicProfile(
                profileModel.id,
                avatar,
                profileModel.fullName,
                address,
                gender,
                birthday,
                hashtags,
                profileModel.isFollower,
                profileModel.isFollowed,
                followType
        )
    }

    @JvmStatic
    fun toBusinessProfile(profileModel: BusinessProfileModel): BusinessProfile {
        val address = Address(
                profileModel.address,
                profileModel.latitude,
                profileModel.longitude)
        val avatar = AvatarMapper.toAvatar(profileModel.avatar)
        val webSite = profileModel.site?.let { WebSite(it) }
        val email = profileModel.email?.let { Email(it) }
        val phone = profileModel.phone?.let { Phone(it) }
        val radius = VisibilityRadius(profileModel.radius)
        val hashtags = toHashtagList(profileModel.hashtags)
        val images = toPictureList(profileModel.images)
        return BusinessProfile(
                profileModel.id,
                avatar,
                profileModel.name,
                profileModel.description,
                address,
                webSite,
                email,
                phone,
                radius,
                hashtags,
                images
        )
    }

    @JvmStatic
    fun toPublicBusinessProfile(profileModel: BusinessProfileModel): PublicBusinessProfile {
        val address = Address(
                profileModel.address,
                profileModel.latitude,
                profileModel.longitude)
        val avatar = AvatarMapper.toAvatar(profileModel.avatar)
        val webSite = profileModel.site?.let { WebSite(it) }
        val email = profileModel.email?.let { Email(it) }
        val phone = profileModel.phone?.let { Phone(it) }
        val hashtags = toHashtagList(profileModel.hashtags)
        val images = toPictureList(profileModel.images)
        val followType = getFollowType(profileModel.isFollowed, profileModel.isFollower)
        return PublicBusinessProfile(
                profileModel.id,
                avatar,
                profileModel.name,
                profileModel.description,
                address,
                webSite,
                email,
                phone,
                hashtags,
                images,
                profileModel.isFollower,
                profileModel.isFollowed,
                followType
        )
    }


    @JvmStatic
    fun toMyProfiles(userModel: UserModel): List<MyProfile> {
        val myProfiles: MutableList<MyProfile> = ArrayList()
        var avatar = AvatarMapper.toAvatar(userModel.profile.avatar)
        val basicProfile = MyProfile(
                userModel.profile.id,
                avatar,
                userModel.profile.fullName,
                userModel.profile.address,
                false,
                Coordinates(userModel.profile.latitude, userModel.profile.longitude),
                userModel.profile.seeBusinessPosts == 1,
                !userModel.profile.hashtags.isNullOrEmpty(),
                userModel.profile.hasUnreadMessages
        )
        myProfiles.add(basicProfile)
        for (businessProfile in userModel.businessProfiles) {
            avatar = AvatarMapper.toAvatar(businessProfile.avatar)
            myProfiles.add(MyProfile(
                    businessProfile.id,
                    avatar,
                    businessProfile.name,
                    businessProfile.address,
                    true,
                    Coordinates(businessProfile.latitude, businessProfile.longitude),
                    true,
                    !businessProfile.hashtags.isNullOrEmpty(),
                    businessProfile.hasUnreadMessages
            ))
        }
        return myProfiles
    }

    @JvmStatic
    fun toCurrentProfileModel(currentProfile: CurrentProfile): CurrentProfileModel? {
        val avatarModel = AvatarMapper.toAvatarModel(currentProfile.avatar)
        return CurrentProfileModel(
                currentProfile.id,
                currentProfile.title,
                avatarModel,
                currentProfile.isBusiness,
                currentProfile.location.latitude,
                currentProfile.location.longitude,
                currentProfile.isBusinessContentShow,
                currentProfile.containsHashtags
        )
    }

    @JvmStatic
    fun toCurrentProfile(basicProfile: BasicProfile): CurrentProfile {
        val address = basicProfile.address
        return CurrentProfile(
                basicProfile.id,
                basicProfile.fullName,
                basicProfile.avatar,
                false,
                Coordinates(address.latitude, address.longitude),
                basicProfile.isBusinessContentShown,
                !basicProfile.hashtags.isNullOrEmpty()
        )
    }

    @JvmStatic
    fun toCurrentProfile(myProfile: MyProfile): CurrentProfile {
        return CurrentProfile(
                myProfile.id,
                myProfile.title,
                myProfile.avatar,
                myProfile.isBusiness,
                myProfile.location,
                myProfile.isBusinessContentShown,
                myProfile.containsInterests
        )
    }

    @JvmStatic
    fun toCurrentProfile(businessProfile: BusinessProfile): CurrentProfile {
        val address = businessProfile.address
        return CurrentProfile(
                businessProfile.id,
                businessProfile.name,
                businessProfile.avatar,
                true,
                Coordinates(address.latitude, address.longitude),
                true,
                !businessProfile.hashtags.isNullOrEmpty()
        )
    }

    @JvmStatic
    fun toCurrentProfile(profileModel: ProfileModel): CurrentProfile {
        val avatar = AvatarMapper.toAvatar(profileModel.avatar)
        return CurrentProfile(
                profileModel.id,
                profileModel.fullName,
                avatar,
                false,
                Coordinates(profileModel.latitude, profileModel.longitude),
                profileModel.seeBusinessPosts == 1,
                !profileModel.hashtags.isNullOrEmpty()
        )
    }

    @JvmStatic
    fun toCurrentProfile(model: CurrentProfileModel): CurrentProfile {
        val isBusinessContentShown = model.isBusinessContentShown
        val containsInterests = model.isContainsInterests
        val avatar = AvatarMapper.toAvatar(model.avatar)
        return CurrentProfile(
                model.id,
                model.title,
                avatar,
                model.isBusiness,
                Coordinates(model.latitude, model.longitude),
                isBusinessContentShown != null && isBusinessContentShown,
                containsInterests != null && containsInterests
        )
    }

    @JvmStatic
    fun toProfileSearchItem(model: ProfileSearchModel): ProfileSearchItem {
        val isBusiness = ProfileTypeMapper.isBusiness(model.type)
        val avatar = AvatarMapper.toAvatar(model.avatar)
        val followType = getFollowType(model.isFollowed, model.isFollower)
        return ProfileSearchItem(
                model.id,
                isBusiness,
                model.fullName,
                avatar,
                followType,
                model.isFollower,
                model.isFollowed
        )
    }

    private fun convertGender(genderName: String?): Gender? {
        return if (genderName == null) null else when (genderName) {
            "Male" -> Gender(resources.getString(R.string.gender_male), Gender.MALE)
            "Female" -> Gender(resources.getString(R.string.gender_female), Gender.FEMALE)
            "Other" -> Gender(resources.getString(R.string.gender_other), Gender.OTHER)
            else -> null
        }
    }

    private fun getFollowType(isFollowed: Boolean, isFollower: Boolean): FollowType {
        if (isFollowed) {
            return FollowType.FOLLOWING
        }
        return if (isFollower) {
            FollowType.FOLLOW_BACK
        } else {
            FollowType.FOLLOW
        }

    }

}