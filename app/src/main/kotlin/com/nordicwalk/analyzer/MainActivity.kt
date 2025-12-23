package com.nordicwalk.analyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.nordicwalk.analyzer.ui.NordicWalkNavigation
import com.nordicwalk.analyzer.ui.theme.NordicWalkTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NordicWalkTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NordicWalkNavigation()
                }
            }
        }
    }
}
