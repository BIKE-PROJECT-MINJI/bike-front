package com.bikeprojectminji.bikefront.home;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FeaturedCourseResponse implements Serializable {

    private final String sortingMode;
    private final List<FeaturedCourseUiModel> courses;

    public FeaturedCourseResponse(String sortingMode, List<FeaturedCourseUiModel> courses) {
        this.sortingMode = sortingMode;
        this.courses = new ArrayList<>(courses);
    }

    public String getSortingMode() {
        return sortingMode;
    }

    public List<FeaturedCourseUiModel> getCourses() {
        return new ArrayList<>(courses);
    }
}
