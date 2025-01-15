package boilerplate.di

import boilerplate.ui.main.MainVM
import boilerplate.ui.splash.StartVM
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
	viewModel { MainVM(get()) }
	viewModel { StartVM() }
}