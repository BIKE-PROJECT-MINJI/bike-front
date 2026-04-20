package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        topBar = { GajaBrandTopBar(title = "자유 주행 준비") },
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

            GajaMapPreview(modifier = Modifier.fillMaxWidth(), heightDp = 260)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "자유 주행 화면으로 이동",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = GajaColors.TextPrimary,
                )
                Text(
                    text = "현재 구현에서는 지도 화면으로 진입하고 화면 종료 동작까지 확인할 수 있습니다.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GajaColors.TextSecondary,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing)) {
                GajaPrimaryButton(text = "주행 화면 열기", onClick = onStartRide)
                SecondaryActionButton(text = "돌아가기", onClick = onBack)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
