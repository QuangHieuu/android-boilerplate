package boilerplate.widget.customtext

import android.content.Context
import android.os.Build
import android.text.InputType
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.ViewCompat
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import boilerplate.R

class AppEditText @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	//if error Re-sync project to generating R.class
	defStyle: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyle) {

	abstract class SimpleEvent : KeyImeChange, TextMenuListener {

		override fun onKeyIme(keyCode: Int, event: KeyEvent?) {}
		override fun onPaste() {}
	}

	private interface KeyImeChange {

		fun onKeyIme(keyCode: Int, event: KeyEvent?)
	}

	private interface TextMenuListener {

		fun onPaste()
	}

	private var _listener: SimpleEvent? = null

	init {
		init(context, attrs, defStyle)
	}

	override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
		_listener?.onKeyIme(keyCode, event)
		return false
	}

	fun setListener(listener: SimpleEvent?) {
		_listener = listener
	}

	private fun init(context: Context, attrs: AttributeSet?, defStyle: Int) {
		highlightColor = ContextCompat.getColor(getContext(), R.color.colorHighlight)

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			textCursorDrawable =
				ResourcesCompat.getDrawable(resources, R.drawable.bg_cursor, null)
		}

		context.withStyledAttributes(attrs, R.styleable.AppEditText, defStyle, 0) {
			val typefaceAssetPath: Int =
				getResourceId(R.styleable.AppEditText_customEditTypeface, R.font.roboto_regular)
			val typeface = ResourcesCompat.getFont(getContext(), typefaceAssetPath)
			setTypeface(typeface)
		}
	}

	override fun setInputType(type: Int) {
		super.setInputType(type or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
	}

	override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
		var ic = super.onCreateInputConnection(outAttrs)
		EditorInfoCompat.setContentMimeTypes(outAttrs, KeyboardReceiver.MIME_TYPES)
		val mimeTypes = ViewCompat.getOnReceiveContentMimeTypes(this)
		if (mimeTypes != null) {
			EditorInfoCompat.setContentMimeTypes(outAttrs, mimeTypes)
			ic = InputConnectionCompat.createWrapper(this, ic!!, outAttrs)
		}
		return ic
	}

	override fun onTextContextMenuItem(id: Int): Boolean {
		val consumed: Boolean = super.onTextContextMenuItem(id)
		if (_listener == null) {
			return consumed
		}
		when (id) {
			android.R.id.paste -> _listener!!.onPaste()
		}
		return consumed
	}
}