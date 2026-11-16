package com.daniebeler.nowandagain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.daniebeler.nowandagain.ui.home.HomeScreen
import com.daniebeler.nowandagain.ui.theme.NowAndAgainTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NowAndAgainTheme {
                HomeScreen()
            }
        }
    }
}
