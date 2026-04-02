package com.bikeprojectminji.bikefront;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bikeprojectminji.bikefront.home.FeaturedCourseAdapter;
import com.bikeprojectminji.bikefront.home.FeaturedCourseApiClient;
import com.bikeprojectminji.bikefront.home.FeaturedCourseRepository;
import com.bikeprojectminji.bikefront.home.FeaturedCourseResponse;
import com.bikeprojectminji.bikefront.home.FeaturedCourseUiModel;
import com.bikeprojectminji.bikefront.home.HomeContract;
import com.bikeprojectminji.bikefront.home.HomePresenter;
import com.bikeprojectminji.bikefront.sheet.FeaturedCourseConfirmDialogFragment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements HomeContract.View {

    private static final String KEY_RESPONSE = "key_response";
    private static final String KEY_STATE = "key_state";

    private FeaturedCourseAdapter featuredCourseAdapter;
    private HomePresenter homePresenter;
    private ExecutorService executorService;

    private ProgressBar featuredProgressBar;
    private RecyclerView featuredRecyclerView;
    private TextView featuredStatusTextView;
    private TextView featuredSortingModeTextView;
    private Button featuredRetryButton;
    private Button homeIntroActionButton;

    private String currentState = "loading";
    private FeaturedCourseResponse currentResponse;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        featuredProgressBar = findViewById(R.id.featuredProgressBar);
        featuredRecyclerView = findViewById(R.id.featuredRecyclerView);
        featuredStatusTextView = findViewById(R.id.featuredStatusTextView);
        featuredSortingModeTextView = findViewById(R.id.featuredSortingModeTextView);
        featuredRetryButton = findViewById(R.id.featuredRetryButton);
        homeIntroActionButton = findViewById(R.id.homeIntroActionButton);

        featuredCourseAdapter = new FeaturedCourseAdapter(this::openConfirmSheet);
        featuredRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        featuredRecyclerView.setAdapter(featuredCourseAdapter);

        executorService = Executors.newSingleThreadExecutor();
        homePresenter = new HomePresenter(
                new FeaturedCourseRepository(new FeaturedCourseApiClient()),
                executorService
        );
        homePresenter.attachView(this);

        featuredRetryButton.setOnClickListener(v -> homePresenter.loadFeaturedCourses());
        homeIntroActionButton.setOnClickListener(v -> {
            if (currentResponse != null && !currentResponse.getCourses().isEmpty()) {
                openConfirmSheet(currentResponse.getCourses().get(0));
            }
        });

        if (savedInstanceState != null) {
            currentState = savedInstanceState.getString(KEY_STATE, "loading");
            currentResponse = (FeaturedCourseResponse) savedInstanceState.getSerializable(KEY_RESPONSE);
            renderSavedState();
        } else {
            homePresenter.loadFeaturedCourses();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        homePresenter.detachView();
        executorService.shutdownNow();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_STATE, currentState);
        outState.putSerializable(KEY_RESPONSE, currentResponse);
    }

    @Override
    public void showFeaturedLoading() {
        currentState = "loading";
        currentResponse = null;
        featuredProgressBar.setVisibility(View.VISIBLE);
        featuredRecyclerView.setVisibility(View.GONE);
        featuredStatusTextView.setVisibility(View.GONE);
        featuredSortingModeTextView.setVisibility(View.GONE);
        featuredRetryButton.setVisibility(View.GONE);
        homeIntroActionButton.setEnabled(false);
    }

    @Override
    public void showFeaturedCourses(FeaturedCourseResponse response) {
        currentState = "success";
        currentResponse = response;
        featuredProgressBar.setVisibility(View.GONE);
        featuredRecyclerView.setVisibility(View.VISIBLE);
        featuredStatusTextView.setVisibility(View.GONE);
        featuredRetryButton.setVisibility(View.GONE);
        featuredSortingModeTextView.setVisibility(View.VISIBLE);
        featuredSortingModeTextView.setText(resolveSortingModeLabel(response.getSortingMode()));
        featuredCourseAdapter.submitList(response.getCourses());
        homeIntroActionButton.setEnabled(!response.getCourses().isEmpty());
    }

    @Override
    public void showFeaturedEmpty() {
        currentState = "empty";
        currentResponse = null;
        featuredProgressBar.setVisibility(View.GONE);
        featuredRecyclerView.setVisibility(View.GONE);
        featuredStatusTextView.setVisibility(View.VISIBLE);
        featuredStatusTextView.setText(R.string.featured_empty_message);
        featuredSortingModeTextView.setVisibility(View.GONE);
        featuredRetryButton.setVisibility(View.GONE);
        homeIntroActionButton.setEnabled(false);
    }

    @Override
    public void showFeaturedError(String message) {
        currentState = "error";
        currentResponse = null;
        featuredProgressBar.setVisibility(View.GONE);
        featuredRecyclerView.setVisibility(View.GONE);
        featuredStatusTextView.setVisibility(View.VISIBLE);
        featuredStatusTextView.setText(message);
        featuredSortingModeTextView.setVisibility(View.GONE);
        featuredRetryButton.setVisibility(View.VISIBLE);
        homeIntroActionButton.setEnabled(false);
    }

    private void openConfirmSheet(FeaturedCourseUiModel course) {
        FeaturedCourseConfirmDialogFragment.newInstance(course)
                .show(getSupportFragmentManager(), "featured_course_confirm_sheet");
    }

    private void renderSavedState() {
        switch (currentState) {
            case "success":
                if (currentResponse != null) {
                    showFeaturedCourses(currentResponse);
                } else {
                    homePresenter.loadFeaturedCourses();
                }
                break;
            case "empty":
                showFeaturedEmpty();
                break;
            case "error":
                showFeaturedError(getString(R.string.featured_error_message));
                break;
            case "loading":
            default:
                homePresenter.loadFeaturedCourses();
                break;
        }
    }

    private String resolveSortingModeLabel(String sortingMode) {
        if ("distance".equalsIgnoreCase(sortingMode)) {
            return getString(R.string.featured_sorting_mode_distance);
        }

        return getString(R.string.featured_sorting_mode_fallback);
    }
}
