package com.gbksoft.neighbourhood.ui.fragments.business_profile.add_or_edit

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.gbksoft.neighbourhood.data.repositories.payments.PaymentRepository
import com.gbksoft.neighbourhood.mvvm.ViewModelFactory

class AddEditBusinessProfileViewModelFactory(private val activity: FragmentActivity) : ViewModelFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val constructor = modelClass.getConstructor(PaymentRepository::class.java, Context::class.java)
        val paymentRepository = PaymentRepository(activity)
        return constructor.newInstance(paymentRepository, activity)
    }
}