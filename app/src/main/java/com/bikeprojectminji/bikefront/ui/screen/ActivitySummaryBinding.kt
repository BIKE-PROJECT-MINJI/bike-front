package com.bikeprojectminji.bikefront.ui.screen

import com.bikeprojectminji.bikefront.auth.AuthLoginGateway
import com.bikeprojectminji.bikefront.auth.AuthSession
import com.bikeprojectminji.bikefront.auth.AuthSessionFactory
import com.bikeprojectminji.bikefront.auth.AuthSessionStore
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private const val ACTIVITY_SUMMARY_SIGNED_OUT_MESSAGE = "로그인하면 이번 주 활동이 표시됩니다."
private const val ACTIVITY_SUMMARY_SESSION_MESSAGE = "로그인 세션을 갱신해 주세요."
private const val ACTIVITY_SUMMARY_EMPTY_WEEK_MESSAGE = "이번 주 첫 라이딩을 시작해 보세요."
private const val ACTIVITY_SUMMARY_AUTH_EXPIRED_MESSAGE = "로그인 정보가 필요합니다."

sealed interface RideStartActivitySummaryState {
    data object Loading : RideStartActivitySummaryState

    data class Ready(
        val primaryDistanceText: String,
        val rideCountText: String,
        val durationText: String,
        val savedCourseText: String,
        val helperText: String,
        val isEmptyWeek: Boolean,
    ) : RideStartActivitySummaryState

    data class SignedOut(val message: String) : RideStartActivitySummaryState

    data class Error(val message: String) : RideStartActivitySummaryState

    companion object {
        fun from(summary: AuthLoginGateway.ActivitySummaryResult): RideStartActivitySummaryState {
            val weeklySummary = summary.weeklySummary
            val isEmptyWeek = weeklySummary.distanceKm <= 0.0 && weeklySummary.rideCount == 0L && weeklySummary.durationMinutes == 0L && weeklySummary.savedCourseCount == 0L
            return Ready(
                primaryDistanceText = formatOneDecimal(weeklySummary.distanceKm, grouped = false),
                rideCountText = "${weeklySummary.rideCount}회 주행",
                durationText = "${formatOneDecimal(weeklySummary.durationMinutes / 60.0, grouped = false)}시간",
                savedCourseText = "${weeklySummary.savedCourseCount}개 저장",
                helperText = if (isEmptyWeek) ACTIVITY_SUMMARY_EMPTY_WEEK_MESSAGE else "최근 주행 흐름이 정상적으로 동기화되었습니다.",
                isEmptyWeek = isEmptyWeek,
            )
        }

        fun fromSignedOut(): RideStartActivitySummaryState = SignedOut(ACTIVITY_SUMMARY_SIGNED_OUT_MESSAGE)

        fun fromFailure(message: String): RideStartActivitySummaryState = Error(message)
    }
}

internal fun AuthLoginGateway.ActivitySummaryResult.isWeeklySummaryEmpty(): Boolean {
    return weeklySummary.distanceKm <= 0.0 && weeklySummary.rideCount == 0L && weeklySummary.durationMinutes == 0L && weeklySummary.savedCourseCount == 0L
}

internal fun AuthLoginGateway.ActivitySummaryResult.formatTotalDistance(): String {
    return "${formatOneDecimal(overallSummary.totalDistanceKm)} km"
}

internal fun AuthLoginGateway.ActivitySummaryResult.formatTotalElevation(): String {
    return "${formatWholeNumber(overallSummary.totalElevationM)} m"
}

internal fun AuthLoginGateway.ActivitySummaryResult.formatTotalRides(): String {
    return formatWholeNumber(overallSummary.totalRides)
}

internal fun AuthLoginGateway.ActivitySummaryResult.formatAvgSpeed(): String {
    return "${formatOneDecimal(overallSummary.avgSpeedKmh)} km/h"
}

internal fun loadActivitySummary(
    authSessionStore: AuthSessionStore,
    gateway: AuthLoginGateway,
    onResult: (ActivitySummaryLoadResult) -> Unit,
) {
    loadActivitySummary(
        sessionPort = AuthSessionStoreActivitySummarySessionPort(authSessionStore),
        gateway = gateway,
        currentTimeMillisProvider = { System.currentTimeMillis() },
        onResult = onResult,
    )
}

internal fun loadActivitySummary(
    sessionPort: ActivitySummarySessionPort,
    gateway: AuthLoginGateway,
    currentTimeMillisProvider: () -> Long,
    onResult: (ActivitySummaryLoadResult) -> Unit,
) {
    if (!sessionPort.isSignedIn()) {
        onResult(ActivitySummaryLoadResult.SignedOut)
        return
    }

    val accessToken = sessionPort.accessToken()
    if (accessToken.isBlank()) {
        if (sessionPort.canRefreshSession()) {
            requestActivitySummaryRefreshAndRetry(sessionPort, gateway, currentTimeMillisProvider, onResult)
            return
        }
        onResult(ActivitySummaryLoadResult.Failure(ACTIVITY_SUMMARY_SESSION_MESSAGE))
        return
    }

    requestActivitySummary(
        sessionPort = sessionPort,
        gateway = gateway,
        accessToken = accessToken,
        allowRefreshRetry = sessionPort.canRefreshSession(),
        currentTimeMillisProvider = currentTimeMillisProvider,
        onResult = onResult,
    )
}

