package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.record

import java.io.File

interface VideoRecordingListener {
    fun onVideoRecordingStart()
    fun onVideoRecordingError()
    fun onVideoRecordingEnd()
    fun onVideoTaken(file: File)
}