package com.gbksoft.neighbourhood.ui.notifications

interface FirebaseMessagingTokenStorage {
    fun getFirebaseMessagingToken(): String?
    fun getUploadedFirebaseMessagingToken(): String?
    fun setFirebaseMessagingToken(token: String)
    fun setUploadedFirebaseMessagingToken(token: String)
}