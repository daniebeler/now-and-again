package com.daniebeler.nowandagain.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daniebeler.nowandagain.data.Task
import com.daniebeler.nowandagain.domain.TaskStatus
import com.daniebeler.nowandagain.domain.dueLabel
import com.daniebeler.nowandagain.ui.theme.AppLogo
import com.daniebeler.nowandagain.ui.theme.ThemeToggleButton
import com.daniebeler.nowandagain.ui.theme.barColor
import kotlinx.coroutines.launch

private sealed interface SheetMode {
    data object Add : SheetMode
    data class Edit(val task: Task) : SheetMode
}

@Composable
fun HomeScreen(
    darkTheme: Boolean, onToggleTheme: () -> Unit, vm: HomeViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var sheetMode by remember { mutableStateOf<SheetMode?>(null) }

    val topBarHeight = 120.dp
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val background = MaterialTheme.colorScheme.background

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { sheetMode = SheetMode.Add }) {
                Icon(Icons.Default.Add, contentDescription = "Add task")
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize()) {

            // 1. Content, drawn behind the bar
            when {
                state.loading -> Unit
                state.items.isEmpty() -> EmptyState(
                    modifier = Modifier.padding(padding),
                    onAdd = { sheetMode = SheetMode.Add },
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = statusBarTop + topBarHeight,
                        bottom = padding.calculateBottomPadding() + 96.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.items, key = { it.task.id }) { item ->
                        TaskCard(
                            item = item,
                            modifier = Modifier.animateItem(),
                            onClick = { sheetMode = SheetMode.Edit(item.task) },
                            onDone = { /* unchanged */ },
                        )
                    }
                }
            }

            // 2. Fade: cards dissolve into the background toward the top
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(statusBarTop + topBarHeight + 24.dp)
                    .background(
                        Brush.verticalGradient(
                            0f to background,
                            0.55f to background.copy(alpha = 0.9f),
                            1f to background.copy(alpha = 0f),
                        )
                    )
            )

            // 3. Bar: logo and theme toggle, transparent
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(topBarHeight)
                    .padding(start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppLogo(size = 67.dp)
                ThemeToggleButton(darkTheme = darkTheme, onClick = onToggleTheme)
            }
        }
    }

    sheetMode?.let { mode ->
        val editing = (mode as? SheetMode.Edit)?.task
        TaskSheet(
            initial = editing,
            onDismiss = { sheetMode = null },
            onSave = { name, days ->
                if (editing == null) vm.addTask(name, days) else vm.editTask(editing, name, days)
                sheetMode = null
            },
            onDelete = editing?.let { task ->
                {
                    vm.delete(task)
                    sheetMode = null
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        val result = snackbarHostState.showSnackbar(
                            message = "${task.name} deleted",
                            actionLabel = "Undo",
                            duration = SnackbarDuration.Short,
                        )
                        if (result == SnackbarResult.ActionPerformed) vm.undoDelete(task)
                    }
                }
            },
        )
    }
}

@Composable
private fun TaskCard(
    item: TaskItem, onClick: () -> Unit, onDone: () -> Unit, modifier: Modifier = Modifier
) {
    val barColor = item.status.barColor(neutral = MaterialTheme.colorScheme.primary)
    val target = if (item.status == TaskStatus.DoneToday) 1f else item.progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(target, label = "progress")

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(item.task.name, style = MaterialTheme.typography.headlineSmall)
            Text(item.task.dueLabel(), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { animated },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = barColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                strokeCap = StrokeCap.Butt,
                gapSize = 0.dp,
                drawStopIndicator = {},
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onDone,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.align(Alignment.End),
            ) { Text("Done") }
        }
    }
}

@Composable
private fun EmptyState(onAdd: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Nothing to keep up with yet", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAdd) { Text("Add your first task") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskSheet(
    initial: Task?,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var days by remember { mutableIntStateOf(initial?.intervalDays ?: 3) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding(),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Task") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(24.dp))
            IntervalPicker(days = days, onChange = { days = it.coerceIn(1, MAX_INTERVAL) })
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) { Text("Delete") }
                } else {
                    Spacer(Modifier.width(1.dp))
                }
                Button(
                    onClick = { onSave(name, days) },
                    enabled = name.isNotBlank(),
                ) { Text("Save") }
            }
        }
    }
}

private const val MAX_INTERVAL = 365

private val presets = listOf(
    1 to "Daily",
    2 to "2 days",
    3 to "3 days",
    7 to "Weekly",
    14 to "2 weeks",
    30 to "30 days",
)

@Composable
private fun IntervalPicker(days: Int, onChange: (Int) -> Unit) {
    Column {
        Text(
            "At least every",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            FilledTonalIconButton(
                onClick = { onChange(days - 1) },
                enabled = days > 1,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.size(56.dp),
            ) { Text("−", style = MaterialTheme.typography.headlineMedium) }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$days", style = MaterialTheme.typography.displayMedium)
                Text(
                    if (days == 1) "day" else "days",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            FilledTonalIconButton(
                onClick = { onChange(days + 1) },
                enabled = days < MAX_INTERVAL,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.size(56.dp),
            ) { Text("+", style = MaterialTheme.typography.headlineMedium) }
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            presets.forEach { (value, label) ->
                FilterChip(
                    selected = days == value,
                    onClick = { onChange(value) },
                    label = { Text(label) },
                    shape = MaterialTheme.shapes.small,
                )
            }
        }
    }
}