package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
        containerColor = GajaColors.Background
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = GajaSpacing.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(GajaSpacing.Large)
        ) {
            Spacer(Modifier.height(GajaSpacing.Small))

            // Free Ride Hero Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = GajaColors.TextPrimary)
            ) {
                Column(
                    modifier = Modifier.padding(GajaSpacing.Large),
                    verticalArrangement = Arrangement.spacedBy(GajaSpacing.Medium)
                ) {
                    Surface(
                        color = GajaColors.Success,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "GPS READY",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "자유 주행을 바로 시작할 수 있습니다",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                }
            }

            SectionHeader(title = "주행 설정", subtitle = "현재 위치에서 즉시 기록을 시작합니다")
            
            Row(horizontalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing)) {
                MetricChip(label = "시작 지점", value = "현재 위치", modifier = Modifier.weight(1f))
                MetricChip(label = "기록 모드", value = "자유 주행", modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.weight(1f))

            Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing)) {
                GajaPrimaryButton(
                    text = "기록 시작",
                    onClick = onStartRide
                )
                SecondaryActionButton(
                    text = "돌아가기",
                    onClick = onBack
                )
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
