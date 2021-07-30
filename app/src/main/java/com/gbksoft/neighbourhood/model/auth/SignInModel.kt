package com.gbksoft.neighbourhood.model.auth

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR

class SignInModel : BaseObservable() {
    @Bindable
    var email: String = ""
        set(email) {
            field = email
            notifyPropertyChanged(BR.email)
        }

    @Bindable
    var password: String = ""
        set(password) {
            field = password
            notifyPropertyChanged(BR.password)
        }

}