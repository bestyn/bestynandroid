package com.gbksoft.neighbourhood.data.analytics

import android.os.Bundle
import com.gbksoft.neighbourhood.BuildConfig
import com.gbksoft.neighbourhood.app.NApplication
import com.google.firebase.analytics.FirebaseAnalytics


object Analytics {
    private val firebase = FirebaseAnalytics.getInstance(NApplication.context)
    private val isDisabled = BuildConfig.DEBUG

    @JvmStatic
    fun getInstance() = this

    //General
    private const val EVENT_SIGNED_UP = "signed_up"
    private const val EVENT_CONFIRMED_EMAIL = "confirmed_email"

    //Personal Profile
    private const val EVENT_SELECTED_INTEREST = "selected_interest"
    private const val EVENT_CREATED_GENERAL_POST = "created_general_post"
    private const val EVENT_CREATED_NEWS_POST = "created_news_post"
    private const val EVENT_OPENED_API_NEWS = "opened_api_news"
    private const val EVENT_TOLD_ABOUT_CRIME = "told_about_crime"
    private const val EVENT_CREATED_EVENT = "created_event"
    private const val EVENT_FOLLOWED_OTHER_POST = "followed_other_post"
    private const val EVENT_OPENED_CREATED_POSTS = "opened_created_posts"
    private const val EVENT_OPENED_FOLLOWED_POSTS = "opened_followed_posts"
    private const val EVENT_OPENED_MAP_VIEW = "opened_map_view"
    private const val EVENT_ADDED_COMMENT_ON_POST = "added_comment_on_post"
    private const val EVENT_OPENED_MY_CHATS = "opened_my_chats"
    private const val EVENT_SENT_DIRECT_MESSAGE = "sent_direct_message"

    //Business Profile
    private const val EVENT_CREATED_BUSINESS_PROFILE = "created_business_profile"
    private const val EVENT_CREATED_OFFER_POST = "created_offer_post"
    private const val EVENT_PAYMENT_PLAN_OPENED = "payment_plan_opened"
    private const val EVENT_SUBSCRIPTION_PURCHASED = "subscription_purchased"

    //Params
    private const val PARAM_IS_FIRST_SELECT_INTERESTS = "is_first_select"
    private const val PARAM_POST_ID = "post_id"
    private const val PARAM_NEWS_ID = "news_id"
    private const val PARAM_SUBSCRIPTION_ID = "subscription_id"
    private const val PARAM_SUBSCRIPTION_PRICE = "subscription_price"

    fun onSignedUp() {
        if (isDisabled) return
        firebase.logEvent(EVENT_SIGNED_UP, null)
    }

    fun onConfirmedEmail() {
        if (isDisabled) return
        firebase.logEvent(EVENT_CONFIRMED_EMAIL, null)
    }

    fun onSelectedInterests(isFirstSelect: Boolean) {
        if (isDisabled) return
        val params = Bundle()
        params.putBoolean(PARAM_IS_FIRST_SELECT_INTERESTS, isFirstSelect)
        firebase.logEvent(EVENT_SELECTED_INTEREST, null)
    }

    fun onCreatedGeneralPost(postId: Long) {
        if (isDisabled) return
        val params = Bundle()
        params.putLong(PARAM_POST_ID, postId)
        firebase.logEvent(EVENT_CREATED_GENERAL_POST, null)
    }

    fun onCreatedNewsPost(postId: Long) {
        if (isDisabled) return
        val params = Bundle()
        params.putLong(PARAM_POST_ID, postId)
        firebase.logEvent(EVENT_CREATED_NEWS_POST, params)
    }

    fun onOpenedApiNews(newsId: Long) {
        if (isDisabled) return
        val params = Bundle()
        params.putLong(PARAM_NEWS_ID, newsId)
        firebase.logEvent(EVENT_OPENED_API_NEWS, params)
    }

    fun onCreatedCrimePost(postId: Long) {
        if (isDisabled) return
        val params = Bundle()
        params.putLong(PARAM_POST_ID, postId)
        firebase.logEvent(EVENT_TOLD_ABOUT_CRIME, params)
    }

    fun onCreatedEvent(postId: Long) {
        if (isDisabled) return
        val params = Bundle()
        params.putLong(PARAM_POST_ID, postId)
        firebase.logEvent(EVENT_CREATED_EVENT, params)
    }

    fun onFollowedOtherPost(postId: Long) {
        if (isDisabled) return
        val params = Bundle()
        params.putLong(PARAM_POST_ID, postId)
        firebase.logEvent(EVENT_FOLLOWED_OTHER_POST, params)
    }

    fun onOpenedCreatedPosts() {
        if (isDisabled) return
        firebase.logEvent(EVENT_OPENED_CREATED_POSTS, null)
    }

    fun onOpenedFollowedPosts() {
        if (isDisabled) return
        firebase.logEvent(EVENT_OPENED_FOLLOWED_POSTS, null)
    }

    fun onOpenedMapView() {
        if (isDisabled) return
        firebase.logEvent(EVENT_OPENED_MAP_VIEW, null)
    }

    fun onAddedCommentOnPost(postId: Long) {
        if (isDisabled) return
        val params = Bundle()
        params.putLong(PARAM_POST_ID, postId)
        firebase.logEvent(EVENT_ADDED_COMMENT_ON_POST, params)
    }

    fun onOpenedMyChats() {
        if (isDisabled) return
        firebase.logEvent(EVENT_OPENED_MY_CHATS, null)
    }

    fun onSentDirectMessage() {
        if (isDisabled) return
        firebase.logEvent(EVENT_SENT_DIRECT_MESSAGE, null)
    }

    fun onCreatedBusinessProfile() {
        if (isDisabled) return
        firebase.logEvent(EVENT_CREATED_BUSINESS_PROFILE, null)
    }

    fun onCreatedOfferPost(postId: Long) {
        if (isDisabled) return
        val params = Bundle()
        params.putLong(PARAM_POST_ID, postId)
        firebase.logEvent(EVENT_CREATED_OFFER_POST, params)
    }

    fun onPaymentPlanOpened() {
        if (isDisabled) return
        firebase.logEvent(EVENT_PAYMENT_PLAN_OPENED, null)
    }

    fun onSubscriptionPurchased(subsPlanId: String, subsPlanPrice: String) {
        if (isDisabled) return
        val params = Bundle()
        params.putString(PARAM_SUBSCRIPTION_ID, subsPlanId)
        params.putString(PARAM_SUBSCRIPTION_PRICE, subsPlanPrice)
        firebase.logEvent(EVENT_SUBSCRIPTION_PURCHASED, params)
    }

}