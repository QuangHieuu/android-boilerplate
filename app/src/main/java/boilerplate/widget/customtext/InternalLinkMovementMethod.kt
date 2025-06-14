package boilerplate.widget.customtext

import android.graphics.RectF
import android.text.Spannable
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.MotionEvent
import android.widget.TextView

open class InternalLinkMovementMethod : LinkMovementMethod() {

	private var clickableSpanUnderTouchOnActionDown: ClickableSpan? = null
	private var activeTextViewHashcode = 0
	private val mTouchedLineBounds = RectF()
	private var mListener: OnLinkListener? = null

	interface OnLinkListener {

		fun onLinkClicked(textView: TextView, link: String): Boolean
	}

	override fun onTouchEvent(textView: TextView, text: Spannable, event: MotionEvent): Boolean {
		if (activeTextViewHashcode != textView.hashCode()) {
			activeTextViewHashcode = textView.hashCode()
			textView.autoLinkMask = 0
		}
		val clickableSpanUnderTouch = getLinkText(textView, text, event)
		if (event.action == MotionEvent.ACTION_DOWN) {
			clickableSpanUnderTouchOnActionDown = clickableSpanUnderTouch
		}
		val touchStartedOverAClickableSpan = clickableSpanUnderTouchOnActionDown != null
		return when (event.action) {
			MotionEvent.ACTION_DOWN,
			MotionEvent.ACTION_MOVE -> touchStartedOverAClickableSpan

			MotionEvent.ACTION_UP -> {
				if (touchStartedOverAClickableSpan && clickableSpanUnderTouch === clickableSpanUnderTouchOnActionDown) {
					dispatchUrlClick(textView, clickableSpanUnderTouch)
				}
				cleanupOnTouchUp(textView)
				touchStartedOverAClickableSpan
			}

			MotionEvent.ACTION_CANCEL -> {
				cleanupOnTouchUp(textView)
				false
			}

			else -> false
		}
	}

	private fun getLinkText(
		textView: TextView,
		text: Spannable,
		event: MotionEvent
	): ClickableSpan? {
		var touchX = event.x.toInt()
		var touchY = event.y.toInt()
		touchX -= textView.totalPaddingLeft
		touchY -= textView.totalPaddingTop
		touchX += textView.scrollX
		touchY += textView.scrollY
		val layout = textView.layout
		val touchedLine = layout.getLineForVertical(touchY)
		val touchOffset = layout.getOffsetForHorizontal(touchedLine, touchX.toFloat())
		mTouchedLineBounds.left = layout.getLineLeft(touchedLine)
		mTouchedLineBounds.top = layout.getLineTop(touchedLine).toFloat()
		mTouchedLineBounds.right = layout.getLineWidth(touchedLine) + mTouchedLineBounds.left
		mTouchedLineBounds.bottom = layout.getLineBottom(touchedLine).toFloat()
		return if (mTouchedLineBounds.contains(touchX.toFloat(), touchY.toFloat())) {
			// Find a ClickableSpan that lies under the touched area.
			val spans = text.getSpans(touchOffset, touchOffset, ClickableSpan::class.java)
			for (span in spans) {
				if (span is ClickableSpan) {
					return span
				}
			}
			// No ClickableSpan found under the touched location.
			null
		} else {
			// Touch lies outside the line's horizontal bounds where no spans should exist.
			null
		}
	}

	private fun dispatchUrlClick(textView: TextView, clickableSpan: ClickableSpan?) {
		val clickableSpanWithText = ClickableSpanWithText.ofSpan(textView, clickableSpan)
		val handled =
			mListener != null && mListener!!.onLinkClicked(textView, clickableSpanWithText.text())
		if (!handled) {
			// Let Android handle this click.
			clickableSpanWithText.span()!!.onClick(textView)
		}
	}

	private fun cleanupOnTouchUp(textView: TextView) {
		clickableSpanUnderTouchOnActionDown = null
	}

	protected open class ClickableSpanWithText protected constructor(
		private val span: ClickableSpan?,
		private val text: String
	) {

		fun span(): ClickableSpan? {
			return span
		}

		fun text(): String {
			return text
		}

		companion object {

			fun ofSpan(textView: TextView, span: ClickableSpan?): ClickableSpanWithText {
				val s = textView.text as Spanned
				val text: String = if (span is URLSpan) {
					span.url
				} else {
					val start = s.getSpanStart(span)
					val end = s.getSpanEnd(span)
					s.subSequence(start, end).toString()
				}
				return ClickableSpanWithText(span, text)
			}
		}
	}

	fun setClick(clickListener: OnLinkListener): InternalLinkMovementMethod {
		mListener = clickListener
		return this
	}

	companion object {

		fun newInstance(): InternalLinkMovementMethod {
			return InternalLinkMovementMethod()
		}
	}
}