package boilerplate.ui.conversation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import boilerplate.R
import boilerplate.constant.AccountManager
import boilerplate.databinding.ItemConversationBinding
import boilerplate.databinding.ItemLoadMoreBinding
import boilerplate.model.conversation.Conversation
import boilerplate.model.message.Message
import boilerplate.utils.DateTimeUtil.compareTwoDateWithFormat
import boilerplate.widget.holder.LoadingVH
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter
import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl

class ConversationAdapter(
    private val _listener: SimpleEvent,
    private val _pin: Boolean = false
) :
    RecyclerSwipeAdapter<RecyclerView.ViewHolder>() {
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

    private val _list: MutableList<Any?> = ArrayList()
    private val _swipeManager = SwipeItemRecyclerMangerImpl(this)

    private var _isShowPin = true

    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.swipe_layout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        if (viewType == TYPE_ITEM) {
            return ConversationVH(
                ItemConversationBinding.inflate(layoutInflater, parent, false),
                _swipeManager,
                _listener,
                _pin
            )
        } else {
            return LoadingVH(ItemLoadMoreBinding.inflate(layoutInflater, parent, false))
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is ConversationVH) {
            holder.clear()
        }
        super.onViewRecycled(holder)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (_list[position] is Conversation && _list[position] != null) {
            (holder as ConversationVH).setData(_list[position] as Conversation?, _isShowPin)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (_list[position] is Conversation) {
            return TYPE_ITEM
        }
        return TYPE_LOAD
    }

    override fun getItemCount(): Int {
        return _list.size
    }

    fun closeAll() {
        _swipeManager.closeAllItems()
    }

    val lastConversationId: String
        get() {
            if (_list.isNotEmpty()) {
                val last = _list[_list.size - 1]
                if (last == null) {
                    val item = _list[_list.size - 2] as Conversation?
                    return item!!.conversationId
                }
                val item = last as Conversation
                return item.conversationId
            }
            return ""
        }

    fun loadMore() {
        _list.add(null)
        notifyItemInserted(_list.size - 1)
    }

    fun cancelLoadMore() {
        val size = _list.size
        _list.remove(null)
        notifyItemRemoved(size - 1)
    }

    fun addMore(items: ArrayList<Conversation>) {
        val size = _list.size
        _list.addAll(items)
        notifyItemRangeInserted(size, items.size)
    }

    fun insertData(result: ArrayList<Conversation>) {
        val size = _list.size
        if (size != 0) {
            _list.clear()
            notifyItemRangeRemoved(0, size)
        }
        _list.addAll(result)
        notifyItemRangeInserted(0, result.size)
    }

    fun updateImportant(id: String, isImportant: Boolean) {
        val iterator = _list.listIterator()
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
        val index = _list.indexOf(conversation)
        _list[index] = conversation
        notifyItemChanged(index, conversation)
    }

    fun newConversation(conversation: Conversation) {
        var needReplace = false
        var foundItem = false
        val iterator = _list.listIterator()
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
            _list.add(0, conversation)
            notifyItemInserted(0)
        }
    }

    fun removeConversation(conversation: Conversation) {
        val iterator = _list.listIterator()
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
        val iterator = _list.listIterator()
        while (iterator.hasNext()) {
            val index = iterator.nextIndex()
            val ob = iterator.next()
            if (ob is Conversation) {
                if (ob.conversationId == conversationId) {
                    iterator.remove()
                    notifyItemRemoved(index)
                    break
                }
            }
        }
    }

    fun deleteMessage(conversationId: String) {
        val iterator = _list.listIterator()
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
        val iterator = _list.listIterator()
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
        synchronized(_list) {
            val iterator = _list.listIterator()
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
        synchronized(_list) {
            val iterator = _list.listIterator()
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
                _list.add(0, foundConv)
                notifyItemInserted(0)
            }
        }
        return foundConv != null
    }

    fun updateConversation(conversation: Conversation): Boolean {
        synchronized(_list) {
            var isFound = false
            var foundIndex = 0
            val iterator = _list.listIterator()
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
                _list.add(0, conversation)
                notifyItemMoved(foundIndex, 0)
            }
            return isFound
        }
    }

    fun updateConversation(conversation: Conversation, newPosition: Int) {
        var isRepalce = false
        synchronized(_list) {
            val iterator = _list.listIterator()
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
                _list.add(newPosition, conversation)
                notifyItemInserted(newPosition)
            }
        }
    }

    fun onOffNotifyConversation(id: String): Boolean? {
        var isOffNotify: Boolean? = null
        val iterator = _list.listIterator()
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
        val size = _list.size
        _list.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun insertPin(conversation: Conversation) {
        _list.add(0, conversation)
        notifyItemInserted(0)
    }

    fun reInsertPinAfterRemove(conversation: Conversation) {
        val iterator: MutableListIterator<Any?> = _list.listIterator()
        while (iterator.hasNext()) {
            val index = iterator.nextIndex()
            val ob = iterator.next()
            if (ob is Conversation) {
                //Kiểm tra hội thoại pin có ngày hoạt động cuối nhỏ hơn thì mới add vào
                if (!compareTwoDateWithFormat(conversation.lastActive, ob.lastActive)) {
                    _list.add(index, conversation)
                    notifyItemInserted(index)
                    return
                }
            }
        }
    }

    fun updatePinImportant(id: String, important: Boolean): Boolean {
        val iterator: MutableListIterator<Any?> = _list.listIterator()
        while (iterator.hasNext()) {
            val index = iterator.nextIndex()
            val ob = iterator.next()
            if (ob is Conversation) {
                if (ob.conversationId!! == id) {
                    for (user in ob.conversationUsers) {
                        if (user.user.id.equals(AccountManager.getCurrentUserId())) {
                            user.isImportant = important
                            break
                        }
                    }
                    iterator.set(ob)
                    notifyItemChanged(index, ob)
                    return true
                }
            }
        }
        return false
    }

    fun newPinConversation(conversation: Conversation): Boolean {
        val iterator: MutableListIterator<Any?> = _list.listIterator()
        while (iterator.hasNext()) {
            val index = iterator.nextIndex()
            val ob = iterator.next()
            if (ob is Conversation) {
                if (conversation.conversationId.equals(ob.conversationId)) {
                    iterator.set(ob)
                    notifyItemChanged(index)
                    return true
                }
            }
        }
        return false
    }

    fun newPinConversation(message: Message, isMe: Boolean, isUpdate: Boolean): Boolean {
        val iterator: MutableListIterator<Any?> = _list.listIterator()
        while (iterator.hasNext()) {
            val index = iterator.nextIndex()
            val ob = iterator.next()
            if (ob is Conversation) {
                if (message.conversationId.equals(ob.conversationId)) {
                    if (isUpdate || message.messageId.equals(ob.lastMessage.messageId)) {
                        ob.apply {
                            if (isMe) {
                                for (user in conversationUsers) {
                                    if (user.user.id.equals(AccountManager.getCurrentUserId())) {
                                        user.readNumber = message.conversation.totalMessage
                                    }
                                }
                            }
                            lastActive = message.conversation.lastActive
                            lastMessage = message
                            totalMessage = message.conversation.totalMessage
                        }
                        iterator.set(ob)
                        notifyItemChanged(index)
                    }
                    return true
                }
            }
        }
        return false
    }

    fun removePinConversation(conversationId: String?): Boolean {
        val iterator: MutableListIterator<Any?> = _list.listIterator()
        while (iterator.hasNext()) {
            val index = iterator.nextIndex()
            val ob = iterator.next()
            if (ob is Conversation) {
                if (ob.conversationId.equals(conversationId!!)) {
                    iterator.remove()
                    notifyItemRemoved(index)
                    return true
                }
            }
        }
        return false
    }

    fun leavePinConversation(conversationId: String): Boolean {
        val iterator: MutableListIterator<Any?> = _list.listIterator()
        while (iterator.hasNext()) {
            val index = iterator.nextIndex()
            val ob = iterator.next()
            if (ob is Conversation) {
                if (ob.conversationId.equals(conversationId)) {
                    iterator.remove()
                    notifyItemRemoved(index)
                    return true
                }
            }
        }
        return false
    }

    fun updatePinConversation(conversation: Conversation): Boolean {
        val iterator: MutableListIterator<Any?> = _list.listIterator()
        while (iterator.hasNext()) {
            val removeIndex = iterator.nextIndex()
            val ob = iterator.next()
            if (ob is Conversation) {
                if (ob.conversationId.equals(conversation.conversationId)) {
                    iterator.set(conversation)
                    notifyItemChanged(removeIndex)
                    return false
                }
            }
        }
        return true
    }

    fun newPinMessage(message: Message, isMe: Boolean, isUpdate: Boolean): Boolean {
        val iterator: MutableListIterator<Any?> = _list.listIterator()
        while (iterator.hasNext()) {
            val index = iterator.nextIndex()
            iterator.next().let { ob ->
                if (ob is Conversation) {
                    if (message.conversationId.equals(ob.conversationId)) {
                        if (isUpdate || message.messageId.equals(ob.lastMessage.messageId)) {
                            if (isMe) {
                                for (user in ob.conversationUsers) {
                                    if (user.user.id.equals(AccountManager.getCurrentUserId())) {
                                        user.readNumber = message.conversation.totalMessage
                                    }
                                }
                            }
                            ob.apply {
                                lastActive = message.conversation.lastActive
                                lastMessage = message
                                totalMessage = message.conversation.totalMessage
                            }
                            iterator.set(ob)
                            notifyItemChanged(index)
                        }
                        return true
                    }
                }
            }
        }
        return false
    }

    fun deletePinMessage(conversationId: String?): Boolean {
        val iterator: MutableListIterator<Any?> = _list.listIterator()
        while (iterator.hasNext()) {
            val index = iterator.nextIndex()
            val ob = iterator.next()
            if (ob is Conversation) {
                val c = ob
                if (c.conversationId.equals(conversationId)) {
                    c.totalMessage -= 1
                    c.lastMessage = null
                    iterator.set(c)
                    notifyItemChanged(index, c)
                    return true
                }
            }
        }
        return false
    }

    fun syncPinLastRead(conversationId: String?): Boolean {
        val id: String = AccountManager.getCurrentNhanVien().id ?: ""
        val iterator: MutableListIterator<Any?> = _list.listIterator()
        while (iterator.hasNext()) {
            val index = iterator.nextIndex()
            val ob = iterator.next()
            if (ob is Conversation) {
                val c = ob
                if (c.conversationId.equals(conversationId)) {
                    for (user in c.conversationUsers) {
                        if (user.user.id.equals(id)) {
                            user.readNumber = c.totalMessage
                        }
                    }
                    iterator.set(c)
                    notifyItemChanged(index)
                    return true
                }
            }
        }
        return false
    }

    val topConversation: ArrayList<Any?>
        get() = ArrayList(_list.subList(0, 9))

    companion object {
        private const val TYPE_ITEM = 1
        private const val TYPE_LOAD = 0
    }
}
