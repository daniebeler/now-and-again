package com.daniebeler.nowandagain.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daniebeler.nowandagain.R
import com.daniebeler.nowandagain.data.Task
import com.daniebeler.nowandagain.domain.TaskStatus
import com.daniebeler.nowandagain.domain.dueLabel
import com.daniebeler.nowandagain.ui.theme.AddTaskButton
import com.daniebeler.nowandagain.ui.theme.AppLogo
import com.daniebeler.nowandagain.ui.theme.ThemeToggleButton
import com.daniebeler.nowandagain.ui.theme.barColor
import kotlinx.coroutines.launch

private sealed interface SheetMode {
    data object Add : SheetMode
    data class Edit(val task: Task) : SheetMode
}

private const val FOOTER_KEY = "footer"
private val FOOTER_MIN_HEIGHT = 200.dp

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

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { padding ->
            Box(Modifier.fillMaxSize()) {

                // 1. Content, drawn behind the bar
                when {
                    state.loading -> Unit
                    state.items.isEmpty() -> EmptyState(
                        modifier = Modifier.padding(padding),
                        onAdd = { sheetMode = SheetMode.Add },
                    )

                    else -> {
                        val listState = rememberLazyListState()
                        val density = LocalDensity.current
                        val footerMinPx = with(density) { FOOTER_MIN_HEIGHT.roundToPx() }
                        val taskCount = state.items.size

                        // Height of the footer item. With few tasks it grows, so that the
                        // whole list is exactly one footer taller than the screen.
                        val footerHeightPx by remember(taskCount, footerMinPx) {
                            derivedStateOf {
                                val info = listState.layoutInfo
                                val tasks = info.visibleItemsInfo.filter { it.key != FOOTER_KEY }
                                if (tasks.size < taskCount) {
                                    footerMinPx // long list: not every task is visible, so no extra space is needed
                                } else {
                                    val used =
                                        info.beforeContentPadding + tasks.sumOf { it.size } + info.afterContentPadding
                                    maxOf(
                                        footerMinPx, info.viewportSize.height + footerMinPx - used
                                    )
                                }
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = statusBarTop + topBarHeight),
                        ) {
                            itemsIndexed(
                                state.items, key = { _, item -> item.task.id }) { index, item ->
                                Column(Modifier.animateItem()) {
                                    if (index > 0) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 20.dp),
                                            thickness = 1.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                                alpha = 0.5f
                                            ),
                                        )
                                    }
                                    TaskListItem(
                                        item = item,
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
                            item(key = FOOTER_KEY) {
                                ListFooter(Modifier.height(with(density) { footerHeightPx.toDp() }))
                            }
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AddTaskButton(onClick = { sheetMode = SheetMode.Add })
                        ThemeToggleButton(darkTheme = darkTheme, onClick = onToggleTheme)
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
                    if (editing == null) vm.addTask(name, days) else vm.editTask(
                        editing, name, days
                    )
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
}

@Composable
private fun TaskListItem(
    item: TaskItem,
    onClick: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val barColor = item.status.barColor(neutral = MaterialTheme.colorScheme.primary)
    val target = if (item.status == TaskStatus.DoneToday) 1f else item.progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(target, label = "progress")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 32.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    item.task.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    item.task.dueLabel(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(16.dp))

            val doneToday = item.status == TaskStatus.DoneToday

            if (doneToday) {
                Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = "Done today",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp),
                    )
                }
            } else {
                FilledIconButton(
                    onClick = onDone,
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = "Mark ${item.task.name} as done",
                        modifier = Modifier.size(24.dp),
                    )
                }
            }


        }
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { animated },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            strokeCap = StrokeCap.Butt,
            gapSize = 0.dp,
            drawStopIndicator = {},
        )
    }
}

@Composable
private fun EmptyState(onAdd: () -> Unit, modifier: Modifier = Modifier) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    BoxWithConstraints(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = maxHeight)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(
                    if (dark) R.drawable.playful_cat else R.drawable.playful_cat
                ),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp),
            )
            Spacer(Modifier.height(32.dp))
            Text(
                "Keep up with what matters",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Add something you want to do regularly, like calling a friend, " + "watering the plants or going for a run, and choose the longest " + "break you're okay with. Tap Done whenever you've done it and " + "the timer starts over. Doing it earlier is always fine.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onAdd,
                shape = MaterialTheme.shapes.small,
                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 16.dp),
            ) { Text("Add your first task") }
        }
    }
}

@Composable
private fun TaskSheet(
    initial: Task?,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    BackHandler(onBack = onDismiss)

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    )
            )
        }

        AnimatedVisibility(
            visible = visible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding(),
            enter = slideInVertically(animationSpec = tween(300)) { it },
            exit = slideOutVertically(animationSpec = tween(200)) { it },
        ) {
            SheetContent(initial, onSave, onDelete)
        }
    }
}

@Composable
private fun SheetContent(
    initial: Task?,
    onSave: (String, Int) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var days by remember { mutableIntStateOf(initial?.intervalDays ?: 3) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        if (initial == null) focusRequester.requestFocus()
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge.copy(
            bottomStart = CornerSize(0.dp),
            bottomEnd = CornerSize(0.dp),
        ),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            Modifier
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = 24.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Task") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
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
                    shape = MaterialTheme.shapes.small,
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

@Composable
private fun ListFooter(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        Image(
            painter = painterResource(R.drawable.cow),
            contentDescription = null,
            modifier = Modifier.height(72.dp),
        )
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        )
        Spacer(Modifier.height(12.dp))

        val uriHandler = LocalUriHandler.current

        Text(
            "A Ghostbyte Production",
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 0.6.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable(onClickLabel = "Open ghostbyte.dev") {
                    try {
                        uriHandler.openUri("https://ghostbyte.dev")
                    } catch (_: Exception) {
                        // No browser installed: do nothing
                    }
                }
                .padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}