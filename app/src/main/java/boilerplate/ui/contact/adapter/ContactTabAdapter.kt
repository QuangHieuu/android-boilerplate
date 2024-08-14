package boilerplate.ui.contact.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import boilerplate.constant.AccountManager
import boilerplate.databinding.ItemContactAvatarBinding
import boilerplate.databinding.ItemContactCompanyBinding
import boilerplate.databinding.ItemContactSearchUserBinding
import boilerplate.databinding.ItemContactUserBinding
import boilerplate.databinding.ItemLoadMoreBinding
import boilerplate.model.conversation.Conversation
import boilerplate.model.user.Company
import boilerplate.model.user.Department
import boilerplate.model.user.SelectedContact
import boilerplate.model.user.User
import boilerplate.ui.contact.listener.SimpleListener
import boilerplate.ui.contact.viewholder.AvatarHolder
import boilerplate.ui.contact.viewholder.CompanyHolder
import boilerplate.ui.contact.viewholder.SearchHolder
import boilerplate.ui.contact.viewholder.UserHolder
import boilerplate.utils.extension.lastIndex
import boilerplate.utils.extension.notNull
import boilerplate.widget.holder.LoadingVH

class ContactTabAdapter(
	private val _listener: SimpleListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
	companion object {
		var LIMIT_AVATAR: Int = 6
		var LIMIT_AVATAR_POSITION: Int = LIMIT_AVATAR - 1

		const val TYPE_LOAD_MORE: Int = 0
		const val TYPE_USER: Int = 1
		const val TYPE_COMPANY: Int = 2
		const val TYPE_FAVORITE_USER: Int = 3
		const val TYPE_SEARCH_USER: Int = 4
		const val TYPE_AVATAR: Int = 5
		const val TYPE_REGULAR_CONVERSATION: Int = 6
		const val TYPE_ONLY_NAME: Int = 7
		const val TYPE_BIRTH: Int = 8
		const val TYPE_GROUP: Int = 9
	}

	private val _list = arrayListOf<Any?>()

	fun getList(): ArrayList<Any?> {
		return _list
	}

	val members: ArrayList<User>
		get() = arrayListOf<User>().apply { _list.forEach { if (it is User) add(it) } }

	val conversations: List<Conversation>
		get() = arrayListOf<Conversation>().apply { _list.forEach { if (it is Conversation) add(it) } }

	var check = false
	var onlyMember = false
	var onlyConversation = false
	var showDescription = false
	var allowCheckSelf = false
	var viewType: Int = TYPE_USER

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
					_listener,
					check,
					onlyConversation
				)
			}

			TYPE_USER -> {
				return UserHolder(
					ItemContactUserBinding.inflate(
						layoutInflater,
						parent,
						false
					),
					_listener,
					viewType,
					showDescription,
					check,
					onlyConversation,
					allowCheckSelf
				)
			}

			TYPE_AVATAR -> {
				return AvatarHolder(ItemContactAvatarBinding.inflate(layoutInflater, parent, false))
			}

			TYPE_SEARCH_USER -> {
				return SearchHolder(
					ItemContactSearchUserBinding.inflate(layoutInflater, parent, false),
					check,
					onlyConversation,
					_listener
				)
			}

			else -> {
				return LoadingVH(ItemLoadMoreBinding.inflate(layoutInflater, parent, false))
			}
		}
	}

	override fun getItemCount(): Int {
		return if (viewType == TYPE_AVATAR && _list.size > LIMIT_AVATAR - 1) LIMIT_AVATAR else _list.size
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		val ob: Any = _list[position] ?: return
		when (holder.itemViewType) {
			TYPE_AVATAR -> {
				(holder as AvatarHolder).setData(ob, _list.size)
			}

			TYPE_COMPANY -> {
				(holder as CompanyHolder).setData(ob)
			}

			TYPE_SEARCH_USER -> {
				(holder as SearchHolder).setData(ob as User)
			}

			TYPE_USER -> {
				(holder as UserHolder).setData(ob)
			}
		}
	}

	override fun getItemViewType(position: Int): Int {
		val item = _list[position]
		if (item is User) {
			if (item.regularGroupId.isNotEmpty()) return TYPE_FAVORITE_USER
			return viewType
		}

		if (item is Department || item is Company) {
			return TYPE_COMPANY
		}

		if (item is Conversation) {
			if (viewType == TYPE_AVATAR) {
				return TYPE_AVATAR
			}
			return TYPE_REGULAR_CONVERSATION
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

	fun loadMore() {
		_list.add(null)
		notifyItemInserted(_list.lastIndex)
	}

	fun cancelLoadMore() {
		val size: Int = _list.size
		if (size > 0) {
			val ob: Any? = _list.last()
			if (ob == null) {
				_list.remove(null)
				notifyItemRemoved(size - 1)
			}
		}
	}

	fun addMore(items: ArrayList<Any>) {
		val size: Int = _list.size
		_list.addAll(items)
		notifyItemRangeInserted(size - 1, items.size)
	}

	fun expandDepartment(department: Department) {
		val index: Int = _list.indexOf(department)
		val list: ArrayList<Any> = ArrayList(department.users)
		for (child in department.childDepartments) {
			list.add(child)
			if (child.isExpanding) {
				list.addAll(child.users)
			}
		}

		val childSize = list.size
		val isExpand: Boolean = department.isExpanding
		val size: Int = _list.size
		department.isExpanding = !isExpand
		notifyItemChanged(index, department)
		if (isExpand) {
			_list.removeAll(list)
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

	fun expandCompany(company: Company) {
		val expand = company.isExpanding
		val find = _list.findLast { any -> any is Company && any.id == company.id }
		find.notNull {
			val index: Int = _list.indexOf(find)
			if (it is Company) {
				it.apply {
					isExpanding = expand
					setDepartments(company.departments)
					setChildCompanies(company.childCompanies)
				}
				notifyItemChanged(index, it)
			}
			if (!expand) {
				val removeList = arrayListOf<Any>()
				for (child in company.departments) {
					addRemoveList(child, removeList)
				}
				for (childCompany in company.childCompanies) {
					addRemoveList(childCompany, removeList)
				}
				_list.removeAll(removeList)
				notifyItemRangeRemoved(index + 1, removeList.size)
			} else {
				val addList = arrayListOf<Any>()
				for (item in company.departments) {
					if (company.isChecked) {
						item.isChecked = true
					}
					addList.add(item)
				}
				for (item in company.childCompanies) {
					if (company.isChecked) {
						item.isChecked = true
					}
					addList.add(item)
				}
				_list.addAll(index + 1, addList)
				notifyItemRangeInserted(index + 1, addList.size)
			}
		}
	}

	fun selectedUser(user: User, isCheck: Boolean) {
		val iterator: MutableListIterator<Any?> = _list.listIterator()
		while (iterator.hasNext()) {
			val index = iterator.nextIndex()
			val ob = iterator.next()
			if (ob is User) {
				if (ob.id == user.id) {
					ob.isChecked = isCheck
					iterator.set(ob)
					notifyItemChanged(index, ob)
				}
			}
		}
		checkUserSelected(user)
	}

	fun selectedConversation(item: Conversation) {
		val index: Int = _list.indexOf(item)
		if (index != -1) {
			val selected: Conversation = _list[index] as Conversation
			selected.isSelected = false
			selected.isEnable = true
			notifyItemChanged(index, selected)
		}
	}

	fun selectedDepartment(department: Department) {
		val iterator: ListIterator<Any?> = _list.listIterator()
		while (iterator.hasNext()) {
			val index = iterator.nextIndex()
			val ob = iterator.next()
			if (ob is Department && ob === department) {
				notifyItemChanged(index, department)
			}
		}
	}

	fun updateSelected(event: SelectedContact) {
		if (!event.isCheck) {
			var index = -1
			val iterator: MutableListIterator<Any?> = _list.listIterator()
			while (iterator.hasNext()) {
				index = iterator.nextIndex()
				val ob = iterator.next()
				if (event.user != null && ob is User) {
					if (ob.id == event.user.id) {
						iterator.remove()
						removeSelectedAvatar(index)
						break
					}
				}
				if (event.conversation != null && ob is Conversation) {
					if (ob.id == event.conversation.id) {
						iterator.remove()
						removeSelectedAvatar(index)
						break
					}
				}
			}
			if (index != -1) {
				notifyItemRangeChanged(index, _list.size)
			}
		} else {
			var isContain = false
			for (ob in _list) {
				if (event.user != null && ob is User && ob.id == event.user.id) {
					isContain = true
					break
				}

				if (event.conversation != null && ob is Conversation && ob.id == event.conversation.id) {
					isContain = true
					break
				}
			}
			if (!isContain) {
				if (viewType == TYPE_AVATAR) {
					_list.add(event.user)
					if (_list.size > LIMIT_AVATAR_POSITION) {
						notifyItemChanged(LIMIT_AVATAR_POSITION)
					} else {
						notifyItemInserted(_list.size - 1)
					}
					if (event.conversation != null) {
						_list.add(event.conversation)
						if (_list.size > LIMIT_AVATAR_POSITION) {
							notifyItemChanged(LIMIT_AVATAR_POSITION)
						} else {
							notifyItemInserted(_list.size - 1)
						}
					}
				} else {
					if (event.user != null) {
						event.user.isForContactEdit = true
						_list.add(event.user)
						notifyItemInserted(_list.size - 1)
					}
					if (event.conversation != null) {
						event.conversation.isForContactEdit = true
						_list.add(event.conversation)
						notifyItemInserted(_list.size - 1)
					}
				}
			}
		}
	}

	fun updateRegularGroup(regular: Conversation) {
		val iterator: MutableListIterator<Any?> = _list.listIterator()
		while (iterator.hasNext()) {
			val index = iterator.nextIndex()
			val obj = iterator.next()
			if (obj is Conversation) {
				if (obj.regularGroupId == regular.regularGroupId) {
					obj.regularName = regular.regularName
					obj.regularMember = regular.regularMember
					obj.regularTotalMember = regular.regularTotalMember
					iterator.set(obj)
					notifyItemChanged(index, obj)
					break
				}
			}
		}
	}

	private fun checkUserSelected(item: User) {
		for (ob in _list) {
			if (ob is Department) {
				handleDepartment(ob, item)
			}
			if (ob is Company) {
				handleCompany(ob, item)
			}
		}
	}

	private fun handleDepartment(dp: Department, item: User) {
		val departIndex: Int = _list.indexOf(dp)
		if (item.titles.isEmpty() && item.isRegularMember) {
			handleCheckTitle(departIndex, dp, item)
		} else {
			for (title in item.titles) {
				if (title.department.id == dp.id) {
					handleCheckTitle(departIndex, dp, item)
				}
			}
		}
	}

	private fun handleCompany(company: Company, item: User) {
		val indexCurrent: Int = _list.indexOf(company)
		var count: Int
		for (title in item.titles) {
			if (title.company.id == company.id) {
				count = company.departments.size + company.childCompanies.size
				if (count != 0) {
					for (inside in company.childCompanies) {
						synchronized(this) {
							handleCompany(inside, item)
							if (inside.isChecked) {
								count -= 1
							}
						}
					}
					for (dp in company.departments) {
						handleDepartment(dp, item)
						if (dp.isChecked) {
							count -= 1
						}
					}
					company.isChecked = count <= 0
					notifyItemChanged(indexCurrent, company)
				}
				break
			}
		}
	}

	private fun handleCheckTitle(departIndex: Int, dp: Department, input: User) {
		var count: Int = dp.users.size
		for (user in dp.users) {
			if (user.id == input.id) {
				user.isChecked = input.isChecked
			}
			if (allowCheckSelf) {
				if (user.isChecked) {
					count -= 1
				}
			} else {
				if (user.isChecked || user.id == AccountManager.getCurrentUserId()) {
					count -= 1
				}
			}
		}
		dp.isChecked = count <= 0
		notifyItemChanged(departIndex, dp)
		if (dp.isExpanding) {
			notifyItemRangeChanged(departIndex + 1, dp.users.size)
		}
	}

	private fun removeSelectedAvatar(index: Int) {
		if (viewType == TYPE_AVATAR) {
			if (index < LIMIT_AVATAR) {
				notifyItemRemoved(index)
			} else {
				notifyItemChanged(LIMIT_AVATAR_POSITION)
			}
		} else {
			notifyItemRemoved(index)
			notifyItemRangeChanged(index, _list.lastIndex - index)
		}
	}

	private fun addRemoveList(company: Company, removeList: ArrayList<Any>) {
		removeList.add(company)
		if (company.isExpanding) {
			company.isExpanding = false
			for (department in company.departments) {
				addRemoveList(department, removeList)
			}
		}
	}

	private fun addRemoveList(department: Department, removeList: java.util.ArrayList<Any>) {
		removeList.add(department)
		if (department.isExpanding) {
			department.isExpanding = false
			removeList.addAll(department.users)
			removeList.addAll(department.childDepartments)
			for (child in department.childDepartments) {
				if (child.isExpanding) {
					child.isExpanding = false
					removeList.addAll(child.users)
				}
			}
		}
	}
}