private fun requestActivitySummary(
    sessionPort: ActivitySummarySessionPort,
    gateway: AuthLoginGateway,
    accessToken: String,
    allowRefreshRetry: Boolean,
    currentTimeMillisProvider: () -> Long,
    onResult: (ActivitySummaryLoadResult) -> Unit,
) {
    gateway.getMyActivitySummary(accessToken, object : AuthLoginGateway.ActivitySummaryCallback {
        override fun onSuccess(result: AuthLoginGateway.ActivitySummaryResult) {
            onResult(ActivitySummaryLoadResult.Success(result))
        }

        override fun onFailure(message: String) {
            if (allowRefreshRetry && sessionPort.canRefreshSession() && message.isActivitySummaryAuthExpired()) {
                requestActivitySummaryRefreshAndRetry(sessionPort, gateway, currentTimeMillisProvider, onResult)
                return
            }
            onResult(ActivitySummaryLoadResult.Failure(message.ifBlank { "활동 요약을 확인하지 못했습니다." }))
        }
    })
}

private fun requestActivitySummaryRefreshAndRetry(
    sessionPort: ActivitySummarySessionPort,
    gateway: AuthLoginGateway,
    currentTimeMillisProvider: () -> Long,
    onResult: (ActivitySummaryLoadResult) -> Unit,
) {
    val refreshToken = sessionPort.refreshToken()
    if (refreshToken.isBlank() || sessionPort.isRefreshExpired()) {
        onResult(ActivitySummaryLoadResult.Failure(ACTIVITY_SUMMARY_SESSION_MESSAGE))
        return
    }

    gateway.refresh(refreshToken, object : AuthLoginGateway.Callback {
        override fun onSuccess(result: AuthLoginGateway.LoginResult) {
            sessionPort.saveSession(
                AuthSessionFactory.create(
                    result,
                    sessionPort.displayName(),
                    sessionPort.profileImageUrl(),
                    currentTimeMillisProvider(),
                )
            )
            requestActivitySummary(
                sessionPort = sessionPort,
                gateway = gateway,
                accessToken = result.accessToken,
                allowRefreshRetry = false,
                currentTimeMillisProvider = currentTimeMillisProvider,
                onResult = onResult,
            )
        }

        override fun onFailure(message: String) {
            sessionPort.clear()
            onResult(ActivitySummaryLoadResult.Failure(message.ifBlank { ACTIVITY_SUMMARY_SESSION_MESSAGE }))
        }
    })
}

internal interface ActivitySummarySessionPort {
    fun isSignedIn(): Boolean
    fun accessToken(): String
    fun refreshToken(): String
    fun isRefreshExpired(): Boolean
    fun displayName(): String
    fun profileImageUrl(): String
    fun saveSession(session: AuthSession)
    fun clear()

    fun canRefreshSession(): Boolean = refreshToken().isNotBlank() && !isRefreshExpired()
}

private class AuthSessionStoreActivitySummarySessionPort(
    private val authSessionStore: AuthSessionStore,
) : ActivitySummarySessionPort {
    override fun isSignedIn(): Boolean = authSessionStore.isSignedIn

    override fun accessToken(): String = authSessionStore.accessToken

    override fun refreshToken(): String = authSessionStore.refreshToken

    override fun isRefreshExpired(): Boolean = authSessionStore.isRefreshExpired

    override fun displayName(): String = authSessionStore.displayName

    override fun profileImageUrl(): String = authSessionStore.profileImageUrl

    override fun saveSession(session: AuthSession) {
        authSessionStore.saveSession(session)
    }

    override fun clear() {
        authSessionStore.clear()
    }
}

sealed interface ActivitySummaryLoadResult {
    data class Success(val summary: AuthLoginGateway.ActivitySummaryResult) : ActivitySummaryLoadResult

    data object SignedOut : ActivitySummaryLoadResult

    data class Failure(val message: String) : ActivitySummaryLoadResult
}

internal fun activitySummarySignedOutMessage(): String = ACTIVITY_SUMMARY_SIGNED_OUT_MESSAGE

internal fun activitySummarySessionMessage(): String = ACTIVITY_SUMMARY_SESSION_MESSAGE

private fun String.isActivitySummaryAuthExpired(): Boolean {
    return trim() == ACTIVITY_SUMMARY_AUTH_EXPIRED_MESSAGE
}

private fun formatOneDecimal(value: Double, grouped: Boolean = true): String {
    val pattern = if (grouped) "#,##0.0" else "0.0"
    val formatter = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
    return formatter.format(value)
}

private fun formatWholeNumber(value: Long): String {
    val formatter = DecimalFormat("#,##0", DecimalFormatSymbols(Locale.US))
    return formatter.format(value)
}
