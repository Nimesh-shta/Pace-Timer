package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.data.WorkoutConfig
import com.example.service.WorkoutTimerService
import com.example.ui.screens.CompletionScreen
import com.example.ui.screens.RunScreen
import com.example.ui.screens.SetupScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme {
        Scaffold(
          containerColor = DarkBackground,
          modifier = Modifier.fillMaxSize()
        ) { _ ->
          IntervalTimerApp()
        }
      }
    }
  }
}

@Composable
fun IntervalTimerApp() {
  val context = LocalContext.current
  val workoutState by WorkoutTimerService.workoutState.collectAsState()
  val isServiceRunning by WorkoutTimerService.isServiceRunning.collectAsState()

  // Local config state for setup adjustments
  var currentConfig by rememberSaveable(stateSaver = WorkoutConfig.Saver) {
    mutableStateOf(WorkoutConfig.PRESET_BEGINNER)
  }

  // Permission launcher for POST_NOTIFICATIONS on Android 13+
  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { _ ->
    // Regardless of permission outcome, start the workout foreground service
    WorkoutTimerService.startWorkout(context, currentConfig)
  }

  fun checkAndStartWorkout() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      val hasPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED

      if (!hasPermission) {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      } else {
        WorkoutTimerService.startWorkout(context, currentConfig)
      }
    } else {
      WorkoutTimerService.startWorkout(context, currentConfig)
    }
  }

  when {
    workoutState.isCompleted -> {
      CompletionScreen(
        state = workoutState,
        onDone = {
          WorkoutTimerService.stopWorkout(context)
        }
      )
    }
    isServiceRunning -> {
      RunScreen(
        state = workoutState,
        onPauseResume = {
          WorkoutTimerService.pauseResume(context)
        },
        onReset = {
          WorkoutTimerService.reset(context)
        },
        onToggleMute = {
          WorkoutTimerService.toggleMute(context)
          currentConfig = currentConfig.copy(isMuted = !workoutState.isMuted)
        },
        onStop = {
          WorkoutTimerService.stopWorkout(context)
        }
      )
    }
    else -> {
      SetupScreen(
        config = currentConfig,
        onConfigChange = { newConfig ->
          currentConfig = newConfig
        },
        onStartWorkout = {
          checkAndStartWorkout()
        }
      )
    }
  }
}
