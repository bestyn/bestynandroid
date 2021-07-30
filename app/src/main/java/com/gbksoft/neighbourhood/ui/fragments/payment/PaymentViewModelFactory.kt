package com.gbksoft.neighbourhood.ui.fragments.payment

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.gbksoft.neighbourhood.data.repositories.payments.PaymentRepository
import com.gbksoft.neighbourhood.mappers.payment.SubscriptionPlanMapper
import com.gbksoft.neighbourhood.mvvm.ViewModelFactory

class PaymentViewModelFactory(private val activity: FragmentActivity) : ViewModelFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val constructor = modelClass.getConstructor(PaymentRepository::class.java, SubscriptionPlanMapper::class.java)
        val paymentRepository = PaymentRepository(activity)
        val subscriptionPlanMapper = SubscriptionPlanMapper(activity)
        return constructor.newInstance(paymentRepository, subscriptionPlanMapper)
    }
}