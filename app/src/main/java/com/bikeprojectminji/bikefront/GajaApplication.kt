package com.bikeprojectminji.bikefront

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GajaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val nativeAppKey = getString(R.string.kakao_native_app_key)
        if (nativeAppKey.isNotBlank()) {
            KakaoSdk.init(this, nativeAppKey)
        }
    }
}
