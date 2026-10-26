package com.example.c001apk.ui.captcha

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.c001apk.util.captcha.CaptchaCoordinator

/**
 * 人机验证 Activity（View 版），移植自 KiritoXDone/c001apk（AGPL-3.0）。
 * 加载 NetEase(NEC) 验证码，解出 validate 后通过 CaptchaCoordinator 回传。
 */
class CaptchaActivity : AppCompatActivity() {

    private var requestId: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestId = intent.getStringExtra("request")
        val pending = CaptchaCoordinator.pending(requestId)
        if (pending == null) {
            finish()
            return
        }
        pending.result.whenComplete { _, _ -> runOnUiThread { if (!isDestroyed) finish() } }

        val webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.javaScriptCanOpenWindowsAutomatically = false
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: android.webkit.WebResourceRequest?,
                ) = request?.isForMainFrame == true
            }
            addJavascriptInterface(object {
                @JavascriptInterface
                fun onValidate(validate: String?) {
                    val proof = pending.challenge.proof(validate ?: "")
                    if (proof != null) CaptchaCoordinator.complete(pending.id, proof)
                    runOnUiThread { if (!isDestroyed) finish() }
                }
            }, "AndroidBridge")
            loadDataWithBaseURL(
                "https://captcha.c001apk.invalid/",
                html(pending.challenge.id),
                "text/html",
                "UTF-8",
                null,
            )
        }
        setContentView(webView)
    }

    override fun onDestroy() {
        if (!isChangingConfigurations) requestId?.let { CaptchaCoordinator.complete(it, null) }
        super.onDestroy()
    }

    private fun html(id: String) = """
        <!doctype html><html lang="zh-CN"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
        <style>:root{color-scheme:light dark}body{font:16px sans-serif;margin:24px;background:Canvas;color:CanvasText}p{line-height:1.6}</style>
        <p id="status">正在加载验证组件…</p><div id="captcha"></div><script>
        function statusText(t){document.getElementById('status').textContent=t;}
        var timeout=setTimeout(function(){statusText('加载超时，请点击重新加载');},15000);
        var script=document.createElement('script');script.src='https://cstaticdun.126.net/load.min.js';
        script.onerror=function(){clearTimeout(timeout);statusText('加载失败，请检查网络后重试');};
        script.onload=function(){try{initNECaptcha({captchaId:'$id',element:'#captcha',protocol:'https',mode:'embed',apiVersion:2,
        width:Math.min(320,window.innerWidth-48)+'px',lang:'zh-CN',onVerify:function(err,data){
        if(!err&&data&&data.validate){AndroidBridge.onValidate(data.validate);}else{statusText('验证未通过，请重试');}}},
        function(){clearTimeout(timeout);statusText('请按下方提示完成人机验证');},function(){clearTimeout(timeout);statusText('初始化失败，请重新加载');});
        }catch(e){clearTimeout(timeout);statusText('初始化失败，请重新加载');}};document.head.appendChild(script);
        </script></html>
    """.trimIndent()
}
