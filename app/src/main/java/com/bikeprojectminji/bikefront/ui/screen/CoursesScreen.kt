package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private enum class CourseTab(val label: String) {
    RECOMMENDED("추천"),
    ALL("전체"),
}

@Composable
fun CoursesScreen(
    innerPadding: PaddingValues,
    onOpenCourse: (CourseCardUiModel) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val repository = remember(context) { CoursesRepository(context) }
    var selectedTab by remember { mutableStateOf(CourseTab.RECOMMENDED) }
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

    val featuredState = produceState<List<CourseCardUiModel>>(initialValue = emptyList(), key1 = refreshKey) {
        value = runCatching { withContext(Dispatchers.IO) { repository.fetchFeaturedCourses() } }.getOrDefault(emptyList())
    }
    val allCoursesState = produceState<CoursesPageUiModel?>(initialValue = null, key1 = refreshKey) {
        value = runCatching { withContext(Dispatchers.IO) { repository.fetchAllCourses() } }.getOrNull()
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
                text = "코스",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Tab Chips
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CourseTab.entries.forEach { tab ->
                    FilterChip(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        label = {
                            Text(
                                text = tab.label,
                                fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            }

            // Course List
            val courses = if (selectedTab == CourseTab.RECOMMENDED) featuredState.value else allCoursesState.value?.items.orEmpty()

            if (courses.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                    ) {
                        Text(
                            text = if (selectedTab == CourseTab.RECOMMENDED) "추천 코스를 준비 중입니다." else "전체 코스를 준비 중입니다.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(courses, key = { it.id }) { course ->
                        CourseCard(course = course, onClick = { onOpenCourse(course) })
                    }
                    if (selectedTab == CourseTab.ALL && allCoursesState.value?.hasNext == true) {
                        item {
                            GajaSecondaryButton(
                                text = "추가 코스 불러오기는 아직 연결하지 않았습니다",
                                onClick = { },
                                enabled = false,
                            )
                        }
                    }
                }
            }
        }
    }
}