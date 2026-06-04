package com.bikeprojectminji.bikefront.airoute

data class AiRoutePlanPresentation(
    val headline: String,
    val reason: String,
    val scoreLabel: String,
    val scoreChips: List<String>,
    val evidenceLabels: List<String>,
    val cautionText: String,
    val provenanceLabel: String,
) {
    companion object {
        fun from(plan: AiRoutePlanUiModel): AiRoutePlanPresentation {
            val score = plan.recommendationScore
            return AiRoutePlanPresentation(
                headline = plan.explanation.headline.ifBlank { plan.summary },
                reason = plan.explanation.reason,
                scoreLabel = score.total.takeIf { it > 0 }?.let { "추천점수 $it" } ?: "추천점수 대기",
                scoreChips = buildList {
                    addIfPositive("경치", score.scenery)
                    addIfPositive("자전거길", score.bikePath)
                    addIfPositive("안전", score.safety)
                    addIfPositive("조건", score.condition)
                },
                evidenceLabels = plan.evidenceBadges.take(4).map { "${it.label} ${it.statusLabel}" },
                cautionText = conservativeCaution(plan),
                provenanceLabel = if (plan.aiGenerated) "AI worker 반영" else "fallback 경로",
            )
        }

        private fun MutableList<String>.addIfPositive(label: String, value: Int) {
            if (value > 0) add("$label $value")
        }

        private fun conservativeCaution(plan: AiRoutePlanUiModel): String {
            val needsCaution = plan.evidenceBadges.any {
                it.status == AiRouteEvidenceStatusUi.Unknown ||
                    it.status == AiRouteEvidenceStatusUi.Failed ||
                    it.status == AiRouteEvidenceStatusUi.Warning
            }
            return when {
                needsCaution && plan.explanation.caution.isNotBlank() -> plan.explanation.caution
                plan.evidenceBadges.isEmpty() -> plan.explanation.caution.ifBlank {
                    "일부 조건은 정보 없음 상태라 출발 전 확인이 필요해요."
                }
                else -> "확인된 조건 기준의 추천입니다. 출발 전 현장 표지는 다시 확인하세요."
            }
        }
    }
}
