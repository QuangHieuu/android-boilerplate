package boilerplate.ui.splash

import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RadioButton
import androidx.fragment.app.activityViewModels
import boilerplate.BuildConfig
import boilerplate.R
import boilerplate.base.BaseFragment
import boilerplate.data.remote.api.ApiServer
import boilerplate.databinding.DialogChosenServerBinding
import boilerplate.databinding.FragmentLoginBinding
import boilerplate.utils.extension.click
import boilerplate.utils.extension.showDialog

class LoginFragment : BaseFragment<FragmentLoginBinding, StartVM>() {
	override val viewModel: StartVM by activityViewModels()

	companion object {
		fun newInstance(): LoginFragment {
			return LoginFragment()
		}
	}

	private var mIsShowPass = false
	private var mNumberOfClicks = 0

	override fun initialize() {
		val version = resources.getString(R.string.app_name)
		binding.tvVersion.text = java.lang.String.format("%s %s", version, BuildConfig.VERSION_NAME)
	}

	override fun onSubscribeObserver() {
		with(viewModel) {
			val name: String? = userName.value
			val pass: String? = password.value

			if (!name.isNullOrEmpty() && !pass.isNullOrEmpty()) {
				binding.apply {
					edtUsername.setText(name)
					edtPassword.setText(pass)
					btnLogin.isEnabled = true

					imgClearUsername.setVisibility(View.VISIBLE)
					imgClearPass.setVisibility(View.VISIBLE)
					imgShowPass.setVisibility(View.VISIBLE)
				}
			}
			userName.observe(this@LoginFragment) {
				validateInput()
				with(binding) {
					if (it.isNotEmpty()) {
						imgClearUsername.setVisibility(View.VISIBLE)
					} else {
						imgClearUsername.setVisibility(View.GONE)
					}
				}
			}

			password.observe(this@LoginFragment) {
				validateInput()
				with(binding) {
					if (it.isNotEmpty()) {
						imgClearPass.setVisibility(View.VISIBLE)
						imgShowPass.setVisibility(View.VISIBLE)
					} else {
						imgClearPass.setVisibility(View.GONE)
						imgShowPass.setVisibility(View.GONE)
					}
				}
			}
		}
	}

	override fun registerEvent() {
		with(binding) {

			btnLogin.click {
				val username: String = edtUsername.editableText.toString()
				val password: String = edtPassword.editableText.toString()
				viewModel.postLogin(username, password)
			}

			imgClearUsername.click { edtUsername.setText(R.string.no_text) }
			imgClearPass.click { edtPassword.setText(R.string.no_text) }
			imgShowPass.click {
				if (mIsShowPass) {
					imgShowPass.setImageResource(R.drawable.ic_eye_grey)
					edtPassword.transformationMethod = PasswordTransformationMethod.getInstance()
				} else {
					imgShowPass.setImageResource(R.drawable.ic_eye_invisible_grey)
					edtPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
				}
				mIsShowPass = !mIsShowPass
			}

			edtUsername.addTextChangedListener(object : TextWatcher {
				override fun beforeTextChanged(
					charSequence: CharSequence, i: Int, i1: Int, i2: Int
				) {
				}

				override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
					viewModel.inputUserName(charSequence.toString())
				}

				override fun afterTextChanged(editable: Editable) {
				}
			})

			edtPassword.addTextChangedListener(object : TextWatcher {
				override fun beforeTextChanged(s: CharSequence, i: Int, i1: Int, i2: Int) {
				}

				override fun onTextChanged(s: CharSequence, i: Int, i1: Int, i2: Int) {
					viewModel.inputPassword(s.toString())
				}

				override fun afterTextChanged(editable: Editable) {
				}
			})
			edtPassword.setOnEditorActionListener { textView, actionId, keyEvent ->
				when (actionId) {
					EditorInfo.IME_ACTION_DONE -> {
						edtPassword.clearFocus()
						edtUsername.clearFocus()
						if (btnLogin.isEnabled) {
							btnLogin.performClick()
						}
						return@setOnEditorActionListener true
					}

					else -> false
				}
			}
			tvCopyRight.setOnClickListener {
				mNumberOfClicks++
				if (mNumberOfClicks == 5) {
					showServerConfig()
					mNumberOfClicks = 0
				}
			}
		}
	}

	override fun callApi() {
	}

	private fun validateInput() {
		with(binding) {
			val username: String = edtUsername.editableText.toString()
			val password: String = edtPassword.editableText.toString()

			btnLogin.isEnabled = username.isNotEmpty() && password.isNotEmpty()
		}

	}

	private fun showServerConfig() {
		showDialog(DialogChosenServerBinding.inflate(layoutInflater)) { b, dialog ->
			with(b) {
				val host: String = viewModel.getServer()
				for (server in ApiServer.listServer()) {
					val index: Int = ApiServer.listServer().indexOf(server)
					radioGroup.addView(RadioButton(context).apply {
						isChecked = server.serverName == host
						text = server.displayName
						id = index
						tag = server.serverName
					})
				}
				tvConfirm.click {
					val selectedId = radioGroup.checkedRadioButtonId
					val radioButton = radioGroup.findViewById<RadioButton>(selectedId)
					val server = radioButton.tag.toString()
					viewModel.setServer(server)
					dialog.dismiss()
				}
			}
		}
	}
}