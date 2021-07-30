package com.gbksoft.neighbourhood.data.repositories

import com.gbksoft.neighbourhood.app.NApplication
import com.gbksoft.neighbourhood.data.forms.BusinessProfileCreation
import com.gbksoft.neighbourhood.data.forms.BusinessProfileEditing
import com.gbksoft.neighbourhood.data.models.request.email.ChangeEmailReq
import com.gbksoft.neighbourhood.data.models.request.user.ChangePasswordReq
import com.gbksoft.neighbourhood.data.models.request.user.CreateBusinessProfileReq
import com.gbksoft.neighbourhood.data.models.request.user.UpdateBusinessProfileReq
import com.gbksoft.neighbourhood.data.models.request.user.UpdateProfileReq
import com.gbksoft.neighbourhood.data.models.response.user.ProfileModel
import com.gbksoft.neighbourhood.data.models.response.user.UserModel
import com.gbksoft.neighbourhood.data.network.ApiFactory
import com.gbksoft.neighbourhood.mappers.profile.ProfileMapper
import com.gbksoft.neighbourhood.model.business_profile.BusinessProfile
import com.gbksoft.neighbourhood.model.business_profile.PublicBusinessProfile
import com.gbksoft.neighbourhood.model.profile.CurrentProfile
import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import java.util.*

class ProfileRepository : BaseRepository() {
    private val userModelSubject: Subject<UserModel> = BehaviorSubject.create()
    private var latestUserModel: UserModel? = null
    private val userExpand = "profile.avatar.formatted,businessProfiles.avatar.formatted," +
        "businessProfiles.images.formatted, profile.hasUnreadMessages,businessProfiles.hasUnreadMessages," +
        "profile.hashtags,businessProfiles.hashtags"
    private val profileExpand = "avatar.formatted,images.formatted,profile.hashtags,businessProfiles.hashtags,hashtags,isFollowed,isFollower"
    private val imagesExpand = "images.formatted"

    init {
        getUserModel()?.let {
            latestUserModel = it
            userModelSubject.onNext(it)
        }
    }

    fun subscribeCurrentUser(): Flowable<UserModel> {
        return if (latestUserModel == null) {
            subscribeCurrentUserWithRemote()
        } else {
            userModelSubject.toFlowable(BackpressureStrategy.LATEST)
        }
    }

    fun subscribeHasUnreadMessages(profileId: Long): Flowable<Boolean> {
        return subscribeCurrentUserWithRemote()
            .map { ProfileMapper.toMyProfiles(it) }
            .flatMap { Flowable.fromIterable(it) }
            .filter { it.id == profileId }
            .map { it.hasUnreadMessages }
    }

    fun subscribeHasUserUnreadMessages(): Flowable<Boolean> {
        return subscribeCurrentUser()
            .map { ProfileMapper.toMyProfiles(it) }
            .map { myProfiles ->
                var hasUnreadMessages = false
                for (myProfile in myProfiles) {
                    hasUnreadMessages = myProfile.hasUnreadMessages
                    if (hasUnreadMessages) break
                }
                hasUnreadMessages
            }
    }

    fun setHasUnreadMessages(profileId: Long, hasUnreadMessages: Boolean) {
        val userModel = latestUserModel ?: return

        if (userModel.profile.id == profileId) {
            userModel.profile.hasUnreadMessages = hasUnreadMessages
            saveUserModel(userModel)
            userModelSubject.onNext(latestUserModel!!)
            return
        }

        for (businessProfile in userModel.businessProfiles) {
            if (businessProfile.id == profileId) {
                businessProfile.hasUnreadMessages = hasUnreadMessages
                saveUserModel(userModel)
                userModelSubject.onNext(latestUserModel!!)
                return
            }
        }
    }

    fun subscribeCurrentUserWithRemote(): Flowable<UserModel> {
        val subject = BehaviorSubject.create<UserModel>()
        val cd = CompositeDisposable()
        return subject
            .doOnSubscribe {
                cd.add(userModelSubject.subscribe(
                    { subject.onNext(it) },
                    { subject.onError(it) }))
                cd.add(updateCurrentUserFromServer().subscribe(
                    { subject.onNext(it) },
                    { subject.onError(it) }))
            }
            .doOnDispose { cd.dispose() }
            .toFlowable(BackpressureStrategy.LATEST)
    }

    fun pullBusinessProfile(id: Long): Flowable<BusinessProfile> {
        val subject = BehaviorSubject.create<UserModel>()
        val cd = CompositeDisposable()
        return subject
            .doOnSubscribe {
                cd.add(userModelSubject.subscribe(
                    { subject.onNext(it) },
                    { subject.onError(it) }))
                cd.add(updateCurrentUserFromServer().subscribe(
                    { subject.onNext(it) },
                    { subject.onError(it) }))
            }
            .doOnDispose { cd.dispose() }
            .map { userModel ->
                for (model in userModel.businessProfiles) {
                    if (model.id == id) return@map model
                }
                val error = String.format(Locale.US, "Business profile with id %d not found", id)
                throw NullPointerException(error)
            }
            .map { ProfileMapper.toBusinessProfile(it) }
            .toFlowable(BackpressureStrategy.LATEST)
    }

