package com.gbksoft.neighbourhood.mappers.payment

import android.content.Context
import com.android.billingclient.api.SkuDetails
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.data.models.response.payment.SubscriptionModel
import com.gbksoft.neighbourhood.model.payment.Platform
import com.gbksoft.neighbourhood.model.payment.SubscriptionPlan
import org.threeten.bp.Period
import java.util.*

class SubscriptionPlanMapper(private val context: Context) {

    private val skuTitleAppNameRegex = """(?> \(.+?\))$""".toRegex()

    fun map(skuDetails: SkuDetails): SubscriptionPlan {
        val period = Period.parse(skuDetails.subscriptionPeriod)
        val monthQuantity = context.resources
            .getQuantityString(R.plurals.month, period.months)
            .toLowerCase(Locale.getDefault())
        val monthCount = if (period.months > 1) "${period.months} " else ""
        return SubscriptionPlan(
            skuDetails.sku,
            skuDetails.title.replace(skuTitleAppNameRegex, ""),
            "${skuDetails.price} /${monthCount}${monthQuantity}",
            false,
            managePlatform = Platform.ANDROID)
    }

    fun mapSubscriptionFromAnotherPlatform(skuDetails: SkuDetails, platform: Platform) = SubscriptionPlan(
        skuDetails.sku,
        skuDetails.title.replace(skuTitleAppNameRegex, ""),
        context.getString(R.string.title_subscription_from_another_platform, platform.id),
        managePlatform = platform
    )


    fun buildSubscriptionPlanFromAnotherPlatform(subscriptionModel: SubscriptionModel) = SubscriptionPlan(
        subscriptionModel.productName,
        context.getString(R.string.title_subscription_from_another_platform, subscriptionModel.platform),
        "",
        managePlatform = Platform.getPlatform(subscriptionModel.platform)
    )
}