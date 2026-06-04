package com.bikeprojectminji.bikefront.auth;

import org.json.JSONException;
import org.json.JSONObject;

public final class KakaoLoginRequestBodyFactory {

    private KakaoLoginRequestBodyFactory() {
    }

    public static JSONObject create(String kakaoAccessToken) {
        try {
            JSONObject requestJson = new JSONObject();
            requestJson.put("kakaoAccessToken", kakaoAccessToken);
            requestJson.put("privacyPolicyVersion", KakaoLoginPolicyVersions.PRIVACY_POLICY_VERSION);
            requestJson.put("termsVersion", KakaoLoginPolicyVersions.TERMS_VERSION);
            requestJson.put("locationTermsVersion", KakaoLoginPolicyVersions.LOCATION_TERMS_VERSION);
            return requestJson;
        } catch (JSONException exception) {
            throw new IllegalStateException("카카오 로그인 요청을 만들 수 없습니다.", exception);
        }
    }
}
