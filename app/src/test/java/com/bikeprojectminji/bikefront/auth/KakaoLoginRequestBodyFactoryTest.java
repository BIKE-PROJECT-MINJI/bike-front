package com.bikeprojectminji.bikefront.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.json.JSONObject;
import org.junit.Test;

public class KakaoLoginRequestBodyFactoryTest {

    @Test
    public void factoryIncludesKakaoTokenAndRequiredPolicyVersions() throws Exception {
        JSONObject json = KakaoLoginRequestBodyFactory.create("kakao-access-token");

        assertEquals("kakao-access-token", json.getString("kakaoAccessToken"));
        assertEquals("privacy-2026-05-24", json.getString("privacyPolicyVersion"));
        assertEquals("terms-2026-05-24", json.getString("termsVersion"));
        assertEquals("location-2026-05-24", json.getString("locationTermsVersion"));
    }

    @Test
    public void factoryDoesNotIncludeEmailPasswordOrProviderSecretFields() {
        JSONObject json = KakaoLoginRequestBodyFactory.create("kakao-access-token");
        String raw = json.toString();

        assertFalse(raw.contains("password"));
        assertFalse(raw.contains("clientSecret"));
        assertFalse(raw.contains("REST_API_KEY"));
    }
}
