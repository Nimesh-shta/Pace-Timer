package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.WorkoutConfig
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Walk/Jog Timer", appName)
  }

  @Test
  fun `verify beginner preset calculation`() {
    val config = WorkoutConfig.PRESET_BEGINNER
    assertEquals(300, config.warmupSeconds)
    assertEquals(60, config.jogSeconds)
    assertEquals(60, config.walkSeconds)
    assertEquals(5, config.repeats)
    // 300 + (60 + 60) * 5 = 300 + 600 = 900 seconds (15 minutes)
    assertEquals(900, config.totalDurationSeconds)
  }

  @Test
  fun `verify workout config saver save and restore`() {
    val original = WorkoutConfig(
      warmupSeconds = 120,
      jogSeconds = 45,
      walkSeconds = 75,
      repeats = 8,
      isMuted = true
    )
    val saved = with(WorkoutConfig.Saver) {
      // simulate Saver save
      androidx.compose.runtime.saveable.SaverScope { true }.save(original)
    }
    org.junit.Assert.assertNotNull(saved)
    val restored = WorkoutConfig.Saver.restore(saved!!)
    assertEquals(original, restored)
  }
}
