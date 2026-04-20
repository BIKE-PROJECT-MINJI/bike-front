package com.bikeprojectminji.bikefront.free

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bikeprojectminji.bikefront.ui.screen.GajaBrandTopBar
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaSpacing
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
    val context = LocalContext.current

    Scaffold(
        topBar = { GajaBrandTopBar(title = "HUD") },
        containerColor = GajaColors.SurfaceDim,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            GajaColors.Surface.copy(alpha = 0.95f),
                            GajaColors.SurfaceDim,
                        ),
                    ),
                )
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    WarningBanner()
                    Spacer(Modifier.height(12.dp))
                    GpsPill()
                }

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    HudMetricCard(
                        title = "현재 속도",
                        value = "24.5",
                        unit = "km/h",
                        modifier = Modifier.align(Alignment.TopEnd),
                        prominent = true,
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(top = 84.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        HudMetricCard(title = "주행 거리", value = "12.8", unit = "km", modifier = Modifier.fillMaxWidth(0.45f))
                        HudMetricCard(title = "주행 시간", value = "45:12", unit = null, modifier = Modifier.fillMaxWidth(0.5f))
                    }
                    HeartRateCard(modifier = Modifier.align(Alignment.BottomEnd))
                }

                Button(
                    onClick = {
                        Toast.makeText(context, "주행이 기록되었습니다.", Toast.LENGTH_SHORT).show()
                        onFinish()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GajaColors.PrimaryContainer,
                        contentColor = GajaColors.White,
                    ),
                ) {
                    Text("주행 종료 및 저장", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun WarningBanner() {
    Surface(
        color = GajaColors.ErrorContainer.copy(alpha = 0.92f),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = GajaColors.Error)
            Text("경로 이탈 주의", style = MaterialTheme.typography.labelLarge, color = GajaColors.Error, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun GpsPill() {
    Surface(
        color = GajaColors.TertiaryContainer.copy(alpha = 0.15f),
        shape = CircleShape,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(modifier = Modifier.size(8.dp).background(GajaColors.Tertiary, CircleShape))
            Text("GPS OK", style = MaterialTheme.typography.labelSmall, color = GajaColors.Tertiary)
        }
    }
}

@Composable
private fun HudMetricCard(
    title: String,
    value: String,
    unit: String?,
    modifier: Modifier = Modifier,
    prominent: Boolean = false,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = GajaColors.White.copy(alpha = 0.88f)),
    ) {
        Column(
            modifier = Modifier.padding(if (prominent) 24.dp else 18.dp),
            horizontalAlignment = if (prominent) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = GajaColors.TextSecondary)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    value,
                    style = if (prominent) MaterialTheme.typography.displayLarge else MaterialTheme.typography.headlineLarge,
                    color = GajaColors.TextPrimary,
                    fontWeight = FontWeight.Black,
                )
                if (unit != null) {
                    Text(unit, style = MaterialTheme.typography.bodySmall, color = GajaColors.TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                }
            }
        }
    }
}

@Composable
private fun HeartRateCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = GajaColors.White.copy(alpha = 0.88f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("심박수", style = MaterialTheme.typography.labelSmall, color = GajaColors.TextSecondary)
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("142", style = MaterialTheme.typography.headlineMedium, color = GajaColors.TextPrimary, fontWeight = FontWeight.Bold)
                    Text("BPM", style = MaterialTheme.typography.bodySmall, color = GajaColors.TextSecondary, modifier = Modifier.padding(bottom = 4.dp))
                }
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(GajaColors.ErrorContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = GajaColors.Error)
            }
        }
    }
}
