package com.bikeprojectminji.bikefront.free

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bikeprojectminji.bikefront.ui.screen.GajaBrandTopBar
import com.bikeprojectminji.bikefront.ui.screen.GajaPrimaryButton
import com.bikeprojectminji.bikefront.ui.screen.SectionHeader
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaTheme
import com.bikeprojectminji.bikefront.ui.theme.GajaSpacing

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
    Scaffold(
        topBar = { GajaBrandTopBar(title = "Free Ride Session") },
        containerColor = GajaColors.Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = GajaSpacing.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(GajaSpacing.Large)
        ) {
            Spacer(Modifier.height(GajaSpacing.Small))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = GajaColors.TextPrimary)
            ) {
                Column(modifier = Modifier.padding(GajaSpacing.Large)) {
                    Text("주행 기록 중", style = MaterialTheme.typography.labelSmall, color = GajaColors.Accent)
                    Text("00:42:15", style = MaterialTheme.typography.displayLarge, color = GajaColors.White)
                }
            }

            SectionHeader(title = "현재 지표", subtitle = "실시간 주행 데이터")

            Row(horizontalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing)) {
                MetricItem("속도", "24.5 km/h", Modifier.weight(1f))
                MetricItem("거리", "12.4 km", Modifier.weight(1f))
            }

            Spacer(Modifier.weight(1f))

            GajaPrimaryButton(
                text = "주행 종료 및 저장",
                onClick = { 
                    Toast.makeText(onFinish as? android.content.Context ?: return@GajaPrimaryButton, "주행이 기록되었습니다.", Toast.LENGTH_SHORT).show()
                    onFinish()
                }
            )
            Spacer(Modifier.height(GajaSpacing.Large))
        }
    }
}

@Composable
fun MetricItem(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = GajaColors.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, GajaColors.Border)
    ) {
        Column(modifier = Modifier.padding(GajaSpacing.Medium)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = GajaColors.TextSecondary)
            Text(value, style = MaterialTheme.typography.titleLarge, color = GajaColors.TextPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Black)
        }
    }
}
