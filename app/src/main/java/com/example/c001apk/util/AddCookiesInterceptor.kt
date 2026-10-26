package com.example.c001apk.util

import com.example.c001apk.constant.Constants.APP_ID
import com.example.c001apk.constant.Constants.CHANNEL
import com.example.c001apk.constant.Constants.DARK_MODE
import com.example.c001apk.constant.Constants.LOCALE
import com.example.c001apk.constant.Constants.MODE
import com.example.c001apk.constant.Constants.REQUEST_WITH
import com.example.c001apk.util.CoolapkRequestPolicy.isApi
import com.example.c001apk.util.captcha.ApiSessions
import com.example.c001apk.util.captcha.SkipCaptcha
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

object AddCookiesInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.tag(SkipCaptcha::class.java) != null) return chain.proceed(request)

        val builder: Request.Builder = request.newBuilder()
        val deviceCode = TokenDeviceUtils.getLastingDeviceCode()
        val token = TokenV3.create(deviceCode, PrefManager.VERSION_CODE.toInt())
        builder.apply {
            addHeader("User-Agent", PrefManager.USER_AGENT)
            addHeader("X-Requested-With", REQUEST_WITH)
            addHeader("X-Sdk-Int", PrefManager.SDK_INT)
            addHeader("X-Sdk-Locale", LOCALE)
            addHeader("X-App-Id", APP_ID)
            addHeader("X-App-Token", token)
            addHeader("X-App-Version", PrefManager.VERSION_NAME)
            addHeader("X-App-Code", PrefManager.VERSION_CODE)
            addHeader("X-Api-Version", PrefManager.API_VERSION)
            addHeader("X-App-Device", deviceCode)
            addHeader("X-Dark-Mode", DARK_MODE)
            addHeader("X-App-Channel", CHANNEL)
            addHeader("X-App-Mode", MODE)
            addHeader("Content-Type", "application/x-www-form-urlencoded")
            val parts = ArrayList<String>()
            if (PrefManager.isLogin)
                parts.add("uid=" + PrefManager.uid + "; username=" + PrefManager.username + "; token=" + PrefManager.token)
            else if (CookieUtil.SESSID.isNotEmpty()) parts.add(CookieUtil.SESSID)
            val validate = if (isApi(request.url)) ApiSessions.validation() else null
            if (validate != null) parts.add("validate=" + validate)
            if (parts.isNotEmpty()) addHeader("Cookie", parts.joinToString("; "))
        }
        return chain.proceed(builder.build())
    }
}
