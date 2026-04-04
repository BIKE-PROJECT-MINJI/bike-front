package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bikeprojectminji.bikefront.auth.AuthSessionStore
import androidx.compose.ui.platform.LocalContext

@Composable
fun MyInfoScreen(
    innerPadding: PaddingValues,
    onOpenProfile: () -> Unit,
) {
    val context = LocalContext.current
    val authSessionStore = AuthSessionStore(context)
    val displayName = authSessionStore.displayName?.takeIf { it.isNotBlank() } ?: "아직 로그인하지 않았습니다"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        SectionTitle(title = "내 정보", subtitle = "로그인 상태와 추후 기록/설정 진입 영역입니다.")
        Text(text = displayName, style = MaterialTheme.typography.headlineSmall)
        Button(onClick = onOpenProfile, modifier = Modifier.fillMaxWidth()) {
            Text("로그인 / 프로필 열기")
        }
        OutlinedButton(onClick = { }, enabled = false, modifier = Modifier.fillMaxWidth()) {
            Text("내 기록은 다음 phase에서 연결")
        }
    }
}
