package com.bikeprojectminji.bikefront.free

import android.content.Context
import android.content.Intent
import com.bikeprojectminji.bikefront.course.CourseEditorActivity
import com.bikeprojectminji.bikefront.ride.RideRecordGateway
import com.bikeprojectminji.bikefront.ridemap.CourseRoutePointsGateway
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

internal sealed class FreeRideSavePreparation {
    data class Ready(
        val accessToken: String,
        val draft: RideRecordGateway.RideRecordDraft,
        val durationMinutes: Int,
        val distanceKm: Double,
    ) : FreeRideSavePreparation()

    data class Blocked(
        val message: String,
        val requiresAuth: Boolean = false,
    ) : FreeRideSavePreparation()
}

internal object FreeRideSaveCoordinator {
    private const val SHORT_RIDE_MIN_DURATION_SEC = 10

    fun prepare(
        accessToken: String,
        trackedPoints: List<RideRecordGateway.RideRecordPoint>,
        routePoints: List<CourseRoutePointsGateway.RoutePoint>,
        distanceMeters: Int,
        startedAtMillis: Long,
        endedAtMillis: Long,
        activeDurationMillis: Long? = null,
    ): FreeRideSavePreparation {
        if (accessToken.isBlank()) {
            return FreeRideSavePreparation.Blocked("로그인이 필요합니다.", requiresAuth = true)
        }
        val sourcePoints = resolveSourcePoints(trackedPoints, routePoints)
        if (sourcePoints.isEmpty()) {
            return FreeRideSavePreparation.Blocked("저장할 주행 기록이 없습니다.")
        }
        val durationMillis = activeDurationMillis?.coerceAtLeast(0L) ?: (endedAtMillis - startedAtMillis).coerceAtLeast(0L)
        val draft = RideRecordGateway.RideRecordDraft(
            OffsetDateTime.ofInstant(Instant.ofEpochMilli(startedAtMillis), ZoneId.systemDefault()),
            OffsetDateTime.ofInstant(Instant.ofEpochMilli(endedAtMillis), ZoneId.systemDefault()),
            distanceMeters,
            (durationMillis / 1000L).toInt(),
            sourcePoints,
        )
        return FreeRideSavePreparation.Ready(
            accessToken = accessToken,
            draft = draft,
            durationMinutes = (durationMillis / 60000L).toInt(),
            distanceKm = distanceMeters / 1000.0,
        )
    }

    fun isShortRideDuration(durationSec: Int): Boolean {
        return durationSec < SHORT_RIDE_MIN_DURATION_SEC
    }

    fun createEditorIntent(
        context: Context,
        rideRecordId: Long,
        distanceKm: Double,
        durationMinutes: Int,
    ): Intent {
        return Intent(context, CourseEditorActivity::class.java).apply {
            putExtra(CourseEditorActivity.EXTRA_RIDE_RECORD_ID, rideRecordId)
            putExtra(CourseEditorActivity.EXTRA_DISTANCE_KM, distanceKm)
            putExtra(CourseEditorActivity.EXTRA_DURATION_MIN, durationMinutes)
            putExtra(CourseEditorActivity.EXTRA_SOURCE_SUMMARY, "주행 기록에서 코스 초안을 이어서 정리합니다.")
        }
    }

    private fun resolveSourcePoints(
        trackedPoints: List<RideRecordGateway.RideRecordPoint>,
        routePoints: List<CourseRoutePointsGateway.RoutePoint>,
    ): List<RideRecordGateway.RideRecordPoint> {
        if (trackedPoints.isNotEmpty()) {
            return trackedPoints
        }
        return routePoints.map {
            RideRecordGateway.RideRecordPoint(it.pointOrder, it.latitude, it.longitude)
        }
    }
}
