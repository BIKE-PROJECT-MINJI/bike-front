package com.bikeprojectminji.bikefront.ui.screen

import com.bikeprojectminji.bikefront.ridemap.CourseRoutePointsGateway

sealed interface CoursePreRideMapPreviewUiState {
    val title: String
    val message: String

    data object Loading : CoursePreRideMapPreviewUiState {
        override val title: String = "경로 준비 중"
        override val message: String = "경로 미리보기를 불러오는 중입니다."
    }

    data object Ready : CoursePreRideMapPreviewUiState {
        override val title: String = "경로 미리보기 준비"
        override val message: String = "경로 미리보기가 준비되었습니다."
    }

    data object Empty : CoursePreRideMapPreviewUiState {
        override val title: String = "경로 정보 없음"
        override val message: String = "경로 정보가 없어 미리보기를 표시할 수 없습니다."
    }

    data class Error(override val message: String) : CoursePreRideMapPreviewUiState {
        override val title: String = "경로 불러오기 실패"
    }
}

object CoursePreRideMapPreviewStateResolver {
    private const val FALLBACK_ERROR_MESSAGE = "경로를 불러오지 못했습니다."

    fun fromSuccess(points: List<CourseRoutePointsGateway.RoutePoint>): CoursePreRideMapPreviewUiState {
        return if (points.isEmpty()) {
            CoursePreRideMapPreviewUiState.Empty
        } else {
            CoursePreRideMapPreviewUiState.Ready
        }
    }

    fun fromFailure(message: String?): CoursePreRideMapPreviewUiState {
        return CoursePreRideMapPreviewUiState.Error(
            message = message?.takeIf { it.isNotBlank() } ?: FALLBACK_ERROR_MESSAGE,
        )
    }
}

object CoursePreRideMapPreviewStateReducer {
    fun onRoutePointsLoaded(
        currentState: CoursePreRideMapPreviewUiState,
        points: List<CourseRoutePointsGateway.RoutePoint>,
    ): CoursePreRideMapPreviewUiState {
        if (currentState is CoursePreRideMapPreviewUiState.Error && points.isEmpty()) {
            return currentState
        }
        return CoursePreRideMapPreviewStateResolver.fromSuccess(points)
    }

    fun onRouteLoadFailed(message: String?): CoursePreRideMapPreviewUiState {
        return CoursePreRideMapPreviewStateResolver.fromFailure(message)
    }
}
