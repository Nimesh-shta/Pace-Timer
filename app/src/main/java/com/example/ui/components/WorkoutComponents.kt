package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WorkoutConfig
import com.example.data.WorkoutPhase
import com.example.ui.theme.CoralJog
import com.example.ui.theme.CoralJogContainer
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TealWalk
import com.example.ui.theme.TealWalkContainer
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CircularTimerRing(
  progress: Float,
  phase: WorkoutPhase,
  timeFormatted: String,
  modifier: Modifier = Modifier,
  strokeWidth: Dp = 14.dp,
  size: Dp = 270.dp
) {
  val animatedProgress by animateFloatAsState(
    targetValue = progress.coerceIn(0f, 1f),
    animationSpec = tween(durationMillis = 350),
    label = "TimerProgress"
  )

  val ringColor = when (phase) {
    WorkoutPhase.JOG -> CoralJog
    WorkoutPhase.WALK, WorkoutPhase.WARMUP -> TealWalk
    WorkoutPhase.COMPLETED -> Color(0xFF22C55E)
  }

  val trackColor = DarkSurfaceVariant

  Box(
    modifier = modifier.size(size),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.size(size)) {
      val strokePx = strokeWidth.toPx()
      val arcSize = this.size.width - strokePx
      val offset = strokePx / 2f

      // Background Track
      drawArc(
        color = trackColor,
        startAngle = -90f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = androidx.compose.ui.geometry.Offset(offset, offset),
        size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
        style = Stroke(width = strokePx, cap = StrokeCap.Round)
      )

      // Active Progress Arc
      val sweep = animatedProgress * 360f
      if (sweep > 0f) {
        drawArc(
          color = ringColor,
          startAngle = -90f,
          sweepAngle = sweep,
          useCenter = false,
          topLeft = androidx.compose.ui.geometry.Offset(offset, offset),
          size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
          style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )
      }
    }

    // Inside Ring: Digits and Status
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.padding(24.dp)
    ) {
      // Phase Badge
      val badgeBg = when (phase) {
        WorkoutPhase.JOG -> CoralJogContainer
        WorkoutPhase.WALK, WorkoutPhase.WARMUP -> TealWalkContainer
        WorkoutPhase.COMPLETED -> Color(0xFF13381B)
      }
      val badgeTextColor = when (phase) {
        WorkoutPhase.JOG -> CoralJog
        WorkoutPhase.WALK, WorkoutPhase.WARMUP -> TealWalk
        WorkoutPhase.COMPLETED -> Color(0xFF4ADE80)
      }

      Surface(
        color = badgeBg,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(bottom = 12.dp)
      ) {
        Text(
          text = phase.displayName.uppercase(),
          color = badgeTextColor,
          fontSize = 13.sp,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 1.5.sp,
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
      }

      // Timer Display
      Text(
        text = timeFormatted,
        color = TextPrimary,
        fontSize = 58.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 1.sp,
        modifier = Modifier.testTag("segment_timer_text")
      )

      Text(
        text = "REMAINING",
        color = TextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(top = 4.dp)
      )
    }
  }
}

@Composable
fun RoundProgressDots(
  currentRound: Int,
  totalRounds: Int,
  currentPhase: WorkoutPhase,
  modifier: Modifier = Modifier
) {
  val activeColor = if (currentPhase == WorkoutPhase.JOG) CoralJog else TealWalk

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.fillMaxWidth()
  ) {
    Text(
      text = if (currentPhase == WorkoutPhase.WARMUP) {
        "Warmup Phase"
      } else if (currentPhase == WorkoutPhase.COMPLETED) {
        "All $totalRounds Rounds Finished"
      } else {
        "Round $currentRound of $totalRounds"
      },
      color = TextSecondary,
      fontSize = 15.sp,
      fontWeight = FontWeight.SemiBold,
      modifier = Modifier
        .padding(bottom = 10.dp)
        .testTag("round_indicator_label")
    )

    Row(
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .testTag("round_progress_dots_row")
    ) {
      for (round in 1..totalRounds) {
        val isPast = round < currentRound || currentPhase == WorkoutPhase.COMPLETED
        val isCurrent = round == currentRound && currentPhase != WorkoutPhase.COMPLETED && currentPhase != WorkoutPhase.WARMUP

        Box(
          modifier = Modifier
            .padding(horizontal = 4.dp)
            .size(if (isCurrent) 14.dp else 10.dp)
            .clip(CircleShape)
            .background(
              when {
                isCurrent -> activeColor
                isPast -> activeColor.copy(alpha = 0.6f)
                else -> DarkSurfaceVariant
              }
            )
            .then(
              if (isCurrent) {
                Modifier.border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
              } else if (!isPast) {
                Modifier.border(1.dp, DarkSurfaceBorder, CircleShape)
              } else {
                Modifier
              }
            )
        )
      }
    }
  }
}

