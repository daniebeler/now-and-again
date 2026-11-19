package dev.ghostbyte.nowandagain

import android.os.Bundle
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.ghostbyte.nowandagain.ui.home.HomeScreen
import dev.ghostbyte.nowandagain.ui.theme.NowAndAgainTheme
import dev.ghostbyte.nowandagain.ui.theme.ThemePreference

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themePref = ThemePreference(
            applicationContext
        )

        setContent {
            var override by remember { mutableStateOf(themePref.load()) }
            val dark = override ?: isSystemInDarkTheme()

            // Keeps status and navigation bar icons readable when the app theme
            // differs from the system theme.
            DisposableEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT, Color.TRANSPARENT
                    ) { dark },
                    navigationBarStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT, Color.TRANSPARENT
                    ) { dark },
                )
                onDispose {}
            }

            NowAndAgainTheme(darkTheme = dark) {
                HomeScreen(
                    darkTheme = dark,
                    onToggleTheme = {
                        override = !dark
                        themePref.save(!dark)
                    },
                )
            }
        }
    }
}
