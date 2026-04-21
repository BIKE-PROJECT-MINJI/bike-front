package com.bikeprojectminji.bikefront.course

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bikeprojectminji.bikefront.R
import com.bikeprojectminji.bikefront.auth.AuthSessionStore
import com.bikeprojectminji.bikefront.ui.screen.BikeSurfaceCard
import com.bikeprojectminji.bikefront.ui.screen.GajaBrandTopBar
import com.bikeprojectminji.bikefront.ui.screen.HeroCard
import com.bikeprojectminji.bikefront.ui.screen.GajaPrimaryButton
import com.bikeprojectminji.bikefront.ui.screen.SecondaryActionButton
import com.bikeprojectminji.bikefront.ui.screen.SectionHeader
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaTheme
import com.bikeprojectminji.bikefront.ui.theme.GajaSpacing

class CourseEditorActivity : ComponentActivity() {

    companion object {
        const val EXTRA_SOURCE_SUMMARY = "extra_source_summary"
        const val EXTRA_RIDE_RECORD_ID = "extra_ride_record_id"
        const val EXTRA_DISTANCE_KM = "extra_distance_km"
        const val EXTRA_DURATION_MIN = "extra_duration_min"
    }

    private lateinit var authSessionStore: AuthSessionStore
    private lateinit var courseWriteGateway: CourseWriteGateway
    private lateinit var recordedCourseStore: RecordedCourseStore

    private var titleState by mutableStateOf("")
    private var descriptionState by mutableStateOf("")
    private var visibilityState by mutableStateOf("PRIVATE")
    private var helperMessageState by mutableStateOf("")
    private var inFlightState by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authSessionStore = AuthSessionStore(this)
        courseWriteGateway = HttpCourseWriteGateway()
        recordedCourseStore = RecordedCourseStore(this)
        helperMessageState = getString(R.string.course_editor_helper_default)

        setContent {
            GajaTheme {
                CourseEditorScreen(
                    sourceSummary = intent.getStringExtra(EXTRA_SOURCE_SUMMARY).orEmpty(),
                    title = titleState,
                    description = descriptionState,
                    visibility = visibilityState,
                    helperMessage = helperMessageState,
                    inFlight = inFlightState,
                    onTitleChange = { titleState = it },
                    onDescriptionChange = { descriptionState = it },
                    onVisibilityChange = { visibilityState = it },
                    onSave = { saveCourse() },
                    onShare = {
                        helperMessageState = getString(R.string.course_editor_share_ready_message)
                        Toast.makeText(this, R.string.course_editor_share_ready_toast, Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }
    }

    private fun saveCourse() {
        val safeTitle = titleState.trim()
        val safeDescription = descriptionState.trim()
        if (safeTitle.isBlank()) {
            helperMessageState = getString(R.string.course_editor_title_required_message)
            return
        }

        val rideRecordId = intent.getLongExtra(EXTRA_RIDE_RECORD_ID, -1L)
        if (rideRecordId <= 0L) {
            helperMessageState = getString(R.string.course_editor_missing_source_message)
            return
        }

        val accessToken = authSessionStore.accessToken
        if (accessToken.isBlank()) {
            helperMessageState = getString(R.string.course_editor_login_required_message)
            return
        }

        inFlightState = true
        helperMessageState = getString(R.string.course_editor_saving_message)
        courseWriteGateway.createCourse(
            accessToken,
            CourseWriteGateway.CreateCourseDraft(
                rideRecordId,
                safeTitle,
                safeDescription,
                visibilityState,
            ),
            object : CourseWriteGateway.Callback {
                override fun onSuccess(result: CourseWriteGateway.CourseCreateResult) {
                    inFlightState = false
                    recordedCourseStore.save(
                        RecordedCourseItem(
                            id = result.courseId,
                            title = safeTitle,
                            distanceKm = intent.getDoubleExtra(EXTRA_DISTANCE_KM, 0.0),
                            estimatedDurationMin = intent.getIntExtra(EXTRA_DURATION_MIN, 0),
                        ),
                    )
                    helperMessageState = getString(R.string.course_editor_save_success_message, result.courseId, result.visibility)
                    Toast.makeText(this@CourseEditorActivity, R.string.course_editor_save_success_toast, Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(message: String) {
                    inFlightState = false
                    helperMessageState = message
                }
            },
        )
    }

    @Composable
    private fun CourseEditorScreen(
        sourceSummary: String,
        title: String,
        description: String,
        visibility: String,
        helperMessage: String,
        inFlight: Boolean,
        onTitleChange: (String) -> Unit,
        onDescriptionChange: (String) -> Unit,
        onVisibilityChange: (String) -> Unit,
        onSave: () -> Unit,
        onShare: () -> Unit,
    ) {
        Scaffold(
            topBar = { GajaBrandTopBar(title = "코스 편집") },
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

                HeroCard(
                    title = "코스 초안 다듬기",
                    description = sourceSummary.ifBlank { "저장할 코스 정보를 정리하고 공개 범위를 설정합니다." },
                    buttonText = "공개 범위 확인",
                    onClick = onShare,
                    icon = "course"
                )

                SectionHeader(
                    title = "코스 정보 입력",
                    subtitle = "코스 이름과 공개 범위를 정리한 뒤 저장합니다."
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("코스 제목") },
                    placeholder = { Text("예: 한강 북단 평지 코스") },
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GajaColors.Primary,
                        unfocusedBorderColor = GajaColors.Border,
                        focusedTextColor = GajaColors.TextPrimary,
                        unfocusedTextColor = GajaColors.TextPrimary
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("설명") },
                    placeholder = { Text("경로의 특징이나 기억할 점을 기록하세요.") },
                    minLines = 3,
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GajaColors.Primary,
                        unfocusedBorderColor = GajaColors.Border,
                        focusedTextColor = GajaColors.TextPrimary,
                        unfocusedTextColor = GajaColors.TextPrimary
                    )
                )

                SectionHeader(title = "공개 범위")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("PRIVATE" to "비공개", "UNLISTED" to "링크", "PUBLIC" to "공개").forEach { (v, l) ->
                        FilterChip(
                            selected = visibility == v,
                            onClick = { onVisibilityChange(v) },
                            label = { Text(l) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GajaColors.Primary,
                                selectedLabelColor = GajaColors.White,
                                labelColor = GajaColors.TextSecondary
                            )
                        )
                    }
                }

                BikeSurfaceCard { Text(text = helperMessage, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall, color = GajaColors.TextSecondary) }

                Spacer(Modifier.weight(1f))

                Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing)) {
                    GajaPrimaryButton(
                        text = if (inFlight) "저장 중..." else "코스 저장하기",
                        enabled = !inFlight,
                        onClick = onSave
                    )
                    SecondaryActionButton(
                        text = "공유 옵션 보기",
                        enabled = !inFlight,
                        onClick = onShare
                    )
                }
                Spacer(Modifier.height(GajaSpacing.Large))
            }
        }
    }
}
