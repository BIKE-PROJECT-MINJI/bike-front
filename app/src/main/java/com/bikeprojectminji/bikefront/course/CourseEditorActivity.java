package com.bikeprojectminji.bikefront.course;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bikeprojectminji.bikefront.R;
import com.bikeprojectminji.bikefront.auth.AuthSessionStore;

public class CourseEditorActivity extends AppCompatActivity {

    public static final String EXTRA_SOURCE_SUMMARY = "extra_source_summary";
    public static final String EXTRA_RIDE_RECORD_ID = "extra_ride_record_id";

    private AuthSessionStore authSessionStore;
    private CourseWriteGateway courseWriteGateway;
    private CourseCreateCoordinator courseCreateCoordinator;
    private TextView helperTextView;
    private Button saveButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_editor);

        authSessionStore = new AuthSessionStore(this);
        courseWriteGateway = new HttpCourseWriteGateway();
        CourseCreateCoordinator retainedCoordinator = (CourseCreateCoordinator) getLastCustomNonConfigurationInstance();
        courseCreateCoordinator = retainedCoordinator != null ? retainedCoordinator : new CourseCreateCoordinator(courseWriteGateway);
        courseCreateCoordinator.attach(this);

        TextView sourceSummaryTextView = findViewById(R.id.courseEditorSourceSummaryTextView);
        EditText titleEditText = findViewById(R.id.courseEditorTitleEditText);
        EditText descriptionEditText = findViewById(R.id.courseEditorDescriptionEditText);
        Spinner visibilitySpinner = findViewById(R.id.courseEditorVisibilitySpinner);
        helperTextView = findViewById(R.id.courseEditorHelperTextView);
        saveButton = findViewById(R.id.courseEditorSaveButton);
        Button shareButton = findViewById(R.id.courseEditorShareButton);

        sourceSummaryTextView.setText(getIntent().getStringExtra(EXTRA_SOURCE_SUMMARY));
        visibilitySpinner.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{getString(R.string.course_visibility_private), getString(R.string.course_visibility_unlisted), getString(R.string.course_visibility_public)}
        ));

        saveButton.setOnClickListener(v -> {
            if (titleEditText.getText().toString().trim().isBlank()) {
                helperTextView.setText(R.string.course_editor_title_required_message);
                return;
            }

            long rideRecordId = getIntent().getLongExtra(EXTRA_RIDE_RECORD_ID, -1L);
            if (rideRecordId <= 0L) {
                helperTextView.setText(R.string.course_editor_missing_source_message);
                return;
            }

            String accessToken = authSessionStore.getAccessToken();
            if (accessToken.isBlank()) {
                helperTextView.setText(R.string.course_editor_login_required_message);
                return;
            }

            courseCreateCoordinator.startCreate(
                    accessToken,
                    new CourseWriteGateway.CreateCourseDraft(
                            rideRecordId,
                            titleEditText.getText().toString().trim(),
                            descriptionEditText.getText().toString().trim(),
                            resolveVisibilityValue(visibilitySpinner.getSelectedItemPosition())
                    )
            );
        });

        shareButton.setOnClickListener(v -> {
            helperTextView.setText(R.string.course_editor_share_ready_message);
            Toast.makeText(this, R.string.course_editor_share_ready_toast, Toast.LENGTH_SHORT).show();
        });
    }

    private String resolveVisibilityValue(int position) {
        if (position == 1) {
            return "UNLISTED";
        }
        if (position == 2) {
            return "PUBLIC";
        }
        return "PRIVATE";
    }

    void renderCourseCreateInFlight() {
        saveButton.setEnabled(false);
        helperTextView.setText(R.string.course_editor_saving_message);
    }

    void renderCourseCreateSuccess(CourseWriteGateway.CourseCreateResult result) {
        saveButton.setEnabled(true);
        helperTextView.setText(getString(R.string.course_editor_save_success_message, result.getCourseId(), result.getVisibility()));
        Toast.makeText(this, R.string.course_editor_save_success_toast, Toast.LENGTH_SHORT).show();
    }

    void renderCourseCreateFailure(String message) {
        saveButton.setEnabled(true);
        helperTextView.setText(message);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        courseCreateCoordinator.detach();
        return courseCreateCoordinator;
    }

    @Override
    protected void onDestroy() {
        courseCreateCoordinator.detach();
        super.onDestroy();
    }

    private static final class CourseCreateCoordinator {
        private final CourseWriteGateway courseWriteGateway;
        private CourseEditorActivity activity;
        private boolean inFlight;
        private CourseWriteGateway.CourseCreateResult pendingSuccess;
        private String pendingFailure;

        private CourseCreateCoordinator(CourseWriteGateway courseWriteGateway) {
            this.courseWriteGateway = courseWriteGateway;
        }

        void attach(CourseEditorActivity activity) {
            this.activity = activity;
            if (inFlight) {
                activity.renderCourseCreateInFlight();
            } else if (pendingSuccess != null) {
                CourseWriteGateway.CourseCreateResult result = pendingSuccess;
                pendingSuccess = null;
                activity.renderCourseCreateSuccess(result);
            } else if (pendingFailure != null) {
                String message = pendingFailure;
                pendingFailure = null;
                activity.renderCourseCreateFailure(message);
            }
        }

        void detach() {
            this.activity = null;
        }

        void startCreate(String accessToken, CourseWriteGateway.CreateCourseDraft draft) {
            if (inFlight) {
                return;
            }
            inFlight = true;
            if (activity != null) {
                activity.renderCourseCreateInFlight();
            }
            courseWriteGateway.createCourse(accessToken, draft, new CourseWriteGateway.Callback() {
                @Override
                public void onSuccess(CourseWriteGateway.CourseCreateResult result) {
                    inFlight = false;
                    if (activity != null) {
                        activity.renderCourseCreateSuccess(result);
                        return;
                    }
                    pendingSuccess = result;
                }

                @Override
                public void onFailure(String message) {
                    inFlight = false;
                    if (activity != null) {
                        activity.renderCourseCreateFailure(message);
                        return;
                    }
                    pendingFailure = message;
                }
            });
        }
    }
}
