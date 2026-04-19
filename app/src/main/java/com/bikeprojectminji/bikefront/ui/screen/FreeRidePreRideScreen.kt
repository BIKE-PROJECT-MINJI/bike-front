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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FreeRidePreRideScreen(
    innerPadding: PaddingValues,
    onBack: () -> Unit,
    onStartRide: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(MaterialTheme.colorScheme.background),
    ) {
        // GAJA Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            Text(
                text = "자유 주행 준비",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = Color(0x15000000),
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "자유 주행 모드",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "코스 없이 바로 기록하는 모드입니다. 위치를 확보하면 ride 화면으로 진입하고 종료 후 기록 저장 흐름으로 이어집니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Feature List
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = Color(0x10000000),
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    FeatureRow(
                        icon = Icons.Default.LocationOn,
                        text = "courseId 없이 시작합니다",
                    )
                    FeatureRow(
                        icon = Icons.Default.CheckCircle,
                        text = "현재 위치 포인터가 우선이어야 합니다",
                    )
                    FeatureRow(
                        icon = Icons.Default.Save,
                        text = "종료 후 기록 코스를 전체 코스에 반영하는 흐름으로 이어집니다",
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons
            GajaPrimaryCard(
                title = "시작 준비",
                description = "자유 주행을 시작하려면 아래 버튼을 눌러주세요.",
                buttonText = "자유 주행 시작",
                onClick = onStartRide,
            )

            Spacer(modifier = Modifier.height(12.dp))

            GajaOutlinedButton(
                text = "뒤로 가기",
                onClick = onBack,
            )
        }
    }
}

@Composable
private fun FeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 2.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
