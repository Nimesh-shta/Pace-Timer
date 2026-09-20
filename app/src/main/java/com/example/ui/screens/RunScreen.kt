package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WorkoutPhase
import com.example.data.WorkoutState
import com.example.ui.components.CircularTimerRing
import com.example.ui.components.RoundProgressDots
import com.example.ui.theme.CoralJog
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TealWalk
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RunScreen(
  state: WorkoutState,
  onPauseResume: () -> Unit,
  onReset: () -> Unit,
  onToggleMute: () -> Unit,
  onStop: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showStopConfirmDialog by remember { mutableStateOf(false) }

  val accentColor = when (state.phase) {
    WorkoutPhase.JOG -> CoralJog
    WorkoutPhase.WALK, WorkoutPhase.WARMUP -> TealWalk
    WorkoutPhase.COMPLETED -> Color(0xFF22C55E)
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .statusBarsPadding()
      .navigationBarsPadding()
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top Navigation / Status Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Exit / Stop Button
        IconButton(
          onClick = { showStopConfirmDialog = true },
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(DarkSurfaceVariant)
            .testTag("exit_workout_button")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Stop Workout",
            tint = TextPrimary
          )
        }

        // Voice Cue Mute Toggle
        Surface(
          onClick = onToggleMute,
          shape = RoundedCornerShape(12.dp),
          color = if (state.isMuted) DarkSurfaceVariant else DarkSurface,
          border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
          modifier = Modifier.testTag("mute_toggle_button")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
          ) {
            Icon(
              imageVector = if (state.isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
              contentDescription = if (state.isMuted) "Unmute voice cues" else "Mute voice cues",
              tint = if (state.isMuted) TextMuted else accentColor,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (state.isMuted) "Muted" else "Voice On",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = if (state.isMuted) TextMuted else TextPrimary
            )
          }
        }
      }

      // Round Progress Indicator & Dots
      RoundProgressDots(
        currentRound = state.currentRound,
        totalRounds = state.totalRounds,
        currentPhase = state.phase,
        modifier = Modifier.padding(top = 8.dp)
      )

      // Circular Countdown Timer
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.padding(vertical = 12.dp)
      ) {
        CircularTimerRing(
          progress = state.segmentProgress,
          phase = state.phase,
          timeFormatted = state.formattedSegmentRemaining,
          size = 280.dp
        )
      }

      // Secondary Stats Strip: Total Elapsed & Remaining
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(DarkSurface)
          .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp))
          .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "ELAPSED",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
          Text(
            text = state.formattedTotalElapsed,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.testTag("total_elapsed_text")
          )
        }

        Box(
          modifier = Modifier
            .width(1.dp)
            .height(28.dp)
            .background(DarkSurfaceBorder)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "TOTAL LEFT",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
          Text(
            text = state.formattedTotalRemaining,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.testTag("total_remaining_text")
          )
        }
      }

      // Bottom Main Controls: Reset and Pause/Resume
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Reset Button
        IconButton(
          onClick = onReset,
          modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkSurfaceBorder, CircleShape)
            .testTag("reset_button")
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Reset segment",
            tint = TextSecondary,
            modifier = Modifier.size(26.dp)
          )
        }

        Spacer(modifier = Modifier.width(32.dp))

        // Large Primary Play/Pause Button
        val isPaused = state.isPaused || !state.isRunning
        Surface(
          onClick = onPauseResume,
          shape = CircleShape,
          color = accentColor,
          shadowElevation = 8.dp,
          modifier = Modifier
            .size(80.dp)
            .testTag("pause_resume_button")
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
          ) {
            Icon(
              imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
              contentDescription = if (isPaused) "Resume workout" else "Pause workout",
              tint = Color.White,
              modifier = Modifier.size(42.dp)
            )
          }
        }
      }
    }
  }

  if (showStopConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showStopConfirmDialog = false },
      containerColor = DarkSurface,
      title = {
        Text(
          text = "End Workout?",
          color = TextPrimary,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Text(
          text = "Do you want to stop this workout session? Current progress will be ended.",
          color = TextSecondary
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            showStopConfirmDialog = false
            onStop()
          },
          modifier = Modifier.testTag("confirm_stop_button")
        ) {
          Text(text = "End Workout", color = CoralJog, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showStopConfirmDialog = false }) {
          Text(text = "Keep Going", color = TextSecondary)
        }
      }
    )
  }
}
