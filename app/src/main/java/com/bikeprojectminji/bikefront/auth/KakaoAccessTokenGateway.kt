package com.bikeprojectminji.bikefront.auth

import android.app.Activity
import com.kakao.sdk.user.UserApiClient

interface KakaoAccessTokenGateway {
    fun requestAccessToken(activity: Activity, callback: Callback)

    interface Callback {
        fun onSuccess(accessToken: String)
        fun onFailure(message: String)
    }
}

class KakaoSdkAccessTokenGateway : KakaoAccessTokenGateway {
    override fun requestAccessToken(activity: Activity, callback: KakaoAccessTokenGateway.Callback) {
        val loginCallback: (com.kakao.sdk.auth.model.OAuthToken?, Throwable?) -> Unit = { token, error ->
            when {
                token != null -> callback.onSuccess(token.accessToken)
                error != null -> callback.onFailure("카카오 로그인을 완료하지 못했습니다. 다시 시도해 주세요.")
                else -> callback.onFailure("카카오 로그인 결과를 확인하지 못했습니다.")
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
            UserApiClient.instance.loginWithKakaoTalk(activity, callback = loginCallback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(activity, callback = loginCallback)
        }
    }
}
