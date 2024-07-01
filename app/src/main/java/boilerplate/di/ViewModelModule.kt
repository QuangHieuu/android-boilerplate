package boilerplate.di

import boilerplate.ui.chat.ChatVM
import boilerplate.ui.main.MainVM
import boilerplate.ui.splash.StartVM
import boilerplate.ui.work.WorkManagerVM
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainVM(get()) }
    viewModel { ChatVM() }
    viewModel { StartVM(get(), get(), get(), get(), get()) }
    viewModel { WorkManagerVM() }
}