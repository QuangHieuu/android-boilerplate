package boilerplate.base

import android.app.Application
import boilerplate.di.appModule
import boilerplate.di.contextRequireModule
import boilerplate.di.remoteModule
import boilerplate.di.repositoryModule
import boilerplate.di.viewModelModule
import it.cpc.vn.permission.PermissionUtils
import microsoft.aspnet.signalr.client.Logger
import microsoft.aspnet.signalr.client.Platform
import microsoft.aspnet.signalr.client.PlatformComponent
import microsoft.aspnet.signalr.client.http.HttpConnection
import microsoft.aspnet.signalr.client.http.java.JavaHttpConnection
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class BaseApp : Application() {

    companion object {
        lateinit var sInstance: BaseApp
    }

    override fun onCreate() {
        super.onCreate()
        sInstance = this

        PermissionUtils.initPermissionCheck()
        Platform.loadPlatformComponent(object : PlatformComponent {
            override fun createHttpConnection(logger: Logger): HttpConnection {
                return JavaHttpConnection(logger)
            }

            override fun getOSName(): String {
                return "android"
            }
        })

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