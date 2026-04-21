package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bikeprojectminji.bikefront.analytics.AnalyticsTracker
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaSpacing

@Composable
fun FreeRidePreRideScreen(
    innerPadding: PaddingValues,
    onBack: () -> Unit,
    onStartRide: () -> Unit,
) {
    val context = LocalContext.current
    val analyticsTracker = remember(context) { AnalyticsTracker(context) }
    Box(modifier = Modifier.fillMaxSize().background(GajaColors.Background)) {
        GajaMapPreview(
            modifier = Modifier.fillMaxSize(),
            heightDp = 220,
            mode = MapDisplayMode.PREVIEW,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.10f),
                        0.22f to Color.Transparent,
                        0.72f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.24f),
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = GajaSpacing.ScreenPadding),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            GajaBrandTopBar(title = "자유 주행", onProfileClick = {})

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(
                    color = GajaColors.Carbon.copy(alpha = 0.78f),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FreeRideStatusBadge(text = "LIVE", containerColor = GajaColors.Primary)
                            FreeRideStatusBadge(text = "GPS 준비", containerColor = GajaColors.Success)
                        }
                        Text(
                            text = "지금 바로 자유 주행",
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            text = "현재 위치를 기준으로 출발하고, 기록을 저장해 코스로 이어갈 수 있습니다.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.72f),
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing), modifier = Modifier.fillMaxWidth()) {
                    MetricChip(label = "지도", value = "실시간", modifier = Modifier.weight(1f))
                    MetricChip(label = "기록", value = "저장 가능", modifier = Modifier.weight(1f))
                }
            }

            Column(
                modifier = Modifier.padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    color = GajaColors.White.copy(alpha = 0.78f),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Text(
                        text = "주행 중에는 속도, 거리, 날씨, 주행 상태를 지도 위 HUD로 바로 확인할 수 있습니다.",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = GajaColors.TextSecondary,
                    )
                }
                GajaPrimaryButton(
                    text = "주행 시작",
                    onClick = {
                        analyticsTracker.track("ride_start_clicked", "free_ride_pre", mapOf("button" to "start_ride"))
                        onStartRide()
                    },
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                )
                SecondaryActionButton(text = "돌아가기", onClick = onBack)
            }
        }
    }
}

@Composable
private fun FreeRideStatusBadge(text: String, containerColor: Color) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}
