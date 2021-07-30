package com.gbksoft.neighbourhood.ui.widgets.chat.speech

import android.speech.tts.UtteranceProgressListener

class SimpleUtteranceProgressListener(
    private val onStartSpeech: (id: String) -> Unit,
    private val onEndSpeech: (id: String) -> Unit
) : UtteranceProgressListener() {

    override fun onStart(utteranceId: String) {
        onStartSpeech.invoke(utteranceId)
    }

    override fun onDone(utteranceId: String) {
        onEndSpeech.invoke(utteranceId)
    }

    override fun onError(utteranceId: String) {
        onEndSpeech.invoke(utteranceId)
    }
}