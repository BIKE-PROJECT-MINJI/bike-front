package com.bikeprojectminji.bikefront.ridepolicy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RidePolicyUiMapperTest {

    @Test
    public void mapPreStartBlockedIncludesDistanceAndThresholdCaption() {
        RidePolicyUiMapper mapper = new RidePolicyUiMapper();

        RidePolicyEvaluationGateway.EvaluationResult result = new RidePolicyEvaluationGateway.EvaluationResult(
                "PRE_START",
                new RidePolicyEvaluationGateway.GateResult("BLOCKED", "TOO_FAR_FROM_COURSE", 132d, 50d),
                new RidePolicyEvaluationGateway.OffRouteResult("UNDETERMINED", "UNKNOWN", null, null, null, null, null),
                new RidePolicyEvaluationGateway.CompletionResult("UNDETERMINED", "UNKNOWN", null, null, null, null, null, null),
                "BLOCKED",
                "경로 인근에서 시작해야 합니다."
        );

        RidePolicyUiModel uiModel = mapper.map(result);

        assertEquals("시작 위치 확인 필요", uiModel.getStateLabel());
        assertTrue(uiModel.getDetailCaption().contains("132m"));
        assertTrue(uiModel.getDetailCaption().contains("50m"));
    }
}
