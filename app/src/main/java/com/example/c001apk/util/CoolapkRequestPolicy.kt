package com.example.c001apk.util

import okhttp3.HttpUrl
import okhttp3.Request

/**
 * 判断请求是否走酷安 API，以及公共动态页处理。移植自 KiritoXDone/c001apk。
 */
object CoolapkRequestPolicy {

    fun isApi(url: HttpUrl): Boolean = url.host.endsWith("coolapk.com")

    fun isPublicFeed(url: HttpUrl): Boolean = false

    fun withoutPrivateHeaders(request: Request): Request.Builder = request.newBuilder()

    fun publicRequest(request: Request): Request = request
}
