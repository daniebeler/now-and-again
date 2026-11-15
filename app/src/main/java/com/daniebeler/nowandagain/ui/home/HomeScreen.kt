package com.daniebeler.nowandagain.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun HomeScreen(vm: HomeViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                //Icon(Icons.Default.Add, contentDescription = "Add task")
            }
        },
    ) { padding ->
        when {
            state.loading -> Unit
            state.items.isEmpty() -> EmptyState(
                modifier = Modifier.padding(padding),
                onAdd = { showAddSheet = true },
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

    if (showAddSheet) {
        AddTaskSheet(
            onDismiss = { showAddSheet = false },
            onSave = { name, days ->
                vm.addTask(name, days)
                showAddSheet = false
            },
        )
    }
}

@Composable
private fun TaskCard(item: TaskItem, onDone: () -> Unit, modifier: Modifier = Modifier) {
    val overdue = item.daysLeft < 0
    Card(
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
private fun AddTaskSheet(onDismiss: () -> Unit, onSave: (String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var days by remember { mutableFloatStateOf(3f) }
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
            Button(
                onClick = { onSave(name, interval) },
                enabled = name.isNotBlank(),
                modifier = Modifier.align(Alignment.End),
            ) { Text("Save") }
        }
    }
}