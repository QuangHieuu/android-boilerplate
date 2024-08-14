package boilerplate.ui.contact.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import boilerplate.base.BaseViewModel
import boilerplate.data.local.repository.user.UserRepository
import boilerplate.data.remote.repository.conversation.ConversationRepository
import boilerplate.model.conversation.Conversation
import boilerplate.model.conversation.ConversationSignalR
import boilerplate.model.conversation.Search
import boilerplate.model.user.Company
import boilerplate.model.user.Department
import boilerplate.model.user.SelectedContact
import boilerplate.model.user.User
import boilerplate.ui.contact.tab.ContactTab
import boilerplate.utils.extension.loading
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.result
import boilerplate.utils.extension.withScheduler
import boilerplate.utils.mutipleLiveEvent.MultipleLiveEvent
import io.reactivex.rxjava3.core.Flowable
import kotlinx.coroutines.launch

class ContactVM(
	private val userRepo: UserRepository,
	private val conversationRepos: ConversationRepository
) : BaseViewModel() {

	val config = userRepo.getConversationConfig()

	private val _departments by lazy { MutableLiveData<ArrayList<Any>>() }
	val department = _departments
	private val _companies by lazy { MutableLiveData<ArrayList<Company>>() }
	val company = _companies

	private val _expandCompany by lazy { MutableLiveData<Company>() }
	val expandCompany = _expandCompany
	private val _expandDepartment by lazy { MutableLiveData<Department>() }
	val expandDepartment = _expandDepartment
	private val _selectedContact by lazy { MultipleLiveEvent<SelectedContact>() }
	val selectedContact = _selectedContact

	private val _searchContact by lazy { MutableLiveData<ArrayList<Any>>() }
	val searchContact = _searchContact

	private val _putEditGroup by lazy { MutableLiveData<Conversation>() }
	val putEditGroup = _putEditGroup

	private val _user = userRepo.getUser()

	val selected = arrayListOf<String>()
	val createGroup = ConversationSignalR()
	var checkDisable = false
	var checkSelected = false
	var checkRegular = false

	private val searchData: Search = Search(false)
	private var _keyWork = ""
	private var _companyId = ""

	fun getCurrentCompanyId() = userRepo.getCurrentCompany().id

	fun getCurrentDepartmentId() = userRepo.getCurrentDepartment().id

	fun getAllCompanies() {
		val currentCompanyId = userRepo.getCurrentCompany().id
		launchDisposable {
			Flowable.fromArray(userRepo.getContactCompany())
				.map { list ->
					val iterator: MutableIterator<Company> = list.iterator()
					while (iterator.hasNext()) {
						val company = iterator.next()
						if (company.id == currentCompanyId) {
							iterator.remove()
							continue
						}
						val level = company.childLevel
						company.childCompanies.forEach { child -> child.childLevel = level + 1 }
					}
					_companies.postValue(list)
				}
				.withScheduler(schedulerProvider)
				.loading(_loading)
				.result()
		}
	}

	fun getCurrentCompany() {
		val companyId = getCurrentCompanyId()
		val list = userRepo.getContactCompany()

		launchDisposable {
			Flowable.fromArray(list)
				.flatMap { array ->
					val company = array.find { it.id == companyId }
					userRepo.getCompanyDepartment(companyId)
						.map { res ->
							res.result.notNull {
								parseDepartment(it.items)
									.apply { company.notNull { temp -> addAll(temp.childCompanies) } }
									.let { list -> _departments.postValue(list) }
							}
						}
						.flatMap { Flowable.fromIterable(company?.childCompanies.orEmpty()) }
				}
				.flatMap { childCompany(it) }
				.toList()
				.withScheduler(schedulerProvider)
				.loading(_loading)
				.result()
		}
	}

	fun getCompany(company: Company) {
		if (company.departments.isEmpty()) {
			launchDisposable {
				userRepo.getCompanyDepartment(company.id)
					.map { getCompany(company, it.result?.items ?: arrayListOf()) }
					.withScheduler(schedulerProvider)
					.loading(_loading)
					.result({ _expandCompany.postValue(it) })
			}
		} else {
			_expandCompany.postValue(getCompany(company, company.departments))
		}
	}

	private fun getCompany(
		company: Company,
		departments: ArrayList<Department> = arrayListOf()
	): Company {
		val level: Int = company.childLevel
		val isCheck: Boolean = company.isChecked
		val listChild = arrayListOf<Department>()
		val iterator = departments.iterator()
		while (iterator.hasNext()) {
			val value = iterator.next()
			if (value.parentDepartment.isNotEmpty()) {
				listChild.add(value)
				iterator.remove()
			}
		}
		departments.forEach { department: Department ->
			department.apply {
				isChecked = isCheck
				childLevel = level + 1
			}
			listChild.forEach { childDepartment ->
				childDepartment.apply {
					isChecked = isCheck
					childLevel = department.childLevel + 1
				}
				if (childDepartment.parentDepartment == department.id) {
					handleDepartment(company, childDepartment, isCheck)
					department.childDepartments.add(childDepartment)
				}
			}
			handleDepartment(company, department, isCheck)
		}

		company.setDepartments(departments)

		company.childCompanies.forEach { childCompany ->
			childCompany.apply {
				isChecked = isCheck
				childLevel = level + 1
			}
			getCompany(childCompany)
		}
		return company
	}

	fun removeSelectedId(input: String) {
		synchronized(this) {
			val removeList = java.util.ArrayList<String>()
			for (id in selected) {
				if (id == input) {
					removeList.add(id)
				}
			}
			selected.removeAll(removeList)
		}
	}

	fun getDepartment(input: Department) {
		launchDisposable {
			userRepo.getDepartments(input.id)
				.withScheduler(schedulerProvider)
				.loading(_loading)
				.result({
					it.result?.let { department ->
						input.setUser(department.users)
						handleDepartment(null, input, input.isChecked)
					}
				})
		}
	}

	private fun childCompany(company: Company): Flowable<Company> {
		return userRepo.getCompanyDepartment(company.id)
			.withScheduler(schedulerProvider)
			.map { res -> getCompany(company, res.result?.items ?: arrayListOf()) }
	}

	private fun parseDepartment(departments: ArrayList<Department>): ArrayList<Any> {
		val listChild = ArrayList<Department>()
		val iterator = departments.iterator()
		while (iterator.hasNext()) {
			val item = iterator.next()
			if (item.parentDepartment.isNotEmpty()) {
				listChild.add(item)
				iterator.remove()
			}
		}
		val list = ArrayList<Any>()
		for (item in departments) {
			val level: Int = item.childLevel
			for (child in listChild) {
				if (child.parentDepartment == item.id) {
					(level + 1).also { child.childLevel = it }
					for (user in child.users) {
						if (checkRegular) {
							user.isRegularMember = true
						}
						if (checkDisable) {
							user.isEnable = !isAlreadySelected(user.id)
						}
						if (checkSelected) {
							user.isChecked = isAlreadySelected(user.id)
						}
						user.childLevel = child.childLevel + 1
					}
					item.childDepartments.add(child)
				}
			}
			var count: Int = item.users.size
			val isOnlyOne = count == 1
			for (user in item.users) {
				if (checkRegular) {
					user.isRegularMember = true
				}
				if (checkDisable) {
					user.isEnable = !isAlreadySelected(user.id)
				}
				if (checkSelected) {
					user.isChecked = isAlreadySelected(user.id)
				}
				user.childLevel = level + 1
				if (isOnlyOne) {
					if (user.id == _user.id) {
						item.isEnable = false
					} else {
						count -= if (user.isChecked) 1 else 0
						if (count == 0) {
							item.isChecked = true
						}
					}
				} else {
					if (user.isChecked || user.id == _user.id) {
						count -= 1
					}

					if (count <= 0) {
						item.isChecked = true
					}
				}
			}
			list.add(item)
			if (item.id == getCurrentDepartmentId()) {
				item.isExpanding = true
				list.addAll(item.users)
				list.addAll(item.childDepartments)
			}
		}
		return list
	}

	fun handleDepartment(company: Company?, department: Department, isCheck: Boolean) {
		department.users.forEach { child ->
			child.apply {
				childLevel = department.childLevel + 1
				isChecked = isCheck
			}
			handleUser(company, child, isCheck, checkRegular, checkDisable, checkSelected)
		}
		department.childDepartments.forEach { child -> handleDepartment(company, child, isCheck) }
	}

	private fun handleUser(
		company: Company?,
		user: User,
		isCheck: Boolean,
		checkRegular: Boolean,
		checkDisable: Boolean,
		checkSelected: Boolean
	) {
		if (checkRegular) {
			user.isRegularMember = true
		}
		if (checkDisable) {
			user.isEnable = !isAlreadySelected(user.id)
		}
		if (user.id != _user.id && user.isEnable) {
			if (checkSelected) {
				user.isEnable = isAlreadySelected(user.id) || isCheck
			} else {
				user.isChecked = isCheck
			}
			viewModelScope.launch {
				if (company != null) {
					postSelectedContact(user, null, company.isChecked)
				} else {
					postSelectedContact(user, null, user.isChecked)
				}
			}
		}
	}

	fun postSelectedContact(user: User?, conversation: Conversation?, isCheck: Boolean) {
		_selectedContact.postValue(SelectedContact(user, conversation, isCheck))
	}

	fun isAlreadySelected(id: String): Boolean {
		return selected.contains(id)
	}

	fun searchContact(keyWork: String, tabCheck: Int, tabIndex: Int, page: Int) {
		_keyWork = keyWork
		_companyId = if (tabCheck == ContactTab.TYPE_TAB_DEPARTMENT.index) {
			getCurrentCompanyId()
		} else {
			_companyId
		}

		if (tabCheck == ContactTab.TYPE_TAB_CONVERSATION.index) {
			apiSearchConversation(page, keyWork)
		} else {
			apiSearch(keyWork, _companyId, page, tabIndex)
		}
	}

	fun searchMore(page: Int, tabCheck: Int, tabIndex: Int) {
		if (tabCheck == ContactTab.TYPE_TAB_CONVERSATION.index) {
			apiSearchConversation(page, _keyWork)
		} else {
			apiSearch(_keyWork, _companyId, page, tabIndex)
		}
	}

	private fun apiSearchConversation(page: Int, keyWork: String) {
		searchData.setTextSearch(keyWork)
		searchData.setPage(page)
		launchDisposable {
			conversationRepos.getSearchConversations(searchData.getData())
				.withScheduler(schedulerProvider)
				.apply { if (page == 1) loading(_loading) }
				.result()
		}
	}

	private fun apiSearch(keyWork: String, companyId: String, page: Int, tabIndex: Int) {
		val api = if (companyId.isEmpty()) {
			userRepo.getSearchUser(
				if (tabIndex == 0) null else getCurrentCompanyId(),
				keyWork,
				page,
				limit
			)
		} else {
			userRepo.getSearchContact(companyId, keyWork, page, limit)
		}
		launchDisposable {
			api.withScheduler(schedulerProvider)
				.apply { if (page == 1) loading(_loading) }
				.result({ response ->
					_searchContact.postValue(response.result?.items?.let { ArrayList(it) } ?: arrayListOf())
				})
		}
	}

	fun putEditGroup() {
		launchDisposable {
			conversationRepos.putEditGroup(createGroup.regularGroupId, createGroup)
				.withScheduler(schedulerProvider)
				.result({ _putEditGroup.postValue(it.result) }, { _putEditGroup.postValue(null) })
		}
	}
}