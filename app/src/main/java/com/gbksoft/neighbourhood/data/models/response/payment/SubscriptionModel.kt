package com.gbksoft.neighbourhood.data.models.response.payment

import com.gbksoft.neighbourhood.model.payment.Platform

class SubscriptionModel(
    var id: Int,
    var platform: String,
    var productName: String,
    var transactionToken: String
) {
    fun getPlatform() = Platform.getPlatform(platform)
}