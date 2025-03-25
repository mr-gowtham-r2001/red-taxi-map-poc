package com.example.redtaximappoc.ui.screens.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.redtaximappoc.ui.screens.mainscreen.RTMainScreen
import com.example.redtaximappoc.ui.theme.RedTaxiMapPOCTheme

class RTMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RedTaxiMapPOCTheme {
                RTMainScreen()
            }
        }
    }
}