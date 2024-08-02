package boilerplate.ui.contact.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import boilerplate.databinding.ItemContactCompanyBinding
import boilerplate.databinding.ItemContactUserBinding
import boilerplate.databinding.ItemLoadMoreBinding
import boilerplate.model.user.Company
import boilerplate.model.user.Department
import boilerplate.model.user.User
import boilerplate.ui.contact.listener.SimpleListener
import boilerplate.ui.contact.viewholder.CompanyHolder
import boilerplate.ui.contact.viewholder.UserHolder
import boilerplate.utils.extension.notNull
import boilerplate.widget.holder.LoadingVH

class ContactTabAdapter(
    private val _listener: SimpleListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val _list = arrayListOf<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        when (viewType) {
            TYPE_COMPANY -> {
                return CompanyHolder(
                    ItemContactCompanyBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    ),
                    _listener
                )
            }

            TYPE_USER -> {
                return UserHolder(
                    ItemContactUserBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    ),
                    _listener
                )
            }

            else -> {
                return LoadingVH(ItemLoadMoreBinding.inflate(layoutInflater, parent, false))
            }
        }
    }

    override fun getItemCount(): Int {
        return _list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TYPE_COMPANY -> {
                (holder as CompanyHolder).setData(_list[position])
            }

            TYPE_USER -> {
                (holder as UserHolder).setData(_list[position])
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = _list[position]
        if (item is User) {
            return TYPE_USER
        }

        if (item is Department || item is Company) {
            return TYPE_COMPANY
        }

        return TYPE_LOAD_MORE
    }

    fun insertData(list: ArrayList<Any>) {
        val size: Int = _list.size
        if (size != 0) {
            _list.clear()
            notifyItemRangeRemoved(0, size)
        }
        _list.addAll(list)
        notifyItemRangeInserted(0, list.size)
    }

    fun expandDepartment(department: Department) {
        synchronized(_list) {
            val index: Int = _list.indexOf(department)
            val list: ArrayList<Any> = ArrayList(department.users.orEmpty())
            for (child in department.childDepartments.orEmpty()) {
                list.add(child)
                if (child.isExpanding) {
                    list.addAll(child.users.orEmpty())
                }
            }

            val childSize = list.size
            val isExpand: Boolean = department.isExpanding
            val size: Int = _list.size
            department.isExpanding = !isExpand
            notifyItemChanged(index, department)
            if (isExpand) {
                _list.removeAll(list.toSet())
                notifyItemRangeRemoved(index + 1, childSize)
                notifyItemRangeChanged(index + 1 + childSize, size - childSize)
            } else {
                for (item in list) {
                    if (department.isChecked) {
                        if (item is Department) {
                            item.isChecked = true
                        }
                        if (item is User) {
                            item.isChecked = true
                        }
                    }
                }
                _list.addAll(index + 1, list)
                notifyItemRangeInserted(index + 1, childSize)
            }
        }
    }

    fun updateContact(value: User?) {
        value.notNull {
            val listIterator = _list.listIterator()
            while (listIterator.hasNext()) {
                val index = listIterator.nextIndex()
                val ob = listIterator.next()
                if (ob is User && ob.id == it.id) {
                    ob.apply {
                        mood = it.mood
                        phoneNumber = it.phoneNumber
                        diffPhoneNumber = it.diffPhoneNumber
                    }
                    listIterator.set(ob)
                    notifyItemChanged(index)
                }
            }
        }
    }

    companion object {
        const val TYPE_LOAD_MORE: Int = 0
        const val TYPE_USER: Int = 1
        const val TYPE_COMPANY: Int = 2
    }
}