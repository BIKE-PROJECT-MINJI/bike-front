package com.bikeprojectminji.bikefront.auth

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import com.bikeprojectminji.bikefront.R
import com.bikeprojectminji.bikefront.ui.screen.GajaOutlinedButton
import com.bikeprojectminji.bikefront.ui.screen.GajaSecondaryButton
import com.bikeprojectminji.bikefront.ui.theme.BikeFrontTheme

class AuthProfileActivity : ComponentActivity() {

    companion object {
        const val EXTRA_REASON = "extra_reason"
    }

    private lateinit var authSessionStore: AuthSessionStore
    private lateinit var authLoginGateway: AuthLoginGateway

    private var email by mutableStateOf("")
    private var password by mutableStateOf("")
    private var displayName by mutableStateOf("")
    private var statusMessage by mutableStateOf("")
    private var inFlight by mutableStateOf(false)
    private var isRegisterMode by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authSessionStore = AuthSessionStore(this)
        authLoginGateway = HttpAuthLoginGateway()
        displayName = authSessionStore.displayName

        setContent {
            BikeFrontTheme {
                AuthProfileScreen(
                    reason = intent.getStringExtra(EXTRA_REASON).orEmpty(),
                    email = email,
                    password = password,
                    displayName = displayName,
                    statusMessage = statusMessage,
                    inFlight = inFlight,
                    isRegisterMode = isRegisterMode,
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onDisplayNameChange = { displayName = it },
                    onToggleMode = { isRegisterMode = !isRegisterMode },
                    onRegister = { submit(isRegister = true) },
                    onLogin = { submit(isRegister = false) },
                    onLater = { finish() },
                )
            }
        }
    }

    private fun submit(isRegister: Boolean) {
        val safeEmail = email.trim()
        val safePassword = password.trim()
        val safeDisplayName = displayName.trim()

        if (safeEmail.isBlank()) {
            statusMessage = getString(R.string.auth_profile_email_required_message)
            return
        }
        if (safePassword.isBlank()) {
            statusMessage = getString(R.string.auth_profile_password_required_message)
            return
        }
        if (isRegister && safeDisplayName.isBlank()) {
            statusMessage = getString(R.string.auth_profile_name_required_message)
            return
        }

        inFlight = true
        statusMessage = getString(R.string.auth_profile_signing_in_message)
        val callback = object : AuthLoginGateway.Callback {
            override fun onSuccess(result: AuthLoginGateway.LoginResult) {
                inFlight = false
                authSessionStore.saveSession(result.displayName, "", result.accessToken)
                setResult(Activity.RESULT_OK)
                finish()
            }

            override fun onFailure(message: String) {
                inFlight = false
                statusMessage = message
            }
        }

        if (isRegister) {
            authLoginGateway.register(safeEmail, safePassword, safeDisplayName, callback)
        } else {
            authLoginGateway.login(safeEmail, safePassword, callback)
        }
    }
}

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.runtime.Composable
private fun AuthProfileScreen(
    reason: String,
    email: String,
    password: String,
    displayName: String,
    statusMessage: String,
    inFlight: Boolean,
    isRegisterMode: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onToggleMode: () -> Unit,
    onRegister: () -> Unit,
    onLogin: () -> Unit,
    onLater: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "GAJA",
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    titleContentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.inverseOnSurface,
                ),
            )
        },
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // === Hero Section with Strong Visual Hierarchy ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.inverseSurface,
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                        ),
                    )
                    .padding(horizontal = 24.dp, vertical = 32.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    ) {
                        Text(
                            text = if (isRegisterMode) "ACCOUNT SETUP" else "ACCOUNT ACCESS",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    Text(
                        text = if (isRegisterMode) "라이딩 기록을 남길 준비를 시작합니다" else "기록을 이어서 주행 흐름으로 복귀합니다",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                    )
                    Text(
                        text = if (reason.isBlank()) {
                            if (isRegisterMode) "계정을 만들면 주행 저장, 코스 보관, 계정 기반 동기화가 하나의 흐름으로 연결됩니다." else "로그인 후 저장, 코스 작성, 계정 기반 기록 관리를 바로 이어갈 수 있습니다."
                        } else {
                            reason
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.82f),
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // === Mode Toggle ===
                ModeToggleCard(
                    isRegisterMode = isRegisterMode,
                    onToggleMode = onToggleMode,
                    enabled = !inFlight,
                )

                // === Form Card ===
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = 2.dp,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        Text(
                            text = if (isRegisterMode) "새 계정 만들기" else "계정으로 로그인",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        // === Email Field ===
                        AuthInputField(
                            label = "이메일",
                            value = email,
                            placeholder = "example@email.com",
                            onValueChange = onEmailChange,
                            keyboardType = KeyboardType.Email,
                            enabled = !inFlight,
                        )

                        // === Password Field ===
                        AuthInputField(
                            label = "비밀번호",
                            value = password,
                            placeholder = "비밀번호를 입력하세요",
                            onValueChange = onPasswordChange,
                            password = true,
                            keyboardType = KeyboardType.Password,
                            enabled = !inFlight,
                        )

                        // === Display Name Field (Register only) ===
                        AnimatedVisibility(
                            visible = isRegisterMode,
                            enter = fadeIn(animationSpec = tween(200)) + slideInVertically(),
                            exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(),
                        ) {
                            AuthInputField(
                                label = "표시 이름",
                                value = displayName,
                                placeholder = "예: 한강라이더",
                                onValueChange = onDisplayNameChange,
                                keyboardType = KeyboardType.Text,
                                enabled = !inFlight,
                            )
                        }
                    }
                }

                // === Status Message ===
                AnimatedVisibility(
                    visible = statusMessage.isNotBlank(),
                    enter = fadeIn(animationSpec = tween(200)) + slideInVertically(),
                    exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(),
                ) {
                    StatusMessageCard(
                        message = statusMessage,
                        isError = statusMessage.contains("실패") || statusMessage.contains("오류"),
                    )
                }

                // === Loading Indicator ===
                AnimatedVisibility(
                    visible = inFlight,
                    enter = fadeIn(animationSpec = tween(200)),
                    exit = fadeOut(animationSpec = tween(200)),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = "처리 중입니다...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // === Primary Action Button ===
                Button(
                    onClick = if (isRegisterMode) onRegister else onLogin,
                    enabled = !inFlight,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.outline,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        text = if (isRegisterMode) "가입하고 계속" else "로그인",
                        modifier = Modifier.padding(vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                // === Secondary Actions ===
                if (isRegisterMode) {
                    TextButton(onClick = onToggleMode, enabled = !inFlight, modifier = Modifier.fillMaxWidth()) {
                        Text("기존 계정으로 로그인")
                    }
                } else {
                    TextButton(onClick = onToggleMode, enabled = !inFlight, modifier = Modifier.fillMaxWidth()) {
                        Text("새 계정 만들기")
                    }
                }

                GajaOutlinedButton(text = "나중에 할게요", onClick = onLater, enabled = !inFlight)

                // === Help Section ===
                HelpSection()
            }
        }
    }
}

// === Mode Toggle Card ===
@Composable
private fun ModeToggleCard(
    isRegisterMode: Boolean,
    onToggleMode: () -> Unit,
    enabled: Boolean,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            ) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (isRegisterMode) "01" else "02",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = if (isRegisterMode) "처음 오셨나요?" else "이미 계정이 있으신가요?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (isRegisterMode) "새 계정을 만들어 시작하세요" else "로그인하고 바로 시작하세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(
                onClick = onToggleMode,
                enabled = enabled,
            ) {
                Text(if (isRegisterMode) "로그인" else "가입")
            }
        }
    }
}

// === Auth Input Field with Enhanced Readability ===
@Composable
private fun AuthInputField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    password: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            },
            singleLine = true,
            enabled = enabled,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal,
            ),
            visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Done,
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            ),
            shape = RoundedCornerShape(14.dp),
        )
    }
}

// === Status Message Card ===
@Composable
private fun StatusMessageCard(
    message: String,
    isError: Boolean,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isError) {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (isError) Icons.Outlined.WarningAmber else Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                },
            )
        }
    }
}

// === Help Section ===
@Composable
private fun HelpSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "도움이 필요하신가요?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
        TextButton(
            onClick = { /* TODO: Open help */ },
        ) {
            Text(
                text = "고객센터 문의하기",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
