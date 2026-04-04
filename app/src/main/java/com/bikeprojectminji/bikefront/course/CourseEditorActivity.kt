package com.bikeprojectminji.bikefront.course

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
import com.bikeprojectminji.bikefront.auth.AuthSessionStore
import com.bikeprojectminji.bikefront.ui.screen.SectionTitle
import com.bikeprojectminji.bikefront.ui.theme.BikeFrontTheme

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

    private var title by mutableStateOf("")
    private var description by mutableStateOf("")
    private var visibility by mutableStateOf("PRIVATE")
    private var helperMessage by mutableStateOf("")
    private var inFlight by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authSessionStore = AuthSessionStore(this)
        courseWriteGateway = HttpCourseWriteGateway()
        recordedCourseStore = RecordedCourseStore(this)
        helperMessage = getString(R.string.course_editor_helper_default)

        setContent {
            BikeFrontTheme {
                CourseEditorScreen(
                    sourceSummary = intent.getStringExtra(EXTRA_SOURCE_SUMMARY).orEmpty(),
                    title = title,
                    description = description,
                    visibility = visibility,
                    helperMessage = helperMessage,
                    inFlight = inFlight,
                    onTitleChange = { title = it },
                    onDescriptionChange = { description = it },
                    onVisibilityChange = { visibility = it },
                    onSave = { saveCourse() },
                    onShare = {
                        helperMessage = getString(R.string.course_editor_share_ready_message)
                        Toast.makeText(this, R.string.course_editor_share_ready_toast, Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }
    }

    private fun saveCourse() {
        val safeTitle = title.trim()
        val safeDescription = description.trim()
        if (safeTitle.isBlank()) {
            helperMessage = getString(R.string.course_editor_title_required_message)
            return
        }

        val rideRecordId = intent.getLongExtra(EXTRA_RIDE_RECORD_ID, -1L)
        if (rideRecordId <= 0L) {
            helperMessage = getString(R.string.course_editor_missing_source_message)
            return
        }

        val accessToken = authSessionStore.accessToken
        if (accessToken.isBlank()) {
            helperMessage = getString(R.string.course_editor_login_required_message)
            return
        }

        inFlight = true
        helperMessage = getString(R.string.course_editor_saving_message)
        courseWriteGateway.createCourse(
            accessToken,
            CourseWriteGateway.CreateCourseDraft(
                rideRecordId,
                safeTitle,
                safeDescription,
                visibility,
            ),
            object : CourseWriteGateway.Callback {
                override fun onSuccess(result: CourseWriteGateway.CourseCreateResult) {
                    inFlight = false
                    recordedCourseStore.save(
                        RecordedCourseItem(
                            id = result.courseId,
                            title = safeTitle,
                            distanceKm = intent.getDoubleExtra(EXTRA_DISTANCE_KM, 0.0),
                            estimatedDurationMin = intent.getIntExtra(EXTRA_DURATION_MIN, 0),
                        ),
                    )
                    helperMessage = getString(R.string.course_editor_save_success_message, result.courseId, result.visibility)
                    Toast.makeText(this@CourseEditorActivity, R.string.course_editor_save_success_toast, Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(message: String) {
                    inFlight = false
                    helperMessage = message
                }
            },
        )
    }
}

@Suppress("LongParameterList")
@androidx.compose.runtime.Composable
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionTitle(
            title = "코스 초안 정리",
            subtitle = sourceSummary.ifBlank { "주행 기록을 바탕으로 코스를 정리합니다." },
        )

        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("코스 제목") },
            placeholder = { Text("예: 퇴근길 한강 코스") },
            singleLine = true,
        )

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("설명") },
            placeholder = { Text("오늘 주행 느낌이나 경로 특징을 간단히 남겨 주세요.") },
            minLines = 4,
        )

        Text(text = "공개 범위", style = MaterialTheme.typography.titleMedium)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "PRIVATE" to "비공개",
                "UNLISTED" to "링크 공유",
                "PUBLIC" to "공개",
            ).forEach { (value, label) ->
                FilterChip(
                    selected = visibility == value,
                    onClick = { onVisibilityChange(value) },
                    label = { Text(label) },
                )
            }
        }

        Text(text = helperMessage, style = MaterialTheme.typography.bodyMedium)

        Button(onClick = onSave, enabled = !inFlight, modifier = Modifier.fillMaxWidth()) {
            Text(if (inFlight) "저장 중" else "코스 저장 준비")
        }
        OutlinedButton(onClick = onShare, enabled = !inFlight, modifier = Modifier.fillMaxWidth()) {
            Text("공유 방식 보기")
        }
    }
}
