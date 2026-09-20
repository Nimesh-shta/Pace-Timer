package com.example.data

enum class WorkoutPhase(val displayName: String) {
  WARMUP("Warmup walk"),
  JOG("Jog"),
  WALK("Walk"),
  COMPLETED("Workout Complete")
}

data class WorkoutState(
  val config: WorkoutConfig = WorkoutConfig(),
  val phase: WorkoutPhase = WorkoutPhase.WARMUP,
  val currentRound: Int = 1,
  val totalRounds: Int = 5,
  val segmentRemainingSeconds: Int = 300,
  val segmentTotalSeconds: Int = 300,
  val totalElapsedSeconds: Int = 0,
  val isRunning: Boolean = false,
  val isPaused: Boolean = false,
  val isCompleted: Boolean = false,
  val isMuted: Boolean = false
) {
  val segmentProgress: Float
    get() = if (segmentTotalSeconds > 0) {
      (segmentTotalSeconds - segmentRemainingSeconds).toFloat() / segmentTotalSeconds.toFloat()
    } else 1f

  val formattedSegmentRemaining: String
    get() = WorkoutConfig.formatSecondsPadded(segmentRemainingSeconds)

  val formattedTotalElapsed: String
    get() = WorkoutConfig.formatSecondsPadded(totalElapsedSeconds)

  val formattedTotalRemaining: String
    get() {
      val remaining = (config.totalDurationSeconds - totalElapsedSeconds).coerceAtLeast(0)
      return WorkoutConfig.formatSecondsPadded(remaining)
    }
}
