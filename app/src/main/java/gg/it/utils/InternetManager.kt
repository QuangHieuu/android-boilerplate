package gg.it.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import gg.it.R
import gg.it.base.BaseApp.Companion.sInstance
import gg.it.data.remote.api.error.RetrofitException
import gg.it.data.remote.api.error.Type

class InternetManager {

    companion object {
        fun isConnected(): Boolean {
            val connectivityManager =
                sInstance.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }
    }
}

class InternetException : RetrofitException(
    Type.NETWORK,
    Exception(sInstance.getString(R.string.text_internet_error))
)