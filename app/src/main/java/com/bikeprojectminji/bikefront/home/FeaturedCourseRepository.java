package com.bikeprojectminji.bikefront.home;

public class FeaturedCourseRepository {

    private final FeaturedCourseApiClient apiClient;

    public FeaturedCourseRepository(FeaturedCourseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public FeaturedCourseResponse loadFeaturedCourses() throws Exception {
        return apiClient.fetchFeaturedCourses();
    }
}
