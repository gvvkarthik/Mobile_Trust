package com.example.mobiletrust

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mobiletrust.ui.screens.DashboardScreen
import com.example.mobiletrust.ui.theme.MobileTrustTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileTrustTheme {
                DashboardScreen()
            }
        }
    }
}