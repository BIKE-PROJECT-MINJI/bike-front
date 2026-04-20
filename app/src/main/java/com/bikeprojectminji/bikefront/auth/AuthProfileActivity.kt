package com.bikeprojectminji.bikefront.auth

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
import com.bikeprojectminji.bikefront.ui.theme.GajaColors

class AuthProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GajaTheme {
                AuthProfileScreen(onFinish = { finish() })
            }
        }
    }
}

@Composable
fun AuthProfileScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val authSessionStore = remember { AuthSessionStore(context) }
    var inputName by remember { mutableStateOf(authSessionStore.displayName ?: "") }

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

            Spacer(Modifier.weight(1f))

            GajaPrimaryButton(
                text = "저장하기",
                onClick = {
                    // AuthSessionStore is Java and only has saveSession(displayName, profileImageUrl, accessToken)
                    // No separate setter for displayName exists.
                    authSessionStore.saveSession(
                        inputName,
                        authSessionStore.profileImageUrl,
                        authSessionStore.accessToken
                    )
                    Toast.makeText(context, "프로필이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                    onFinish()
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
