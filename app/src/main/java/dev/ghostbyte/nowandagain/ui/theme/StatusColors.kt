package dev.ghostbyte.nowandagain.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import dev.ghostbyte.nowandagain.domain.TaskStatus

@Immutable
data class StatusColors(val success: Color, val warning: Color, val danger: Color)

val LightStatus = StatusColors(success = Green40, warning = Orange40, danger = Red40)
val DarkStatus = StatusColors(success = Green80, warning = Orange80, danger = Red80)

val LocalStatusColors = staticCompositionLocalOf { LightStatus }

@Composable
fun TaskStatus.barColor(neutral: Color): Color {
    val c = LocalStatusColors.current
    return when (this) {
        TaskStatus.DoneToday -> c.success
        TaskStatus.Fine -> neutral
        TaskStatus.DueToday -> c.warning
        TaskStatus.Overdue -> c.danger
    }
}