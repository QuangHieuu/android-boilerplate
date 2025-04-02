package boilerplate.widget.recyclerview.edgeEffect

import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import android.widget.EdgeEffect
import androidx.core.graphics.toColorInt
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.RecyclerView

class BouncyEdgeEffectFactory : RecyclerView.EdgeEffectFactory() {

	companion object {
		private const val OVERSCROLL_TRANSLATION_MAGNITUDE = 1f
		private const val FLING_TRANSLATION_MAGNITUDE = 1f

		inline fun <reified T : RecyclerView.ViewHolder> RecyclerView.forEachVisibleHolder(action: (T) -> Unit) {
			for (i in 0 until childCount) {
				action(getChildViewHolder(getChildAt(i)) as T)
			}
		}
	}

	override fun createEdgeEffect(recyclerView: RecyclerView, direction: Int): EdgeEffect {
		return object : EdgeEffect(recyclerView.context) {
			var translationAnims: HashMap<Int, SpringAnimation?> = HashMap()
			val sign: Int
				get() = when (direction) {
					DIRECTION_BOTTOM,
					DIRECTION_RIGHT -> {
						-1
					}

					DIRECTION_LEFT,
					DIRECTION_TOP -> {
						1
					}

					else -> 1
				}

			override fun onPull(deltaDistance: Float) {
				super.onPull(deltaDistance)
				handlePull(deltaDistance)
			}

			override fun onPull(deltaDistance: Float, displacement: Float) {
				super.onPull(deltaDistance, displacement)
				handlePull(deltaDistance)
			}

			private fun handlePull(deltaDistance: Float) {
				when (direction) {
					DIRECTION_BOTTOM,
					DIRECTION_TOP -> {
						val translationYDelta =
							sign * recyclerView.width * deltaDistance * OVERSCROLL_TRANSLATION_MAGNITUDE
						recyclerView.forEachVisibleHolder<RecyclerView.ViewHolder> { holder ->
							holder.itemView.translationY += translationYDelta
						}
					}

					DIRECTION_LEFT,
					DIRECTION_RIGHT -> {
						val translationXDelta =
							sign * recyclerView.height * deltaDistance * OVERSCROLL_TRANSLATION_MAGNITUDE
						recyclerView.forEachVisibleHolder<RecyclerView.ViewHolder> { holder ->
							holder.itemView.translationX += translationXDelta
						}
					}
				}
			}

			override fun onRelease() {
				super.onRelease()
				when (direction) {
					DIRECTION_BOTTOM,
					DIRECTION_TOP -> {
						recyclerView.forEachVisibleHolder<RecyclerView.ViewHolder> { holder ->
							if (holder.itemView.translationY != 0f) {
								val code = holder.itemView.hashCode()
								translationAnims[code] = createAnim(holder.itemView)?.also { it.start() }
							}
						}
					}

					DIRECTION_LEFT,
					DIRECTION_RIGHT -> {
						recyclerView.forEachVisibleHolder<RecyclerView.ViewHolder> { holder ->
							if (holder.itemView.translationX != 0f) {
								val code = holder.itemView.hashCode()
								translationAnims[code] = createAnim(holder.itemView)?.also { it.start() }
							}
						}
					}
				}
			}

			override fun onAbsorb(velocity: Int) {
				super.onAbsorb(velocity)
				val sign = this.sign
				val translationVelocity = sign * velocity * FLING_TRANSLATION_MAGNITUDE
				recyclerView.forEachVisibleHolder<RecyclerView.ViewHolder> { holder ->
					val code = holder.itemView.hashCode()
					val translationAnim = translationAnims[code]
					translationAnim?.cancel()
					translationAnims[code] =
						createAnim(holder.itemView)?.setStartVelocity(translationVelocity)?.also { it.start() }
				}
			}

			override fun draw(canvas: Canvas?): Boolean {
				return false
			}

			override fun isFinished(): Boolean {
				var isFinished = true
				for (index in 0 until recyclerView.childCount) {
					val holder = recyclerView.getChildViewHolder(recyclerView.getChildAt(index))
					val translationAnim = translationAnims[holder.itemView.hashCode()]
					val notFinished = translationAnim?.isRunning ?: false
					if (notFinished) {
						isFinished = false
						break
					}
				}
				return isFinished
			}

			private fun createAnim(view: View): SpringAnimation? {
				val property = when (direction) {
					DIRECTION_BOTTOM,
					DIRECTION_TOP -> {
						SpringAnimation.TRANSLATION_Y
					}

					DIRECTION_LEFT,
					DIRECTION_RIGHT -> {
						SpringAnimation.TRANSLATION_X
					}

					else -> return null
				}
				return SpringAnimation(view, property)
					.setSpring(
						SpringForce()
							.setFinalPosition(0f)
							.setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY)
							.setStiffness(SpringForce.STIFFNESS_LOW)
					)
			}
		}
	}
}