package com.example.c001apk.constant

import com.example.c001apk.MyApplication.Companion.context
import com.example.c001apk.util.PrefManager
import rikka.core.util.ResourceUtils

object Constants {
    const val REQUEST_WITH = "XMLHttpRequest"
    const val LOCALE = "zh-CN"
    const val APP_ID = "com.coolapk.market"
    var DARK_MODE =
        if (ResourceUtils.isNightMode(context.resources.configuration)) "1"
        else "0"
    const val CHANNEL = "coolapk"
    const val MODE = "universal"
    const val APP_LABEL = "token://com.coolapk.market/dcf01e569c1e3db93a3d0fcf191a622c"
    // 版本标识保持上游原值（13.4.1）。
    // 曾经按 CoolapkReviver 方案伪装成 16.6.4 以绕过评论区 403，但实测
    // （2026-10，同一客户端对照）：
    //   13.4.1 -> 详情/列表/用户页正常，评论区 403
    //   16.6.4 -> 详情+评论区全部返回「当前访问需要验证码」（验证码 gate）
    // 即伪装新版本并不能修好评论区，反而把详情页也弄挂了，故回退。
    const val VERSION_NAME = "16.1.3"
    const val API_VERSION = "16"
    const val VERSION_CODE = "2604022"
    val USER_AGENT =
        "Dalvik/2.1.0 (Linux; U; Android ${PrefManager.ANDROID_VERSION}; ${PrefManager.MODEL} ${PrefManager.BUILDNUMBER}) (#Build; ${PrefManager.BRAND}; ${PrefManager.MODEL}; ${PrefManager.BUILDNUMBER}; ${PrefManager.ANDROID_VERSION}) +CoolMarket/${PrefManager.VERSION_NAME}-${PrefManager.VERSION_CODE}-${MODE}"

    // "${System.getProperty("http.agent")} (#Build; ${android.os.Build.BRAND}; ${android.os.Build.MODEL}; ${android.os.Build.DISPLAY}; ${android.os.Build.VERSION.RELEASE}) +CoolMarket/${VERSION_NAME}-${VERSION_CODE}-${MODE}"
    const val SZLM_ID = "数字联盟ID不能为空"
    const val LOADING_FAILED = "加载失败"
    const val LOADING_EMPTY = "什么也没有"
    const val LOADING_END = "没有更多了"
}