package boilerplate.widget.pulse

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import boilerplate.R
import boilerplate.utils.extension.notNull
import java.util.Random
import java.util.Timer
import java.util.TimerTask

class PulseLayout @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet?,
	defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	private var mTimer: Timer? = null

	private var mPeriod = 800
	private var mIsRandom = true
	private var mMinRandom = 1000
	private var mMaxRandom = 4000

	private var mSize = 0f

	constructor(context: Context) : this(context, null, 0)

	init {
		initViews(context, attrs)
	}

	private fun initViews(context: Context, attrs: AttributeSet?) {
		if (attrs == null) {
			return
		}
		val a = context.obtainStyledAttributes(attrs, R.styleable.PulseLayout, 0, 0)

		with(a) {
			try {
				mPeriod = getInt(R.styleable.PulseLayout_pulse_period, mPeriod)
				mIsRandom = getBoolean(R.styleable.PulseLayout_pulse_is_random, mIsRandom)
				mMaxRandom = getInt(R.styleable.PulseLayout_pulse_high_random, mMaxRandom)
				mMinRandom = getInt(R.styleable.PulseLayout_pulse_low_random, mMinRandom)
				mSize = getDimension(
					R.styleable.PulseLayout_pulse_size,
					resources.getDimension(R.dimen.dp_120)
				)
			} finally {
				recycle()
			}
		}
	}

	fun start() {
		if (mTimer == null) {
			mTimer = Timer(true).also {
				it.schedule(object : TimerTask() {
					override fun run() {
						val animation =
							AnimationUtils.loadAnimation(context, R.anim.anim_pulse).apply {
								duration = Random().nextInt(mMaxRandom) + mMinRandom.toLong()
								isFillEnabled = true
								fillAfter = true
							}

						val params: LayoutParams =
							LayoutParams(mSize.toInt(), mSize.toInt(), Gravity.CENTER)

						val pulseView = FrameLayout(context).apply {
							layoutParams = params
							setBackgroundResource(R.drawable.bg_pulse)
							startAnimation(animation)
							animation.setAnimationListener(object : Animation.AnimationListener {
								override fun onAnimationStart(animation: Animation) {
								}

								override fun onAnimationEnd(animation: Animation) {
									post { removeView(this@apply) }
								}

								override fun onAnimationRepeat(animation: Animation) {
								}
							})
						}
						if (isShown) {
							post { addView(pulseView) }
						}
					}
				}, 0, mPeriod.toLong())
			}
		}
	}

	fun stop() {
		mTimer.notNull {
			it.cancel()
		}
		mTimer = null
		removeAllViews()
	}
}