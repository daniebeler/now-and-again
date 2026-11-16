package com.daniebeler.nowandagain.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
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
import com.daniebeler.nowandagain.MainActivity
import com.daniebeler.nowandagain.data.AppDatabase
import com.daniebeler.nowandagain.data.Task
import com.daniebeler.nowandagain.domain.daysLeft
import com.daniebeler.nowandagain.domain.markDone
import com.daniebeler.nowandagain.domain.statusText
import java.time.LocalDate

class NextTasksWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val tasks = AppDatabase.get(context).taskDao().getActive()
        provideContent {
            GlanceTheme { WidgetContent(tasks) }
        }
    }
}

val TaskIdKey = ActionParameters.Key<Long>("taskId")

@Composable
private fun WidgetContent(tasks: List<Task>) {
    val today = LocalDate.now()
    val rowHeight = 56.dp
    val maxItems = ((LocalSize.current.height - 24.dp) / rowHeight).toInt().coerceIn(1, 6)
    val items = tasks.sortedBy { it.daysLeft(today) }.take(maxItems)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(24.dp)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        if (items.isEmpty()) {
            Box(GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Nothing to keep up with",
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant),
                )
            }
        } else {
            items.forEach { task ->
                TaskRow(task, task.daysLeft(today), rowHeight)
            }
        }
    }
}

@Composable
private fun TaskRow(task: Task, daysLeft: Long, height: Dp) {
    val overdue = daysLeft < 0
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
                statusText(daysLeft),
                maxLines = 1,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = if (overdue) GlanceTheme.colors.error
                    else GlanceTheme.colors.onSurfaceVariant,
                ),
            )
        }
        Button(
            text = "Done",
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