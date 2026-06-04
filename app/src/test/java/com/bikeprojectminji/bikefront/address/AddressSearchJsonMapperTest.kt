package com.bikeprojectminji.bikefront.address

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddressSearchJsonMapperTest {

    @Test
    fun `success wrapper maps address candidates without exposing raw query`() {
        val result = AddressSearchJsonMapper.toUiModel(
            JSONObject(
                """
                {
                  "code": 200,
                  "message": "success",
                  "data": {
                    "status": "AMBIGUOUS",
                    "page": 1,
                    "size": 3,
                    "totalCount": 2,
                    "provider": "FAKE",
                    "message": "주소 후보가 여러 개입니다.",
                    "candidates": [
                      {
                        "candidateId": "fake-1",
                        "label": "북악스카이웨이 팔각정",
                        "address": "서울 종로구 북악산로 267",
                        "lat": 37.6026,
                        "lon": 126.9803,
                        "source": "FAKE",
                        "type": "PLACE",
                        "confidence": "HIGH"
                      }
                    ]
                  }
                }
                """.trimIndent()
            )
        )

        assertEquals(AddressSearchStatusUi.Ambiguous, result.status)
        assertEquals(2, result.totalCount)
        assertEquals("FAKE", result.provider)
        assertEquals(1, result.candidates.size)
        assertEquals("북악스카이웨이 팔각정", result.candidates[0].label)
        assertEquals(37.6026, result.candidates[0].lat, 0.0)
        assertEquals(126.9803, result.candidates[0].lon, 0.0)
    }

    @Test
    fun `empty response maps to explicit empty state`() {
        val result = AddressSearchJsonMapper.toUiModel(
            JSONObject(
                """
                {
                  "data": {
                    "status": "EMPTY",
                    "page": 1,
                    "size": 3,
                    "totalCount": 0,
                    "provider": "FAKE",
                    "message": "검색 결과가 없습니다.",
                    "candidates": []
                  }
                }
                """.trimIndent()
            )
        )

        assertEquals(AddressSearchStatusUi.Empty, result.status)
        assertTrue(result.candidates.isEmpty())
        assertEquals("검색 결과가 없습니다.", result.message)
    }

    @Test
    fun `provider failure maps to retryable failure state`() {
        val result = AddressSearchJsonMapper.toUiModel(
            JSONObject(
                """
                {
                  "data": {
                    "status": "PROVIDER_FAILURE",
                    "page": 1,
                    "size": 3,
                    "totalCount": 0,
                    "provider": "FAKE",
                    "message": "주소 provider 확인 실패",
                    "candidates": []
                  }
                }
                """.trimIndent()
            )
        )

        assertEquals(AddressSearchStatusUi.ProviderFailure, result.status)
        assertEquals("주소 provider 확인 실패", result.message)
        assertTrue(result.isFailure)
    }
}

