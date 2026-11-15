package com.daniebeler.nowandagain.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daniebeler.nowandagain.data.Task
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private sealed interface SheetMode {
    data object Add : SheetMode
    data class Edit(val task: Task) : SheetMode
}

@Composable
fun HomeScreen(vm: HomeViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var sheetMode by remember { mutableStateOf<SheetMode?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { sheetMode = SheetMode.Add }) {
                //Icon(Icons.Default.Add, contentDescription = "Add task")
            }
        },
    ) { padding ->
        when {
            state.loading -> Unit
            state.items.isEmpty() -> EmptyState(
                modifier = Modifier.padding(padding),
                onAdd = { sheetMode = SheetMode.Add },
            )

            else -> LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.items, key = { it.task.id }) { item ->
                    TaskCard(
                        item = item,
                        modifier = Modifier.animateItem(),
                        onClick = { sheetMode = SheetMode.Edit(item.task) },
                        onDone = {
                            vm.markDone(item.task)
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                val result = snackbarHostState.showSnackbar(
                                    message = "${item.task.name} done",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short,
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    vm.restore(item.task)
                                }
                            }
                        },
                    )
                }
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
    val overdue = item.daysLeft < 0
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (overdue) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(item.task.name, style = MaterialTheme.typography.headlineSmall)
            Text(statusText(item.daysLeft), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { item.progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = onDone, modifier = Modifier.align(Alignment.End)) {
                Text("Done")
            }
        }
    }
}

private fun statusText(daysLeft: Long): String = when {
    daysLeft < -1 -> "Overdue by ${-daysLeft} days"
    daysLeft == -1L -> "Overdue by 1 day"
    daysLeft == 0L -> "Due today"
    daysLeft == 1L -> "Due tomorrow"
    else -> "In $daysLeft days"
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
    var days by remember {
        mutableFloatStateOf(
            (initial?.intervalDays ?: 3).toFloat().coerceIn(1f, 60f)
        )
    }
    val interval = days.roundToInt()

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
            Text(
                if (interval == 1) "At least every day" else "At least every $interval days",
                style = MaterialTheme.typography.titleMedium,
            )
            Slider(
                value = days,
                onValueChange = { days = it },
                valueRange = 1f..60f,
            )
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
                    onClick = { onSave(name, interval) },
                    enabled = name.isNotBlank(),
                ) { Text("Save") }
            }
        }
    }
}