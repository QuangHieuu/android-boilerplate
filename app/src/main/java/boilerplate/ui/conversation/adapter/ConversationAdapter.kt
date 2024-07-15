package boilerplate.ui.conversation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import boilerplate.R
import boilerplate.constant.AccountManager
import boilerplate.databinding.ItemConversationBinding
import boilerplate.model.conversation.Conversation
import boilerplate.model.message.Message
import boilerplate.widget.holder.LoadingVH
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter
import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl

class ConversationAdapter(listener: SimpleEvent) : RecyclerSwipeAdapter<RecyclerView.ViewHolder>() {
    abstract class SimpleEvent {
        open fun onItemClick(con: Conversation) {
        }

        open fun onMarkAsImportant(conversation: Conversation) {
        }

        open fun onDelete(conversation: Conversation) {
        }

        open fun onNotify(item: Conversation, isNotify: Boolean) {
        }
    }

    private val mList: MutableList<Any?> = ArrayList()
    private val mSwipeManager = SwipeItemRecyclerMangerImpl(this)

    private val mListener = listener

    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.swipe_layout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        if (viewType == TYPE_ITEM) {
            return ConversationVH(
                ItemConversationBinding.inflate(layoutInflater, parent, false),
                mSwipeManager,
                mListener
            )
        } else {
            val view = layoutInflater.inflate(R.layout.item_load_more, parent, false)
            return LoadingVH(view)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is ConversationVH) {
            holder.clear()
        }
        super.onViewRecycled(holder)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (mList[position] is Conversation && mList[position] != null) {
            (holder as ConversationVH).setData(mList[position] as Conversation?)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (mList[position] is Conversation) {
            return TYPE_ITEM
        }
        return TYPE_LOAD
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun closeAll() {
        mSwipeManager.closeAllItems()
    }

    val lastConversationId: String
        get() {
            if (mList.isNotEmpty()) {
                val last = mList[mList.size - 1]
                if (last == null) {
                    val item = mList[mList.size - 2] as Conversation?
                    return item!!.conversationId
                }
                val item = last as Conversation
                return item.conversationId
            }
            return ""
        }

    fun loadMore() {
        mList.add(null)
        notifyItemInserted(mList.size - 1)
    }

    fun cancelLoadMore() {
        val size = mList.size
        mList.remove(null)
        notifyItemRemoved(size - 1)
    }

    fun addMore(items: ArrayList<Conversation>) {
        val size = mList.size
        mList.addAll(items)
        notifyItemRangeInserted(size, items.size)
    }

    fun insertData(result: ArrayList<Conversation>) {
        val size = mList.size
        if (size != 0) {
            mList.clear()
            notifyItemRangeRemoved(0, size)
        }
        mList.addAll(result)
        notifyItemRangeInserted(0, result.size)
    }

    fun updateImportant(id: String, isImportant: Boolean) {
        val iterator = mList.listIterator()
        while (iterator.hasNext()) {
            val index = iterator.nextIndex()
            val ob = iterator.next()
            if (ob is Conversation) {
                val check = ob
                if (check.conversationId == id) {
                    for (user in check.conversationUsers) {
                        if (user.user.id == AccountManager.getCurrentUserId()) {
                            user.isImportant = isImportant
                            break
                        }
                    }
                    iterator.set(check)
                    notifyItemChanged(index, check)
                    break
                }
            }
        }
    }

    fun updateImportant(conversation: Conversation?) {
        val index = mList.indexOf(conversation)
        mList[index] = conversation
        notifyItemChanged(index, conversation)
    }

    fun newConversation(conversation: Conversation) {
        var needReplace = false
        var foundItem = false
        val iterator = mList.listIterator()
        while (iterator.hasNext()) {
            val index = iterator.nextIndex()
            val ob = iterator.next()
            if (ob is Conversation) {
                val c = ob
                if (conversation.conversationId == c.conversationId) {
                    foundItem = true
                    if (index == 0) {
                        iterator.set(c)
                        notifyItemChanged(index)
                    } else {
                        needReplace = true
                        iterator.remove()
                        notifyItemRemoved(index)
                    }
                    break
                }
            }
        }
        if (needReplace || !foundItem) {
            mList.add(0, conversation)
            notifyItemInserted(0)
        }
    }

    fun removeConversation(conversation: Conversation) {
        val iterator = mList.listIterator()
        while (iterator.hasNext()) {
            val index = iterator.nextIndex()
            val ob = iterator.next()
            if (ob is Conversation) {
                if (ob.conversationId == conversation.conversationId) {
                    iterator.remove()
                    notifyItemRemoved(index)
                }
            }
        }
    }

    fun removeConversation(conversationId: String?) {
        val iterator = mList.listIterator()
        while (iterator.hasNext()) {
            val index = iterator.nextIndex()
            val ob = iterator.next()
            if (ob is Conversation) {
                if (ob.conversationId == String.format(conversationId!!)) {
                    iterator.remove()
                    notifyItemRemoved(index)
                    break
                }
            }
        }
    }

    fun deleteMessage(conversationId: String) {
        val iterator = mList.listIterator()
        while (iterator.hasNext()) {
            val index = iterator.nextIndex()
            val ob = iterator.next()
            if (ob is Conversation) {
                val c = ob
                if (c.conversationId == conversationId) {
                    c.totalMessage = c.totalMessage - 1
                    c.lastMessage = null
                    iterator.set(c)
                    notifyItemChanged(index, c)
                    return
                }
            }
        }
    }

    fun leaveConversation(value: String) {
        val iterator = mList.listIterator()
        while (iterator.hasNext()) {
            val index = iterator.nextIndex()
            val ob = iterator.next()
            if (ob is Conversation) {
                if (ob.conversationId == value) {
                    iterator.remove()
                    notifyItemRemoved(index)
                    return
                }
            }
        }
    }

    fun syncLastRead(conversationId: String, isOnlyUnread: Boolean) {
        val id: String? = AccountManager.getCurrentUserId()
        synchronized(mList) {
            val iterator = mList.listIterator()
            while (iterator.hasNext()) {
                val index = iterator.nextIndex()
                val ob = iterator.next()
                if (ob is Conversation) {
                    val c = ob
                    if (c.conversationId == conversationId) {
                        if (isOnlyUnread) {
                            iterator.remove()
                            notifyItemRemoved(index)
                        } else {
                            val userIterator = c.conversationUsers.listIterator()
                            while (userIterator.hasNext()) {
                                val user = userIterator.next()
                                if (user.user.id == id) {
                                    user.readNumber = c.totalMessage
                                    userIterator.set(user)
                                    break
                                }
                            }
                            iterator.set(c)
                            notifyItemChanged(index)
                        }
                        return
                    }
                }
            }
        }
    }

    /**
     * Check if conversation is exist in list
     */
    fun newMessage(message: Message, isMe: Boolean, isUpdate: Boolean): Boolean {
        var foundConv: Conversation? = null
        var isChangePos = false
        synchronized(mList) {
            val iterator = mList.listIterator()
            while (iterator.hasNext()) {
                val index = iterator.nextIndex()
                val ob = iterator.next()
                if (ob is Conversation) {
                    val c = ob
                    if (message.getConversationId().equals(c.conversationId)) {
                        if (isUpdate || message.getMessageId().equals(c.lastMessage.messageId)) {
                            if (isMe) {
                                for (user in c.conversationUsers) {
                                    if (user.user.id == AccountManager.getCurrentUserId()) {
                                        user.readNumber =
                                            message.getConversation().getTotalMessage()
                                    }
                                }
                            }
                            c.lastActive = message.getConversation().getLastActive()
                            c.lastMessage = message
                            c.totalMessage = message.getConversation().getTotalMessage()
                            foundConv = c
                            if (isUpdate && index != 0) {
                                isChangePos = true
                                iterator.remove()
                                notifyItemRemoved(index)
                            } else {
                                iterator.set(c)
                                notifyItemChanged(index)
                            }
                        }
                        break
                    }
                }
            }
            if (isChangePos) {
                mList.add(0, foundConv)
                notifyItemInserted(0)
            }
        }
        return foundConv != null
    }

    fun updateConversation(conversation: Conversation): Boolean {
        synchronized(mList) {
            var isFound = false
            var foundIndex = 0
            val iterator = mList.listIterator()
            while (iterator.hasNext()) {
                val index = iterator.nextIndex()
                val ob = iterator.next()
                if (ob is Conversation) {
                    val c = ob
                    if (c.conversationId == conversation.conversationId) {
                        isFound = true
                        c.setTotalUser(conversation.tongSoNhanVien)
                        c.conversationAvatar = conversation.avatarId
                        c.conversationName = conversation.conversationName
                        c.conversationUsers.clear()
                        c.conversationUsers.addAll(conversation.conversationUsers)
                        if (index == 0) {
                            iterator.set(c)
                        } else {
                            foundIndex = index
                            iterator.remove()
                            notifyItemRemoved(index)
                        }
                        break
                    }
                }
            }
            if (foundIndex != 0) {
                mList.add(0, conversation)
                notifyItemMoved(foundIndex, 0)
            }
            return isFound
        }
    }

    fun updateConversation(conversation: Conversation, newPosition: Int) {
        var isRepalce = false
        synchronized(mList) {
            val iterator = mList.listIterator()
            while (iterator.hasNext()) {
                val removeIndex = iterator.nextIndex()
                val ob = iterator.next()
                if (ob is Conversation) {
                    if (ob.conversationId == conversation.conversationId) {
                        if (removeIndex == newPosition) {
                            iterator.set(conversation)
                            notifyItemChanged(removeIndex)
                        } else {
                            iterator.remove()
                            notifyItemRemoved(removeIndex)
                            isRepalce = true
                        }
                        break
                    }
                }
            }
            if (isRepalce) {
                mList.add(newPosition, conversation)
                notifyItemInserted(newPosition)
            }
        }
    }

    fun onOffNotifyConversation(id: String): Boolean? {
        var isOffNotify: Boolean? = null
        val iterator = mList.listIterator()
        while (iterator.hasNext()) {
            val index = iterator.nextIndex()
            val ob = iterator.next()
            if (ob is Conversation) {
                val c = ob
                if (c.conversationId == id) {
                    for (user in c.conversationUsers) {
                        if (user.user.id == AccountManager.getCurrentUserId()) {
                            user.isOffNotify = !user.isOffNotify
                            isOffNotify = user.isOffNotify
                            break
                        }
                    }
                    iterator.set(c)
                    notifyItemChanged(index)
                    break
                }
            }
        }
        return isOffNotify
    }

    fun clearAll() {
        val size = mList.size
        mList.clear()
        notifyItemRangeRemoved(0, size)
    }

    val topConversation: ArrayList<Any?>
        get() = ArrayList(mList.subList(0, 9))

    companion object {
        private const val TYPE_ITEM = 1
        private const val TYPE_LOAD = 0
    }
}
