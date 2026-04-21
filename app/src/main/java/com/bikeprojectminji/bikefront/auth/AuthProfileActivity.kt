package com.bikeprojectminji.bikefront.auth

import android.app.Activity
import android.content.Context
import android.content.Intent

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bikeprojectminji.bikefront.ui.screen.GajaBrandTopBar
import com.bikeprojectminji.bikefront.ui.screen.GajaPrimaryButton
import com.bikeprojectminji.bikefront.ui.screen.SecondaryActionButton
import com.bikeprojectminji.bikefront.ui.theme.GajaTheme
import com.bikeprojectminji.bikefront.ui.theme.GajaSpacing
import com.bikeprojectminji.bikefront.BuildConfig
import com.bikeprojectminji.bikefront.config.AppConfig
import com.bikeprojectminji.bikefront.ui.theme.GajaColors

private const val MOCK_TEST_TOKEN = "mock-test-token"

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

@Composable
fun AuthProfileScreen(returnAfterSave: Boolean, onFinish: () -> Unit, onSaved: () -> Unit) {
    val context = LocalContext.current
    val authSessionStore = remember { AuthSessionStore(context) }
    var inputName by remember { mutableStateOf(authSessionStore.displayName ?: "") }
    var inputAccessToken by remember { mutableStateOf(authSessionStore.accessToken ?: "") }

    Scaffold(
        topBar = { GajaBrandTopBar(title = "Profile Settings") },
        containerColor = GajaColors.Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = GajaSpacing.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(GajaSpacing.Large)
        ) {
            Spacer(Modifier.height(GajaSpacing.Small))

            Text(
                text = "사용자 이름", 
                style = MaterialTheme.typography.titleMedium, 
                color = GajaColors.TextPrimary
            )

            OutlinedTextField(
                value = inputName,
                onValueChange = { inputName = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("이름을 입력하세요") },
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GajaColors.Primary,
                    unfocusedBorderColor = GajaColors.Border,
                    focusedTextColor = GajaColors.TextPrimary,
                    unfocusedTextColor = GajaColors.TextPrimary
                )
            )

            Text(
                text = "접근 토큰",
                style = MaterialTheme.typography.titleMedium,
                color = GajaColors.TextPrimary
            )

            OutlinedTextField(
                value = inputAccessToken,
                onValueChange = { inputAccessToken = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Bearer 토큰을 입력하세요") },
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GajaColors.Primary,
                    unfocusedBorderColor = GajaColors.Border,
                    focusedTextColor = GajaColors.TextPrimary,
                    unfocusedTextColor = GajaColors.TextPrimary
                )
            )

            if (BuildConfig.DEBUG && (AppConfig.API_BASE_URL.contains("127.0.0.1") || AppConfig.API_BASE_URL.contains("10.0.2.2") || AppConfig.API_BASE_URL.contains("localhost"))) {
                SecondaryActionButton(
                    text = "테스트 토큰 채우기",
                    onClick = {
                        inputName = "bikeoasis"
                        inputAccessToken = MOCK_TEST_TOKEN
                        authSessionStore.saveSession(
                            "bikeoasis",
                            authSessionStore.profileImageUrl,
                            MOCK_TEST_TOKEN
                        )
                        Toast.makeText(context, "테스트 토큰이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            Spacer(Modifier.weight(1f))

            GajaPrimaryButton(
                text = "저장하기",
                onClick = {
                    // AuthSessionStore is Java and only has saveSession(displayName, profileImageUrl, accessToken)
                    // No separate setter for displayName exists.
                    authSessionStore.saveSession(
                        inputName,
                        authSessionStore.profileImageUrl,
                        inputAccessToken.trim()
                    )
                    Toast.makeText(context, "프로필과 토큰이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                    if (returnAfterSave) onSaved() else onFinish()
                }
            )
            
            SecondaryActionButton(
                text = "로그아웃",
                onClick = {
                    authSessionStore.clear()
                    onFinish()
                }
            )
            Spacer(Modifier.height(GajaSpacing.Large))
        }
    }
}
