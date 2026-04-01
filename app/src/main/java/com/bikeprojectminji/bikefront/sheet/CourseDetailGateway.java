package com.bikeprojectminji.bikefront.sheet;

public interface CourseDetailGateway {

    final class CourseDetail {
        private final long id;
        private final String title;
        private final double distanceKm;
        private final int estimatedDurationMin;

        public CourseDetail(long id, String title, double distanceKm, int estimatedDurationMin) {
            this.id = id;
            this.title = title;
            this.distanceKm = distanceKm;
            this.estimatedDurationMin = estimatedDurationMin;
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
    }

    interface Callback {
        void onSuccess(CourseDetail courseDetail);

        void onFailure(String message);
    }

    void loadCourseDetail(long courseId, Callback callback);
}