@Composable
fun DurationAdjusterCard(
  title: String,
  subtitle: String,
  durationSeconds: Int,
  stepSeconds: Int,
  minSeconds: Int,
  maxSeconds: Int,
  accentColor: Color,
  onValueChange: (Int) -> Unit,
  modifier: Modifier = Modifier,
  testTagPrefix: String
) {
  Surface(
    color = DarkSurface,
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
    modifier = modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          color = TextPrimary,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = subtitle,
          color = TextSecondary,
          fontSize = 12.sp,
          modifier = Modifier.padding(top = 2.dp)
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Minus Button
        IconButton(
          onClick = {
            val newValue = (durationSeconds - stepSeconds).coerceAtLeast(minSeconds)
            onValueChange(newValue)
          },
          enabled = durationSeconds > minSeconds,
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(DarkSurfaceVariant)
            .testTag("${testTagPrefix}_decrement")
        ) {
          Icon(
            imageVector = Icons.Default.Remove,
            contentDescription = "Decrease $title",
            tint = if (durationSeconds > minSeconds) TextPrimary else TextMuted
          )
        }

        // Display Value
        Box(
          modifier = Modifier
            .width(68.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = WorkoutConfig.formatMinutesSeconds(durationSeconds),
            color = accentColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.testTag("${testTagPrefix}_value")
          )
        }

        // Plus Button
        IconButton(
          onClick = {
            val newValue = (durationSeconds + stepSeconds).coerceAtMost(maxSeconds)
            onValueChange(newValue)
          },
          enabled = durationSeconds < maxSeconds,
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(DarkSurfaceVariant)
            .testTag("${testTagPrefix}_increment")
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Increase $title",
            tint = if (durationSeconds < maxSeconds) TextPrimary else TextMuted
          )
        }
      }
    }
  }
}

@Composable
fun RepeatsAdjusterCard(
  repeats: Int,
  onRepeatsChange: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    color = DarkSurface,
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
    modifier = modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Interval Repeats",
          color = TextPrimary,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Jog + Walk cycles (1–20)",
          color = TextSecondary,
          fontSize = 12.sp,
          modifier = Modifier.padding(top = 2.dp)
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        IconButton(
          onClick = { if (repeats > 1) onRepeatsChange(repeats - 1) },
          enabled = repeats > 1,
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(DarkSurfaceVariant)
            .testTag("repeats_decrement")
        ) {
          Icon(
            imageVector = Icons.Default.Remove,
            contentDescription = "Decrease repeats",
            tint = if (repeats > 1) TextPrimary else TextMuted
          )
        }

        Box(
          modifier = Modifier
            .width(68.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(10.dp)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "$repeats",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("repeats_value")
          )
        }

        IconButton(
          onClick = { if (repeats < 20) onRepeatsChange(repeats + 1) },
          enabled = repeats < 20,
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(DarkSurfaceVariant)
            .testTag("repeats_increment")
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Increase repeats",
            tint = if (repeats < 20) TextPrimary else TextMuted
          )
        }
      }
    }
  }
}

@Composable
fun WorkoutStructureTimeline(
  config: WorkoutConfig,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(DarkSurface)
      .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp))
      .padding(16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "WORKOUT SUMMARY",
        color = TextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp
      )
      Text(
        text = WorkoutConfig.formatMinutesSeconds(config.totalDurationSeconds) + " total",
        color = TextPrimary,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.testTag("summary_total_time")
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Visual Timeline Bar
    val totalSecs = config.totalDurationSeconds.coerceAtLeast(1).toFloat()
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(10.dp)
        .clip(RoundedCornerShape(5.dp))
        .background(DarkSurfaceVariant)
    ) {
      if (config.warmupSeconds > 0) {
        val warmupWeight = config.warmupSeconds / totalSecs
        Box(
          modifier = Modifier
            .weight(warmupWeight)
            .height(10.dp)
            .background(TealWalk)
        )
      }
      for (i in 0 until config.repeats) {
        val jogWeight = config.jogSeconds / totalSecs
        val walkWeight = config.walkSeconds / totalSecs
        Box(
          modifier = Modifier
            .weight(jogWeight)
            .height(10.dp)
            .background(CoralJog)
        )
        Box(
          modifier = Modifier
            .weight(walkWeight)
            .height(10.dp)
            .background(TealWalk.copy(alpha = 0.75f))
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Legend Breakdown
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      if (config.warmupSeconds > 0) {
        TimelineLegendItem(
          label = "Warmup",
          value = WorkoutConfig.formatMinutesSeconds(config.warmupSeconds),
          color = TealWalk
        )
      }
      TimelineLegendItem(
        label = "Jog (${config.repeats}x)",
        value = WorkoutConfig.formatMinutesSeconds(config.totalJogSeconds),
        color = CoralJog
      )
      TimelineLegendItem(
        label = "Walk (${config.repeats}x)",
        value = WorkoutConfig.formatMinutesSeconds(config.walkSeconds * config.repeats),
        color = TealWalk.copy(alpha = 0.75f)
      )
    }
  }
}

@Composable
private fun TimelineLegendItem(
  label: String,
  value: String,
  color: Color
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .clip(CircleShape)
        .background(color)
    )
    Spacer(modifier = Modifier.width(6.dp))
    Column {
      Text(
        text = label,
        color = TextSecondary,
        fontSize = 11.sp
      )
      Text(
        text = value,
        color = TextPrimary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}
