package com.bikeprojectminji.bikefront.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.bikeprojectminji.bikefront.ui.screen.GajaBrandTopBar
import com.bikeprojectminji.bikefront.ui.screen.GajaPrimaryButton
import com.bikeprojectminji.bikefront.ui.screen.SecondaryActionButton
import com.bikeprojectminji.bikefront.ui.screen.SectionHeader
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaSpacing
import com.bikeprojectminji.bikefront.ui.theme.GajaTheme
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
    val authSessionStore = remember { AuthSessionStore(context) }
    val authLoginGateway = remember { HttpAuthLoginGateway() }

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

    fun completeSession(loginResult: AuthLoginGateway.LoginResult, successMessage: String, returnOnSuccess: Boolean, fallbackProfileImageUrl: String) {
        authLoginGateway.getMyProfile(loginResult.accessToken, object : AuthLoginGateway.ProfileCallback {
            override fun onSuccess(result: AuthLoginGateway.ProfileResult) {
                authSessionStore.saveSession(
                    AuthSessionFactory.create(
                        loginResult,
                        result.displayName.ifBlank { loginResult.displayName },
                        result.profileImageUrl.ifBlank { fallbackProfileImageUrl },
                        System.currentTimeMillis(),
                    )
                )
                inFlight = false
                refreshSessionSnapshot(clearExpiredSession = false)
                helperMessage = successMessage
                if (returnOnSuccess) {
                    onSaved()
                }
            }

            override fun onFailure(message: String) {
                authSessionStore.saveSession(
                    AuthSessionFactory.create(
                        loginResult,
                        fallbackProfileImageUrl,
                        System.currentTimeMillis(),
                    )
                )
                inFlight = false
                refreshSessionSnapshot(clearExpiredSession = false)
                helperMessage = if (message.isBlank()) successMessage else "$successMessage 프로필 동기화는 다음에 다시 시도합니다."
                if (returnOnSuccess) {
                    onSaved()
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
                completeSession(result, "세션이 갱신되었습니다.", returnOnSuccess, fallbackProfileImageUrl)
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
                completeSession(result, "로그인되었습니다.", returnAfterSave, authSessionStore.profileImageUrl)
            }

            override fun onFailure(message: String) {
                inFlight = false
                helperMessage = message
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
                completeSession(result, "회원가입이 완료되었습니다.", returnAfterSave, authSessionStore.profileImageUrl)
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
        topBar = { GajaBrandTopBar(title = "프로필") },
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

            Text(
                text = helperMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = GajaColors.TextSecondary,
            )

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
    onSubmit: () -> Unit,
    onClose: () -> Unit,
) {
    SectionHeader(
        title = if (authMode == AuthMode.LOGIN) "로그인" else "회원가입",
        subtitle = if (authMode == AuthMode.LOGIN) {
            if (returnAfterSave) {
                "기존 계정으로 로그인하고 저장 중이던 코스 흐름을 바로 이어갑니다."
            } else {
                "이메일/비밀번호로 세션을 만들고 갱신합니다."
            }
        } else {
            if (returnAfterSave) {
                "새 계정을 만든 뒤 바로 코스 저장과 공유 흐름으로 돌아갑니다."
            } else {
                "새 계정을 만든 뒤 바로 프로필 세션을 저장합니다."
            }
        },
    )

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
    onClose: () -> Unit,
) {
    SectionHeader(
        title = "계정 상태",
        subtitle = if (sessionSnapshot.needsRefresh) "access token이 만료되어 refresh로 갱신할 수 있습니다." else "현재 세션이 저장되어 있습니다.",
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = GajaColors.White),
    ) {
        Column(
            modifier = Modifier.padding(GajaSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(GajaSpacing.Small),
        ) {
            Text(sessionSnapshot.displayName, style = MaterialTheme.typography.headlineSmall, color = GajaColors.TextPrimary)
            Text(
                text = if (sessionSnapshot.profileImageUrl.isBlank()) "프로필 이미지 URL이 아직 없습니다." else sessionSnapshot.profileImageUrl,
                style = MaterialTheme.typography.bodySmall,
                color = GajaColors.TextSecondary,
            )
            SessionStatusRow("세션 상태", if (sessionSnapshot.hasUsableAccessToken) "즉시 사용 가능" else "갱신 필요")
            SessionStatusRow("access 만료 시각", sessionSnapshot.accessExpiryText)
            SessionStatusRow("refresh 만료 시각", sessionSnapshot.refreshExpiryText)
        }
    }

    GajaPrimaryButton(
        text = if (inFlight) "세션 처리 중..." else if (sessionSnapshot.needsRefresh) "세션 갱신" else "세션 다시 확인",
        onClick = onRefresh,
        enabled = !inFlight,
    )
    SecondaryActionButton(text = "로그아웃", onClick = onLogout)
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
        profileImageUrl = store.profileImageUrl,
        accessExpiryText = formatExpiry(session?.accessTokenExpiresAtEpochMillis ?: 0L),
        refreshExpiryText = formatExpiry(session?.refreshTokenExpiresAtEpochMillis ?: 0L),
    )
}

private fun formatExpiry(epochMillis: Long): String {
    if (epochMillis <= 0L) {
        return "저장되지 않음"
    }
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).format(Date(epochMillis))
}
