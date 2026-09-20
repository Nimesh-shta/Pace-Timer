package com.example.data

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver

data class WorkoutConfig(
  val warmupSeconds: Int = 300,  // 5:00
  val jogSeconds: Int = 60,      // 1:00
  val walkSeconds: Int = 60,     // 1:00
  val repeats: Int = 5,          // 1..20
  val isMuted: Boolean = false
) {
  val totalIntervalSeconds: Int
    get() = (jogSeconds + walkSeconds) * repeats

  val totalDurationSeconds: Int
    get() = warmupSeconds + totalIntervalSeconds

  val totalJogSeconds: Int
    get() = jogSeconds * repeats

  val totalWalkSeconds: Int
    get() = warmupSeconds + (walkSeconds * repeats)

  companion object {
    val Saver: Saver<WorkoutConfig, Any> = listSaver(
      save = { listOf(it.warmupSeconds, it.jogSeconds, it.walkSeconds, it.repeats, it.isMuted) },
      restore = { list ->
        WorkoutConfig(
          warmupSeconds = list[0] as Int,
          jogSeconds = list[1] as Int,
          walkSeconds = list[2] as Int,
          repeats = list[3] as Int,
          isMuted = list[4] as Boolean
        )
      }
    )

    val PRESET_BEGINNER = WorkoutConfig(
      warmupSeconds = 300,
      jogSeconds = 60,
      walkSeconds = 60,
      repeats = 5
    )

    val PRESET_SHORT = WorkoutConfig(
      warmupSeconds = 0,
      jogSeconds = 30,
      walkSeconds = 90,
      repeats = 5
    )

    fun formatMinutesSeconds(totalSeconds: Int): String {
      val minutes = totalSeconds / 60
      val seconds = totalSeconds % 60
      return String.format("%d:%02d", minutes, seconds)
    }

    fun formatSecondsPadded(totalSeconds: Int): String {
      val minutes = totalSeconds / 60
      val seconds = totalSeconds % 60
      return String.format("%02d:%02d", minutes, seconds)
    }
  }
}
