package com.bikeprojectminji.bikefront.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import com.bikeprojectminji.bikefront.ui.screen.GajaBrandTopBar
import com.bikeprojectminji.bikefront.ui.screen.GajaSectionCard
import com.bikeprojectminji.bikefront.ui.screen.GajaStatusBadge
import com.bikeprojectminji.bikefront.ui.screen.GajaPrimaryButton
import com.bikeprojectminji.bikefront.ui.screen.SecondaryActionButton
import com.bikeprojectminji.bikefront.ui.screen.SectionHeader
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaSpacing
import com.bikeprojectminji.bikefront.ui.theme.GajaTheme
import com.bikeprojectminji.bikefront.R
import com.bikeprojectminji.bikefront.curator.CuratorOnboardingActivity
import com.bikeprojectminji.bikefront.curator.CuratorTravelPreferenceStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuthProfileActivity : ComponentActivity() {
    companion object {
        private const val EXTRA_RETURN_AFTER_SAVE = "extra_return_after_save"

        fun createIntent(context: Context, returnAfterSave: Boolean = false): Intent {
            return Intent(context, AuthProfileActivity::class.java).apply {
                putExtra(EXTRA_RETURN_AFTER_SAVE, returnAfterSave)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val returnAfterSave = intent.getBooleanExtra(EXTRA_RETURN_AFTER_SAVE, false)
        setContent {
            GajaTheme {
                AuthProfileScreen(
                    returnAfterSave = returnAfterSave,
                    onFinish = { finish() },
                    onSaved = {
                        setResult(Activity.RESULT_OK)
                        finish()
                    },
                )
            }
        }
    }
}

private data class AuthProfileSessionSnapshot(
    val signedIn: Boolean,
    val needsRefresh: Boolean,
    val refreshExpired: Boolean,
    val hasUsableAccessToken: Boolean,
    val displayName: String,
    val email: String,
    val userId: Long,
    val loginProvider: String,
    val profileImageUrl: String,
    val accessExpiryText: String,
    val refreshExpiryText: String,
)

private enum class AuthMode {
    LOGIN,
    REGISTER,
}

@Composable
fun AuthProfileScreen(returnAfterSave: Boolean, onFinish: () -> Unit, onSaved: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val authSessionStore = remember { AuthSessionStore(context) }
    val authLoginGateway = remember { HttpAuthLoginGateway() }
    val kakaoAccessTokenGateway = remember { KakaoSdkAccessTokenGateway() }
    val curatorPreferenceStore = remember { CuratorTravelPreferenceStore(context) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }
    var helperMessage by remember {
        mutableStateOf(
            if (returnAfterSave) {
                "로그인 후 저장 중이던 코스 흐름으로 바로 돌아갑니다."
            } else {
                "이메일과 비밀번호로 로그인해 주세요."
            },
        )
    }
    var inFlight by remember { mutableStateOf(false) }
    var sessionSnapshot by remember { mutableStateOf(readAuthProfileSessionSnapshot(authSessionStore)) }

    fun refreshSessionSnapshot(clearExpiredSession: Boolean = true) {
        if (clearExpiredSession && authSessionStore.isRefreshExpired()) {
            authSessionStore.clear()
        }
        sessionSnapshot = readAuthProfileSessionSnapshot(authSessionStore)
    }

    fun completeSession(
        loginResult: AuthLoginGateway.LoginResult,
        successMessage: String,
        returnOnSuccess: Boolean,
        fallbackProfileImageUrl: String,
        loginProvider: String,
    ) {
        authLoginGateway.getMyProfile(loginResult.accessToken, object : AuthLoginGateway.ProfileCallback {
            override fun onSuccess(result: AuthLoginGateway.ProfileResult) {
                authSessionStore.saveSession(
                    AuthSessionFactory.create(
                        loginResult,
                        result,
                        loginProvider,
                        fallbackProfileImageUrl,
                        System.currentTimeMillis(),
                    )
                )
                inFlight = false
                refreshSessionSnapshot(clearExpiredSession = false)
                helperMessage = successMessage
                if (returnOnSuccess) {
                    onSaved()
                } else if (!curatorPreferenceStore.isCompleted()) {
                    context.startActivity(CuratorOnboardingActivity.createIntent(context))
                }
            }

            override fun onFailure(message: String) {
                authSessionStore.saveSession(
                    AuthSessionFactory.create(
                        loginResult,
                        loginResult.displayName,
                        fallbackProfileImageUrl,
                        loginProvider,
                        System.currentTimeMillis(),
                    )
                )
                inFlight = false
                refreshSessionSnapshot(clearExpiredSession = false)
                helperMessage = if (message.isBlank()) successMessage else "$successMessage 프로필 동기화는 다음에 다시 시도합니다."
                if (returnOnSuccess) {
                    onSaved()
                } else if (!curatorPreferenceStore.isCompleted()) {
                    context.startActivity(CuratorOnboardingActivity.createIntent(context))
                }
            }
        })
    }

    fun requestRefresh(returnOnSuccess: Boolean) {
        val refreshToken = authSessionStore.refreshToken
        if (refreshToken.isBlank()) {
            authSessionStore.clear()
            refreshSessionSnapshot(clearExpiredSession = false)
            helperMessage = "로그인 세션이 만료되었습니다. 다시 로그인해 주세요."
            return
        }

        inFlight = true
        helperMessage = "세션을 갱신하는 중입니다."
        authLoginGateway.refresh(refreshToken, object : AuthLoginGateway.Callback {
            override fun onSuccess(result: AuthLoginGateway.LoginResult) {
                val fallbackProfileImageUrl = authSessionStore.profileImageUrl
                completeSession(result, "세션이 갱신되었습니다.", returnOnSuccess, fallbackProfileImageUrl, authSessionStore.loginProvider.ifBlank { "email" })
            }

            override fun onFailure(message: String) {
                inFlight = false
                authSessionStore.clear()
                refreshSessionSnapshot(clearExpiredSession = false)
                helperMessage = message
            }
        })
    }

    fun requestLogin() {
        val safeEmail = email.trim()
        val safePassword = password.trim()
        if (safeEmail.isBlank() || safePassword.isBlank()) {
            helperMessage = "이메일과 비밀번호를 모두 입력해 주세요."
            return
        }

        inFlight = true
        helperMessage = "로그인 중입니다."
        authLoginGateway.login(safeEmail, safePassword, object : AuthLoginGateway.Callback {
            override fun onSuccess(result: AuthLoginGateway.LoginResult) {
                completeSession(result, "로그인되었습니다.", returnAfterSave, authSessionStore.profileImageUrl, "email")
            }

            override fun onFailure(message: String) {
                inFlight = false
                helperMessage = message
            }
        })
    }

    fun requestKakaoLogin() {
        if (activity == null) {
            helperMessage = "카카오 로그인을 시작할 수 없습니다."
            return
        }
        if (context.getString(R.string.kakao_native_app_key).isBlank()) {
            helperMessage = "카카오 네이티브 앱 키를 설정하면 카카오 로그인을 사용할 수 있습니다."
            return
        }

        inFlight = true
        helperMessage = "카카오 계정으로 확인하는 중입니다."
        kakaoAccessTokenGateway.requestAccessToken(activity, object : KakaoAccessTokenGateway.Callback {
            override fun onSuccess(accessToken: String) {
                authLoginGateway.kakaoLogin(accessToken, object : AuthLoginGateway.Callback {
                    override fun onSuccess(result: AuthLoginGateway.LoginResult) {
                        completeSession(result, "카카오 로그인되었습니다.", returnAfterSave, authSessionStore.profileImageUrl, "kakao")
                    }

                    override fun onFailure(message: String) {
                        inFlight = false
                        helperMessage = message
                    }
                })
            }

            override fun onFailure(message: String) {
                inFlight = false
                helperMessage = message
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun requestRegister() {
        val safeEmail = email.trim()
        val safePassword = password.trim()
        val safeDisplayName = displayName.trim()
        if (safeEmail.isBlank()) {
            helperMessage = "이메일을 먼저 입력해 주세요."
            return
        }
        if (safePassword.isBlank()) {
            helperMessage = "비밀번호를 먼저 입력해 주세요."
            return
        }
        if (safeDisplayName.isBlank()) {
            helperMessage = "표시 이름을 먼저 입력해 주세요."
            return
        }

        inFlight = true
        helperMessage = "회원가입 중입니다."
        authLoginGateway.register(safeEmail, safePassword, safeDisplayName, object : AuthLoginGateway.Callback {
            override fun onSuccess(result: AuthLoginGateway.LoginResult) {
                completeSession(result, "회원가입이 완료되었습니다.", returnAfterSave, authSessionStore.profileImageUrl, "email")
            }

            override fun onFailure(message: String) {
                inFlight = false
                helperMessage = message
            }
        })
    }

    LaunchedEffect(Unit) {
        refreshSessionSnapshot()
        if (sessionSnapshot.refreshExpired) {
            helperMessage = "로그인 세션이 만료되었습니다. 다시 로그인해 주세요."
        } else if (sessionSnapshot.needsRefresh) {
            requestRefresh(returnAfterSave)
        } else if (sessionSnapshot.signedIn) {
            helperMessage = "저장된 로그인 세션을 사용 중입니다."
        }
    }

    Scaffold(
        topBar = { GajaBrandTopBar(title = if (sessionSnapshot.signedIn) "내 계정" else "로그인") },
        containerColor = GajaColors.Background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = GajaSpacing.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(GajaSpacing.Large),
        ) {
            Spacer(Modifier.height(GajaSpacing.Small))

            GajaSectionCard(
                containerColor = GajaColors.SurfaceMuted,
                shape = RoundedCornerShape(GajaSpacing.Large),
            ) {
                Text(
                    text = helperMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GajaColors.TextSecondary,
                )
            }

            if (sessionSnapshot.signedIn) {
                SignedInSessionContent(
                    sessionSnapshot = sessionSnapshot,
                    inFlight = inFlight,
                    onRefresh = { requestRefresh(returnAfterSave) },
                    onLogout = {
                        authSessionStore.clear()
                        refreshSessionSnapshot(clearExpiredSession = false)
                        helperMessage = "로그아웃되었습니다."
                        if (returnAfterSave) {
                            onFinish()
                        }
                    },
                    onOpenPreferences = {
                        context.startActivity(CuratorOnboardingActivity.createIntent(context))
                    },
                    onClose = onFinish,
                )
            } else {
                LoggedOutLoginContent(
                    returnAfterSave = returnAfterSave,
                    authMode = authMode,
                    email = email,
                    password = password,
                    displayName = displayName,
                    inFlight = inFlight,
                    onModeChange = {
                        authMode = it
                        helperMessage = if (it == AuthMode.LOGIN) {
                            if (returnAfterSave) {
                                "기존 계정으로 로그인하면 저장 중이던 코스 흐름을 이어갑니다."
                            } else {
                                "이메일과 비밀번호로 로그인해 주세요."
                            }
                        } else {
                            if (returnAfterSave) {
                                "회원가입 후 바로 코스 저장과 공유 흐름으로 돌아갑니다."
                            } else {
                                "회원가입 후 바로 코스 저장과 공유를 이어갈 수 있습니다."
                            }
                        }
                    },
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onDisplayNameChange = { displayName = it },
                    onKakaoLogin = { requestKakaoLogin() },
                    onSubmit = {
                        if (authMode == AuthMode.LOGIN) {
                            requestLogin()
                        } else {
                            requestRegister()
                        }
                    },
                    onClose = onFinish,
                )
            }
            Spacer(Modifier.height(GajaSpacing.Large))
        }
    }
}

@Composable
private fun LoggedOutLoginContent(
    returnAfterSave: Boolean,
    authMode: AuthMode,
    email: String,
    password: String,
    displayName: String,
    inFlight: Boolean,
    onModeChange: (AuthMode) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onKakaoLogin: () -> Unit,
    onSubmit: () -> Unit,
    onClose: () -> Unit,
) {
    GajaSectionCard(
        containerColor = GajaColors.PrimarySoft,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(GajaSpacing.Large),
    ) {
        Text(
            text = if (authMode == AuthMode.LOGIN) "이메일로 바로 시작" else "간단히 가입하고 이어서 이용",
            style = MaterialTheme.typography.titleLarge,
            color = GajaColors.TextPrimary,
        )
        Text(
            text = if (authMode == AuthMode.LOGIN) {
                if (returnAfterSave) "로그인 후 저장 중이던 코스 흐름으로 바로 돌아갑니다." else "필요한 정보만 입력하면 바로 라이딩 기록을 이어갈 수 있어요."
            } else {
                if (returnAfterSave) "가입을 마치면 저장 중이던 흐름으로 곧바로 돌아갑니다." else "표시 이름까지 입력하면 바로 이용을 시작할 수 있어요."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = GajaColors.TextSecondary,
        )
    }

    SectionHeader(
        title = if (authMode == AuthMode.LOGIN) "로그인" else "회원가입",
        subtitle = if (authMode == AuthMode.LOGIN) {
            if (returnAfterSave) {
                "기존 계정으로 바로 이어가기"
            } else {
                "이메일로 빠르게 시작"
            }
        } else {
            if (returnAfterSave) {
                "가입 후 저장 흐름으로 돌아가기"
            } else {
                "기본 정보만 입력하고 시작"
            }
        },
    )

    GajaPrimaryButton(
        text = if (inFlight) "확인 중..." else "카카오로 계속하기",
        onClick = onKakaoLogin,
        enabled = !inFlight,
    )

    GajaSectionCard(
        containerColor = GajaColors.SurfaceMuted,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(GajaSpacing.Medium),
    ) {
        Text(
            text = "개인정보 처리방침, 이용약관, 위치기반서비스 고지에 동의한 뒤 카카오 계정으로 시작합니다.",
            style = MaterialTheme.typography.bodySmall,
            color = GajaColors.TextSecondary,
        )
        Text(
            text = "${KakaoLoginPolicyVersions.PRIVACY_POLICY_VERSION} · ${KakaoLoginPolicyVersions.TERMS_VERSION} · ${KakaoLoginPolicyVersions.LOCATION_TERMS_VERSION}",
            style = MaterialTheme.typography.labelMedium,
            color = GajaColors.TextPrimary,
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Small)) {
        FilterChip(
            selected = authMode == AuthMode.LOGIN,
            onClick = { onModeChange(AuthMode.LOGIN) },
            label = { Text("로그인") },
            enabled = !inFlight,
            colors = authModeChipColors(),
        )
        FilterChip(
            selected = authMode == AuthMode.REGISTER,
            onClick = { onModeChange(AuthMode.REGISTER) },
            label = { Text("회원가입") },
            enabled = !inFlight,
            colors = authModeChipColors(),
        )
    }

    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("이메일") },
        placeholder = { Text("bikeoasis@example.com") },
        enabled = !inFlight,
        shape = MaterialTheme.shapes.medium,
        colors = outlinedFieldColors(),
    )

    if (authMode == AuthMode.REGISTER) {
        OutlinedTextField(
            value = displayName,
            onValueChange = onDisplayNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("표시 이름") },
            placeholder = { Text("예: 한강라이더") },
            enabled = !inFlight,
            shape = MaterialTheme.shapes.medium,
            colors = outlinedFieldColors(),
        )
    }

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("비밀번호") },
        placeholder = { Text("비밀번호를 입력하세요") },
        enabled = !inFlight,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        shape = MaterialTheme.shapes.medium,
        colors = outlinedFieldColors(),
    )

    GajaPrimaryButton(
        text = when {
            inFlight && authMode == AuthMode.LOGIN -> "로그인 중..."
            inFlight && authMode == AuthMode.REGISTER -> "회원가입 중..."
            authMode == AuthMode.LOGIN && returnAfterSave -> "로그인하고 저장 이어가기"
            authMode == AuthMode.LOGIN -> "로그인"
            returnAfterSave -> "회원가입하고 저장 이어가기"
            else -> "회원가입"
        },
        onClick = onSubmit,
        enabled = !inFlight,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing)) {
        SecondaryActionButton(
            text = if (authMode == AuthMode.LOGIN) "회원가입으로 전환" else "로그인으로 전환",
            onClick = { onModeChange(if (authMode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN) },
            enabled = !inFlight,
        )
        Spacer(modifier = Modifier.width(GajaSpacing.Tiny))
    }
    SecondaryActionButton(text = "닫기", onClick = onClose)
}

@Composable
private fun SignedInSessionContent(
    sessionSnapshot: AuthProfileSessionSnapshot,
    inFlight: Boolean,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onOpenPreferences: () -> Unit,
    onClose: () -> Unit,
) {
    SectionHeader(
        title = "계정 상태",
        subtitle = if (sessionSnapshot.needsRefresh) "로그인 상태를 한 번 새로 확인해 주세요." else "현재 계정으로 저장과 공유를 이어갈 수 있어요.",
    )

    GajaSectionCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(GajaSpacing.Large)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny)) {
                Text(sessionSnapshot.displayName, style = MaterialTheme.typography.headlineSmall, color = GajaColors.TextPrimary)
                Text(
                    text = if (sessionSnapshot.hasUsableAccessToken) "현재 계정으로 바로 이용할 수 있어요." else "다시 확인하면 안전하게 이어서 이용할 수 있어요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GajaColors.TextSecondary,
                )
            }
            GajaStatusBadge(text = if (sessionSnapshot.needsRefresh) "재확인 필요" else "이용 가능")
        }

        SessionStatusRow("세션 상태", if (sessionSnapshot.hasUsableAccessToken) "바로 이용 가능" else "새로 확인 필요")
        SessionStatusRow("로그인 방식", sessionSnapshot.loginProviderLabel)
        SessionStatusRow("계정 이메일", sessionSnapshot.email.ifBlank { "카카오에서 제공되지 않음" })
        SessionStatusRow("사용자 ID", if (sessionSnapshot.userId > 0L) sessionSnapshot.userId.toString() else "확인 전")
        SessionStatusRow("프로필 이미지", if (sessionSnapshot.profileImageUrl.isBlank()) "아직 없음" else "연결됨")
    }

    GajaPrimaryButton(
        text = if (inFlight) "확인 중..." else if (sessionSnapshot.needsRefresh) "로그인 상태 새로 확인" else "계속 이용하기",
        onClick = onRefresh,
        enabled = !inFlight,
    )
    SecondaryActionButton(text = "로그아웃", onClick = onLogout)
    SecondaryActionButton(text = "여행 취향 설정", onClick = onOpenPreferences)
    SecondaryActionButton(text = "닫기", onClick = onClose)
}

