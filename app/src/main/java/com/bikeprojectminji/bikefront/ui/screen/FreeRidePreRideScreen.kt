package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bikeprojectminji.bikefront.ui.theme.GajaSpacing

@Composable
fun FreeRidePreRideScreen(
    innerPadding: PaddingValues,
    onBack: () -> Unit,
    onStartRide: () -> Unit,
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            GajaBrandTopBar(
                title = "자유 주행",
                subtitle = "출발 전 상태를 보고 바로 기록을 시작합니다",
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                MaterialTheme.colorScheme.background,
                            ),
                        ),
                    )
                    .padding(
                        horizontal = GajaSpacing.ScreenPadding,
                        vertical = GajaSpacing.Large,
                    ),
                verticalArrangement = Arrangement.spacedBy(GajaSpacing.Large),
            ) {
                FreeRideHero()

                BikeSurfaceCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(GajaSpacing.CardPadding),
                        verticalArrangement = Arrangement.spacedBy(GajaSpacing.Small),
                    ) {
                        SectionHeader(title = "자유 주행 정보")
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricChip(label = "시작점", value = "현재 위치", modifier = Modifier.weight(1f))
                            MetricChip(label = "기록 방식", value = "독립 기록", modifier = Modifier.weight(1f))
                        }
                    }
                }

                PrimaryActionButton(
                    text = "자유 주행 시작",
                    onClick = onStartRide,
                )

                SecondaryActionButton(
                    text = "뒤로 가기",
                    onClick = onBack,
                )
                Spacer(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()))
            }
        }
    }
}

@Composable
private fun FreeRideHero() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.inverseSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.inverseSurface,
                            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f),
                        ),
                    ),
                )
                .padding(GajaSpacing.CardPadding),
            verticalArrangement = Arrangement.spacedBy(GajaSpacing.Medium),
        ) {
            StatusBadge(text = "GPS 신호 양호", isActive = true)
            Text(
                text = "자유 주행을 바로 시작할 수 있습니다",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.inverseOnSurface,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Small)) {
                MetricChip(label = "위치 상태", value = "안정적", modifier = Modifier.weight(1f))
                MetricChip(label = "기록 준비", value = "즉시 가능", modifier = Modifier.weight(1f))
            }
        }
    }
}
