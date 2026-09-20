package com.example.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class AudioCueManager(private val context: Context) : TextToSpeech.OnInitListener {

  private var tts: TextToSpeech? = null
  private var isTtsInitialized = false
  private var toneGenerator: ToneGenerator? = null
  private var pendingSpeech: String? = null

  private val vibrator: Vibrator? by lazy {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      vibratorManager?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
  }

  init {
    try {
      toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    } catch (e: Exception) {
      Log.e("AudioCueManager", "Failed to initialize ToneGenerator", e)
    }

    try {
      tts = TextToSpeech(context.applicationContext, this)
    } catch (e: Exception) {
      Log.e("AudioCueManager", "Failed to initialize TextToSpeech", e)
    }
  }

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      val result = tts?.setLanguage(Locale.US)
      if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
        tts?.setLanguage(Locale.getDefault())
      }
      tts?.setSpeechRate(1.05f)
      tts?.setPitch(1.0f)
      isTtsInitialized = true

      pendingSpeech?.let { text ->
        speak(text)
        pendingSpeech = null
      }
    } else {
      Log.e("AudioCueManager", "TTS init failed with status: $status")
    }
  }

  fun speak(text: String, isMuted: Boolean = false) {
    if (isMuted) return

    if (!isTtsInitialized) {
      pendingSpeech = text
      return
    }

    try {
      tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "workout_tts_${System.currentTimeMillis()}")
    } catch (e: Exception) {
      Log.e("AudioCueManager", "Error in speak()", e)
    }
  }

  fun playCountdownBeep(isMuted: Boolean = false) {
    vibrate(longArrayOf(0, 70), -1)
    if (isMuted) return
    try {
      toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 160)
    } catch (e: Exception) {
      Log.e("AudioCueManager", "Error playing countdown beep", e)
    }
  }

  fun onTransitionToJog(isMuted: Boolean = false) {
    vibrate(longArrayOf(0, 100, 80, 150), -1)
    if (!isMuted) {
      try {
        // High energetic tone for jog
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 220)
      } catch (e: Exception) {
        Log.e("AudioCueManager", "Error playing jog tone", e)
      }
    }
    speak("Jog", isMuted)
  }

  fun onTransitionToWalk(isMuted: Boolean = false) {
    vibrate(longArrayOf(0, 180), -1)
    if (!isMuted) {
      try {
        // Calmer double tone for walk
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_LOW_L, 200)
      } catch (e: Exception) {
        Log.e("AudioCueManager", "Error playing walk tone", e)
      }
    }
    speak("Walk", isMuted)
  }

  fun onStartWarmup(isMuted: Boolean = false) {
    vibrate(longArrayOf(0, 150), -1)
    if (!isMuted) {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 200)
      } catch (e: Exception) {
        Log.e("AudioCueManager", "Error playing warmup tone", e)
      }
    }
    speak("Warmup walk", isMuted)
  }

  fun onWorkoutComplete(isMuted: Boolean = false) {
    vibrate(longArrayOf(0, 120, 80, 120, 80, 300), -1)
    if (!isMuted) {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 450)
      } catch (e: Exception) {
        Log.e("AudioCueManager", "Error playing completion tone", e)
      }
    }
    speak("Workout complete", isMuted)
  }

  private fun vibrate(timings: LongArray, repeatIndex: Int) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(VibrationEffect.createWaveform(timings, repeatIndex))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(timings, repeatIndex)
      }
    } catch (e: Exception) {
      Log.e("AudioCueManager", "Error vibrating", e)
    }
  }

  fun release() {
    try {
      tts?.stop()
      tts?.shutdown()
      tts = null
    } catch (e: Exception) {
      Log.e("AudioCueManager", "Error shutting down TTS", e)
    }

    try {
      toneGenerator?.release()
      toneGenerator = null
    } catch (e: Exception) {
      Log.e("AudioCueManager", "Error releasing ToneGenerator", e)
    }
  }
}
