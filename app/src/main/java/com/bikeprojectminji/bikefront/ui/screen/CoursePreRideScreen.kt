package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CoursePreRideScreen(
    innerPadding: PaddingValues,
    course: CourseCardUiModel,
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
                text = "코스 따라가기",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Course Info Section
            SectionTitle(
                title = "선택한 코스",
                subtitle = "코스 정보를 확인하고 라이딩을 시작하세요.",
            )

            // Course Card
            CourseCard(course = course)

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons
            GajaPrimaryCard(
                title = "라이딩 시작",
                description = "이 코스를 따라 라이딩을 시작합니다.",
                buttonText = "이 코스로 시작",
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