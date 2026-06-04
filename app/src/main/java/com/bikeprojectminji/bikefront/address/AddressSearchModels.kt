package com.bikeprojectminji.bikefront.address

import org.json.JSONObject

data class AddressSearchUiModel(
    val status: AddressSearchStatusUi,
    val page: Int,
    val size: Int,
    val totalCount: Int,
    val provider: String,
    val message: String,
    val candidates: List<AddressCandidateUiModel>,
) {
    val isFailure: Boolean
        get() = status == AddressSearchStatusUi.ProviderFailure || status == AddressSearchStatusUi.RateLimited
}

data class AddressCandidateUiModel(
    val candidateId: String,
    val label: String,
    val address: String,
    val lat: Double,
    val lon: Double,
    val source: String,
    val type: String,
    val confidence: String,
)

enum class AddressSearchStatusUi(val displayText: String) {
    Success("검색 완료"),
    Ambiguous("후보 선택"),
    Empty("결과 없음"),
    RateLimited("잠시 후 재시도"),
    ProviderFailure("검색 실패"),
}

object AddressSearchJsonMapper {

    fun toUiModel(payload: JSONObject): AddressSearchUiModel {
        val data = payload.optJSONObject("data") ?: payload
        val candidatesJson = data.optJSONArray("candidates")
        return AddressSearchUiModel(
            status = mapStatus(data.optString("status", "PROVIDER_FAILURE")),
            page = data.optInt("page", 1),
            size = data.optInt("size", 3),
            totalCount = data.optInt("totalCount", 0),
            provider = data.optString("provider", ""),
            message = data.optString("message", ""),
            candidates = buildList {
                if (candidatesJson != null) {
                    for (index in 0 until candidatesJson.length()) {
                        val item = candidatesJson.optJSONObject(index) ?: continue
                        add(
                            AddressCandidateUiModel(
                                candidateId = item.optString("candidateId", ""),
                                label = item.optString("label", "주소 후보"),
                                address = item.optString("address", ""),
                                lat = item.optDouble("lat", 0.0),
                                lon = item.optDouble("lon", 0.0),
                                source = item.optString("source", ""),
                                type = item.optString("type", ""),
                                confidence = item.optString("confidence", ""),
                            ),
                        )
                    }
                }
            },
        )
    }

    private fun mapStatus(value: String): AddressSearchStatusUi {
        return when (value.uppercase()) {
            "SUCCESS" -> AddressSearchStatusUi.Success
            "AMBIGUOUS" -> AddressSearchStatusUi.Ambiguous
            "EMPTY" -> AddressSearchStatusUi.Empty
            "RATE_LIMITED" -> AddressSearchStatusUi.RateLimited
            else -> AddressSearchStatusUi.ProviderFailure
        }
    }
}

