package com.example.c001apk.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.constant.Constants
import com.example.c001apk.logic.repository.NetworkRepo
import com.example.c001apk.util.CookieUtil
import com.example.c001apk.util.Event
import com.example.c001apk.util.PrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val networkRepo: NetworkRepo
) : ViewModel() {

    var lastCheck = System.currentTimeMillis()
    var isInit: Boolean = true
    val setBadge = MutableLiveData<Event<Boolean>>()

    fun fetchAppInfo(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getAppInfo(id)
                .collect { result ->
                    val appInfo = result.getOrNull()
                    if (appInfo?.data != null) {
                        try {
                            syncVersionParams(
                                appInfo.data.apkversionname,
                                appInfo.data.apkversioncode
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    getCheckLoginInfo()
                }
        }
    }

    /**
     * 将服务端返回的酷安版本号同步到请求参数。
     * 仅在返回版本不低于内置版本（Constants.VERSION_NAME）时才应用，
     * 防止旧数据或异常响应把客户端版本回退到服务端白名单以下，导致
     * 「评论接口异常，请尝试升级到新版酷安」。
     */
    private fun syncVersionParams(name: String?, code: String?) {
        if (name.isNullOrEmpty() || code.isNullOrEmpty()) return
        if (!versionAtLeast(name, Constants.VERSION_NAME)) return
        PrefManager.VERSION_NAME = name
        PrefManager.VERSION_CODE = code
        // API_VERSION 取 VERSION_NAME 的首段，与 CoolapkReviver 的改写表一致
        PrefManager.API_VERSION = name.substringBefore(".")
        PrefManager.USER_AGENT =
            "Dalvik/2.1.0 (Linux; U; Android ${PrefManager.ANDROID_VERSION}; ${PrefManager.MODEL} ${PrefManager.BUILDNUMBER}) (#Build; ${PrefManager.BRAND}; ${PrefManager.MODEL}; ${PrefManager.BUILDNUMBER}; ${PrefManager.ANDROID_VERSION}) +CoolMarket/$name-$code-${Constants.MODE}"
    }

    private fun versionAtLeast(candidate: String, floor: String): Boolean {
        fun parse(version: String): List<Int> =
            version.split(".").map { it.filter { c -> c.isDigit() }.toIntOrNull() ?: 0 }
        val a = parse(candidate)
        val b = parse(floor)
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return true
    }

    private fun getCheckLoginInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.checkLoginInfo()
                .collect { result ->
                    val response = result.getOrNull()
                    response?.let {
                        response.body()?.let {
                            if (response.body()?.data?.token != null) {
                                response.body()?.data?.let { login ->
                                    CookieUtil.badge = login.notifyCount.badge
                                    CookieUtil.atme = login.notifyCount.atme
                                    CookieUtil.atcommentme = login.notifyCount.atcommentme
                                    CookieUtil.feedlike = login.notifyCount.feedlike
                                    CookieUtil.contacts_follow = login.notifyCount.contactsFollow
                                    PrefManager.isLogin = true
                                    PrefManager.uid = login.uid
                                    PrefManager.username =
                                        withContext(Dispatchers.IO) {
                                            URLEncoder.encode(login.username, "UTF-8")
                                        }
                                    PrefManager.token = login.token
                                    PrefManager.userAvatar = login.userAvatar
                                }
                            } else if (response.body()?.message == "登录信息有误") {
                                PrefManager.isLogin = false
                                PrefManager.uid = ""
                                PrefManager.username = ""
                                PrefManager.token = ""
                                PrefManager.userAvatar = ""
                            }

                            try {
                                val headers = response.headers()
                                val cookies = headers.values("Set-Cookie")
                                val session = cookies[0]
                                val sessionID = session.substring(0, session.indexOf(";"))
                                CookieUtil.SESSID = sessionID
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            if (CookieUtil.badge != 0)
                                setBadge.postValue(Event(true))

                        }
                    }
                }
        }
    }

    fun onCheckCount() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.checkCount()
                .collect { result ->
                    val response = result.getOrNull()
                    response?.data?.let {
                        CookieUtil.atme = it.atme
                        CookieUtil.atcommentme = it.atcommentme
                        CookieUtil.feedlike = it.feedlike
                        CookieUtil.contacts_follow = it.contactsFollow
                        CookieUtil.badge = it.badge
                        CookieUtil.notification = it.notification
                        if (CookieUtil.badge != 0)
                            setBadge.postValue(Event(true))
                    }
                }
        }
    }

}