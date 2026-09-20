package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WorkoutConfig
import com.example.ui.components.DurationAdjusterCard
import com.example.ui.components.RepeatsAdjusterCard
import com.example.ui.components.WorkoutStructureTimeline
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
fun SetupScreen(
  config: WorkoutConfig,
  onConfigChange: (WorkoutConfig) -> Unit,
  onStartWorkout: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp),
      contentPadding = PaddingValues(top = 20.dp, bottom = 120.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Header
      item {
        Column(modifier = Modifier.padding(bottom = 4.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column {
              Text(
                text = "Interval Timer",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
              )
              Text(
                text = "Walk & Jog Interval Training",
                color = TextSecondary,
                fontSize = 14.sp
              )
            }

            // Audio Mute Quick Toggle
            Surface(
              onClick = { onConfigChange(config.copy(isMuted = !config.isMuted)) },
              shape = RoundedCornerShape(12.dp),
              color = if (config.isMuted) DarkSurfaceVariant else DarkSurface,
              border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
              modifier = Modifier.testTag("setup_audio_toggle")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
              ) {
                Icon(
                  imageVector = if (config.isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                  contentDescription = if (config.isMuted) "Audio muted" else "Audio enabled",
                  tint = if (config.isMuted) TextMuted else TealWalk,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (config.isMuted) "Muted" else "Voice On",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = if (config.isMuted) TextMuted else TextPrimary
                )
              }
            }
          }
        }
      }

      // Presets Selector
      item {
        Column {
          Text(
            text = "QUICK PRESETS",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            val isBeginnerSelected = config.warmupSeconds == 300 && config.jogSeconds == 60 && config.walkSeconds == 60 && config.repeats == 5
            val isShortSelected = config.warmupSeconds == 0 && config.jogSeconds == 30 && config.walkSeconds == 90 && config.repeats == 5

            PresetCard(
              title = "Beginner",
              description = "5:00 warmup • 1m/1m x5",
              isSelected = isBeginnerSelected,
              accentColor = TealWalk,
              onClick = {
                onConfigChange(WorkoutConfig.PRESET_BEGINNER.copy(isMuted = config.isMuted))
              },
              modifier = Modifier
                .weight(1f)
                .testTag("preset_beginner_button")
            )

            PresetCard(
              title = "Short",
              description = "No warmup • 30s/90s x5",
              isSelected = isShortSelected,
              accentColor = CoralJog,
              onClick = {
                onConfigChange(WorkoutConfig.PRESET_SHORT.copy(isMuted = config.isMuted))
              },
              modifier = Modifier
                .weight(1f)
                .testTag("preset_short_button")
            )
          }
        }
      }

      // Durations Configuration
      item {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            text = "INTERVAL TIMINGS",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
          )

          // Warmup walk (step: 30s, min: 0s, max: 1800s)
          DurationAdjusterCard(
            title = "Warmup Walk",
            subtitle = if (config.warmupSeconds == 0) "Disabled" else "30s adjustments",
            durationSeconds = config.warmupSeconds,
            stepSeconds = 30,
            minSeconds = 0,
            maxSeconds = 1800,
            accentColor = TealWalk,
            onValueChange = { onConfigChange(config.copy(warmupSeconds = it)) },
            testTagPrefix = "warmup"
          )

          // Jog duration (step: 15s, min: 15s, max: 1800s)
          DurationAdjusterCard(
            title = "Jog Duration",
            subtitle = "15s adjustments",
            durationSeconds = config.jogSeconds,
            stepSeconds = 15,
            minSeconds = 15,
            maxSeconds = 1800,
            accentColor = CoralJog,
            onValueChange = { onConfigChange(config.copy(jogSeconds = it)) },
            testTagPrefix = "jog"
          )

          // Walk duration (step: 15s, min: 15s, max: 1800s)
          DurationAdjusterCard(
            title = "Walk Duration",
            subtitle = "15s adjustments",
            durationSeconds = config.walkSeconds,
            stepSeconds = 15,
            minSeconds = 15,
            maxSeconds = 1800,
            accentColor = TealWalk,
            onValueChange = { onConfigChange(config.copy(walkSeconds = it)) },
            testTagPrefix = "walk"
          )

          // Repeats (1 to 20)
          RepeatsAdjusterCard(
            repeats = config.repeats,
            onRepeatsChange = { onConfigChange(config.copy(repeats = it)) }
          )
        }
      }

      // Live Summary & Structure Timeline
      item {
        WorkoutStructureTimeline(config = config)
      }
    }

    // Bottom Sticky Start Button
    Surface(
      color = DarkBackground.copy(alpha = 0.95f),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
      Button(
        onClick = onStartWorkout,
        colors = ButtonDefaults.buttonColors(
          containerColor = CoralJog,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .testTag("start_workout_button")
      ) {
        Icon(
          imageVector = Icons.Default.PlayArrow,
          contentDescription = null,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Start Workout",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

@Composable
private fun PresetCard(
  title: String,
  description: String,
  isSelected: Boolean,
  accentColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(16.dp),
    color = if (isSelected) DarkSurfaceVariant else DarkSurface,
    border = androidx.compose.foundation.BorderStroke(
      width = if (isSelected) 2.dp else 1.dp,
      color = if (isSelected) accentColor else DarkSurfaceBorder
    ),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(14.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = title,
          color = if (isSelected) accentColor else TextPrimary,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
        if (isSelected) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(accentColor)
          )
        }
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = description,
        color = TextSecondary,
        fontSize = 11.sp,
        lineHeight = 15.sp
      )
    }
  }
}
