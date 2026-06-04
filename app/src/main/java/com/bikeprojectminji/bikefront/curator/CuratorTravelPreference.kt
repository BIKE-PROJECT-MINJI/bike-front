package com.bikeprojectminji.bikefront.curator

enum class RidePurpose(val label: String) {
    SCENIC_TRAVEL("경치 여행"),
    BIKE_PATH_CRUISE("자전거도로 크루징"),
    CAFE_DESTINATION("카페/목적지"),
    LIGHT_EXERCISE("가벼운 운동"),
}

enum class RoutePriority(val label: String) {
    SCENERY_FIRST("경치 우선"),
    BIKE_PATH_FIRST("자전거도로 우선"),
    BALANCED("균형"),
    SAFE_FIRST("안전 우선"),
}

enum class DistanceComfort(val label: String) {
    SHORT("짧게"),
    MEDIUM("보통"),
    LONG("길게"),
}

enum class AvoidCondition(val label: String) {
    HILLS("언덕"),
    TRAFFIC("차 많은 길"),
    BAD_SURFACE("나쁜 노면"),
    FINE_DUST("미세먼지"),
    RAIN_WIND("비/바람"),
}

data class CuratorTravelPreference(
    val ridePurpose: RidePurpose,
    val routePriority: RoutePriority,
    val distanceComfort: DistanceComfort,
    val avoidConditions: Set<AvoidCondition>,
) {
    companion object {
        fun default(): CuratorTravelPreference {
            return CuratorTravelPreference(
                ridePurpose = RidePurpose.SCENIC_TRAVEL,
                routePriority = RoutePriority.SCENERY_FIRST,
                distanceComfort = DistanceComfort.MEDIUM,
                avoidConditions = emptySet(),
            )
        }
    }

    fun toggleAvoidCondition(condition: AvoidCondition): CuratorTravelPreference {
        val next = if (avoidConditions.contains(condition)) {
            avoidConditions - condition
        } else {
            avoidConditions + condition
        }
        return copy(avoidConditions = next)
    }
}

object CuratorTravelPreferencePayloadMapper {
    fun toMap(preference: CuratorTravelPreference): Map<String, String> {
        return mapOf(
            "ridePurpose" to preference.ridePurpose.name,
            "routePriority" to preference.routePriority.name,
            "distanceComfort" to preference.distanceComfort.name,
            "avoidCondition" to preference.avoidConditions
                .sortedBy { it.ordinal }
                .joinToString(",") { it.name },
        )
    }
}
