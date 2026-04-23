package com.bikeprojectminji.bikefront.ride;

import java.time.OffsetDateTime;
import java.util.List;

public interface RideRecordGateway {

    void saveRideRecord(String accessToken, RideRecordDraft draft, Callback callback);

    void getRideRecords(String accessToken, HistoryCallback callback);

    void getRideRecordDetail(String accessToken, long rideRecordId, DetailCallback callback);

    void getRideRecordStatus(String accessToken, long rideRecordId, StatusCallback callback);

    void regenerateRideRecord(String accessToken, long rideRecordId, StatusCallback callback);

    interface Callback {
        void onSuccess(RideRecordSaveResult result);

        void onFailure(String message);
    }

    interface StatusCallback {
        void onSuccess(RideRecordFinalizationStatusResult result);

        void onFailure(String message);
    }

    interface HistoryCallback {
        void onSuccess(RideRecordHistoryResult result);

        void onFailure(String message);
    }

    interface DetailCallback {
        void onSuccess(RideRecordDetailResult result);

        void onFailure(String message);
    }

    class RideRecordDraft {
        private final OffsetDateTime startedAt;
        private final OffsetDateTime endedAt;
        private final int distanceM;
        private final int durationSec;
        private final List<RideRecordPoint> routePoints;

        public RideRecordDraft(OffsetDateTime startedAt, OffsetDateTime endedAt, int distanceM, int durationSec, List<RideRecordPoint> routePoints) {
            this.startedAt = startedAt;
            this.endedAt = endedAt;
            this.distanceM = distanceM;
            this.durationSec = durationSec;
            this.routePoints = routePoints;
        }

        public OffsetDateTime getStartedAt() { return startedAt; }
        public OffsetDateTime getEndedAt() { return endedAt; }
        public int getDistanceM() { return distanceM; }
        public int getDurationSec() { return durationSec; }
        public List<RideRecordPoint> getRoutePoints() { return routePoints; }
    }

    class RideRecordPoint {
        private final int pointOrder;
        private final double latitude;
        private final double longitude;

        public RideRecordPoint(int pointOrder, double latitude, double longitude) {
            this.pointOrder = pointOrder;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public int getPointOrder() { return pointOrder; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
    }

    class RideRecordSaveResult {
        private final long rideRecordId;
        private final String finalizationStatus;

        public RideRecordSaveResult(long rideRecordId, String finalizationStatus) {
            this.rideRecordId = rideRecordId;
            this.finalizationStatus = finalizationStatus;
        }

        public long getRideRecordId() { return rideRecordId; }

        public String getFinalizationStatus() { return finalizationStatus; }
    }

    class RideRecordFinalizationStatusResult {
        private final long rideRecordId;
        private final String status;
        private final String errorMessage;

        public RideRecordFinalizationStatusResult(long rideRecordId, String status, String errorMessage) {
            this.rideRecordId = rideRecordId;
            this.status = status;
            this.errorMessage = errorMessage;
        }

        public long getRideRecordId() { return rideRecordId; }

        public String getStatus() { return status; }

        public String getErrorMessage() { return errorMessage; }
    }

    class RideRecordHistoryItem {
        private final long rideRecordId;
        private final String startedAt;
        private final String endedAt;
        private final int distanceM;
        private final int durationSec;
        private final String finalizationStatus;
        private final Long linkedCourseId;

        public RideRecordHistoryItem(
                long rideRecordId,
                String startedAt,
                String endedAt,
                int distanceM,
                int durationSec,
                String finalizationStatus,
                Long linkedCourseId
        ) {
            this.rideRecordId = rideRecordId;
            this.startedAt = startedAt;
            this.endedAt = endedAt;
            this.distanceM = distanceM;
            this.durationSec = durationSec;
            this.finalizationStatus = finalizationStatus;
            this.linkedCourseId = linkedCourseId;
        }

        public long getRideRecordId() { return rideRecordId; }

        public String getStartedAt() { return startedAt; }

        public String getEndedAt() { return endedAt; }

        public int getDistanceM() { return distanceM; }

        public int getDurationSec() { return durationSec; }

        public String getFinalizationStatus() { return finalizationStatus; }

        public Long getLinkedCourseId() { return linkedCourseId; }
    }

    class RideRecordHistoryResult {
        private final List<RideRecordHistoryItem> items;

        public RideRecordHistoryResult(List<RideRecordHistoryItem> items) {
            this.items = items;
        }

        public List<RideRecordHistoryItem> getItems() { return items; }
    }

    class RideRecordDetailResult {
        private final long rideRecordId;
        private final String status;
        private final String startedAt;
        private final String endedAt;
        private final int distanceM;
        private final int durationSec;
        private final int rawPointCount;
        private final int processedPointCount;
        private final Long linkedCourseId;
        private final String errorMessage;

        public RideRecordDetailResult(
                long rideRecordId,
                String status,
                String startedAt,
                String endedAt,
                int distanceM,
                int durationSec,
                int rawPointCount,
                int processedPointCount,
                Long linkedCourseId,
                String errorMessage
        ) {
            this.rideRecordId = rideRecordId;
            this.status = status;
            this.startedAt = startedAt;
            this.endedAt = endedAt;
            this.distanceM = distanceM;
            this.durationSec = durationSec;
            this.rawPointCount = rawPointCount;
            this.processedPointCount = processedPointCount;
            this.linkedCourseId = linkedCourseId;
            this.errorMessage = errorMessage;
        }

        public long getRideRecordId() { return rideRecordId; }

        public String getStatus() { return status; }

        public String getStartedAt() { return startedAt; }

        public String getEndedAt() { return endedAt; }

        public int getDistanceM() { return distanceM; }

        public int getDurationSec() { return durationSec; }

        public int getRawPointCount() { return rawPointCount; }

        public int getProcessedPointCount() { return processedPointCount; }

        public Long getLinkedCourseId() { return linkedCourseId; }

        public String getErrorMessage() { return errorMessage; }
    }
}
