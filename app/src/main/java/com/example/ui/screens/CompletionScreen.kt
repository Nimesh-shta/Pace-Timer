package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WorkoutConfig
import com.example.data.WorkoutState
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
fun CompletionScreen(
  state: WorkoutState,
  onDone: () -> Unit,
  modifier: Modifier = Modifier
) {
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
        .padding(horizontal = 24.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      // Center Hero Section
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
      ) {
        // Glowing Celebration Badge
        Box(
          modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(
              Brush.radialGradient(
                colors = listOf(TealWalk.copy(alpha = 0.35f), Color.Transparent)
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier
              .size(76.dp)
              .clip(CircleShape)
              .background(TealWalk.copy(alpha = 0.2f))
              .border(2.dp, TealWalk, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = "Workout Complete",
              tint = TealWalk,
              modifier = Modifier.size(44.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
          text = "Workout Complete!",
          color = TextPrimary,
          fontSize = 28.sp,
          fontWeight = FontWeight.Black,
          modifier = Modifier.testTag("completion_title")
        )

        Text(
          text = "Awesome interval session completed.",
          color = TextSecondary,
          fontSize = 15.sp,
          modifier = Modifier.padding(top = 6.dp)
        )
      }

      // Stats Grid Card
      Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          Text(
            text = "SESSION STATS",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            StatItem(
              title = "Total Time",
              value = state.formattedTotalElapsed,
              accentColor = TextPrimary,
              icon = Icons.Default.FitnessCenter
            )

            StatItem(
              title = "Rounds",
              value = "${state.totalRounds} / ${state.totalRounds}",
              accentColor = TealWalk,
              icon = Icons.Default.CheckCircle
            )
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(1.dp)
              .background(DarkSurfaceBorder)
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            StatItem(
              title = "Jog Time",
              value = WorkoutConfig.formatMinutesSeconds(state.config.totalJogSeconds),
              accentColor = CoralJog,
              icon = Icons.Default.DirectionsRun
            )

            StatItem(
              title = "Walk Time",
              value = WorkoutConfig.formatMinutesSeconds(state.config.totalWalkSeconds),
              accentColor = TealWalk,
              icon = Icons.Default.DirectionsWalk
            )
          }
        }
      }

      // Done / Return to Setup Button
      Button(
        onClick = onDone,
        colors = ButtonDefaults.buttonColors(
          containerColor = TealWalk,
          contentColor = Color.Black
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .testTag("completion_done_button")
      ) {
        Icon(
          imageVector = Icons.Default.Replay,
          contentDescription = null,
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Start New Workout",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

@Composable
private fun StatItem(
  title: String,
  value: String,
  accentColor: Color,
  icon: ImageVector
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(DarkSurfaceVariant),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = accentColor,
        modifier = Modifier.size(20.dp)
      )
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(
        text = title,
        color = TextSecondary,
        fontSize = 12.sp
      )
      Text(
        text = value,
        color = TextPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}
