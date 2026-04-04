package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun RideStartScreen(
    innerPadding: PaddingValues,
    onOpenFreeRide: () -> Unit,
    onOpenCourse: (CourseCardUiModel) -> Unit,
    onOpenCourses: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val repository = remember(context) { CoursesRepository(context) }
    var refreshKey by remember { mutableStateOf(0) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshKey++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val featuredCourses = produceState<List<CourseCardUiModel>>(initialValue = emptyList(), key1 = refreshKey) {
        value = runCatching {
            withContext(Dispatchers.IO) { repository.fetchFeaturedCourses() }
        }.getOrDefault(emptyList())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        SectionTitle(
            title = "라이딩 시작",
            subtitle = "지금 바로 자유 주행을 시작하거나 추천 코스로 진입하세요.",
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "자유 주행", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary)
                Text(
                    text = "코스 없이 바로 기록을 시작하는 모드입니다. 현재 구현된 자유 주행 화면으로 바로 진입합니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Button(onClick = onOpenFreeRide) {
                    Text("자유 주행 준비 화면 열기")
                }
            }
        }

        SectionTitle(title = "추천 코스", subtitle = "빠르게 바로 탈 수 있는 코스를 먼저 보여줍니다.")
        if (featuredCourses.value.isEmpty()) {
            Text(text = "추천 코스를 불러오는 중이거나 아직 준비되지 않았습니다.", style = MaterialTheme.typography.bodyMedium)
        } else {
            featuredCourses.value.forEach { course ->
                CourseCard(course = course, onClick = { onOpenCourse(course) })
            }
        }

        OutlinedButton(onClick = onOpenCourses, modifier = Modifier.fillMaxWidth()) {
            Text("전체 코스 보러 가기")
        }
        OutlinedButton(onClick = onOpenProfile, modifier = Modifier.fillMaxWidth()) {
            Text("내 정보 / 로그인")
        }
    }
}
