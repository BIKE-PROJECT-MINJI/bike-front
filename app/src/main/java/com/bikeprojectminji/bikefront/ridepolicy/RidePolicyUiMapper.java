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

        if ("ELIGIBLE".equals(status)) {
            return new RidePolicyUiModel(
                    "주행 가능",
                    message,
                    false,
                    "",
                    R.color.success_text,
                    R.color.banner_warning_background,
                    R.color.error_text
            );
        }

        if ("BLOCKED".equals(status)) {
            return new RidePolicyUiModel(
                    "시작 위치 확인 필요",
                    message,
                    true,
                    message,
                    R.color.error_text,
                    R.color.banner_warning_background,
                    R.color.error_text
            );
        }

        return new RidePolicyUiModel(
                "판단 보류",
                message,
                false,
                "",
                R.color.info_text,
                R.color.banner_warning_background,
                R.color.error_text
        );
    }

    private RidePolicyUiModel mapActive(RidePolicyEvaluationGateway.EvaluationResult result, String message) {
        String status = result.getOffRoute().getStatus();

        if ("ON_ROUTE".equals(status)) {
            return new RidePolicyUiModel(
                    "주행 중",
                    message,
                    false,
                    "",
                    R.color.success_text,
                    R.color.banner_warning_background,
                    R.color.error_text
            );
        }

        if ("WARNING".equals(status)) {
            return new RidePolicyUiModel(
                    "경로 이탈 경고",
                    message,
                    true,
                    message,
                    R.color.error_text,
                    R.color.banner_warning_background,
                    R.color.error_text
            );
        }

        return new RidePolicyUiModel(
                "판단 보류",
                message,
                false,
                "",
                R.color.info_text,
                R.color.banner_warning_background,
                R.color.error_text
        );
    }

    private String resolveMessage(RidePolicyEvaluationGateway.EvaluationResult result) {
        String defaultMessage = result.getDefaultMessage();
        if (defaultMessage != null && !defaultMessage.isBlank()) {
            return defaultMessage;
        }

        if ("ACTIVE".equals(result.getPhase())) {
            return fallbackForReasonCode(result.getOffRoute().getReasonCode(), true);
        }

        return fallbackForReasonCode(result.getStartGate().getReasonCode(), false);
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
        if ("OFF_ROUTE_THRESHOLD_EXCEEDED".equals(reasonCode)) {
            return "경로를 벗어났습니다. 코스 라인으로 복귀하세요.";
        }
        if ("COURSE_PATH_INVALID".equals(reasonCode)) {
            return "코스 경로 정보를 확인할 수 없어 이탈 여부를 판단하기 어렵습니다.";
        }

        return activePhase
                ? "현재 위치를 다시 확인해 주세요."
                : "위치 정보를 다시 확인해 주세요.";
    }
}
