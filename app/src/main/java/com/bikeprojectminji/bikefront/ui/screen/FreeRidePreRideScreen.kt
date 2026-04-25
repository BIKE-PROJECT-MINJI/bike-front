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
import com.bikeprojectminji.bikefront.ui.theme.GajaCardTokens
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaRadius
import com.bikeprojectminji.bikefront.ui.theme.GajaSpacing

@Composable
fun FreeRidePreRideScreen(
    innerPadding: PaddingValues,
    onBack: () -> Unit,
    onStartRide: () -> Unit,
) {
    val context = LocalContext.current
    val analyticsTracker = remember(context) { AnalyticsTracker(context) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        GajaColors.Background,
                        Color(0xFFF1F4F8),
                    ),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to GajaColors.White.copy(alpha = 0.84f),
                        0.28f to Color.Transparent,
                        0.72f to Color.Transparent,
                        1f to GajaColors.White.copy(alpha = 0.18f),
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
            GajaBrandTopBar(title = "자유 주행")

            Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.Medium)) {
                GajaSectionCard(
                    containerColor = GajaColors.White.copy(alpha = 0.84f),
                    borderColor = GajaColors.Border.copy(alpha = 0.72f),
                    shape = RoundedCornerShape(GajaRadius.XLarge),
                    contentPadding = PaddingValues(GajaCardTokens.HeroPadding),
                    shadowElevation = GajaCardTokens.SubtleElevation,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.Small)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FreeRideStatusBadge(text = "바로 출발", containerColor = GajaColors.Primary)
                            FreeRideStatusBadge(text = "위치 기반", containerColor = GajaColors.Success)
                        }
                        Text(
                            text = "가볍게 자유 주행 시작",
                            style = MaterialTheme.typography.displayMedium,
                            color = GajaColors.TextPrimary,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            text = "현재 위치를 기준으로 출발하고 주행 기록을 저장할 수 있어요.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = GajaColors.TextSecondary,
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing), modifier = Modifier.fillMaxWidth()) {
                    GajaMetricCard(label = "주행 화면", value = "실시간", modifier = Modifier.weight(1f), emphasized = true)
                    GajaMetricCard(label = "기록", value = "자동 저장 준비", modifier = Modifier.weight(1f))
                }
            }

            Column(
                modifier = Modifier.padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GajaSectionCard(
                    containerColor = GajaColors.White.copy(alpha = 0.82f),
                    borderColor = GajaColors.Border.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(GajaRadius.Large),
                    contentPadding = PaddingValues(horizontal = GajaCardTokens.DefaultPadding, vertical = GajaSpacing.Small),
                ) {
                    Text(
                        text = "주행 중에는 지도 위에서 속도와 상태를 바로 확인할 수 있어요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GajaColors.TextSecondary,
                    )
                }
                GajaPrimaryButton(
                    text = "자유 주행 시작",
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
    GajaStatusBadge(text = text, containerColor = containerColor, contentColor = Color.White)
}
