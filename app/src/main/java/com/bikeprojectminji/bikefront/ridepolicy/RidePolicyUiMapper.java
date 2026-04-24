package com.bikeprojectminji.bikefront.ridepolicy;

import com.bikeprojectminji.bikefront.R;

public class RidePolicyUiMapper {

    public RidePolicyUiModel map(RidePolicyEvaluationGateway.EvaluationResult result) {
        String phase = result.getPhase();
        String message = resolveMessage(result);

        if ("ACTIVE".equals(phase)) {
            return mapActive(result, message);
        }

        return mapPreStart(result, message);
    }

    private RidePolicyUiModel mapPreStart(RidePolicyEvaluationGateway.EvaluationResult result, String message) {
        String status = result.getStartGate().getStatus();
        String detailCaption = formatStartGateCaption(result.getStartGate());

        if ("ELIGIBLE".equals(status)) {
            return new RidePolicyUiModel(
                    "주행 가능",
                    message,
                    detailCaption,
                    false,
                    "",
                    R.color.success_text,
                    R.color.banner_warning_background,
                    R.color.error_text,
                    false,
                    ""
            );
        }

        if ("BLOCKED".equals(status)) {
            return new RidePolicyUiModel(
                    "시작 위치 확인 필요",
                    message,
                    detailCaption,
                    true,
                    message,
                    R.color.error_text,
                    R.color.banner_warning_background,
                    R.color.error_text,
                    false,
                    ""
            );
        }

        return new RidePolicyUiModel(
                "판단 보류",
                message,
                detailCaption,
                false,
                "",
                R.color.info_text,
                R.color.banner_warning_background,
                R.color.error_text,
                false,
                ""
        );
    }

    private RidePolicyUiModel mapActive(RidePolicyEvaluationGateway.EvaluationResult result, String fallbackMessage) {
        RidePolicyEvaluationGateway.OffRouteResult offRoute = result.getOffRoute();
        RidePolicyEvaluationGateway.CompletionResult completion = result.getCompletion();
        String status = offRoute.getStatus();
        String detailCaption = formatActiveDetailCaption(offRoute, completion);
        boolean completionEligible = "ELIGIBLE".equals(completion.getStatus());
        String completionDialogMessage = buildCompletionDialogMessage(completion);

        if ("WARNING".equals(status)) {
            String warningMessage = buildActiveWarningMessage(offRoute, fallbackMessage);
            return new RidePolicyUiModel(
                    "경로 이탈 경고",
                    warningMessage,
                    detailCaption,
                    true,
                    warningMessage,
                    R.color.error_text,
                    R.color.banner_warning_background,
                    R.color.error_text,
                    completionEligible,
                    completionDialogMessage
            );
        }

        if ("CANDIDATE".equals(status)) {
            String candidateMessage = buildActiveCandidateMessage(offRoute, fallbackMessage);
            return new RidePolicyUiModel(
                    "이탈 후보",
                    candidateMessage,
                    detailCaption,
                    true,
                    candidateMessage,
                    R.color.error_text,
                    R.color.banner_warning_background,
                    R.color.error_text,
                    completionEligible,
                    completionDialogMessage
            );
        }

        if ("ON_ROUTE".equals(status) && "RECOVERED_WITHIN_THRESHOLD".equals(offRoute.getReasonCode())) {
            String recoveredMessage = buildRecoveredMessage(completion);
            return new RidePolicyUiModel(
                    "복귀 완료",
                    recoveredMessage,
                    detailCaption,
                    false,
                    "",
                    R.color.success_text,
                    R.color.banner_warning_background,
                    R.color.error_text,
                    completionEligible,
                    completionDialogMessage
            );
        }

        if ("ON_ROUTE".equals(status)) {
            String onRouteMessage = buildOnRouteMessage(completion, fallbackMessage);
            return new RidePolicyUiModel(
                    completionEligible ? "완주 가능" : "주행 중",
                    onRouteMessage,
                    detailCaption,
                    completionEligible,
                    completionEligible ? onRouteMessage : "",
                    R.color.success_text,
                    R.color.banner_warning_background,
                    R.color.error_text,
                    completionEligible,
                    completionDialogMessage
            );
        }

        return new RidePolicyUiModel(
                "판단 보류",
                fallbackMessage,
                detailCaption,
                false,
                "",
                R.color.info_text,
                R.color.banner_warning_background,
                R.color.error_text,
                completionEligible,
                completionDialogMessage
        );
    }

