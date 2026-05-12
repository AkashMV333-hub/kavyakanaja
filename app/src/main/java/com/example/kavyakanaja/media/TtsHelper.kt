package com.example.kavyakanaja.media

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsHelper(context: Context, private val onInit: (Boolean) -> Unit = {}) {
    private val tts: TextToSpeech = TextToSpeech(context) { status ->
        onInit(status == TextToSpeech.SUCCESS)
    }

    fun speak(text: String, locale: Locale = Locale("kn")) {
        try {
            tts.language = locale
        } catch (e: Exception) {
            // fallback to default
        }
        tts.setPitch(1.0f)
        tts.setSpeechRate(0.9f)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun stop() {
        if (tts.isSpeaking) tts.stop()
    }

    fun shutdown() {
        tts.shutdown()
    }
}

