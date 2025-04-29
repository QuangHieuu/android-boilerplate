package boilerplate.glide

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import boilerplate.constant.Constants.ANIMATION_DELAY
import boilerplate.utils.ImageUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import okhttp3.OkHttpClient
import java.io.InputStream
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@GlideModule
class GlideAppModule : AppGlideModule() {
	override fun applyOptions(context: Context, builder: GlideBuilder) {
		with(builder) {
			setLogLevel(Log.ERROR)
			setDefaultRequestOptions(
				RequestOptions()
					.override(ImageUtil.IMAGE_MAX_SIZE)
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.optionalFitCenter()
					.dontAnimate()
			)
			setDefaultTransitionOptions(
				Drawable::class.java,
				DrawableTransitionOptions.withCrossFade(ANIMATION_DELAY.toInt())
			)
		}
	}

	override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
		val client: OkHttpClient = getUnsafeOkHttpClient()
		registry.replace(
			GlideUrl::class.java,
			InputStream::class.java, OkHttpUrlLoader.Factory(client)
		)
	}

	@SuppressLint("CustomX509TrustManager", "TrustAllX509TrustManager")
	private fun getUnsafeOkHttpClient(): OkHttpClient {
		try {
			// Create a trust manager that does not validate certificate chains
			val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
				override fun checkClientTrusted(
					chain: Array<X509Certificate>,
					authType: String
				) {
				}

				override fun checkServerTrusted(
					chain: Array<X509Certificate>,
					authType: String
				) {
				}

				override fun getAcceptedIssuers(): Array<X509Certificate> {
					return arrayOf()
				}
			})
			val sslContext = SSLContext.getInstance("SSL")
				.apply { init(null, trustAllCerts, SecureRandom()) }
			return OkHttpClient.Builder()
				.apply {
					sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
					hostnameVerifier { _, _ -> true }
				}.build()
		} catch (e: Exception) {
			throw RuntimeException(e)
		}
	}
}