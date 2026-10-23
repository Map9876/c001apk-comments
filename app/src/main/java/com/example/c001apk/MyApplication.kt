package com.example.c001apk

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import com.example.c001apk.constant.Constants
import com.example.c001apk.ui.others.BugHandlerActivity
import com.example.c001apk.util.PrefManager
import dagger.hilt.android.HiltAndroidApp
import net.mikaelzero.mojito.Mojito
import net.mikaelzero.mojito.loader.glide.GlideImageLoader
import net.mikaelzero.mojito.view.sketch.SketchImageLoadFactory
import kotlin.system.exitProcess

@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        context = applicationContext

        migrateVersionParams()

        AppCompatDelegate.setDefaultNightMode(PrefManager.darkTheme)

        Mojito.initialize(
            GlideImageLoader.with(this),
            SketchImageLoadFactory()
        )

        Thread.setDefaultUncaughtExceptionHandler { _, paramThrowable ->
            val exceptionMessage = android.util.Log.getStackTraceString(paramThrowable)

            val intent = Intent(this, BugHandlerActivity::class.java)
            intent.putExtra("exception_message", exceptionMessage)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)

            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(10)
        }
    }

    /**
     * 一次性迁移：旧版本客户端把 13.4.1 / 2312121 / api 13 这套版本参数写进了
     * SharedPreferences，升级后会继续以旧版本请求服务端，评论区等 v6 接口仍会
     * 返回 403。若检测到用户没有手动改过参数，则刷新为内置的修复后版本
     * （方案来自 CoolapkReviver，见 Constants 中的说明）。
     */
    private fun migrateVersionParams() {
        val pref = getSharedPreferences("settings", MODE_PRIVATE)
        if (pref.getBoolean("version_params_migrated", false)) return
        val name = pref.getString("VERSION_NAME", null)
        val code = pref.getString("VERSION_CODE", null)
        val api = pref.getString("API_VERSION", null)
        val stale = name == "13.4.1" && code == "2312121" && api == "13"
        pref.edit().apply {
            if (stale) {
                putString("VERSION_NAME", Constants.VERSION_NAME)
                putString("VERSION_CODE", Constants.VERSION_CODE)
                putString("API_VERSION", Constants.API_VERSION)
                // USER_AGENT 里嵌有旧版本号，用新版本号重建，格式同 TokenDeviceUtils
                val androidVersion = pref.getString("ANDROID_VERSION", "") ?: ""
                val brand = pref.getString("BRAND", "") ?: ""
                val model = pref.getString("MODEL", "") ?: ""
                val buildNumber = pref.getString("BUILDNUMBER", "") ?: ""
                putString(
                    "USER_AGENT",
                    "Dalvik/2.1.0 (Linux; U; Android ${androidVersion}; ${model} ${buildNumber}) " +
                        "(#Build; ${brand}; ${model}; ${buildNumber}; ${androidVersion}) " +
                        "+CoolMarket/${Constants.VERSION_NAME}-${Constants.VERSION_CODE}-${Constants.MODE}"
                )
            }
            putBoolean("version_params_migrated", true)
            apply()
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

}