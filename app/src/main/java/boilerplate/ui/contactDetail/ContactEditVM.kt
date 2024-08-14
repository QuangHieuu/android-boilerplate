package boilerplate.ui.contactDetail

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import boilerplate.base.BaseViewModel
import boilerplate.data.local.repository.user.UserRepository
import boilerplate.data.remote.repository.auth.LoginRepository
import boilerplate.data.remote.repository.file.FileRepository
import boilerplate.model.user.UpdateBody
import boilerplate.model.user.User
import boilerplate.utils.FileUtils
import boilerplate.utils.extension.loading
import boilerplate.utils.extension.result
import boilerplate.utils.extension.withScheduler
import io.reactivex.rxjava3.core.Flowable

class ContactEditVM(
	private val userRepo: UserRepository,
	private val loginRepo: LoginRepository,
	private val fileRepo: FileRepository,
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

	fun patchUser(changePhone: String, changeDiffPhone: String, changeMood: String, uri: Uri?) {
		launchDisposable {
			Flowable.just(uri ?: "")
				.flatMap {
					if (it is Uri) {
						fileRepo.postAvatarFile(
							userRepo.getUserName(),
							FileUtils.multiPartFile(application, it)
						).map { res ->
							UpdateBody(changePhone, changeDiffPhone, changeMood, res.result[0].path)
						}
					} else {
						Flowable.just(UpdateBody(changePhone, changeDiffPhone, changeMood, null))
					}
				}
				.flatMap { body ->
					userRepo.patchUser(body).doOnNext {
						_userDetail.value?.apply {
							phoneNumber = changePhone
							diffPhoneNumber = changeDiffPhone
							mood = changeMood
							avatarId = body.avatar ?: ""
						}?.let { user ->
							userRepo.saveUser(user)
							_userDetail.postValue(user)
						}
					}
				}
				.withScheduler(schedulerProvider)
				.loading(_loading)
				.result(
					{ _updateSuccess.postValue(true) },
					{ _updateSuccess.postValue(false) }
				)
		}
	}
}