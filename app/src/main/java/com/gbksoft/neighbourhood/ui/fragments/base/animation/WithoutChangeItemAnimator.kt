package com.gbksoft.neighbourhood.ui.fragments.base.animation

import androidx.recyclerview.widget.DefaultItemAnimator

class WithoutChangeItemAnimator : DefaultItemAnimator() {
    init {
        supportsChangeAnimations = false
    }
}