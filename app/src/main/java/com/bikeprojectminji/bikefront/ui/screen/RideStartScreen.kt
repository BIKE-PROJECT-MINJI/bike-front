package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaSpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RideStartScreen(
    innerPadding: PaddingValues,
    onStartFreeRide: () -> Unit,
    onOpenCourse: (CourseCardUiModel) -> Unit,
    onOpenMyInfo: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val repository = remember(context) { CoursesRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    var refreshKey by remember { mutableStateOf(0) }

    var featuredState by remember { mutableStateOf<SectionState<List<CourseCardUiModel>>>(SectionState.Loading) }
    var listState by remember { mutableStateOf<SectionState<CoursesPageUiModel>>(SectionState.Loading) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshKey++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(refreshKey) {
        if (featuredState !is SectionState.Success) featuredState = SectionState.Loading
        if (listState !is SectionState.Success) listState = SectionState.Loading

        launch {
            val result = runCatching { withContext(Dispatchers.IO) { repository.fetchFeaturedCourses() } }
            featuredState = result.fold({ SectionState.Success(it) }, { SectionState.Error("추천 로드 실패") })
        }
        launch {
            val result = runCatching { withContext(Dispatchers.IO) { repository.fetchAllCourses(limit = 10) } }
            listState = result.fold({ SectionState.Success(it) }, { SectionState.Error("목록 로드 실패") })
        }
    }

    Scaffold(
        topBar = { GajaBrandTopBar(title = "탐색", onProfileClick = onOpenMyInfo) },
        containerColor = GajaColors.Background
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // 1. 요약 대시보드
            Box(modifier = Modifier.padding(horizontal = GajaSpacing.ScreenPadding)) {
                ActivitySummaryDashboard()
            }

            // 2. 가로 스크롤 추천 섹션
            Column {
                SectionHeader(
                    title = "인기 라이딩 경로",
                    subtitle = "지금 가장 핫한 코스들을 만나보세요",
                    modifier = Modifier.padding(horizontal = GajaSpacing.ScreenPadding),
                    action = {
                        TextButton(onClick = { }) {
                            Text("모두보기", color = GajaColors.Primary, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = GajaSpacing.ScreenPadding),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (val state = featuredState) {
                        is SectionState.Loading -> LoadingStateView("추천 분석 중")
                        is SectionState.Success -> {
                            state.data.forEach { course ->
                                FeaturedCourseGridItem(course = course, onClick = { onOpenCourse(course) })
                            }
                        }
                        is SectionState.Error -> Text("데이터를 불러올 수 없습니다", modifier = Modifier.padding(16.dp))
                    }
                }
            }

            // 3. 중앙 액션 버튼 (한글화 및 강조)
            Box(modifier = Modifier.padding(horizontal = GajaSpacing.ScreenPadding)) {
                HeroCard(
                    title = "지금 바로 자유 주행",
                    description = "코스 없이도 당신의 모든 움직임을 기록합니다.",
                    buttonText = "주행 시작하기",
                    onClick = onStartFreeRide,
                    icon = "실시간"
                )
            }

            // 4. 리스트형 주변 코스
            Column(modifier = Modifier.padding(horizontal = GajaSpacing.ScreenPadding)) {
                SectionHeader(title = "내 주변 코스", subtitle = "현재 위치에서 가깝게 시작할 수 있어요")
                
                when (val state = listState) {
                    is SectionState.Loading -> LoadingStateView("주변 검색 중")
                    is SectionState.Success -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            state.data.items.forEach { course ->
                                CourseCard(course = course, onClick = { onOpenCourse(course) })
                            }
                        }
                    }
                    is SectionState.Error -> Text("목록 로드 실패")
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun ActivitySummaryDashboard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = GajaColors.Surface,
        border = BorderStroke(1.dp, GajaColors.Border)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text("이번 주 나의 활동", style = MaterialTheme.typography.labelSmall, color = GajaColors.TextSecondary)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("124.8", style = MaterialTheme.typography.displayMedium, color = GajaColors.TextPrimary, fontWeight = FontWeight.Bold)
                Text("km", style = MaterialTheme.typography.titleLarge, color = GajaColors.TextTertiary, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = GajaColors.Divider)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatSubItem(Icons.AutoMirrored.Filled.DirectionsBike, "4회 주행")
                StatSubItem(Icons.Default.History, "6.2시간")
                StatSubItem(Icons.Default.Map, "12개 저장")
            }
        }
    }
}

@Composable
fun StatSubItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, tint = GajaColors.Primary, modifier = Modifier.size(16.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = GajaColors.TextSecondary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun FeaturedCourseGridItem(course: CourseCardUiModel, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(220.dp)
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = GajaColors.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, GajaColors.Border)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                Surface(color = GajaColors.PrimaryContainer, shape = RoundedCornerShape(4.dp)) {
                    Text("추천", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = GajaColors.Primary, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Text(course.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${course.distanceKm}km", style = MaterialTheme.typography.labelSmall, color = GajaColors.TextSecondary, fontWeight = FontWeight.Medium)
                Text("${course.estimatedDurationMin}분", style = MaterialTheme.typography.labelSmall, color = GajaColors.TextSecondary, fontWeight = FontWeight.Medium)
            }
        }
    }
}

private sealed interface SectionState<out T> {
    data object Loading : SectionState<Nothing>
    data class Success<T>(val data: T) : SectionState<T>
    data class Error(val message: String) : SectionState<Nothing>
}
