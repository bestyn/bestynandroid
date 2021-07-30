package com.gbksoft.neighbourhood.data.models.request.payment

data class PaymentReq(
    private val transactionToken: String,
    private val productName: String,
    private val platform: String = "Android"
)