package dev.ghostbyte.nowandagain.ui.theme

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.ghostbyte.nowandagain.R

@Composable
fun SettingsButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    TopBarButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(R.drawable.ic_settings),
            contentDescription = stringResource(R.string.cd_open_settings),
            modifier = Modifier.size(22.dp),
        )
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