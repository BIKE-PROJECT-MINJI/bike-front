package com.bikeprojectminji.bikefront.ridemap;

import java.util.ArrayList;
import java.util.List;

public interface CourseRoutePointsGateway {

    final class RoutePoint {
        private final int pointOrder;
        private final double latitude;
        private final double longitude;

        public RoutePoint(int pointOrder, double latitude, double longitude) {
            this.pointOrder = pointOrder;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public int getPointOrder() {
            return pointOrder;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }

    final class RoutePointsResult {
        private final long courseId;
        private final List<RoutePoint> points;

        public RoutePointsResult(long courseId, List<RoutePoint> points) {
            this.courseId = courseId;
            this.points = new ArrayList<>(points);
        }

        public long getCourseId() {
            return courseId;
        }

        public List<RoutePoint> getPoints() {
            return new ArrayList<>(points);
        }
    }

    interface Callback {
        void onSuccess(RoutePointsResult result);

        void onFailure(String message);
    }

    void loadRoutePoints(long courseId, Callback callback);

    void loadRoutePoints(long courseId, String accessToken, Callback callback);
}
