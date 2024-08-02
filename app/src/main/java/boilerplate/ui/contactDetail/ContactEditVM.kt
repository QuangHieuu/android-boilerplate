package boilerplate.ui.contactDetail

import androidx.lifecycle.MutableLiveData
import boilerplate.base.BaseViewModel
import boilerplate.data.local.repository.user.UserRepository
import boilerplate.data.remote.repository.auth.LoginRepository
import boilerplate.model.user.UpdateBody
import boilerplate.model.user.User
import boilerplate.utils.extension.BaseSchedulerProvider
import boilerplate.utils.extension.loading
import boilerplate.utils.extension.result
import boilerplate.utils.extension.withScheduler

class ContactEditVM(
    private val schedulerProvider: BaseSchedulerProvider,
    private val userRepo: UserRepository,
    private val loginRepo: LoginRepository,
) : BaseViewModel() {
    val user = userRepo.getUser()

    private val _userDetail by lazy { MutableLiveData<User>() }
    val userDetail = _userDetail
    private val _updateSuccess by lazy { MutableLiveData<Boolean>() }
    val updateSuccess = _updateSuccess

    fun getUser(id: String = "") {
        if (id.isEmpty() || user.id == id) {
            _userDetail.postValue(user)
        } else {
            launchDisposable {
                loginRepo.getUser(id).withScheduler(schedulerProvider)
                    .loading(_loading)
                    .result({
                        _userDetail.postValue(it.result)
                    }, {})
            }
        }
    }

    fun patchUser(changePhone: String, changeDiffPhone: String, changeMood: String) {
        launchDisposable {
            userRepo.patchUser(UpdateBody(changePhone, changeDiffPhone, changeMood))
                .withScheduler(schedulerProvider)
                .loading(_loading)
                .result({
                    _userDetail.value?.apply {
                        phoneNumber = changePhone
                        diffPhoneNumber = changeDiffPhone
                        mood = changeMood
                    }?.let {
                        userRepo.saveUser(it)
                        _userDetail.postValue(it)
                    }
                    _updateSuccess.postValue(true)
                }, { _updateSuccess.postValue(false) })
        }
    }
}