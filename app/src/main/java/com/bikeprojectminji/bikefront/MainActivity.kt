package com.bikeprojectminji.bikefront

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bikeprojectminji.bikefront.ui.BikeFrontApp
import com.bikeprojectminji.bikefront.ui.theme.BikeFrontTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BikeFrontTheme {
                BikeFrontApp()
            }
        }
    }
}
