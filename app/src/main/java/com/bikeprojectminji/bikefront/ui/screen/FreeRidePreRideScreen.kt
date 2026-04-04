package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        SectionTitle(
            title = "자유 주행 준비",
            subtitle = "코스 없이 바로 기록하는 모드입니다. 지도/포인터/저장 흐름은 다음 구현 단계에서 연결합니다.",
        )
        Text("- courseId 없이 시작합니다")
        Text("- 현재 위치 포인터가 우선이어야 합니다")
        Text("- 종료 후 기록 코스를 전체 코스에 반영하는 흐름으로 이어집니다")
        Button(onClick = onStartRide, modifier = Modifier.fillMaxWidth()) {
            Text("자유 주행 시작")
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("뒤로 가기")
        }
    }
}
