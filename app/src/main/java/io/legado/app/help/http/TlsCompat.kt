package io.legado.app.help.http

import java.io.IOException
import javax.net.ssl.SSLException

/**
 * Android 15 对 targetSdk 35 的应用禁用了 TLS 1.0/1.1，系统层面无法恢复。
 * 这里只负责把难懂的握手错误转换成用户能理解的提示，避免被误判为书源失效。
 */
object TlsCompat {

    private val legacyTlsMarkers = arrayOf(
        "TLSv1",
        "TLSv1.1",
        "TLS 1.0",
        "TLS 1.1",
        "tls1"
    )

    fun wrap(e: SSLException): IOException {
        if (!isLegacyTlsError(e)) {
            return e
        }
        return IOException(
            "该书源站点仅支持 TLS 1.0/1.1，Android 15 已禁用该协议。" +
                    "请让站点升级到 TLS 1.2 或更高版本，或改用 HTTP 访问。",
            e
        )
    }

    private fun isLegacyTlsError(e: Throwable): Boolean {
        var cause: Throwable? = e
        while (cause != null) {
            val message = cause.message
            if (message != null && legacyTlsMarkers.any { message.contains(it, true) }) {
                return true
            }
            cause = cause.cause
        }
        return false
    }
}
