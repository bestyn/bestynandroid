package com.gbksoft.neighbourhood.model.auth

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import com.google.android.libraries.places.api.model.AddressComponents

class SignUpModel : BaseObservable() {
    @get:Bindable
    var address = ""
        set(address) {
            field = address
            notifyPropertyChanged(BR.address)
        }

    @get:Bindable
    var addressComponents: AddressComponents? = null
        set(addressComponents) {
            field = addressComponents
            notifyPropertyChanged(BR.addressComponents)
        }

    @get:Bindable
    var addressPlaceId: String = ""

    @get:Bindable
    var fullName = ""
        set(fullName) {
            field = fullName
            notifyPropertyChanged(BR.fullName)
        }

    @get:Bindable
    var email = ""
        set(email) {
            field = email
            notifyPropertyChanged(BR.email)
        }

    @get:Bindable
    var password = ""
        set(password) {
            field = password
            notifyPropertyChanged(BR.password)
        }

    @get:Bindable
    var confirmPassword = ""
        set(confirmPassword) {
            field = confirmPassword
            notifyPropertyChanged(BR.confirmPassword)
        }

    @get:Bindable
    var isTermsPolicy = false
        set(isTermsPolicy) {
            field = isTermsPolicy
            notifyPropertyChanged(BR.termsPolicy)
        }

}