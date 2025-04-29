package boilerplate.utils.extension

import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import boilerplate.R
import boilerplate.constant.Constants.KEY_AUTH
import boilerplate.utils.ImageUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.Headers
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions

fun ImageView.loadAvatar(url: String? = "") {
	loadImage(
		url, requestOptions = RequestOptions()
			.override(ImageUtil.AVATAR_MAX_SIZE)
			.placeholder(R.drawable.ic_avatar)
			.error(R.drawable.ic_avatar)
			.circleCrop()
	)
}

fun ImageView.loadGif(
	url: String? = "",
	accessToken: String? = "",
	requestOptions: RequestOptions = RequestOptions().error(R.drawable.bg_error)
) {
	val context = context
	val glideUrl = GlideUrl(url, accessToken?.let {
		LazyHeaders.Builder()
			.addHeader("KEY_AUTH", it)
			.build()
	} ?: Headers.DEFAULT)
	Glide
		.with(context)
		.asGif()
		.load(glideUrl)
		.apply(requestOptions)
		.into(this)
}

fun ImageView.loadDrawable(
	@DrawableRes resId: Int,
	requestOptions: RequestOptions = RequestOptions().error(R.drawable.bg_error)
) {
	Glide.with(context)
		.asDrawable()
		.apply(requestOptions)
		.load(ResourcesCompat.getDrawable(resources, resId, null))
		.into(this)
}

fun ImageView.loadImage(
	url: String? = "",
	accessToken: String? = "",
	requestOptions: RequestOptions = RequestOptions().error(R.drawable.bg_error)
) {
	if (url.isNullOrEmpty()) {
		return
	}
	val glideUrl = GlideUrl(url, accessToken?.let {
		LazyHeaders.Builder()
			.addHeader(KEY_AUTH, it)
			.build()
	} ?: Headers.DEFAULT)
	Glide
		.with(context)
		.asDrawable()
		.load(glideUrl)
		.apply(requestOptions)
		.into(this)
}