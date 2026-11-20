package dev.ghostbyte.nowandagain.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dev.ghostbyte.nowandagain.MainActivity
import dev.ghostbyte.nowandagain.data.AppDatabase
import dev.ghostbyte.nowandagain.data.Task
import dev.ghostbyte.nowandagain.domain.daysLeft
import dev.ghostbyte.nowandagain.domain.markDone
import java.time.LocalDate
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Spacer
import dev.ghostbyte.nowandagain.domain.dueLabel
import dev.ghostbyte.nowandagain.domain.progress
import androidx.glance.material3.ColorProviders
import dev.ghostbyte.nowandagain.domain.TaskStatus
import dev.ghostbyte.nowandagain.domain.status
import dev.ghostbyte.nowandagain.ui.theme.DarkColorScheme
import dev.ghostbyte.nowandagain.ui.theme.Green40
import dev.ghostbyte.nowandagain.ui.theme.Green80
import dev.ghostbyte.nowandagain.ui.theme.LightColorScheme
import dev.ghostbyte.nowandagain.ui.theme.Orange40
import dev.ghostbyte.nowandagain.ui.theme.Orange80
import dev.ghostbyte.nowandagain.ui.theme.Red40
import dev.ghostbyte.nowandagain.ui.theme.Red80
import dev.ghostbyte.nowandagain.R

private val WidgetColors = ColorProviders(light = LightColorScheme, dark = DarkColorScheme)

private val WidgetGreen = ColorProvider(day = Green40, night = Green80)
private val WidgetOrange = ColorProvider(day = Orange40, night = Orange80)
private val WidgetRed = ColorProvider(day = Red40, night = Red80)

class NextTasksWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val tasks = AppDatabase.get(context).taskDao().getActive()
        provideContent {
            GlanceTheme(colors = WidgetColors) { WidgetContent(tasks) }
        }
    }
}

val TaskIdKey = ActionParameters.Key<Long>("taskId")

@Composable
private fun WidgetContent(tasks: List<Task>) {
    val context = LocalContext.current

    val today = LocalDate.now()
    val rowHeight = 72.dp
    val maxItems = ((LocalSize.current.height - 24.dp) / rowHeight).toInt().coerceIn(1, 6)
    val items = tasks.sortedBy { it.daysLeft(today) }.take(maxItems)

    Column(
        modifier = GlanceModifier.fillMaxSize().appWidgetBackground()
            .background(GlanceTheme.colors.widgetBackground).cornerRadius(24.dp).padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        if (items.isEmpty()) {
            Box(GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    context.getString(R.string.widget_empty),
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant),
                )
            }
        } else {
            items.forEach { task ->
                TaskRow(task, today, rowHeight)
            }
        }
    }
}

@Composable
private fun TaskRow(task: Task, today: LocalDate, height: Dp) {

    val context = LocalContext.current

    val status = task.status(today)
    val barColor = when (status) {
        TaskStatus.DoneToday -> WidgetGreen
        TaskStatus.DueToday -> WidgetOrange
        TaskStatus.Overdue -> WidgetRed
        TaskStatus.Fine -> GlanceTheme.colors.primary
    }
    val progress = if (status == TaskStatus.DoneToday) 1f else task.progress().coerceIn(0f, 1f)

    val overdue = task.daysLeft(today) < 0
    Row(
        modifier = GlanceModifier.fillMaxWidth().height(height),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(GlanceModifier.defaultWeight()) {
            Text(
                task.name,
                maxLines = 1,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface,
                ),
            )
            Text(
                task.dueLabel(context.resources, today),
                maxLines = 1,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = if (overdue) GlanceTheme.colors.error
                    else GlanceTheme.colors.onSurfaceVariant,
                ),
            )
            Spacer(GlanceModifier.height(6.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = GlanceModifier.fillMaxWidth().height(8.dp).padding(end = 12.dp),
                color = barColor,
                backgroundColor = GlanceTheme.colors.secondaryContainer,
            )
        }
        Button(
            text = context.getString(R.string.action_done),
            onClick = actionRunCallback<MarkDoneAction>(
                actionParametersOf(TaskIdKey to task.id)
            ),
        )
    }
}

class MarkDoneAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val id = parameters[TaskIdKey] ?: return
        val dao = AppDatabase.get(context).taskDao()
        dao.getById(id)?.let { dao.update(it.markDone()) }
        NextTasksWidget().updateAll(context)
    }
}