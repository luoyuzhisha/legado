package io.legado.app.help.http

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.net.ssl.SSLException

object OkHttpExceptionInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            return chain.proceed(chain.request())
        } catch (e: SSLException) {
            throw TlsCompat.wrap(e)
        } catch (e: IOException) {
            throw e
        } catch (e: Throwable) {
            throw IOException(e)
        }
    }

}
