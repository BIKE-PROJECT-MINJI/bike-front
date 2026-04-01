package com.bikeprojectminji.bikefront.home;

import java.io.Serializable;

public class FeaturedCourseUiModel implements Serializable {

    private final long id;
    private final String title;
    private final double distanceKm;
    private final int estimatedDurationMin;
    private final Integer distanceFromUserM;
    private final int featuredRank;

    public FeaturedCourseUiModel(
            long id,
            String title,
            double distanceKm,
            int estimatedDurationMin,
            Integer distanceFromUserM,
            int featuredRank
    ) {
        this.id = id;
        this.title = title;
        this.distanceKm = distanceKm;
        this.estimatedDurationMin = estimatedDurationMin;
        this.distanceFromUserM = distanceFromUserM;
        this.featuredRank = featuredRank;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public int getEstimatedDurationMin() {
        return estimatedDurationMin;
    }

    public Integer getDistanceFromUserM() {
        return distanceFromUserM;
    }

    public int getFeaturedRank() {
        return featuredRank;
    }
}
