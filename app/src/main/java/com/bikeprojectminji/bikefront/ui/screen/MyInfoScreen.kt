package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bikeprojectminji.bikefront.auth.AuthSessionStore
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaSpacing

private sealed class ProfileState {
    data object Loading : ProfileState()
    data class Loaded(val displayName: String) : ProfileState()
    data object NotLoggedIn : ProfileState()
}

@Composable
fun MyInfoScreen(
    innerPadding: PaddingValues,
    onOpenProfile: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val authSessionStore = remember { AuthSessionStore(context) }
    var profileState by remember { mutableStateOf<ProfileState>(ProfileState.Loading) }

    DisposableEffect(lifecycleOwner, authSessionStore) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val displayName = authSessionStore.displayName?.takeIf { it.isNotBlank() }
                profileState = if (displayName != null) ProfileState.Loaded(displayName) else ProfileState.NotLoggedIn
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = { GajaBrandTopBar(title = "내 정보") },
        containerColor = GajaColors.Background,
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .padding(horizontal = GajaSpacing.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(GajaSpacing.Large),
        ) {
            Spacer(Modifier.height(GajaSpacing.Small))
            when (val state = profileState) {
                is ProfileState.Loading -> LoadingStateView("프로필 불러오는 중...")
                is ProfileState.NotLoggedIn -> {
                    HeroCard(
                        title = "로그인이 필요합니다",
                        description = "현재 화면에서는 로그인과 프로필 이름 수정만 지원합니다.",
                        buttonText = "로그인 / 이름 설정",
                        onClick = onOpenProfile,
                        icon = "profile",
                    )
                }
                is ProfileState.Loaded -> {
                    SectionHeader(title = "프로필", subtitle = "실제 저장된 사용자 정보")
                    BikeSurfaceCard {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("이름", style = MaterialTheme.typography.labelSmall, color = GajaColors.TextSecondary)
                            Text(state.displayName, style = MaterialTheme.typography.headlineMedium, color = GajaColors.TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                    GajaPrimaryButton(text = "프로필 수정", onClick = onOpenProfile)
                    SecondaryActionButton(
                        text = "로그아웃",
                        onClick = {
                            authSessionStore.clear()
                            profileState = ProfileState.NotLoggedIn
                        },
                    )
                }
            }
        }
    }
}
