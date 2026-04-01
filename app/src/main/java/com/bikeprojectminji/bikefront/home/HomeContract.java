package com.bikeprojectminji.bikefront.home;

public interface HomeContract {

    interface View {
        void showFeaturedLoading();

        void showFeaturedCourses(FeaturedCourseResponse response);

        void showFeaturedEmpty();

        void showFeaturedError(String message);
    }

    interface Presenter {
        void attachView(View view);

        void detachView();

        void loadFeaturedCourses();
    }
}
