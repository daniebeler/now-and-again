package com.daniebeler.nowandagain.ui.theme

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.daniebeler.nowandagain.R

@Composable
fun ThemeToggleButton(
    darkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(50.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Crossfade(targetState = darkTheme, label = "themeIcon") { dark ->
                Icon(
                    painter = painterResource(if (dark) R.drawable.ic_moon else R.drawable.ic_sun),
                    contentDescription = if (dark) "Switch to light mode" else "Switch to dark mode",
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}