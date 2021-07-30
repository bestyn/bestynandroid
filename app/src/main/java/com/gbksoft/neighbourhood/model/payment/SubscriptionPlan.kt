package com.gbksoft.neighbourhood.model.payment

data class SubscriptionPlan(
    val id: String,
    val title: String,
    val price: String,
    var active: Boolean = false,
    var canceled: Boolean = false,
    var managePlatform: Platform? = null)