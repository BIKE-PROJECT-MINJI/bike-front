package com.bikeprojectminji.bikefront.curator

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bikeprojectminji.bikefront.ui.theme.GajaTheme

class CuratorOnboardingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GajaTheme {
                CuratorOnboardingScreen(onFinish = { finish() })
            }
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, CuratorOnboardingActivity::class.java)
        }
    }
}
