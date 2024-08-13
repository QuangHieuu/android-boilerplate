package boilerplate.ui.contact

import androidx.lifecycle.MutableLiveData
import boilerplate.base.BaseViewModel
import boilerplate.data.local.repository.user.UserRepository
import boilerplate.model.user.Company
import boilerplate.model.user.Department
import boilerplate.model.user.User
import boilerplate.utils.extension.BaseSchedulerProvider
import boilerplate.utils.extension.loading
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.result
import boilerplate.utils.extension.withScheduler
import io.reactivex.rxjava3.core.Flowable

class ContactVM(
	private val schedulerProvider: BaseSchedulerProvider,
	private val userRepository: UserRepository
) : BaseViewModel() {

	private val _departments by lazy { MutableLiveData<ArrayList<Any>>() }
	val contact = _departments
	private val _expandCompany by lazy { MutableLiveData<Company>() }
	val expandCompany = _expandCompany
	private val _updateUser by lazy { MutableLiveData<User>() }
	val updateUser = _updateUser

	private val _user = userRepository.getUser()

	fun getCurrentCompanyId() = userRepository.getCurrentCompany().id

	fun getCurrentDepartmentId() = userRepository.getCurrentDepartment().id

	fun getCurrentCompany() {
		val companyId = getCurrentCompanyId()
		val list = userRepository.getContactCompany()

		launchDisposable {
			Flowable.fromArray(list)
				.flatMap { array ->
					val company = array.find { it.id.equals(companyId) }
					userRepository.getCompanyDepartment(companyId)
						.map { res ->
							res.result.notNull {
								parseDepartment(it.items).apply {
									company.notNull { temp -> addAll(temp.childCompanies) }
								}.let { list -> _departments.postValue(list) }
							}
						}
						.flatMap { Flowable.fromIterable(company?.childCompanies.orEmpty()) }
				}
				.flatMap { apiChildCompany(it) }
				.toList()
				.withScheduler(schedulerProvider)
				.loading(_loading)
				.result()
		}
	}

	private fun apiChildCompany(company: Company, forExpand: Boolean = false): Flowable<Company> {
		return userRepository.getCompanyDepartment(company.id)
			.withScheduler(schedulerProvider)
			.map { res ->
				res.result.notNull { childDepartment(company, it.items, forExpand) }
				company
			}
	}

	private fun parseDepartment(departments: ArrayList<Department>): ArrayList<Any> {
		val listChild = ArrayList<Department>()
		val iterator = departments.iterator()
		while (iterator.hasNext()) {
			val item = iterator.next()
			if (!item.parentDepartment.isNullOrEmpty()) {
				listChild.add(item)
				iterator.remove()
			}
		}
		val list = ArrayList<Any>()
		for (item in departments) {
			val level: Int = item.contactLevel
			for (child in listChild) {
				if (child.parentDepartment.equals(item.id)) {
					child.contactLevel = level + 1
					for (user in child.users ?: arrayListOf()) {
//                        if (mCheckRegular) {
//                            user.setRegularMember(true)
//                        }
//                        if (mCheckDisable) {
//                            user.setEnable(!mView.checkSelected(user.getId()))
//                        }
//                        if (mCheckSelected) {
//                            user.setChecked(mView.checkSelected(user.getId()))
//                        }
						user.contactLevel = child.contactLevel + 1
					}
					item.childDepartments?.add(child)
				}
			}
			var count: Int = item.users?.size ?: 0
			val isOnlyOne = count == 1
			for (user in item.users ?: arrayListOf()) {
//                if (mCheckRegular) {
//                    user.setRegularMember(true)
//                }
//                if (mCheckDisable) {
//                    user.setEnable(!mView.checkSelected(user.getId()))
//                }
//                if (mCheckSelected) {
//                    user.setChecked(mView.checkSelected(user.getId()))
//                }
				user.contactLevel = level + 1
				if (isOnlyOne) {
					if (user.id.equals(_user.id)) {
						item.isEnable = false
					} else {
						count -= if (user.isChecked) 1 else 0
						if (count == 0) {
							item.isChecked = true
						}
					}
				} else {
					if (user.isChecked || user.id.equals(_user.id)) {
						count -= 1
					}

					if (count <= 0) {
						item.isChecked = true
					}
				}
			}
			list.add(item)
			if (item.id.equals(getCurrentDepartmentId())) {
				item.isExpanding = true
				list.addAll(item.users ?: arrayListOf())
				list.addAll(item.childDepartments ?: arrayListOf())
			}
		}
		return list
	}

	private fun childDepartment(
		company: Company,
		departments: ArrayList<Department> = arrayListOf(),
		forExpand: Boolean = false
	) {
		synchronized(company) {
			val level: Int = company.contactLevel
			val isCheck: Boolean = company.isChecked
			val listChild = java.util.ArrayList<Department>()
			val iterator = departments.iterator()
			while (iterator.hasNext()) {
				val value = iterator.next()
				if (!value.parentDepartment.isNullOrEmpty()) {
					listChild.add(value)
					iterator.remove()
				}
			}
			for (department in departments) {
				department.isChecked = isCheck
				department.contactLevel = (level + 1)
				for (childDepartment in listChild) {
					childDepartment.isChecked = isCheck
					childDepartment.contactLevel = (department.contactLevel + 1)
					if (childDepartment.parentDepartment.equals(department.id)) {
						handleDepartment(company, childDepartment, isCheck)
						department.childDepartments?.add(childDepartment)
					}
				}
				handleDepartment(company, department, isCheck)
			}
			company.departments = departments
			for (childCompany in company.childCompanies) {
				childCompany.isChecked = (isCheck)
				childCompany.contactLevel = (level + 1)
				if (childCompany.departments.isEmpty()) {
					launchDisposable {
						apiChildCompany(childCompany, false)
							.withScheduler(schedulerProvider)
							.result()
					}
				} else {
					childDepartment(childCompany, childCompany.departments, false)
				}
			}
			if (forExpand) {
				_expandCompany.postValue(company)
			}
		}
	}

	private fun handleDepartment(company: Company, department: Department, isCheck: Boolean) {
		synchronized(this) {
			for (user in department.users.orEmpty()) {
				user.contactLevel = (department.contactLevel + 1)
				handleUser(company, user, isCheck)
			}
			for (child in department.childDepartments ?: arrayListOf()) {
				handleDepartment(company, child, isCheck)
			}
		}
	}

	private fun handleUser(company: Company?, user: User, isCheck: Boolean) {
//        if (mCheckRegular) {
//            user.setRegularMember(true)
//        }
//        if (mCheckDisable) {
//            user.setEnable(!mView.checkSelected(user.getId()))
//        }
		if (!user.id.equals(_user.id) && user.isEnable) {
//            if (mCheckSelected) {
//                user.setChecked(mView.checkSelected(user.getId()) || isCheck)
//            } else {
//                user.setChecked(isCheck)
//            }
			if (company != null && company.isChecked) {
//                EventBus.getDefault().post(ShareMessage(user, null, company.isChecked()))
			} else {
//                EventBus.getDefault().post(ShareMessage(user, null, user.isChecked()))
			}
			_updateUser.postValue(user)
		}
	}
}
