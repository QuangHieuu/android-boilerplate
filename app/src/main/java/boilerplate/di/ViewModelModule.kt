package boilerplate.di

import boilerplate.ui.contact.viewModel.ContactVM
import boilerplate.ui.contactDetail.ContactEditVM
import boilerplate.ui.conversationDetail.ConversationVM
import boilerplate.ui.dashboard.DashboardVM
import boilerplate.ui.file.FileVM
import boilerplate.ui.main.MainVM
import boilerplate.ui.splash.StartVM
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
	viewModel { MainVM(get(), get(), get(), get()) }
	viewModel { StartVM(get(), get(), get(), get(), get()) }
	viewModel { DashboardVM(get()) }
	viewModel { ContactVM(get(), get()) }
	viewModel { ConversationVM(get(), get(), get()) }
	viewModel { ContactEditVM(get(), get(), get()) }
	viewModel { FileVM(get(), get()) }
}