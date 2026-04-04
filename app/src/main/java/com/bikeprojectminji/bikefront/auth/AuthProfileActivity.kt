package com.bikeprojectminji.bikefront.auth

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bikeprojectminji.bikefront.R
import com.bikeprojectminji.bikefront.ui.screen.SectionTitle
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
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onDisplayNameChange = { displayName = it },
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
@androidx.compose.runtime.Composable
private fun AuthProfileScreen(
    reason: String,
    email: String,
    password: String,
    displayName: String,
    statusMessage: String,
    inFlight: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onRegister: () -> Unit,
    onLogin: () -> Unit,
    onLater: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionTitle(
            title = "로그인하고 저장 이어가기",
            subtitle = if (reason.isBlank()) "로그인 후 저장과 코스 만들기를 이어갈 수 있습니다." else reason,
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("이메일") },
            placeholder = { Text("예: bikeoasis@example.com") },
            singleLine = true,
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("비밀번호") },
            placeholder = { Text("비밀번호를 입력해 주세요") },
            singleLine = true,
        )

        OutlinedTextField(
            value = displayName,
            onValueChange = onDisplayNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("표시 이름") },
            placeholder = { Text("예: 한강라이더") },
            singleLine = true,
        )

        if (statusMessage.isNotBlank()) {
            Text(text = statusMessage, style = MaterialTheme.typography.bodyMedium)
        }

        if (inFlight) {
            CircularProgressIndicator()
        }

        Button(onClick = onRegister, enabled = !inFlight, modifier = Modifier.fillMaxWidth()) {
            Text("가입하고 계속")
        }
        OutlinedButton(onClick = onLogin, enabled = !inFlight, modifier = Modifier.fillMaxWidth()) {
            Text("기존 계정으로 로그인")
        }
        OutlinedButton(onClick = onLater, enabled = !inFlight, modifier = Modifier.fillMaxWidth()) {
            Text("나중에 할게요")
        }
    }
}
