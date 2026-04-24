package com.bikeprojectminji.bikefront.ridepolicy;

import android.location.Location;

import java.util.List;

public interface RidePolicyEvaluationGateway {

    final class TraceLocation {
        private final double latitude;
        private final double longitude;
        private final double accuracyM;
        private final long capturedAtMillis;

        public TraceLocation(double latitude, double longitude, double accuracyM, long capturedAtMillis) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.accuracyM = accuracyM;
            this.capturedAtMillis = capturedAtMillis;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public double getAccuracyM() {
            return accuracyM;
        }

        public long getCapturedAtMillis() {
            return capturedAtMillis;
        }
    }

    final class GateResult {
        private final String status;
        private final String reasonCode;
        private final double distanceM;
        private final double thresholdM;

        public GateResult(String status, String reasonCode, double distanceM, double thresholdM) {
            this.status = status;
            this.reasonCode = reasonCode;
            this.distanceM = distanceM;
            this.thresholdM = thresholdM;
        }

        public String getStatus() {
            return status;
        }

        public String getReasonCode() {
            return reasonCode;
        }

        public double getDistanceM() {
            return distanceM;
        }

        public double getThresholdM() {
            return thresholdM;
        }
    }

    final class OffRouteResult {
        private final String status;
        private final String reasonCode;
        private final Integer distanceM;
        private final Integer candidateThresholdM;
        private final Integer warningThresholdSec;
        private final Integer recoveryThresholdM;
        private final Integer durationSec;

        public OffRouteResult(
                String status,
                String reasonCode,
                Integer distanceM,
                Integer candidateThresholdM,
                Integer warningThresholdSec,
                Integer recoveryThresholdM,
                Integer durationSec
        ) {
            this.status = status;
            this.reasonCode = reasonCode;
            this.distanceM = distanceM;
            this.candidateThresholdM = candidateThresholdM;
            this.warningThresholdSec = warningThresholdSec;
            this.recoveryThresholdM = recoveryThresholdM;
            this.durationSec = durationSec;
        }

        public String getStatus() {
            return status;
        }

        public String getReasonCode() {
            return reasonCode;
        }

        public Integer getDistanceM() {
            return distanceM;
        }

        public Integer getCandidateThresholdM() {
            return candidateThresholdM;
        }

        public Integer getWarningThresholdSec() {
            return warningThresholdSec;
        }

        public Integer getRecoveryThresholdM() {
            return recoveryThresholdM;
        }

        public Integer getDurationSec() {
            return durationSec;
        }
    }

    final class CompletionResult {
        private final String status;
        private final String reasonCode;
        private final Integer coveragePercent;
        private final Integer coverageThresholdPercent;
        private final Boolean loopCourse;
        private final Boolean leftStartZone;
        private final Integer distanceM;
        private final Integer thresholdM;

        public CompletionResult(
                String status,
                String reasonCode,
                Integer coveragePercent,
                Integer coverageThresholdPercent,
                Boolean loopCourse,
                Boolean leftStartZone,
                Integer distanceM,
                Integer thresholdM
        ) {
            this.status = status;
            this.reasonCode = reasonCode;
            this.coveragePercent = coveragePercent;
            this.coverageThresholdPercent = coverageThresholdPercent;
            this.loopCourse = loopCourse;
            this.leftStartZone = leftStartZone;
            this.distanceM = distanceM;
            this.thresholdM = thresholdM;
        }

        public String getStatus() {
            return status;
        }

        public String getReasonCode() {
            return reasonCode;
        }

        public Integer getCoveragePercent() {
            return coveragePercent;
        }

        public Integer getCoverageThresholdPercent() {
            return coverageThresholdPercent;
        }

        public Boolean getLoopCourse() {
            return loopCourse;
        }

        public Boolean getLeftStartZone() {
            return leftStartZone;
        }

        public Integer getDistanceM() {
            return distanceM;
        }

        public Integer getThresholdM() {
            return thresholdM;
        }
    }

    final class EvaluationResult {
        private final String phase;
        private final GateResult startGate;
        private final OffRouteResult offRoute;
        private final CompletionResult completion;
        private final String overallState;
        private final String defaultMessage;

        public EvaluationResult(
                String phase,
                GateResult startGate,
                OffRouteResult offRoute,
                CompletionResult completion,
                String overallState,
                String defaultMessage
        ) {
            this.phase = phase;
            this.startGate = startGate;
            this.offRoute = offRoute;
            this.completion = completion;
            this.overallState = overallState;
            this.defaultMessage = defaultMessage;
        }

        public String getPhase() {
            return phase;
        }

        public GateResult getStartGate() {
            return startGate;
        }

        public OffRouteResult getOffRoute() {
            return offRoute;
        }

        public CompletionResult getCompletion() {
            return completion;
        }

        public String getOverallState() {
            return overallState;
        }

        public String getDefaultMessage() {
            return defaultMessage;
        }
    }

    interface Callback {
        void onSuccess(EvaluationResult result);

        void onFailure(String message);
    }

    default void evaluate(long courseId, String phase, Location location, Callback callback) {
        evaluate(courseId, phase, location, List.of(), callback);
    }

    void evaluate(long courseId, String phase, Location location, List<TraceLocation> trace, Callback callback);
}
