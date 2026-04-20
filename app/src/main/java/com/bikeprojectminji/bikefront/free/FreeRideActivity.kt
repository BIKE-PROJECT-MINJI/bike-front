package com.bikeprojectminji.bikefront.free

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.bikeprojectminji.bikefront.ui.screen.GajaMapPreview
import com.bikeprojectminji.bikefront.ui.screen.GajaPrimaryButton
import com.bikeprojectminji.bikefront.ui.screen.MapDisplayMode
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaTheme

class FreeRideActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val courseId = intent.getLongExtra(EXTRA_COURSE_ID, -1L).takeIf { it > 0L }
        setContent {
            GajaTheme {
                FreeRideMainScreen(
                    courseId = courseId,
                    onFinish = { finish() },
                )
            }
        }
    }

    companion object {
        const val EXTRA_COURSE_ID = "course_id"
    }
}

@Composable
fun FreeRideMainScreen(courseId: Long?, onFinish: () -> Unit) {
    val context = LocalContext.current
    var locationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        locationGranted = granted
    }

    LaunchedEffect(Unit) {
        if (!locationGranted) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(containerColor = GajaColors.Background) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            GajaMapPreview(
                modifier = Modifier.fillMaxSize(),
                mode = MapDisplayMode.RIDE,
                courseId = courseId,
                locationPermissionGranted = locationGranted,
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Surface(color = GajaColors.White.copy(alpha = 0.92f), shape = MaterialTheme.shapes.large) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(
                            if (courseId != null) "코스 주행 화면" else "자유 주행 화면",
                            style = MaterialTheme.typography.titleLarge,
                            color = GajaColors.TextPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            if (locationGranted) "지도와 현재 위치를 표시합니다." else "위치 권한 허용 시 현재 위치를 표시합니다.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GajaColors.TextSecondary,
                        )
                    }
                }
                GajaPrimaryButton(text = "화면 종료", onClick = onFinish)
            }
        }
    }
}
