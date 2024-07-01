package boilerplate.ui.main

import boilerplate.base.BaseViewModel
import boilerplate.data.local.repository.user.UserRepository

class MainVM(
    private val userRepository: UserRepository

) : BaseViewModel() {

    fun getUser() = userRepository.getUser()

}