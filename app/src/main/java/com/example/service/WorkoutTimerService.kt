package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.MainActivity
import com.example.R
import com.example.data.WorkoutConfig
import com.example.data.WorkoutPhase
import com.example.data.WorkoutState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class WorkoutTimerService : Service() {

  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private var tickerJob: Job? = null
  private var wakeLock: PowerManager.WakeLock? = null
  private lateinit var audioCueManager: AudioCueManager
  private lateinit var notificationManager: NotificationManager

  companion object {
    const val CHANNEL_ID = "workout_interval_timer_channel"
    const val NOTIFICATION_ID = 2001

    const val ACTION_START = "com.example.service.ACTION_START"
    const val ACTION_PAUSE_RESUME = "com.example.service.ACTION_PAUSE_RESUME"
    const val ACTION_RESET = "com.example.service.ACTION_RESET"
    const val ACTION_TOGGLE_MUTE = "com.example.service.ACTION_TOGGLE_MUTE"
    const val ACTION_STOP = "com.example.service.ACTION_STOP"

    const val EXTRA_WARMUP = "extra_warmup"
    const val EXTRA_JOG = "extra_jog"
    const val EXTRA_WALK = "extra_walk"
    const val EXTRA_REPEATS = "extra_repeats"
    const val EXTRA_MUTED = "extra_muted"

    private val _workoutState = MutableStateFlow(WorkoutState())
    val workoutState: StateFlow<WorkoutState> = _workoutState.asStateFlow()

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    fun startWorkout(context: Context, config: WorkoutConfig) {
      val intent = Intent(context, WorkoutTimerService::class.java).apply {
        action = ACTION_START
        putExtra(EXTRA_WARMUP, config.warmupSeconds)
        putExtra(EXTRA_JOG, config.jogSeconds)
        putExtra(EXTRA_WALK, config.walkSeconds)
        putExtra(EXTRA_REPEATS, config.repeats)
        putExtra(EXTRA_MUTED, config.isMuted)
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
      } else {
        context.startService(intent)
      }
    }

    fun pauseResume(context: Context) {
      val intent = Intent(context, WorkoutTimerService::class.java).apply {
        action = ACTION_PAUSE_RESUME
      }
      context.startService(intent)
    }

    fun reset(context: Context) {
      val intent = Intent(context, WorkoutTimerService::class.java).apply {
        action = ACTION_RESET
      }
      context.startService(intent)
    }

    fun toggleMute(context: Context) {
      val intent = Intent(context, WorkoutTimerService::class.java).apply {
        action = ACTION_TOGGLE_MUTE
      }
      context.startService(intent)
    }

    fun stopWorkout(context: Context) {
      val intent = Intent(context, WorkoutTimerService::class.java).apply {
        action = ACTION_STOP
      }
      context.startService(intent)
    }
  }

  override fun onCreate() {
    super.onCreate()
    audioCueManager = AudioCueManager(this)
    notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    createNotificationChannel()

    val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
    wakeLock = powerManager.newWakeLock(
      PowerManager.PARTIAL_WAKE_LOCK,
      "WorkoutTimer::WakeLock"
    ).apply {
      setReferenceCounted(false)
    }
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      ACTION_START -> {
        val warmup = intent.getIntExtra(EXTRA_WARMUP, 300)
        val jog = intent.getIntExtra(EXTRA_JOG, 60)
        val walk = intent.getIntExtra(EXTRA_WALK, 60)
        val repeats = intent.getIntExtra(EXTRA_REPEATS, 5)
        val muted = intent.getBooleanExtra(EXTRA_MUTED, false)

        val config = WorkoutConfig(
          warmupSeconds = warmup,
          jogSeconds = jog,
          walkSeconds = walk,
          repeats = repeats,
          isMuted = muted
        )
        handleStart(config)
      }
      ACTION_PAUSE_RESUME -> handlePauseResume()
      ACTION_RESET -> handleReset()
      ACTION_TOGGLE_MUTE -> handleToggleMute()
      ACTION_STOP -> handleStop()
    }
    return START_STICKY
  }

  private fun handleStart(config: WorkoutConfig) {
    val initialPhase = if (config.warmupSeconds > 0) WorkoutPhase.WARMUP else WorkoutPhase.JOG
    val initialSegmentDuration = if (config.warmupSeconds > 0) config.warmupSeconds else config.jogSeconds

    _workoutState.value = WorkoutState(
      config = config,
      phase = initialPhase,
      currentRound = 1,
      totalRounds = config.repeats,
      segmentRemainingSeconds = initialSegmentDuration,
      segmentTotalSeconds = initialSegmentDuration,
      totalElapsedSeconds = 0,
      isRunning = true,
      isPaused = false,
      isCompleted = false,
      isMuted = config.isMuted
    )
    _isServiceRunning.value = true

    acquireWakeLock()
    startForegroundNotification()

    if (initialPhase == WorkoutPhase.WARMUP) {
      audioCueManager.onStartWarmup(config.isMuted)
    } else {
      audioCueManager.onTransitionToJog(config.isMuted)
    }

    startTicker()
  }

  private fun handlePauseResume() {
    val current = _workoutState.value
    if (current.isCompleted) return

    val newPaused = !current.isPaused
    _workoutState.value = current.copy(
      isPaused = newPaused,
      isRunning = !newPaused
    )

    if (newPaused) {
      releaseWakeLock()
      tickerJob?.cancel()
    } else {
      acquireWakeLock()
      startTicker()
    }
    updateNotification()
  }

  private fun handleReset() {
    val current = _workoutState.value
    val config = current.config
    val initialPhase = if (config.warmupSeconds > 0) WorkoutPhase.WARMUP else WorkoutPhase.JOG
    val initialSegmentDuration = if (config.warmupSeconds > 0) config.warmupSeconds else config.jogSeconds

    tickerJob?.cancel()
    releaseWakeLock()

    _workoutState.value = WorkoutState(
      config = config,
      phase = initialPhase,
      currentRound = 1,
      totalRounds = config.repeats,
      segmentRemainingSeconds = initialSegmentDuration,
      segmentTotalSeconds = initialSegmentDuration,
      totalElapsedSeconds = 0,
      isRunning = false,
      isPaused = true,
      isCompleted = false,
      isMuted = current.isMuted
    )
    updateNotification()
  }

  private fun handleToggleMute() {
    val current = _workoutState.value
    val newMuted = !current.isMuted
    _workoutState.value = current.copy(
      isMuted = newMuted,
      config = current.config.copy(isMuted = newMuted)
    )
  }

  private fun handleStop() {
    tickerJob?.cancel()
    releaseWakeLock()
    _isServiceRunning.value = false
    _workoutState.value = _workoutState.value.copy(
      isRunning = false,
      isPaused = false
    )
    stopForeground(STOP_FOREGROUND_REMOVE)
    stopSelf()
  }

  private fun startTicker() {
    tickerJob?.cancel()
    tickerJob = serviceScope.launch {
      while (isActive) {
        delay(1000L)
        val current = _workoutState.value
        if (!current.isRunning || current.isPaused || current.isCompleted) {
          continue
        }

        val nextRemaining = current.segmentRemainingSeconds - 1
        val nextElapsed = current.totalElapsedSeconds + 1

        // 3-second countdown warning tone
        if (nextRemaining == 3) {
          audioCueManager.playCountdownBeep(current.isMuted)
        }

        if (nextRemaining > 0) {
          _workoutState.value = current.copy(
            segmentRemainingSeconds = nextRemaining,
            totalElapsedSeconds = nextElapsed
          )
          updateNotification()
        } else {
          // Segment transition
          handleSegmentTransition(current, nextElapsed)
        }
      }
    }
  }

  private fun handleSegmentTransition(current: WorkoutState, nextElapsed: Int) {
    val config = current.config
    when (current.phase) {
      WorkoutPhase.WARMUP -> {
        // Warmup finished -> Transition to first Jog
        _workoutState.value = current.copy(
          phase = WorkoutPhase.JOG,
          currentRound = 1,
          segmentRemainingSeconds = config.jogSeconds,
          segmentTotalSeconds = config.jogSeconds,
          totalElapsedSeconds = nextElapsed
        )
        audioCueManager.onTransitionToJog(current.isMuted)
      }
      WorkoutPhase.JOG -> {
        // Jog finished -> Transition to Walk
        _workoutState.value = current.copy(
          phase = WorkoutPhase.WALK,
          segmentRemainingSeconds = config.walkSeconds,
          segmentTotalSeconds = config.walkSeconds,
          totalElapsedSeconds = nextElapsed
        )
        audioCueManager.onTransitionToWalk(current.isMuted)
      }
      WorkoutPhase.WALK -> {
        if (current.currentRound < current.totalRounds) {
          // Walk finished -> Next Round Jog
          val nextRound = current.currentRound + 1
          _workoutState.value = current.copy(
            phase = WorkoutPhase.JOG,
            currentRound = nextRound,
            segmentRemainingSeconds = config.jogSeconds,
            segmentTotalSeconds = config.jogSeconds,
            totalElapsedSeconds = nextElapsed
          )
          audioCueManager.onTransitionToJog(current.isMuted)
        } else {
          // All rounds finished -> Workout Complete!
          tickerJob?.cancel()
          releaseWakeLock()
          _workoutState.value = current.copy(
            phase = WorkoutPhase.COMPLETED,
            segmentRemainingSeconds = 0,
            totalElapsedSeconds = nextElapsed,
            isRunning = false,
            isPaused = false,
            isCompleted = true
          )
          audioCueManager.onWorkoutComplete(current.isMuted)
          updateNotification()
          return
        }
      }
      WorkoutPhase.COMPLETED -> {
        // Already completed
        return
      }
    }
    updateNotification()
  }

  private fun startForegroundNotification() {
    val notification = buildNotification(_workoutState.value)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      ServiceCompat.startForeground(
        this,
        NOTIFICATION_ID,
        notification,
        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
      )
    } else {
      startForeground(NOTIFICATION_ID, notification)
    }
  }

  private fun updateNotification() {
    try {
      val notification = buildNotification(_workoutState.value)
      notificationManager.notify(NOTIFICATION_ID, notification)
    } catch (e: Exception) {
      // Ignored if notification permission is absent on newer OS versions
    }
  }

  private fun buildNotification(state: WorkoutState): Notification {
    val openAppIntent = Intent(this, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val contentPendingIntent = PendingIntent.getActivity(
      this,
      0,
      openAppIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val pauseResumeIntent = Intent(this, WorkoutTimerService::class.java).apply {
      action = ACTION_PAUSE_RESUME
    }
    val pauseResumePendingIntent = PendingIntent.getService(
      this,
      1,
      pauseResumeIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val stopIntent = Intent(this, WorkoutTimerService::class.java).apply {
      action = ACTION_STOP
    }
    val stopPendingIntent = PendingIntent.getService(
      this,
      2,
      stopIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val phaseTitle = when (state.phase) {
      WorkoutPhase.WARMUP -> "Warmup Walk"
      WorkoutPhase.JOG -> "Jogging • Round ${state.currentRound}/${state.totalRounds}"
      WorkoutPhase.WALK -> "Walking • Round ${state.currentRound}/${state.totalRounds}"
      WorkoutPhase.COMPLETED -> "Workout Complete!"
    }

    val statusText = if (state.isCompleted) {
      "Great job! Total time: ${state.formattedTotalElapsed}"
    } else {
      val pauseLabel = if (state.isPaused) " [PAUSED]" else ""
      "${state.formattedSegmentRemaining} left • Total: ${state.formattedTotalElapsed} / ${WorkoutConfig.formatSecondsPadded(state.config.totalDurationSeconds)}$pauseLabel"
    }

    val accentColor = when (state.phase) {
      WorkoutPhase.JOG -> Color.parseColor("#FF5C38") // Coral
      WorkoutPhase.WALK, WorkoutPhase.WARMUP -> Color.parseColor("#00E5BE") // Teal
      WorkoutPhase.COMPLETED -> Color.parseColor("#22C55E") // Green
    }

    val pauseResumeActionTitle = if (state.isPaused) "Resume" else "Pause"
    val pauseResumeIcon = if (state.isPaused) {
      android.R.drawable.ic_media_play
    } else {
      android.R.drawable.ic_media_pause
    }

    val builder = NotificationCompat.Builder(this, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_timer_notification)
      .setContentTitle(phaseTitle)
      .setContentText(statusText)
      .setContentIntent(contentPendingIntent)
      .setOngoing(!state.isCompleted)
      .setOnlyAlertOnce(true)
      .setColor(accentColor)
      .setColorized(true)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setPriority(NotificationCompat.PRIORITY_LOW)

    if (!state.isCompleted) {
      builder.addAction(pauseResumeIcon, pauseResumeActionTitle, pauseResumePendingIntent)
      builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
    } else {
      builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Close", stopPendingIntent)
    }

    return builder.build()
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "Workout Interval Timer",
        NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = "Shows active interval training segment countdown and controls"
        setShowBadge(false)
        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
      }
      notificationManager.createNotificationChannel(channel)
    }
  }

  private fun acquireWakeLock() {
    try {
      if (wakeLock?.isHeld != true) {
        wakeLock?.acquire(60 * 60 * 1000L) // 60 minutes safety timeout
      }
    } catch (e: Exception) {
      // Ignored
    }
  }

  private fun releaseWakeLock() {
    try {
      if (wakeLock?.isHeld == true) {
        wakeLock?.release()
      }
    } catch (e: Exception) {
      // Ignored
    }
  }

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onDestroy() {
    super.onDestroy()
    tickerJob?.cancel()
    serviceScope.cancel()
    releaseWakeLock()
    audioCueManager.release()
    _isServiceRunning.value = false
  }
}
