package com.example.c001apk.util.captcha

import com.example.c001apk.util.PrefManager
import java.security.MessageDigest

/**
 * 验证码校验凭证(validate)的会话级存储，移植自 KiritoXDone/c001apk（AGPL-3.0）。
 * - field 为空：proof 作为 validate Cookie 复用；
 * - field 非空（如 _v2_post_token）：proof 作为同名查询参数复用。
 */
object ApiSessions {

    private data class Validation(
        val identity: String,
        val proof: String,
        val field: String,
        val expires: Long,
    )

    @Volatile private var state: Validation? = null

    private fun identity(): String {
        val gen = if (PrefManager.isLogin) 1L else 0L
        val hash = digest(PrefManager.xAppDevice + PrefManager.isLogin + PrefManager.uid)
        return gen.toString() + ":" + hash
    }

    private fun digest(v: String): String =
        MessageDigest.getInstance("SHA-256").digest(v.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it.toInt() and 255) }

    /** cookie 模式：field 为空时返回 proof */
    fun validation(): String? {
        val now = System.currentTimeMillis()
        val s = state
        return s?.takeIf { it.field.isEmpty() && it.identity == identity() && it.expires > now }?.proof
    }

    /** query 模式：field 非空时返回 (field, proof) */
    fun validationField(): Pair<String, String>? {
        val now = System.currentTimeMillis()
        val s = state
        return s?.takeIf { it.field.isNotEmpty() && it.identity == identity() && it.expires > now }
            ?.let { Pair(it.field, it.proof) }
    }

    fun acceptValidation(proof: String, field: String) {
        state = Validation(identity(), proof, field, System.currentTimeMillis() + 86400000L)
    }

    fun clearValidation() {
        state = null
    }
}
