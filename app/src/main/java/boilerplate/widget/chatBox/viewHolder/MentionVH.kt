package boilerplate.widget.chatBox.viewHolder

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import boilerplate.R
import boilerplate.model.conversation.Member
import boilerplate.model.user.User
import boilerplate.utils.extension.click
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.loadAvatar
import boilerplate.utils.extension.show
import boilerplate.widget.chatBox.adapter.MentionAdapter
import boilerplate.widget.chatBox.adapter.MentionAdapter.Companion.MENTION_ALL
import boilerplate.widget.chatBox.adapter.MentionAdapter.Companion.MENTION_DESCRIPTION
import boilerplate.widget.chatBox.adapter.MentionAdapter.Companion.MENTION_SIGN_ALL
import boilerplate.widget.customText.TextViewFont
import boilerplate.widget.image.RoundedImageView

class MentionVH(
	itemView: View,
	private val mListener: MentionAdapter.OnViewListener
) : RecyclerView.ViewHolder(itemView) {
	private val mTvName: TextViewFont = itemView.findViewById(R.id.tv_name)
	private val mTvDepartment: TextViewFont = itemView.findViewById(R.id.tv_department)
	private val mImgAvatar: RoundedImageView = itemView.findViewById(R.id.img_avatar)
	private val mStyleSpan: ForegroundColorSpan =
		ForegroundColorSpan(ContextCompat.getColor(itemView.context, R.color.colorPrimary))

	fun setData(item: Member) {
		val user: User = item.user
		if (user.name != MENTION_ALL) {
			mImgAvatar.apply {
				show()
				loadAvatar(user.avatar)
			}
			val company = user.mainCompany.shortName
			val department = user.mainDepartment.shortName
			val string = String.format("%s - %s", company, department)

			mTvName.text = item.user.name
			mTvDepartment.apply {
				text = string
				show()
			}
		} else {
			SpannableString(MENTION_DESCRIPTION).apply {
				setSpan(
					mStyleSpan,
					indexOf(MENTION_SIGN_ALL),
					length,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
				)
			}.let {
				mTvName.text = it
			}
			mImgAvatar.setTextImage("@")
			mTvDepartment.gone()
		}

		itemView.click { mListener.onChosen(item) }
	}
}