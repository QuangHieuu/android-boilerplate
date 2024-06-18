package gg.it.ui.splash

import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import gg.it.R
import gg.it.base.BaseFragment
import gg.it.databinding.FragmentLoginBinding
import gg.it.utils.ClickUtil

class LoginFragment : BaseFragment<FragmentLoginBinding, StartVM>() {
    override val mViewModel: StartVM by activityViewModels()
    override fun bindingFactory(): FragmentLoginBinding =
        FragmentLoginBinding.inflate(layoutInflater)

    companion object {
        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }

    private var mIsShowPass = false

    override fun initialize() {
    }

    override fun onSubscribeObserver() {
        val focus =
            ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.colorPrimary))
        val empty =
            ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.color_e6e6e6))
        with(mViewModel) {
            val name: String? = userName.value
            val pass: String? = password.value

            if (!name.isNullOrEmpty() && !pass.isNullOrEmpty()) {
                binding.apply {
                    edtUsername.setText(name)
                    edtPassword.setText(pass)
                    btnLogin.isEnabled = true

                    edtUsername.setBackgroundTintList(focus)
                    edtPassword.setBackgroundTintList(focus)

                    imgPassword.setVisibility(View.VISIBLE)
                    imgClearPass.setVisibility(View.VISIBLE)
                    imgShowPass.setVisibility(View.VISIBLE)
                }
            }
            userName.observe(this@LoginFragment) {
                validateInput()
                with(binding) {
                    if (it.isNotEmpty()) {
                        edtUsername.setBackgroundTintList(focus)
                        imgClearUsername.setVisibility(View.VISIBLE)
                    } else {
                        edtUsername.setBackgroundTintList(empty)
                        imgClearUsername.setVisibility(View.GONE)
                    }
                }
            }

            password.observe(this@LoginFragment) {
                validateInput()
                with(binding) {
                    if (it.isNotEmpty()) {
                        edtPassword.setBackgroundTintList(focus)
                        imgClearPass.setVisibility(View.VISIBLE)
                        imgShowPass.setVisibility(View.VISIBLE)
                    } else {
                        edtPassword.setBackgroundTintList(empty)
                        imgClearPass.setVisibility(View.GONE)
                        imgShowPass.setVisibility(View.GONE)
                    }
                }
            }

            token.observe(this@LoginFragment) {
                if (it.isNotEmpty()) {
                    mViewModel.getMe(true)
                }
            }
        }
    }

    override fun registerOnClick() {
        with(binding) {

            btnLogin.setOnClickListener(ClickUtil.onClick {
                val username: String = edtUsername.editableText.toString()
                val password: String = edtPassword.editableText.toString()
                mViewModel.postLogin(username, password)
            })

            imgClearUsername.setOnClickListener(ClickUtil.onClick {
                edtUsername.setText("")
            })
            imgPassword.setOnClickListener(ClickUtil.onClick {
                edtPassword.setText("")
            })
            imgShowPass.setOnClickListener(ClickUtil.onClick {
                if (mIsShowPass) {
                    imgShowPass.setImageResource(R.drawable.ic_eye_grey)
                    edtPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                } else {
                    imgShowPass.setImageResource(R.drawable.ic_eye_invisible_grey)
                    edtPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                }
                mIsShowPass = !mIsShowPass
            })

            edtUsername.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    charSequence: CharSequence, i: Int, i1: Int, i2: Int
                ) {
                }

                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                    mViewModel.inputUserName(charSequence.toString())
                }

                override fun afterTextChanged(editable: Editable) {
                }
            })

            edtPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, i: Int, i1: Int, i2: Int) {
                }

                override fun onTextChanged(s: CharSequence, i: Int, i1: Int, i2: Int) {
                    mViewModel.inputPassword(s.toString())
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
}