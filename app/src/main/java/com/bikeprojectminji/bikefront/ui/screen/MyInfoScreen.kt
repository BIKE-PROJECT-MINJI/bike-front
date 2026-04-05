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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bikeprojectminji.bikefront.auth.AuthSessionStore
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun MyInfoScreen(
    innerPadding: PaddingValues,
    onOpenProfile: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val authSessionStore = remember(context) { AuthSessionStore(context) }
    var displayName by remember { mutableStateOf(authSessionStore.displayName?.takeIf { it.isNotBlank() } ?: "아직 로그인하지 않았습니다") }

    DisposableEffect(lifecycleOwner, authSessionStore) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                displayName = authSessionStore.displayName?.takeIf { it.isNotBlank() } ?: "아직 로그인하지 않았습니다"
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
                text = "내 정보",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Profile Card
            ProfileCard(
                displayName = displayName,
                onProfileClick = onOpenProfile,
            )

            // Info Section
            SectionTitle(
                title = "계정",
                subtitle = "로그인 상태와 추후 기록/설정 진입 영역입니다.",
            )

            // Action Buttons
            GajaSecondaryButton(
                text = "로그인 / 프로필 열기",
                onClick = onOpenProfile,
            )

            Spacer(modifier = Modifier.height(8.dp))

            GajaOutlinedButton(
                text = "내 기록 화면은 아직 연결하지 않았습니다",
                onClick = { },
                enabled = false,
            )
        }
    }
}