    private String resolveMessage(RidePolicyEvaluationGateway.EvaluationResult result) {
        String defaultMessage = result.getDefaultMessage();
        if (defaultMessage != null && !defaultMessage.isBlank()) {
            return defaultMessage;
        }

        if ("ACTIVE".equals(result.getPhase())) {
            String offRouteReasonCode = result.getOffRoute().getReasonCode();
            if (offRouteReasonCode != null && !offRouteReasonCode.isBlank() && !"UNKNOWN_REASON".equals(offRouteReasonCode)) {
                return fallbackForReasonCode(offRouteReasonCode, true);
            }
            return fallbackForCompletionReasonCode(result.getCompletion().getReasonCode());
        }

        return fallbackForReasonCode(result.getStartGate().getReasonCode(), false);
    }

    private String buildActiveWarningMessage(
            RidePolicyEvaluationGateway.OffRouteResult offRoute,
            String fallbackMessage
    ) {
        Integer distanceM = offRoute.getDistanceM();
        Integer durationSec = offRoute.getDurationSec();
        if (distanceM != null && durationSec != null) {
            return "경로에서 " + distanceM + "m 벗어난 상태가 " + durationSec + "초 이상 이어졌습니다. 코스 라인으로 복귀하세요.";
        }
        return fallbackMessage;
    }

    private String buildActiveCandidateMessage(
            RidePolicyEvaluationGateway.OffRouteResult offRoute,
            String fallbackMessage
    ) {
        Integer distanceM = offRoute.getDistanceM();
        Integer warningThresholdSec = offRoute.getWarningThresholdSec();
        Integer durationSec = offRoute.getDurationSec();
        if (distanceM != null && warningThresholdSec != null && durationSec != null) {
            int remainingSeconds = Math.max(0, warningThresholdSec - durationSec);
            return "경로에서 " + distanceM + "m 벗어났습니다. " + remainingSeconds + "초 안에 복귀하지 않으면 이탈 경고로 전환됩니다.";
        }
        return fallbackMessage;
    }

    private String buildRecoveredMessage(RidePolicyEvaluationGateway.CompletionResult completion) {
        String completionMessage = buildCompletionProgressMessage(completion);
        if (!completionMessage.isBlank()) {
            return "코스 라인으로 복귀했습니다. " + completionMessage;
        }
        return "코스 라인으로 복귀했습니다.";
    }

    private String buildOnRouteMessage(
            RidePolicyEvaluationGateway.CompletionResult completion,
            String fallbackMessage
    ) {
        String completionMessage = buildCompletionProgressMessage(completion);
        if (!completionMessage.isBlank()) {
            return completionMessage;
        }
        return fallbackMessage;
    }

    private String buildCompletionProgressMessage(RidePolicyEvaluationGateway.CompletionResult completion) {
        String completionStatus = completion.getStatus();
        if ("ELIGIBLE".equals(completionStatus)) {
            String targetLabel = completionTargetLabel(completion);
            Integer distanceM = completion.getDistanceM();
            if (distanceM != null) {
                return "완주 조건을 충족했습니다. " + targetLabel + " " + distanceM + "m 이내입니다.";
            }
            return "완주 조건을 충족했습니다.";
        }

        if (!"IN_PROGRESS".equals(completionStatus)) {
            return fallbackForCompletionReasonCode(completion.getReasonCode());
        }

        String reasonCode = completion.getReasonCode();
        Integer coveragePercent = completion.getCoveragePercent();
        Integer coverageThresholdPercent = completion.getCoverageThresholdPercent();
        Integer distanceM = completion.getDistanceM();
        String targetLabel = completionTargetLabel(completion);

        if ("COVERAGE_BELOW_THRESHOLD".equals(reasonCode) && coveragePercent != null && coverageThresholdPercent != null) {
            return "완주 진행률 " + coveragePercent + "% / " + coverageThresholdPercent + "%입니다. 코스를 더 따라가 주세요.";
        }
        if ("START_ZONE_NOT_EXITED".equals(reasonCode)) {
            return "순환 코스는 출발 지점을 충분히 벗어나야 완주를 판단할 수 있습니다.";
        }
        if ("AWAITING_RETURN_TO_START".equals(reasonCode) && distanceM != null) {
            return targetLabel + "까지 " + distanceM + "m 남았습니다. 출발 지점으로 돌아오면 완주할 수 있습니다.";
        }
        if ("AWAITING_DESTINATION".equals(reasonCode) && distanceM != null) {
            return targetLabel + "까지 " + distanceM + "m 남았습니다. 도착 지점에 들어오면 완주할 수 있습니다.";
        }
        return fallbackForCompletionReasonCode(reasonCode);
    }

