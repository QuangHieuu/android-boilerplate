package boilerplate.widget.chatBox.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import boilerplate.R
import boilerplate.model.conversation.Member
import boilerplate.widget.chatBox.viewHolder.MentionVH

class MentionAdapter(private val mListener: OnViewListener) :
	RecyclerView.Adapter<MentionVH>() {
	interface OnViewListener {
		fun onChosen(user: Member)
	}

	private val mListOriginal: ArrayList<Member> = ArrayList()
	private val mListFind: ArrayList<Member> = ArrayList()

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

	fun addData(list: ArrayList<Member>) {
		val size = mListOriginal.size
		if (size != 0) {
			mListFind.clear()
			mListOriginal.clear()
		}
		mListOriginal.add(Member(MENTION_ALL))
		mListOriginal.addAll(list)
	}

	fun clear() {
		val size = mListFind.size
		mListFind.clear()
		notifyItemRangeRemoved(0, size)
	}

	val originMentions: ArrayList<Member>
		get() = mListOriginal

	fun addFind(finds: ArrayList<Member>): Boolean {
		val size = mListFind.size
		mListFind.clear()
		notifyItemRangeRemoved(0, size)

		mListFind.addAll(finds)
		notifyItemRangeInserted(0, finds.size)
		return finds.size > 0
	}

	companion object {
		const val MENTION_ALL = "Tất cả"
		const val MENTION_SIGN_ALL = "@Tất cả"
		const val MENTION_DESCRIPTION = "Báo cho cả nhóm @Tất cả"
	}
}
