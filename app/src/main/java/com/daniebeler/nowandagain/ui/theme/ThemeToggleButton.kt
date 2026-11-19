package com.daniebeler.nowandagain.ui.theme

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.daniebeler.nowandagain.R

@Composable
fun ThemeToggleButton(
    darkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopBarButton(onClick = onClick, modifier = modifier) {
        Crossfade(targetState = darkTheme, label = "themeIcon") { dark ->
            Icon(
                painter = painterResource(if (dark) R.drawable.ic_moon else R.drawable.ic_sun),
                contentDescription = stringResource(
                    if (dark) R.string.cd_switch_to_light else R.string.cd_switch_to_dark
                ),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
fun AddTaskButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    TopBarButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(R.drawable.ic_plus),
            contentDescription = stringResource(R.string.cd_add_task),
            modifier = Modifier.size(22.dp),
        )
    }
}