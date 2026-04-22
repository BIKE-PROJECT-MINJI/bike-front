package com.bikeprojectminji.bikefront.free

internal sealed interface FreeRideSaveFailureUiState {
    data object None : FreeRideSaveFailureUiState
    data class ShortRide(val message: String) : FreeRideSaveFailureUiState
    data class Generic(val message: String) : FreeRideSaveFailureUiState
}

internal object FreeRideSaveFailureResolver {
    private const val SHORT_RIDE_MESSAGE = "주행 시작 후 10초 미만 기록은 저장되지 않습니다."

    fun resolve(message: String?, fallbackMessage: String): FreeRideSaveFailureUiState {
        val normalizedMessage = message?.trim().takeUnless { it.isNullOrBlank() } ?: fallbackMessage
        return if (normalizedMessage == SHORT_RIDE_MESSAGE) {
            FreeRideSaveFailureUiState.ShortRide(normalizedMessage)
        } else {
            FreeRideSaveFailureUiState.Generic(normalizedMessage)
        }
    }
}
