package chat.rocket.android.util

import chat.rocket.android.log
import chat.rocket.android.server.domain.GetBasicAuthInteractor
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.SaveBasicAuthInteractor
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.domain.model.BasicAuth
import chat.rocket.common.model.Token
import okhttp3.Credentials
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

/**
 * An OkHttp interceptor which adds Authorization header based on URI userInfo
 * part. Can be applied as an
 * [application interceptor][OkHttpClient.interceptors]
 * or as a [ ][OkHttpClient.networkInterceptors].
 */
class BasicAuthenticatorInterceptor @Inject constructor(
    private val getBasicAuthInteractor: GetBasicAuthInteractor,
    private val saveBasicAuthInteractor: SaveBasicAuthInteractor,
    getCurrentServer: GetCurrentServerInteractor,
    getCurrentToken: TokenRepository
) : Interceptor {
    private val credentials = HashMap<String, String>()
    private var tokens: Pair<String, Token>? = null

    init {
        val basicAuths = getBasicAuthInteractor.getAll()
        for (basicAuth in basicAuths) {
            credentials[basicAuth.host] = basicAuth.credentials
        }
        getCurrentServer.get()?.let { server ->
            HttpUrl.parse(server)?.host()?.let { host ->
                getCurrentToken.get(server)?.let { token ->
                    log { "Server $host token $token" }
                    tokens = host to token
                }
            }
        }
    }

    private fun saveCredentials(host: String, basicCredentials: String) {
        saveBasicAuthInteractor.save(
            BasicAuth(
                host,
                basicCredentials
            )
        )
        credentials[host] = basicCredentials
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val url = request.url()
        val host = url.host()
        val username = url.username()

        if (!username.isNullOrEmpty()) {
            saveCredentials(host, Credentials.basic(username, url.password()))
            request = request.newBuilder().url(
                url.newBuilder().username("").password("").build()
            ).build()
        }

        credentials[host]?.let {
            request = request.newBuilder().header("Authorization", it).build()
        }

        tokens?.let { t ->
            if (host == t.first) {
                val newUrl = url.newBuilder()
                    .addQueryParameter("rc_uid", t.second.userId)
                    .addQueryParameter("rc_token", t.second.authToken)
                    .build()
                request = request.newBuilder().url(newUrl).build()
            }
        }

        return chain.proceed(request)
    }
}
