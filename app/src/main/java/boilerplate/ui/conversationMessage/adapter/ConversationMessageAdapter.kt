package boilerplate.ui.conversationMessage.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import boilerplate.R
import boilerplate.databinding.ItemConversationMessageBinding
import boilerplate.databinding.ItemLoadMoreBinding
import boilerplate.model.message.Message
import boilerplate.model.user.User
import boilerplate.ui.conversationMessage.ConversationMessageFragment.Companion.MESSAGE_IMPORTANT
import boilerplate.ui.conversationMessage.ConversationMessageFragment.Companion.MESSAGE_PIN
import boilerplate.utils.DateTimeUtil
import boilerplate.utils.StringUtil
import boilerplate.utils.extension.addFile
import boilerplate.utils.extension.addSurvey
import boilerplate.utils.extension.click
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.loadImage
import boilerplate.utils.extension.show
import boilerplate.widget.customText.InternalLinkMovementMethod
import boilerplate.widget.holder.LoadingVH

class ConversationMessageAdapter(
    private val screen: String,
    private val listener: ConversationMessageListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_ITEM: Int = 1
    private val TYPE_LOAD: Int = 0

    private val list = arrayListOf<Message?>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        if (viewType == TYPE_ITEM) {
            return MessageHolder(
                ItemConversationMessageBinding.inflate(
                    layoutInflater,
                    parent.rootView as ViewGroup?,
                    false
                ),
                screen,
                listener
            )
        } else {
            return LoadingVH(ItemLoadMoreBinding.inflate(layoutInflater))
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MessageHolder) {
            list[position]?.let { holder.setData(it) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (list[position] == null) {
            return TYPE_LOAD
        }
        return TYPE_ITEM
    }

    fun addMore(items: ArrayList<Message>) {
        val size: Int = list.size
        list.addAll(items)
        notifyItemRangeInserted(size - 1, items.size)
    }

    fun insertData(result: ArrayList<Message>) {
        val size: Int = list.size
        if (size != 0) {
            list.clear()
            notifyItemRangeRemoved(0, size)
        }
        list.addAll(result)
        notifyItemRangeInserted(0, result.size)
    }

    fun loadMore() {
        list.add(null)
        notifyItemInserted(list.size - 1)
    }

    fun cancelLoadMore() {
        val size = list.size
        list.remove(null)
        notifyItemRemoved(size)
    }

    fun removeMessage(message: Message) {
        val index: Int = list.indexOf(message)
        if (index != -1) {
            list.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}


class MessageHolder(
    private val binding: ItemConversationMessageBinding,
    private val screen: String,
    private val listener: ConversationMessageListener,
    private val context: Context = binding.root.context
) : RecyclerView.ViewHolder(binding.root) {

    init {
        with(binding) {
            when (screen) {
                MESSAGE_PIN -> {
                    imgAction.setImageResource(R.drawable.ic_star_orange)
                    tvSender.gone()
                    tvUserDescription.gone()
                }

                MESSAGE_IMPORTANT -> {
                    imgAction.setImageResource(R.drawable.ic_menu_dot_horizontal)
                    tvSender.show()
                    tvUserDescription.show()
                }
            }


            tvMessageContent.movementMethod = InternalLinkMovementMethod.newInstance()
                .setClick(object : InternalLinkMovementMethod.OnLinkListener {
                    override fun onLinkClicked(textView: TextView, link: String): Boolean {
                        if (link.contains(StringUtil.KEY_MENTION_USER_ID) &&
                            !link.contains(StringUtil.KEY_MENTION_ALL)
                        ) {
                            listener.onMention(link.replace(StringUtil.KEY_MENTION_USER_ID, ""))
                        }
                        if (link.contains(StringUtil.KEY_MENTION_PHONE)) {
                            listener.onPhoneNumber(link.replace(StringUtil.KEY_MENTION_PHONE, ""))
                        }
                        return false
                    }
                })
        }
    }

    fun setData(message: Message) {
        val person: User = if (screen == MESSAGE_PIN) message.personPin else message.personSend

        val text: CharSequence = StringUtil.getHtml(message.mainContent[0])
            .ifEmpty { context.getString(R.string.forward_message) }

        val time = DateTimeUtil.convertWithSuitableFormat(
            message.getDateCreate(),
            DateTimeUtil.FORMAT_NORMAL_WITH_TIME
        )

        with(binding) {
            imgAvatar.loadImage(person.avatar)
            imgAvatar.click { listener.onAvatar(person) }

            imgAction.click {
                if (screen == MESSAGE_PIN) {
                    listener.onRemovePin(message)
                } else {
                    listener.onRemoveImportant(message)
                }
            }

            tvOriginMessage.click { listener.onGoTo(message) }

            tvUser.text = person.name

            tvMessageContent.text = text
            tvMessageTime.text = time

            lnFile.removeAllViews()
            if (message.surveyFiles.isNotEmpty() || message.attachedFiles.isNotEmpty()) {
                lnFile.apply {
                    show()
                    for (survey in message.surveyFiles) {
                        addSurvey(survey) {}
                    }

                    for (file in message.attachedFiles) {
                        addFile(file) {}
                    }
                }
            } else {
                lnFile.gone()
            }
        }
    }

}