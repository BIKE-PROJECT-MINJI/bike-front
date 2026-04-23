package com.bikeprojectminji.bikefront.course

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bikeprojectminji.bikefront.R
import com.bikeprojectminji.bikefront.analytics.AnalyticsTracker
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
    private lateinit var courseShareGateway: CourseShareGateway
    private lateinit var recordedCourseStore: RecordedCourseStore
    private lateinit var analyticsTracker: AnalyticsTracker

    private var titleState by mutableStateOf("")
    private var descriptionState by mutableStateOf("")
    private var visibilityState by mutableStateOf("PRIVATE")
    private var helperMessageState by mutableStateOf("")
    private var inFlightState by mutableStateOf(false)
    private var savedCourseIdState by mutableStateOf<Long?>(null)
    private var savedVisibilityState by mutableStateOf<String?>(null)
    private var savedTitleState by mutableStateOf<String?>(null)
    private var savedDescriptionState by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authSessionStore = AuthSessionStore(this)
        courseWriteGateway = HttpCourseWriteGateway()
        courseShareGateway = HttpCourseShareGateway()
        recordedCourseStore = RecordedCourseStore(this)
        analyticsTracker = AnalyticsTracker(this)
        helperMessageState = getString(R.string.course_editor_helper_default)

        setContent {
            GajaTheme {
                CourseEditorScreen(
                    sourceSummary = intent.getStringExtra(EXTRA_SOURCE_SUMMARY).orEmpty(),
                    title = titleState,
                    description = descriptionState,
                    visibility = visibilityState,
                    savedCourseId = savedCourseIdState,
                    helperMessage = helperMessageState,
                    inFlight = inFlightState,
                    onTitleChange = { titleState = it },
                    onDescriptionChange = { descriptionState = it },
                    onVisibilityChange = { visibilityState = it },
                    onSave = { saveCourse() },
                    onShare = { shareCourse() },
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
                    analyticsTracker.track("course_create_completed", "course_editor", mapOf("courseId" to result.courseId, "rideRecordId" to rideRecordId, "visibility" to result.visibility))
                    savedCourseIdState = result.courseId
                    savedVisibilityState = result.visibility
                    savedTitleState = safeTitle
                    savedDescriptionState = safeDescription
                    recordedCourseStore.save(
                        RecordedCourseItem(
                            id = result.courseId,
                            title = safeTitle,
                            distanceKm = intent.getDoubleExtra(EXTRA_DISTANCE_KM, 0.0),
                            estimatedDurationMin = intent.getIntExtra(EXTRA_DURATION_MIN, 0),
                        ),
                    )
                    helperMessageState = getString(R.string.course_editor_save_success_message, result.courseId, visibilityLabel(result.visibility))
                    Toast.makeText(this@CourseEditorActivity, R.string.course_editor_save_success_toast, Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(message: String) {
                    inFlightState = false
                    helperMessageState = message
                }
            },
        )
    }

    private fun shareCourse() {
        val accessToken = authSessionStore.accessToken
        if (accessToken.isBlank()) {
            helperMessageState = getString(R.string.course_editor_login_required_message)
            return
        }

        val savedCourseId = savedCourseIdState
        if (savedCourseId == null || savedCourseId <= 0L) {
            helperMessageState = getString(R.string.course_editor_share_requires_save_message)
            return
        }

        if (hasUnsavedCourseChanges()) {
            helperMessageState = getString(R.string.course_editor_share_unsaved_changes_message)
            return
        }

        if (savedVisibilityState == "PRIVATE") {
            helperMessageState = getString(R.string.course_editor_private_share_blocked_message)
            return
        }

        inFlightState = true
        helperMessageState = getString(R.string.course_editor_sharing_message)
        courseShareGateway.shareCourse(accessToken, savedCourseId, object : CourseShareGateway.Callback {
            override fun onSuccess(result: CourseShareGateway.ShareResult) {
                inFlightState = false
                if (result.visibility == "PRIVATE") {
                    helperMessageState = getString(R.string.course_editor_private_share_blocked_message)
                    return
                }
                if (result.shareUrl.isBlank()) {
                    helperMessageState = getString(R.string.course_editor_share_missing_url_message)
                    return
                }

                helperMessageState = getString(R.string.course_editor_share_success_message, visibilityLabel(result.visibility))
                openAndroidShareSheet(titleState.trim().ifBlank { getString(R.string.course_editor_share_default_title) }, result.shareUrl)
            }

            override fun onFailure(message: String) {
                inFlightState = false
                helperMessageState = message
            }
        })
    }

    private fun hasUnsavedCourseChanges(): Boolean {
        return savedTitleState != titleState.trim() ||
            savedDescriptionState != descriptionState.trim() ||
            savedVisibilityState != visibilityState
    }

    private fun openAndroidShareSheet(courseTitle: String, shareUrl: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, courseTitle)
            putExtra(Intent.EXTRA_TEXT, "$courseTitle\n$shareUrl")
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.course_editor_share_chooser_title)))
    }

    private fun visibilityLabel(visibility: String): String {
        return when (visibility) {
            "PRIVATE" -> getString(R.string.course_visibility_private)
            "UNLISTED" -> getString(R.string.course_visibility_unlisted)
            "PUBLIC" -> getString(R.string.course_visibility_public)
            else -> visibility
        }
    }

    private fun currentPrimaryAction(savedCourseId: Long?, hasUnsavedChanges: Boolean): () -> Unit {
        return when {
            savedCourseId == null -> ::saveCourse
            hasUnsavedChanges -> ::saveCourse
            else -> ::shareCourse
        }
    }

    private fun currentHeroButtonText(savedCourseId: Long?, hasUnsavedChanges: Boolean): String {
        return when {
            savedCourseId == null -> getString(R.string.course_editor_hero_add_button)
            hasUnsavedChanges -> getString(R.string.course_editor_hero_resave_button)
            else -> getString(R.string.course_editor_hero_share_button)
        }
    }

    private fun currentSaveStatusMessage(savedCourseId: Long?, hasUnsavedChanges: Boolean): String {
        return when {
            savedCourseId == null -> getString(R.string.course_editor_save_status_before_add)
            hasUnsavedChanges -> getString(R.string.course_editor_save_status_unsaved_changes)
            else -> getString(R.string.course_editor_save_status_added, savedCourseId)
        }
    }

    @Composable
    private fun CourseEditorScreen(
        sourceSummary: String,
        title: String,
        description: String,
        visibility: String,
        savedCourseId: Long?,
        helperMessage: String,
        inFlight: Boolean,
        onTitleChange: (String) -> Unit,
        onDescriptionChange: (String) -> Unit,
        onVisibilityChange: (String) -> Unit,
        onSave: () -> Unit,
        onShare: () -> Unit,
    ) {
        val hasUnsavedChanges = savedCourseId != null && (savedTitleState != title.trim() || savedDescriptionState != description.trim() || savedVisibilityState != visibility)
        Scaffold(
            topBar = { GajaBrandTopBar(title = "코스 편집") },
            containerColor = GajaColors.Background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = GajaSpacing.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(GajaSpacing.Large)
            ) {
                Spacer(Modifier.height(GajaSpacing.Small))

                HeroCard(
                    title = "내 코스로 정리하기",
                    description = sourceSummary.ifBlank { getString(R.string.course_editor_hero_default_description) },
                    buttonText = currentHeroButtonText(savedCourseId, hasUnsavedChanges),
                    onClick = currentPrimaryAction(savedCourseId, hasUnsavedChanges),
                    icon = "내 코스"
                )

                BikeSurfaceCard {
                    Text(
                        text = currentSaveStatusMessage(savedCourseId, hasUnsavedChanges),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = GajaColors.TextSecondary,
                    )
                }

                SectionHeader(
                    title = "코스 정보 입력",
                    subtitle = "코스 이름과 공개 범위를 정리하면 저장 후 바로 다시 찾을 수 있어요"
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

                Spacer(Modifier.height(GajaSpacing.Small))

                Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing)) {
                    GajaPrimaryButton(
                        text = if (inFlight) "저장 중..." else "내 코스에 저장",
                        enabled = !inFlight,
                        onClick = onSave
                    )
                    SecondaryActionButton(
                        text = when {
                            savedCourseId == null -> "저장 후 공유 가능"
                            hasUnsavedChanges -> "변경 내용 먼저 저장"
                            visibility == "PRIVATE" -> "링크 공유로 바꾸기"
                            else -> "링크 공유"
                        },
                        enabled = !inFlight,
                        onClick = onShare
                    )
                }
                Spacer(Modifier.height(GajaSpacing.Large))
            }
        }
    }
}
