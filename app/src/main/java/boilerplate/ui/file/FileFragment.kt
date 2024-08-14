package boilerplate.ui.file

import android.annotation.SuppressLint
import android.os.Bundle
import boilerplate.base.BaseFragment
import boilerplate.databinding.FragmentFileBinding
import boilerplate.model.file.AttachedFile
import boilerplate.utils.extension.notNull
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.net.URLEncoder

class FileFragment : BaseFragment<FragmentFileBinding, FileVM>() {
	companion object {
		const val KEY_JSON = "KEY_JSON"

		fun newInstance(fileJson: String): FileFragment {
			return Bundle().let { bundle ->
				bundle.putString(KEY_JSON, fileJson)
				FileFragment().apply { arguments = bundle }
			}
		}
	}

	override val viewModel: FileVM by viewModel()

	@SuppressLint("SetJavaScriptEnabled")
	override fun initialize() {
		binding.webView.settings.apply {
			javaScriptEnabled = true
		}
	}

	override fun onSubscribeObserver() {

	}

	override fun registerEvent() {
	}

	override fun callApi() {
		arguments.notNull {
			val fileJson = it.getString(KEY_JSON)

			val file = viewModel.gson.fromJson(fileJson, AttachedFile::class.java)

			val link = "https://view.officeapps.live.com/op/view.aspx?src="
			val token = "&BearerToken=" + viewModel.accessToken
			val fileName = "?app=${file.application}&fileName=${file.fileName}"
			val origin = "&wdOrigin=BROWSELINK"

			val url = URLEncoder.encode(file.fileChat + fileName + token, Charsets.UTF_8.name())
			val string = link + url + origin
			binding.webView.loadUrl(string)
		}
	}
}
