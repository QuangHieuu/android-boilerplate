package boilerplate.base

import android.app.Application
import boilerplate.di.appModule
import boilerplate.di.contextRequireModule
import boilerplate.di.remoteModule
import boilerplate.di.repositoryModule
import boilerplate.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class BaseApp : Application() {

	companion object {
		lateinit var sInstance: BaseApp
		const val APP_FILTER_INVALID = "APP_FILTER_INVALID"
	}

	override fun onCreate() {
		super.onCreate()
		sInstance = this
		startKoin {
			androidLogger()
			androidContext(this@BaseApp)
			androidFileProperties()
			modules(
				appModule,
				viewModelModule,
				contextRequireModule,
				remoteModule,
				repositoryModule
			)
		}
	}
}