    private String buildCompletionDialogMessage(RidePolicyEvaluationGateway.CompletionResult completion) {
        if (!"ELIGIBLE".equals(completion.getStatus())) {
            return "";
        }

        String targetLabel = completionTargetLabel(completion);
        String actionLabel = Boolean.TRUE.equals(completion.getLoopCourse())
                ? "기록을 저장하고 순환 코스 주행을 마칠까요?"
                : "기록을 저장하고 코스 주행을 마칠까요?";
        Integer distanceM = completion.getDistanceM();
        Integer thresholdM = completion.getThresholdM();
        Integer coveragePercent = completion.getCoveragePercent();
        Integer coverageThresholdPercent = completion.getCoverageThresholdPercent();

        StringBuilder builder = new StringBuilder();
        if (coveragePercent != null && coverageThresholdPercent != null) {
            builder.append("완주 진행률 ")
                    .append(coveragePercent)
                    .append("% / ")
                    .append(coverageThresholdPercent)
                    .append("%를 충족했습니다. ");
        }
        if (distanceM != null && thresholdM != null) {
            builder.append(targetLabel)
                    .append(" ")
                    .append(distanceM)
                    .append("m 이내(")
                    .append(thresholdM)
                    .append("m 기준)에 들어왔습니다. ");
        }
        builder.append(actionLabel);
        return builder.toString().trim();
    }

    private String formatStartGateCaption(RidePolicyEvaluationGateway.GateResult startGate) {
        double distanceM = startGate.getDistanceM();
        double thresholdM = startGate.getThresholdM();
        if (Double.isNaN(distanceM) || Double.isNaN(thresholdM)) {
            return "";
        }
        return "시작점까지 " + formatMeters(distanceM) + " · 허용 반경 " + formatMeters(thresholdM);
    }

    private String formatActiveDetailCaption(
            RidePolicyEvaluationGateway.OffRouteResult offRoute,
            RidePolicyEvaluationGateway.CompletionResult completion
    ) {
        String offRouteCaption = formatOffRouteCaption(offRoute);
        String completionCaption = formatCompletionCaption(completion);

        if (!offRouteCaption.isBlank() && !completionCaption.isBlank()) {
            return offRouteCaption + " · " + completionCaption;
        }
        if (!offRouteCaption.isBlank()) {
            return offRouteCaption;
        }
        return completionCaption;
    }

    private String formatOffRouteCaption(RidePolicyEvaluationGateway.OffRouteResult offRoute) {
        Integer distanceM = offRoute.getDistanceM();
        Integer candidateThresholdM = offRoute.getCandidateThresholdM();
        Integer recoveryThresholdM = offRoute.getRecoveryThresholdM();
        Integer durationSec = offRoute.getDurationSec();

        if ("CANDIDATE".equals(offRoute.getStatus()) || "WARNING".equals(offRoute.getStatus())) {
            if (distanceM != null && candidateThresholdM != null && durationSec != null) {
                return "이탈 " + distanceM + "m · 후보 기준 " + candidateThresholdM + "m · 지속 " + durationSec + "초";
            }
        }

        if ("RECOVERED_WITHIN_THRESHOLD".equals(offRoute.getReasonCode()) && recoveryThresholdM != null) {
            return "복귀 반경 " + recoveryThresholdM + "m 안으로 돌아왔습니다.";
        }

        return "";
    }

    private String formatCompletionCaption(RidePolicyEvaluationGateway.CompletionResult completion) {
        Integer coveragePercent = completion.getCoveragePercent();
        Integer coverageThresholdPercent = completion.getCoverageThresholdPercent();
        Integer distanceM = completion.getDistanceM();
        Integer thresholdM = completion.getThresholdM();

        StringBuilder builder = new StringBuilder();
        if (coveragePercent != null && coverageThresholdPercent != null) {
            builder.append("완주 ")
                    .append(coveragePercent)
                    .append("% / ")
                    .append(coverageThresholdPercent)
                    .append("%");
        }
        if (distanceM != null && thresholdM != null) {
            if (builder.length() > 0) {
                builder.append(" · ");
            }
            builder.append(completionTargetLabel(completion))
                    .append(" ")
                    .append(distanceM)
                    .append("m / ")
                    .append(thresholdM)
                    .append("m");
        }
        return builder.toString();
    }

