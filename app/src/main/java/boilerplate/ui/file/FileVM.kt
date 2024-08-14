package boilerplate.ui.file

import androidx.lifecycle.MutableLiveData
import boilerplate.base.BaseViewModel
import boilerplate.data.local.repository.user.TokenRepository
import boilerplate.data.remote.repository.file.FileRepository
import boilerplate.utils.extension.loading
import boilerplate.utils.extension.result
import boilerplate.utils.extension.withScheduler
import okhttp3.ResponseBody

class FileVM(
	private val fileRepo: FileRepository,
	private val tokenRepo: TokenRepository
) : BaseViewModel() {

	val accessToken = tokenRepo.getOnlyToken()

	private val _responseBody by lazy { MutableLiveData<ResponseBody>() }
	val responseBody = _responseBody

	fun downloadFile(url: String) {
		launchDisposable {
			fileRepo.downloadFile(url)
				.withScheduler(schedulerProvider)
				.loading(_loading)
				.result({ _responseBody.postValue(it) })
		}
	}

}