@Composable
private fun SessionStatusRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = GajaColors.TextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = GajaColors.TextPrimary)
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = GajaColors.Primary,
    unfocusedBorderColor = GajaColors.Border,
    focusedTextColor = GajaColors.TextPrimary,
    unfocusedTextColor = GajaColors.TextPrimary,
)

@Composable
private fun authModeChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = GajaColors.Primary,
    selectedLabelColor = GajaColors.White,
    containerColor = GajaColors.White,
    labelColor = GajaColors.TextSecondary,
)

private fun readAuthProfileSessionSnapshot(store: AuthSessionStore): AuthProfileSessionSnapshot {
    val session = store.storedSession
    val state = AuthSessionStateResolver.resolve(session, System.currentTimeMillis())
    return AuthProfileSessionSnapshot(
        signedIn = state.isSignedIn,
        needsRefresh = state.isNeedsRefresh,
        refreshExpired = state.isRefreshExpired,
        hasUsableAccessToken = state.isHasUsableAccessToken,
        displayName = store.displayName.ifBlank { "bikeoasis" },
        email = store.email,
        userId = store.userId,
        loginProvider = store.loginProvider,
        profileImageUrl = store.profileImageUrl,
        accessExpiryText = formatExpiry(session?.accessTokenExpiresAtEpochMillis ?: 0L),
        refreshExpiryText = formatExpiry(session?.refreshTokenExpiresAtEpochMillis ?: 0L),
    )
}

private val AuthProfileSessionSnapshot.loginProviderLabel: String
    get() = when (loginProvider) {
        "kakao" -> "카카오"
        "email" -> "이메일"
        else -> "확인 전"
    }

private fun formatExpiry(epochMillis: Long): String {
    if (epochMillis <= 0L) {
        return "저장되지 않음"
    }
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).format(Date(epochMillis))
}
