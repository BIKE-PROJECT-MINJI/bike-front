package com.bikeprojectminji.bikefront.ui.screen

import com.bikeprojectminji.bikefront.ridemap.CourseRoutePointsGateway
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoursePreRideMapPreviewStateTest {

    @Test
    fun `real route points resolve to explicit ready preview state`() {
        val state = CoursePreRideMapPreviewStateResolver.fromSuccess(
            points = listOf(
                CourseRoutePointsGateway.RoutePoint(0, 37.5665, 126.9780),
                CourseRoutePointsGateway.RoutePoint(1, 37.5651, 126.98955),
            ),
        )

        assertTrue(state is CoursePreRideMapPreviewUiState.Ready)
        assertEquals(
            "경로 미리보기가 준비되었습니다.",
            (state as CoursePreRideMapPreviewUiState.Ready).message,
        )
    }

    @Test
    fun `empty route resolves to explicit empty preview state`() {
        val state = CoursePreRideMapPreviewStateResolver.fromSuccess(points = emptyList())

        assertTrue(state is CoursePreRideMapPreviewUiState.Empty)
        assertEquals(
            "경로 정보가 없어 미리보기를 표시할 수 없습니다.",
            (state as CoursePreRideMapPreviewUiState.Empty).message,
        )
    }

    @Test
    fun `route load failure resolves to explicit error preview state`() {
        val state = CoursePreRideMapPreviewStateResolver.fromFailure("경로를 불러오지 못했습니다.")

        assertTrue(state is CoursePreRideMapPreviewUiState.Error)
        assertEquals(
            "경로를 불러오지 못했습니다.",
            (state as CoursePreRideMapPreviewUiState.Error).message,
        )
    }

    @Test
    fun `error state is preserved when empty route callback arrives after failure`() {
        val failedState = CoursePreRideMapPreviewStateReducer.onRouteLoadFailed("네트워크 오류")

        val state = CoursePreRideMapPreviewStateReducer.onRoutePointsLoaded(
            currentState = failedState,
            points = emptyList(),
        )

        assertTrue(state is CoursePreRideMapPreviewUiState.Error)
        assertEquals(
            "네트워크 오류",
            (state as CoursePreRideMapPreviewUiState.Error).message,
        )
    }

    @Test
    fun `non empty route callback can recover preview state after failure`() {
        val failedState = CoursePreRideMapPreviewStateReducer.onRouteLoadFailed("네트워크 오류")

        val state = CoursePreRideMapPreviewStateReducer.onRoutePointsLoaded(
            currentState = failedState,
            points = listOf(
                CourseRoutePointsGateway.RoutePoint(0, 37.5665, 126.9780),
                CourseRoutePointsGateway.RoutePoint(1, 37.5651, 126.98955),
            ),
        )

        assertTrue(state is CoursePreRideMapPreviewUiState.Ready)
    }
}
