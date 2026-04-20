package com.bikeprojectminji.bikefront.free

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bikeprojectminji.bikefront.ui.screen.GajaMapPreview
import com.bikeprojectminji.bikefront.ui.screen.GajaPrimaryButton
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaTheme

class FreeRideActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GajaTheme {
                FreeRideMainScreen(onFinish = { finish() })
            }
        }
    }
}

@Composable
fun FreeRideMainScreen(onFinish: () -> Unit) {
    Scaffold(containerColor = GajaColors.Background) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            GajaMapPreview(modifier = Modifier.fillMaxSize(), heightDp = 640)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Surface(color = GajaColors.White.copy(alpha = 0.9f), shape = MaterialTheme.shapes.large) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text("자유 주행 화면", style = MaterialTheme.typography.titleLarge, color = GajaColors.TextPrimary, fontWeight = FontWeight.Bold)
                        Text("실제 지도만 표시하고, 화면 종료 동작을 제공합니다.", style = MaterialTheme.typography.bodyMedium, color = GajaColors.TextSecondary)
                    }
                }
                GajaPrimaryButton(text = "화면 종료", onClick = onFinish)
            }
        }
    }
}