    private String completionTargetLabel(RidePolicyEvaluationGateway.CompletionResult completion) {
        return Boolean.TRUE.equals(completion.getLoopCourse()) ? "출발 지점" : "도착 지점";
    }

    private String fallbackForReasonCode(String reasonCode, boolean activePhase) {
        if ("LOCATION_LOW_ACCURACY".equals(reasonCode)) {
            return activePhase
                    ? "현재 위치 정확도가 낮아 경로 이탈 여부를 판단하기 어렵습니다."
                    : "위치 정확도가 낮아 시작 가능 여부를 판단하기 어렵습니다.";
        }
        if ("LOCATION_STALE".equals(reasonCode)) {
            return activePhase
                    ? "위치 정보가 오래되어 경로 이탈 여부를 판단하기 어렵습니다."
                    : "위치 정보가 오래되어 시작 가능 여부를 판단하기 어렵습니다.";
        }
        if ("TOO_FAR_FROM_COURSE".equals(reasonCode)) {
            return "경로 인근에서 시작해야 합니다. 현재 위치가 선택한 코스와 너무 멉니다.";
        }
        if ("WITHIN_START_OR_ROUTE".equals(reasonCode)) {
            return "주행을 시작할 수 있습니다.";
        }
        if ("WITHIN_ROUTE_THRESHOLD".equals(reasonCode) || "ALREADY_ACTIVE".equals(reasonCode)) {
            return "현재 코스를 따라 주행 중입니다.";
        }
        if ("RECOVERED_WITHIN_THRESHOLD".equals(reasonCode)) {
            return "코스 라인으로 복귀했습니다.";
        }
        if ("OFF_ROUTE_CANDIDATE_ACTIVE".equals(reasonCode)) {
            return "경로 이탈 후보 상태입니다. 코스 라인으로 복귀해 주세요.";
        }
        if ("OFF_ROUTE_WARNING_ACTIVE".equals(reasonCode) || "OFF_ROUTE_RECOVERY_PENDING".equals(reasonCode)) {
            return "경로를 벗어났습니다. 코스 라인으로 복귀하세요.";
        }
        if ("COURSE_PATH_INVALID".equals(reasonCode)) {
            return "코스 경로 정보를 확인할 수 없어 이탈 여부를 판단하기 어렵습니다.";
        }

        return activePhase
                ? "현재 위치를 다시 확인해 주세요."
                : "위치 정보를 다시 확인해 주세요.";
    }

    private String fallbackForCompletionReasonCode(String reasonCode) {
        if ("COVERAGE_BELOW_THRESHOLD".equals(reasonCode)) {
            return "완주 판단을 위해 코스를 더 따라가 주세요.";
        }
        if ("START_ZONE_NOT_EXITED".equals(reasonCode)) {
            return "순환 코스는 출발 지점을 충분히 벗어나야 완주를 판단할 수 있습니다.";
        }
        if ("AWAITING_RETURN_TO_START".equals(reasonCode)) {
            return "출발 지점으로 돌아오면 순환 코스 완주를 마칠 수 있습니다.";
        }
        if ("AWAITING_DESTINATION".equals(reasonCode)) {
            return "도착 지점에 들어오면 코스 완주를 마칠 수 있습니다.";
        }
        if ("LOOP_COMPLETION_READY".equals(reasonCode)) {
            return "순환 코스 완주 조건을 충족했습니다.";
        }
        if ("NON_LOOP_COMPLETION_READY".equals(reasonCode)) {
            return "코스 완주 조건을 충족했습니다.";
        }
        if ("NOT_ACTIVE_YET".equals(reasonCode)) {
            return "주행을 시작하면 완주 진행률을 확인할 수 있습니다.";
        }
        if ("COURSE_PATH_INVALID".equals(reasonCode)) {
            return "코스 경로 정보를 확인할 수 없어 완주 여부를 판단하기 어렵습니다.";
        }
        return "완주 상태를 다시 확인해 주세요.";
    }

    private String formatMeters(double meters) {
        return Math.round(meters) + "m";
    }
}
