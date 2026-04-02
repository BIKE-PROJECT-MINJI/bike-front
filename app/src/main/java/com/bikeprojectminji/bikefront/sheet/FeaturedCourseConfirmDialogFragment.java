package com.bikeprojectminji.bikefront.sheet;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bikeprojectminji.bikefront.R;
import com.bikeprojectminji.bikefront.RideEntryActivity;
import com.bikeprojectminji.bikefront.home.FeaturedCourseUiModel;

import java.util.Locale;

public class FeaturedCourseConfirmDialogFragment extends DialogFragment {

    private static final String ARG_COURSE = "arg_course";

    private FeaturedCourseUiModel course;
    private CourseDetailGateway.CourseDetail courseDetail;
    private final CourseDetailGateway courseDetailGateway = new HttpCourseDetailGateway();

    public static FeaturedCourseConfirmDialogFragment newInstance(FeaturedCourseUiModel course) {
        FeaturedCourseConfirmDialogFragment fragment = new FeaturedCourseConfirmDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_COURSE, course);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext(), R.style.Theme_BikeFront_BottomDialog);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_featured_course_confirm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null) {
            dismissAllowingStateLoss();
            return;
        }

        course = (FeaturedCourseUiModel) args.getSerializable(ARG_COURSE);
        if (course == null) {
            dismissAllowingStateLoss();
            return;
        }

        TextView titleTextView = view.findViewById(R.id.confirmSheetTitleTextView);
        TextView distanceTextView = view.findViewById(R.id.confirmSheetDistanceTextView);
        TextView durationTextView = view.findViewById(R.id.confirmSheetDurationTextView);
        TextView primaryHintTextView = view.findViewById(R.id.confirmSheetPrimaryHintTextView);
        TextView phase2HintTextView = view.findViewById(R.id.confirmSheetPhase2HintTextView);
        TextView secondaryHintTextView = view.findViewById(R.id.confirmSheetSecondaryHintTextView);
        TextView loginHintTextView = view.findViewById(R.id.confirmSheetLoginHintTextView);
        TextView errorTextView = view.findViewById(R.id.confirmSheetErrorTextView);
        ProgressBar progressBar = view.findViewById(R.id.confirmSheetProgressBar);
        Button startButton = view.findViewById(R.id.confirmSheetStartButton);
        Button closeButton = view.findViewById(R.id.confirmSheetCloseButton);
        ImageButton dismissButton = view.findViewById(R.id.confirmSheetDismissButton);

        titleTextView.setText(course.getTitle());
        distanceTextView.setText(formatDistance(course));
        durationTextView.setText(getString(R.string.featured_course_duration_format, course.getEstimatedDurationMin()));
        primaryHintTextView.setText(R.string.confirm_sheet_primary_label);
        phase2HintTextView.setText(R.string.confirm_sheet_phase2_hint);
        secondaryHintTextView.setText(R.string.confirm_sheet_secondary_label);
        loginHintTextView.setText(R.string.confirm_sheet_login_hint);

        startButton.setEnabled(false);
        errorTextView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        closeButton.setOnClickListener(v -> dismiss());
        dismissButton.setOnClickListener(v -> dismiss());
        startButton.setOnClickListener(v -> {
            if (courseDetail == null) {
                return;
            }

            startButton.setEnabled(false);
            Intent intent = RideEntryActivity.newIntent(
                    requireContext(),
                    courseDetail.getId(),
                    courseDetail.getTitle(),
                    formatDistance(courseDetail.getDistanceKm()),
                    getString(R.string.featured_course_duration_format, courseDetail.getEstimatedDurationMin())
            );
            startActivity(intent);
            dismiss();
        });

        courseDetailGateway.loadCourseDetail(course.getId(), new CourseDetailGateway.Callback() {
            @Override
            public void onSuccess(CourseDetailGateway.CourseDetail courseDetailResponse) {
                if (!isAdded()) {
                    return;
                }

                courseDetail = courseDetailResponse;
                titleTextView.setText(courseDetailResponse.getTitle());
                distanceTextView.setText(formatDistance(courseDetailResponse.getDistanceKm()));
                durationTextView.setText(getString(R.string.featured_course_duration_format, courseDetailResponse.getEstimatedDurationMin()));
                progressBar.setVisibility(View.GONE);
                errorTextView.setVisibility(View.GONE);
                startButton.setEnabled(true);
            }

            @Override
            public void onFailure(String message) {
                if (!isAdded()) {
                    return;
                }

                courseDetail = null;
                progressBar.setVisibility(View.GONE);
                errorTextView.setVisibility(View.VISIBLE);
                errorTextView.setText(resolveErrorMessage(message));
                startButton.setEnabled(false);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog == null) {
            return;
        }

        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.BikeFrontBottomDialogAnimation);

        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.gravity = Gravity.BOTTOM;
        window.setAttributes(layoutParams);
    }

    private String formatDistance(FeaturedCourseUiModel item) {
        return formatDistance(item.getDistanceKm());
    }

    private String formatDistance(double distanceKm) {
        if (distanceKm < 1) {
            int distanceInMeters = (int) Math.round(distanceKm * 1000);
            return getString(R.string.featured_course_distance_meter_format, distanceInMeters);
        }

        return String.format(Locale.KOREA, getString(R.string.featured_course_distance_km_format), distanceKm);
    }

    private String resolveErrorMessage(String message) {
        return getString(R.string.course_detail_error_message);
    }
}
