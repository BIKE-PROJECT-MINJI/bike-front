package com.bikeprojectminji.bikefront.home;

import java.util.List;

public class FeaturedCourseResponse {

    private final List<FeaturedCourseUiModel> courses;

    public FeaturedCourseResponse(List<FeaturedCourseUiModel> courses) {
        this.courses = courses;
    }

    public List<FeaturedCourseUiModel> getCourses() {
        return courses;
    }
}
