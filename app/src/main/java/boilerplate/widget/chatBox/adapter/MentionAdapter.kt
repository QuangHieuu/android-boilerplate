package boilerplate.widget.chatBox.adapter

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import boilerplate.R
import boilerplate.model.conversation.ConversationUser
import boilerplate.model.user.User
import boilerplate.utils.extension.click
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.loadImage
import boilerplate.utils.extension.show
import boilerplate.widget.customText.TextViewFont
import boilerplate.widget.image.RoundedImageView

class MentionAdapter(private val mListener: OnViewListener) :
    RecyclerView.Adapter<MentionAdapter.MentionVH>() {
    interface OnViewListener {
        fun onChosen(user: ConversationUser)
    }

    private val mListOriginal: ArrayList<ConversationUser> = ArrayList()
    private val mListFind: ArrayList<ConversationUser> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentionVH {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.item_mention, parent, false)
        return MentionVH(view, mListener)
    }

    override fun getItemCount(): Int {
        return mListFind.size
    }

    override fun onBindViewHolder(holder: MentionVH, position: Int) {
        holder.setData(mListFind[position])
    }

    class MentionVH(
        itemView: View,
        private val mListener: OnViewListener
    ) : RecyclerView.ViewHolder(itemView) {
        private val mTvName: TextViewFont = itemView.findViewById(R.id.tv_name)
        private val mTvDepartment: TextViewFont = itemView.findViewById(R.id.tv_department)
        private val mImgAvatar: RoundedImageView = itemView.findViewById(R.id.img_avatar)
        private val mStyleSpan: ForegroundColorSpan =
            ForegroundColorSpan(ContextCompat.getColor(itemView.context, R.color.colorPrimary))

        fun setData(item: ConversationUser) {
            val user: User = item.getUser()
            if (!user.name.equals(MENTION_ALL)) {
                mImgAvatar.apply {
                    show()
                    loadImage(user.avatar)
                }
                val company = user.mainCompany?.shortName
                val department = user.mainDepartment?.shortName
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
                        indexOf(MENTION_SIGN),
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

    fun addData(list: ArrayList<ConversationUser>?) {
        val size = mListOriginal.size
        if (size != 0) {
            mListFind.clear()
            mListOriginal.clear()
        }
        mListOriginal.add(ConversationUser(MENTION_ALL))
        mListOriginal.addAll(list!!)
    }

    fun clear() {
        val size = mListFind.size
        mListFind.clear()
        notifyItemRangeRemoved(0, size)
    }

    val originMentions: ArrayList<ConversationUser>
        get() = mListOriginal

    fun addFind(finds: ArrayList<ConversationUser>): Boolean {
        val size = mListFind.size
        mListFind.clear()
        notifyItemRangeRemoved(0, size)

        mListFind.addAll(finds)
        notifyItemRangeInserted(0, finds.size)
        return finds.size > 0
    }

    companion object {
        private const val MENTION_ALL = "Tất cả"
        private const val MENTION_SIGN = "@Tất cả"
        private const val MENTION_DESCRIPTION = "Báo cho cả nhóm @Tất cả"
    }
}
