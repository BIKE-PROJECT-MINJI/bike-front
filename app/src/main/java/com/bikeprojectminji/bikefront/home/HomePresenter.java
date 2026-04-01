package com.bikeprojectminji.bikefront.home;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;

public class HomePresenter implements HomeContract.Presenter {

    private final FeaturedCourseRepository repository;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    private HomeContract.View view;

    public HomePresenter(FeaturedCourseRepository repository, ExecutorService executorService) {
        this.repository = repository;
        this.executorService = executorService;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void attachView(HomeContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void loadFeaturedCourses() {
        HomeContract.View currentView = view;
        if (currentView == null) {
            return;
        }

        currentView.showFeaturedLoading();

        executorService.execute(() -> {
            try {
                FeaturedCourseResponse response = repository.loadFeaturedCourses();
                mainHandler.post(() -> {
                    if (view == null) {
                        return;
                    }

                    if (response.getCourses().isEmpty()) {
                        view.showFeaturedEmpty();
                        return;
                    }

                    view.showFeaturedCourses(response);
                });
            } catch (Exception exception) {
                mainHandler.post(() -> {
                    if (view == null) {
                        return;
                    }

                    view.showFeaturedError("추천 코스를 불러오지 못했습니다.");
                });
            }
        });
    }
}
