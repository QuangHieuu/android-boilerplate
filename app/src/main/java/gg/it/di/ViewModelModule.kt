package gg.it.di

import gg.it.ui.chat.ChatVM
import gg.it.ui.main.MainVM
import gg.it.ui.splash.StartVM
import gg.it.ui.workManager.WorkManagerVM
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainVM() }
    viewModel { ChatVM() }
    viewModel { StartVM(get(), get(), get()) }
    viewModel { WorkManagerVM() }
}