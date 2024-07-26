package boilerplate.ui.conversationDetail.adpater

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import boilerplate.R
import boilerplate.constant.AccountManager
import boilerplate.databinding.ItemLoadMoreBinding
import boilerplate.databinding.ItemMessageReceiveBinding
import boilerplate.databinding.ItemMessageSendBinding
import boilerplate.databinding.ItemMessageSystemBinding
import boilerplate.databinding.ItemMessageTimeBinding
import boilerplate.model.message.Message
import boilerplate.ui.conversationDetail.viewHolder.ReceiverHolder
import boilerplate.ui.conversationDetail.viewHolder.SendHolder
import boilerplate.ui.conversationDetail.viewHolder.SystemHolder
import boilerplate.ui.conversationDetail.viewHolder.TimeHolder
import boilerplate.ui.conversationDetail.viewHolder.WithdrawReceiverHolder
import boilerplate.ui.conversationDetail.viewHolder.WithdrawSendHolder
import boilerplate.utils.DateTimeUtil
import boilerplate.utils.extension.notNull
import boilerplate.widget.customText.TextViewExpand
import boilerplate.widget.holder.LoadingVH
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter

class MessageAdapter(
    private val _listener: SimpleMessageEvent
) : RecyclerSwipeAdapter<RecyclerView.ViewHolder?>() {
    companion object {
        const val TYPE_MESSAGE_TEXT_SEND: Int = 0
        const val TYPE_MESSAGE_TEXT_RECEIVE: Int = 1
        const val TYPE_LOAD: Int = 6
        const val TYPE_MESSAGE_SYSTEM: Int = 7
        const val TYPE_MESSAGE_TIME: Int = 8
        const val TYPE_WITHDRAW_SEND: Int = 9
        const val TYPE_WITHDRAW_RECEIVER: Int = 10
    }

    private val _list: ArrayList<Any?> = arrayListOf()
    private var _isGroup = false
    private var _isDisable = false
    private var _isAnswerable = true

    private var mFocusMessage: Message? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        when (viewType) {
            TYPE_MESSAGE_TEXT_SEND -> {
                return SendHolder(
                    ItemMessageSendBinding.inflate(layoutInflater, parent, false),
                    _listener,
                    viewType,
                    _isDisable,
                    _isGroup
                )
            }

            TYPE_WITHDRAW_SEND -> {
                return WithdrawSendHolder(
                    ItemMessageSendBinding.inflate(layoutInflater, parent, false),
                    viewType,
                    _listener
                )
            }

            TYPE_MESSAGE_TEXT_RECEIVE -> {
                return ReceiverHolder(
                    ItemMessageReceiveBinding.inflate(layoutInflater, parent, false),
                    _listener,
                    viewType,
                    _isDisable
                )
            }

            TYPE_WITHDRAW_RECEIVER -> {
                return WithdrawReceiverHolder(
                    ItemMessageReceiveBinding.inflate(layoutInflater, parent, false),
                    _listener,
                    viewType
                )
            }

            TYPE_MESSAGE_TIME -> {
                return TimeHolder(ItemMessageTimeBinding.inflate(layoutInflater, parent, false))
            }

            TYPE_MESSAGE_SYSTEM -> {
                return SystemHolder(ItemMessageSystemBinding.inflate(layoutInflater, parent, false))
            }

            else -> {
                return LoadingVH(ItemLoadMoreBinding.inflate(layoutInflater, parent, false))
            }
        }
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, i: Int) {
        when (vh.itemViewType) {
            TYPE_MESSAGE_TEXT_SEND -> {
                (vh as SendHolder).setData(_list[i] as Message, _isAnswerable)
            }

            TYPE_MESSAGE_TEXT_RECEIVE -> {
                (vh as ReceiverHolder).setData(_list[i] as Message, _isAnswerable)
            }

            TYPE_MESSAGE_SYSTEM -> {
                (vh as SystemHolder).setData(_list[i] as Message)
            }

            TYPE_MESSAGE_TIME -> {
                (vh as TimeHolder).setData(_list[i] as String)
            }

            TYPE_WITHDRAW_RECEIVER -> {
                (vh as WithdrawReceiverHolder).setData(_list[i] as Message, false)
            }

            TYPE_WITHDRAW_SEND -> {
                (vh as WithdrawSendHolder).setData(_list[i] as Message, false)
            }

            else -> {}
        }
    }

    override fun getItemCount(): Int = _list.size

    override fun getSwipeLayoutResourceId(position: Int): Int = R.id.swipe

    override fun getItemViewType(position: Int): Int {
        val ob = _list[position] ?: return TYPE_LOAD
        if (ob is String) return TYPE_MESSAGE_TIME
        if (ob is Message) {
            if (ob.isMsgSystem) return TYPE_MESSAGE_SYSTEM
            return if (ob.personSendId == AccountManager.getCurrentUserId()) {
                if (ob.isWithdraw) TYPE_WITHDRAW_SEND else TYPE_MESSAGE_TEXT_SEND
            } else {
                if (ob.isWithdraw) TYPE_WITHDRAW_RECEIVER else TYPE_MESSAGE_TEXT_RECEIVE
            }
        } else {
            return TYPE_LOAD
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        when (holder.itemViewType) {
            TYPE_MESSAGE_TEXT_SEND, TYPE_MESSAGE_TEXT_RECEIVE -> {
                val expand: TextViewExpand = holder.itemView.findViewById(R.id.tv_message_content)
                expand.clearAnimation()
            }
        }
    }

    fun setAnswerable(isAnswerable: Boolean) {
        _isAnswerable = isAnswerable
        if (_list.size > 0) {
            notifyItemRangeChanged(0, _list.size)
        }
    }

    fun setIsGroup(group: Boolean) {
        _isGroup = group
    }

    fun insertData(list: ArrayList<Any>) {
        val size: Int = _list.size
        if (size > 0) {
            _list.clear()
            notifyItemRangeRemoved(0, size)
        }
        _list.addAll(list)
        notifyItemRangeInserted(0, list.size)
    }

    fun loadMoreData(items: ArrayList<Any>) {
        var size: Int = _list.size
        if (size > 0) {
            val last: Any? = _list[size - 1]
            if (last == null) {
                _list.remove(null)
                notifyItemRemoved(size - 1)
                size -= 1
            }
        }
        if (size != 0 && items.size > 0 && items[0] is Message) {
            val position = size - 1
            val compareTime = _list[position]
            val message = items[0] as Message
            val time: String = DateTimeUtil.convertWithSuitableFormat(
                message.dateCreate,
                DateTimeUtil.FORMAT_NORMAL
            )
            val lastTime: String = DateTimeUtil.convertWithSuitableFormat(
                compareTime as String,
                DateTimeUtil.FORMAT_NORMAL
            )
            if (time == lastTime) {
                _list.remove(compareTime)
                notifyItemRemoved(position)
                size -= 1
            }
        }
        _list.addAll(items)
        notifyItemRangeInserted(size, items.size)
    }

    fun cancelLoadMore() {
        val size: Int = _list.size
        if (size > 0) {
            val last: Any? = _list[size - 1]
            if (last == null) {
                _list.remove(null)
                notifyItemRemoved(size - 1)
            }
        }
    }

    fun cancelLoadMorePre() {
        val first: Any? = _list[0]
        if (first == null) {
            _list.remove(null)
            notifyItemRemoved(0)
            notifyItemRangeChanged(0, _list.size - 1)
        }
    }

    fun getLastMessageId(): String {
        var lastId = ""
        val size: Int = _list.size
        if (size > 0) {
            val item = _list[size - 2]
            if (item is Message) {
                lastId = item.messageId
            }
        }
        return lastId
    }

    fun loadMoreNext() {
        _list.add(null)
        notifyItemInserted(_list.size - 1)
    }

    fun loadMorePre() {
        _list.add(0, null)
        notifyItemInserted(0)
    }

    fun removeFocus() {
        if (mFocusMessage != null) {
            val index: Int = _list.indexOf(mFocusMessage)
            if (index != -1) {
                val message = _list[index] as Message
                message.isFocus = false
                notifyItemChanged(index, message)
            }
            mFocusMessage = null
        }
    }

    fun checkIsLoadPrevious(): Boolean {
        return _list.size != 0 && _list[0] == null
    }

    fun getFirstMessageId(): String {
        val list = ArrayList<String>()
        val iterator: ListIterator<Any?> = _list.listIterator()
        while (iterator.hasNext()) {
            val current = iterator.next()
            if (current is Message) {
                if (current.status != 2) {
                    list.add(current.messageId)
                }
            }
        }
        return list[0]
    }

    fun newMessage(entity: Message) {
        val current = DateTimeUtil.convertWithSuitableFormat(
            entity.dateCreate,
            DateTimeUtil.FORMAT_NORMAL
        )
        val size: Int = _list.size
        if (size > 0) {
            val ob: Any? = _list[0]
            if (ob is Message) {
                val last = DateTimeUtil.convertWithSuitableFormat(
                    ob.dateCreate,
                    DateTimeUtil.FORMAT_NORMAL
                )
                if (last != current) {
                    _list.add(0, current)
                    notifyItemInserted(0)
                    notifyItemChanged(1)
                }
            }
            val index: Int = _list.indexOf(entity)
            if (index == -1) {
                if (ob is Message) {
                    val currentTimeStamp = DateTimeUtil.convertToTimestamp(ob.dateCreate)
                    val inputTimeStamp =
                        DateTimeUtil.convertToTimestamp(entity.dateCreate)
                    val indexInput = if (inputTimeStamp >= currentTimeStamp) 0 else 1
                    _list.add(indexInput, entity)
                    notifyItemInserted(indexInput)
                    notifyItemChanged(indexInput + 1)
                } else {
                    _list.add(0, entity)
                    notifyItemInserted(0)
                    notifyItemChanged(1)
                }
            } else {
                notifyItemChanged(index, entity)
                notifyItemChanged(index + 1)
            }
        } else {
            val list = arrayListOf<Any>()
            list.add(entity)
            list.add(current)
            _list.addAll(list)
            notifyItemRangeInserted(0, list.size)
        }
    }

    fun updateMessage(input: Message) {
        for (ob in _list) {
            val index: Int = _list.indexOf(ob)
            if (ob is Message) {
                if (ob.messageId.equals(input.messageId)) {
                    _list[index] = input
                    notifyItemChanged(index, input)
                    break
                }
            }
        }
    }

    fun removeMessage(id: String? = null, message: Message? = null) {
        val iterator: MutableListIterator<Any?> = _list.listIterator()
        while (iterator.hasNext()) {
            val index = iterator.nextIndex()
            val current = iterator.next()
            if (current is Message) {
                id.notNull {
                    if (current.messageId == it) {
                        iterator.remove()
                        notifyItemRemoved(index)
                        return
                    }
                }
                message.notNull {
                    if (current.messageId == it.messageId) {
                        iterator.remove()
                        notifyItemRemoved(index)
                        return
                    }
                }
            }

        }
    }

    fun updateReaction(message: Message) {
        for (ob in _list) {
            if (ob is Message) {
                val index: Int = _list.indexOf(ob)
                if (ob.messageId.equals(message.messageId)) {
                    ob.reactions.clear()
                    ob.reactions.addAll(message.reactions)
                    _list.set(index, ob)
                    notifyItemChanged(index, ob)
                    break
                }
            }
        }
    }


    fun removeAllMessage() {
        val size: Int = _list.size
        _list.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun markAllUnimportant() {
        for (ob in _list) {
            if (ob is Message) {
                val index: Int = _list.indexOf(ob)
                ob.isImportant = false
                _list.set(index, ob)
                notifyItemChanged(index, ob)
            }
        }
    }

    fun focusMessage(message: Message?) {
        val size: Int = _list.size
        _list.clear()
        notifyItemRemoved(size)
        _list.add(message)
        notifyItemInserted(0)
    }
}