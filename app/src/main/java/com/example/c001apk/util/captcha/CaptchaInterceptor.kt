package com.example.c001apk.util.captcha

import com.example.c001apk.util.CoolapkRequestPolicy
import com.google.gson.JsonParser
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 验证码拦截器：检测 err_request_captcha_v2，弹出人机验证，拿回 proof 后重试。
 * 同时把已存储的 query 模式 proof 注入到 API 请求上。移植自 KiritoXDone/c001apk（AGPL-3.0）。
 */
object CaptchaInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!CoolapkRequestPolicy.isApi(request.url)) return chain.proceed(request)

        var req = request
        val vf = ApiSessions.validationField()
        if (vf != null) {
            req = req.newBuilder()
                .url(req.url.newBuilder().setQueryParameter(vf.first, vf.second).build())
                .build()
        }

        val response = chain.proceed(req)
        if (request.tag(SkipCaptcha::class.java) != null) return response

        val challenge = CaptchaChallenge.from(json(response)) ?: return response
        val proof = CaptchaCoordinator.request(challenge) { !chain.call().isCanceled() } ?: return response

        ApiSessions.acceptValidation(proof, challenge.field)

        val retryBuilder = if (challenge.field.isEmpty()) {
            req.newBuilder()
        } else {
            req.newBuilder()
                .url(req.url.newBuilder().setQueryParameter(challenge.field, proof).build())
        }
        // 标记重试请求，跳过二次验证码处理，避免死循环
        retryBuilder.tag(SkipCaptcha::class.java, SkipCaptcha())
        val verified = chain.proceed(retryBuilder.build())
        if (!verified.isSuccessful) ApiSessions.clearValidation()
        return verified
    }

    private fun json(response: Response): com.google.gson.JsonObject? = runCatching {
        JsonParser.parseString(response.peekBody(1024 * 1024).string()).asJsonObject
    }.getOrNull()
}
