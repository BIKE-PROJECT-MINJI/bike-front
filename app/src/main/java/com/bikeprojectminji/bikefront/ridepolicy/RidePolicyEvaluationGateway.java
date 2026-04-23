package com.bikeprojectminji.bikefront.ridepolicy;

import android.location.Location;

public interface RidePolicyEvaluationGateway {

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

    final class EvaluationResult {
        private final String phase;
        private final GateResult startGate;
        private final GateResult offRoute;
        private final String overallState;
        private final String defaultMessage;

        public EvaluationResult(
                String phase,
                GateResult startGate,
                GateResult offRoute,
                String overallState,
                String defaultMessage
        ) {
            this.phase = phase;
            this.startGate = startGate;
            this.offRoute = offRoute;
            this.overallState = overallState;
            this.defaultMessage = defaultMessage;
        }

        public String getPhase() {
            return phase;
        }

        public GateResult getStartGate() {
            return startGate;
        }

        public GateResult getOffRoute() {
            return offRoute;
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

    void evaluate(long courseId, String phase, Location location, Callback callback);
}
