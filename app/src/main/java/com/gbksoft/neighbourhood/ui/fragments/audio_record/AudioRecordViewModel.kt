package com.gbksoft.neighbourhood.ui.fragments.audio_record

import com.gbksoft.neighbourhood.mvvm.BaseViewModel

class AudioRecordViewModel : BaseViewModel() {

    fun saveChronometerBase(time: Long) {
        sharedStorage.setChronometerBase(time)
    }

    fun getChronometerBase() = sharedStorage.getChronometerBase()

}