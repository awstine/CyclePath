package com.siaka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.siaka.ui.MainScaffold
import com.siaka.ui.MainViewModel
import com.siaka.ui.theme.SiakaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            mainViewModel.startDestination.value == null
        }

        enableEdgeToEdge()
        setContent {
            SiakaTheme {
                MainScaffold(mainViewModel = mainViewModel)
            }
        }
    }
}
