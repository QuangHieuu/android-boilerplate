package boilerplate.widget.loading

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import boilerplate.databinding.ViewLoadingBinding

class LoadingScreen(context: Context) : FrameLayout(context) {
	private var _binding: ViewLoadingBinding? = null

	init {
		_binding = ViewLoadingBinding.inflate(
			LayoutInflater.from(context),
			this,
			true
		)
	}

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		_binding?.pulseView?.start()
	}

	override fun onDetachedFromWindow() {
		_binding?.pulseView?.stop()
		super.onDetachedFromWindow()
	}
}
