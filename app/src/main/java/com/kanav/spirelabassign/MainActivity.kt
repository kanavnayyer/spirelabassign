package com.kanav.spirelabassign

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kanav.spirelabassign.ui.navigation.AppNav
import com.kanav.spirelabassign.ui.theme.SpireLabAssignTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpireLabAssignTheme {
                AppNav()
            }
        }
    }
}