    fun getMyBusinessProfile(id: Long): Observable<BusinessProfile> {
        return subscribeCurrentUserWithRemote()
            .map { userModel ->
                for (model in userModel.businessProfiles) {
                    if (model.id == id) return@map model
                }
                val error = String.format(Locale.US, "Business profile with id %d not found", id)
                throw NullPointerException(error)
            }
            .map { ProfileMapper.toBusinessProfile(it) }
            .toObservable()
    }

    fun getCurrentUserFromServer(): Observable<UserModel> {
        return ApiFactory.apiUser
            .getCurrentUser(userExpand)
            .map { it.result }
            .flatMap {
                latestUserModel = it
                saveUserModel(it)
                userModelSubject.onNext(it)
                Observable.just(it)
            }
    }

    private fun updateCurrentUserFromServer(): Observable<UserModel> {
        return ApiFactory.apiUser
            .getCurrentUser(userExpand)
            .map { it.result }
            .flatMapMaybe {
                latestUserModel = it
                saveUserModel(it)
                userModelSubject.onNext(it)
                Maybe.empty<UserModel>()
            }
    }

    fun updateProfile(updateProfileReq: UpdateProfileReq): Observable<CurrentProfile> {
        return ApiFactory.apiUser
            .updateProfile(updateProfileReq, profileExpand)
            .map { it.requireResult() }
            .doOnNext { profileModel ->
                if (latestUserModel != null) {
                    latestUserModel!!.profile = profileModel
                    saveUserModel(latestUserModel!!)
                    userModelSubject.onNext(latestUserModel!!)
                }
            }
            .map {
                ProfileMapper.toCurrentProfile(it)
            }
    }

    private fun saveUserModel(userModel: UserModel) {
        NApplication.sharedStorage.saveUser(userModel)
    }

    private fun getUserModel(): UserModel? {
        return NApplication.sharedStorage.getUserModel()
    }

    fun changePassword(currentPassword: String, newPassword: String): Completable {
        val changePasswordReq = ChangePasswordReq(currentPassword, newPassword)
        return ApiFactory.apiUser
            .changePassword(changePasswordReq)
            .toObservable().ignoreElements()
    }

    fun getPublicProfile(profileId: Long): Observable<ProfileModel> {
        return ApiFactory.apiUser
            .getPublicProfile(profileId, profileExpand)
            .map { it.result }
            .map { profileModel: ProfileModel ->
                profileModel.id = profileId
                profileModel
            }
    }

    fun changeEmail(newEmail: String): Completable {
        val changeEmailReq = ChangeEmailReq(newEmail)
        return ApiFactory.apiEmail
            .changeEmail(changeEmailReq)
            .toObservable().ignoreElements()
    }

    fun createBusinessProfile(form: BusinessProfileCreation): Observable<BusinessProfile> {
        val req = CreateBusinessProfileReq()
        req.setAvatar(form.image, form.imageFormat)
        req.setName(form.name)
        req.setPlaceId(form.addressPlaceId)
        req.setDescription(form.description)
        req.setRadius(form.radius)
        if (form.hashtagIds.isNotEmpty()) {
            req.setCategories(form.hashtagIds)
        }
        return ApiFactory.apiUser
            .createBusinessProfile(req, profileExpand)
            .map { it.result }
            .map { ProfileMapper.toBusinessProfile(it) }
    }

    fun updateBusinessProfile(form: BusinessProfileEditing): Observable<BusinessProfile> {
        val req = UpdateBusinessProfileReq()
        form.name?.let { req.setName(it) }
        form.description?.let { req.setDescription(it) }
        form.radius?.let { req.setRadius(it) }
        form.image?.let { req.setAvatar(it, form.imageFormat!!) }
        form.addressPlaceId?.let {
            req.setPlaceId(it)
        }

        form.hashtagIds?.let {
            req.setCategories(it)
        }

        form.webSite?.let {
            req.setSite(it)
        }
        form.email?.let {
            req.setEmail(it)
        }
        form.phone?.let {
            req.setPhone(it)
        }

        return ApiFactory.apiUser
            .updateBusinessProfile(form.id, req, profileExpand)
            .map { it.result }
            .map { ProfileMapper.toBusinessProfile(it) }
    }

    fun getPublicBusinessProfile(id: Long): Observable<PublicBusinessProfile> {
        return ApiFactory.apiUser
            .getPublicBusinessProfile(id, profileExpand)
            .map { it.result }
            .map { ProfileMapper.toPublicBusinessProfile(it) }
    }
}