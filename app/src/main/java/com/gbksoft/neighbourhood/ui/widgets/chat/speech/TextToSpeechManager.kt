package com.gbksoft.neighbourhood.ui.widgets.chat.speech

import android.content.Context
import android.os.Handler
import android.speech.tts.TextToSpeech
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.ui.widgets.chat.audio.AudioPlaybackManager
import com.google.mlkit.nl.languageid.IdentifiedLanguage
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions
import java.util.*

class TextToSpeechManager(
    context: Context,
    private val onStartSpeech: (id: String) -> Unit,
    private val onEndSpeech: (id: String) -> Unit,
    private val onErrorSpeech: (id: String, throwable: Throwable) -> Unit
) {
    private var textToSpeech: TextToSpeech? = null
    private var isTextToSpeechInitialized: Boolean = false
    private val speakingIds = mutableSetOf<String>()
    private val mainHandler = Handler(context.mainLooper)
    private val languageIdentifier = LanguageIdentification.getClient(
        LanguageIdentificationOptions.Builder()
            .setConfidenceThreshold(0.01f)
            .build()
    )
    private val textToSpeechUnavailable = context.getString(R.string.msg_text_to_speech_unavailable)
    private val unavailableLanguageMsg = context.getString(R.string.msg_voicing_lang_unavailable)
    private val languageDataMissingMsg = context.getString(R.string.msg_voicing_lang_data_missing)
    private var availableLanguagesTags: HashSet<String>? = null
    var audioPlaybackManager: AudioPlaybackManager? = null

    init {
        textToSpeech = TextToSpeech(context, TextToSpeech.OnInitListener { initStatus ->
            when (initStatus) {
                TextToSpeech.SUCCESS -> {
                    textToSpeech?.let { initTextToSpeech(it) }
                }
                TextToSpeech.ERROR -> {
                }
                TextToSpeech.STOPPED -> {
                }
            }
        })
    }

    private fun initTextToSpeech(textToSpeech: TextToSpeech) {
        textToSpeech.language = Locale.getDefault()
        textToSpeech.setPitch(1.3f)
        textToSpeech.setSpeechRate(0.7f)
        textToSpeech.setOnUtteranceProgressListener(SimpleUtteranceProgressListener(
            ::onStartSpeech, ::onEndSpeech
        ))
        isTextToSpeechInitialized = true
        availableLanguagesTags = with(textToSpeech.availableLanguages) {
            val tags = HashSet<String>()
            for (availableLang in this) {
                tags.add(availableLang.language)
            }
            return@with tags
        }
    }

    private fun onStartSpeech(id: String) {
        mainHandler.post { onStartSpeech.invoke(id) }
    }

    private fun onEndSpeech(id: String) {
        mainHandler.post { onEndSpeech.invoke(id) }
    }

    fun speakOrStop(text: CharSequence, id: String) {
        val textToSpeech = textToSpeech
        if (textToSpeech == null || !isTextToSpeechInitialized) {
            onErrorSpeech.invoke(id, IllegalStateException(textToSpeechUnavailable))
            return
        }

        if (isSpeaking(id)) {
            stopSpeaking()
        } else {
            if (isAnySpeaking()) stopSpeaking()
            startSpeaking(text, id)
        }
    }

    private fun isSpeaking(id: String): Boolean = speakingIds.contains(id)

    private fun isAnySpeaking(): Boolean = speakingIds.isNotEmpty()

    fun stop() {
        stopSpeaking()
    }

    private fun stopSpeaking() {
        textToSpeech?.stop()
        for (id in speakingIds) {
            onEndSpeech(id)
        }
        speakingIds.clear()
    }

    private fun startSpeaking(text: CharSequence, id: String) {
        audioPlaybackManager?.pause()
        languageIdentifier.identifyPossibleLanguages(text.toString())
            .addOnSuccessListener { langs ->
                val locale = selectSuitableLang(langs)
                checkLanguageAvailabilityAndSpeak(locale, text, id)
            }
    }

    private fun selectSuitableLang(possibleLanguages: List<IdentifiedLanguage>): Locale {
        if (possibleLanguages.isEmpty()) return Locale.getDefault()
        val availableLanguages = availableLanguagesTags
            ?: return Locale.forLanguageTag(possibleLanguages[0].languageTag)

        for (possibleLang in possibleLanguages) {
            if (availableLanguages.contains(possibleLang.languageTag)) {
                return Locale.forLanguageTag(possibleLang.languageTag)
            }
        }

        return Locale.forLanguageTag(possibleLanguages[0].languageTag)
    }

    private fun checkLanguageAvailabilityAndSpeak(language: Locale, text: CharSequence, id: String) {
        val textToSpeech = textToSpeech!!

        when (textToSpeech.isLanguageAvailable(language)) {
            TextToSpeech.LANG_AVAILABLE -> {
                speak(textToSpeech, language, text, id)
            }
            TextToSpeech.LANG_COUNTRY_AVAILABLE -> {
                speak(textToSpeech, language, text, id)
            }
            TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE -> {
                speak(textToSpeech, language, text, id)
            }
            TextToSpeech.LANG_MISSING_DATA -> {
                onErrorSpeech.invoke(id, IllegalStateException(languageDataMissingMsg))
            }
            TextToSpeech.LANG_NOT_SUPPORTED -> {
                onErrorSpeech.invoke(id, IllegalArgumentException(unavailableLanguageMsg))
            }
        }
    }

    private fun speak(textToSpeech: TextToSpeech, language: Locale, text: CharSequence, id: String) {
        textToSpeech.language = language
        val result = textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)

        if (result == TextToSpeech.SUCCESS) {
            speakingIds.add(id)
            onStartSpeech(id)
        }
    }
}