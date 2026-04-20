package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaSpacing

@Composable
fun FreeRidePreRideScreen(
    innerPadding: PaddingValues,
    onBack: () -> Unit,
    onStartRide: () -> Unit,
) {
    Scaffold(
        topBar = { GajaBrandTopBar(title = "Free Ride") },
        containerColor = GajaColors.Background,
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = GajaSpacing.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(GajaSpacing.Large),
        ) {
            Spacer(Modifier.height(GajaSpacing.Small))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(Brush.verticalGradient(listOf(GajaColors.SurfaceContainerHigh, GajaColors.SurfaceContainerLow)), MaterialTheme.shapes.extraLarge)
                    .padding(18.dp),
            ) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Surface(color = GajaColors.TertiaryContainer.copy(alpha = 0.14f), shape = MaterialTheme.shapes.small) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = GajaColors.PrimaryContainer, modifier = Modifier.size(16.dp))
                                Text("서울, 한강 자전거길", style = MaterialTheme.typography.labelSmall, color = GajaColors.TextPrimary)
                            }
                        }
                        Surface(color = GajaColors.TertiaryContainer.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Box(modifier = Modifier.size(8.dp).background(GajaColors.Tertiary, CircleShape))
                                Text("GPS 최고", style = MaterialTheme.typography.labelSmall, color = GajaColors.Tertiary)
                            }
                        }
                    }

                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = GajaColors.White.copy(alpha = 0.86f)),
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("자유 주행", style = MaterialTheme.typography.headlineMedium, color = GajaColors.TextPrimary, fontWeight = FontWeight.Bold)
                            Text(
                                "현재 위치에서 바로 기록을 시작합니다. 위치를 확보하면 ride HUD로 진입하고 종료 후 저장 흐름으로 이어집니다.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GajaColors.TextSecondary,
                            )
                        }
                    }
                }
            }

            Card(shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = GajaColors.SurfaceContainerLow)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("장비 연결 상태", style = MaterialTheme.typography.labelLarge, color = GajaColors.TextPrimary)
                        Icon(Icons.Default.BluetoothConnected, contentDescription = null, tint = GajaColors.PrimaryContainer)
                    }
                    SensorRow(icon = Icons.Default.MonitorHeart, title = "가민 심박계", subtitle = "배터리 85%", status = "연결됨", positive = true)
                    SensorRow(icon = Icons.Default.Speed, title = "케이던스 센서", subtitle = "신호 없음", status = "검색 중...", positive = false)
                }
            }

            SectionHeader(title = "주행 목표", subtitle = "바로 시작 전 핵심 요약")
            Row(horizontalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing), modifier = Modifier.fillMaxWidth()) {
                MetricChip(label = "목표 거리", value = "45.0 km", modifier = Modifier.weight(1f))
                MetricChip(label = "예상 시간", value = "2:15 h", modifier = Modifier.weight(1f))
            }

            Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing)) {
                GajaPrimaryButton(text = "주행 시작하기", onClick = onStartRide, icon = Icons.Default.PlayArrow)
                SecondaryActionButton(text = "돌아가기", onClick = onBack)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SensorRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    status: String,
    positive: Boolean,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(GajaColors.Surface, MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = GajaColors.TextSecondary)
            }
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, color = GajaColors.TextPrimary, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = if (positive) GajaColors.TextSecondary else GajaColors.Error)
            }
        }
        Surface(
            color = if (positive) GajaColors.TertiaryContainer.copy(alpha = 0.12f) else GajaColors.SurfaceContainer,
            shape = MaterialTheme.shapes.small,
        ) {
            Text(
                text = status,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelSmall,
                color = if (positive) GajaColors.Tertiary else GajaColors.TextSecondary,
            )
        }
    }
}
