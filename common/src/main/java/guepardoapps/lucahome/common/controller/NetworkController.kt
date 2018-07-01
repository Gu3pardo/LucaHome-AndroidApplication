package guepardoapps.lucahome.common.controller

import android.content.Context
import android.support.annotation.NonNull
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import guepardoapps.lib.openweather.utils.Logger
import android.telephony.TelephonyManager
import java.net.NetworkInterface
import java.net.SocketException

@Suppress("DEPRECATION")
class NetworkController(@NonNull private val context: Context) : INetworkController {
    private val tag: String = NetworkController::class.java.simpleName

    override fun networkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }

    override fun wifiConnected(): Boolean {
        return networkTypeEnabled(ConnectivityManager.TYPE_WIFI)
    }

    override fun mobileDataEnabled(): Boolean {
        return networkTypeEnabled(ConnectivityManager.TYPE_MOBILE)
    }

    override fun networkTypeEnabled(networkType: Int): Boolean {
        if (!networkAvailable()) {
            return false
        }

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo

        return activeNetwork != null && activeNetwork.type == networkType
    }

    override fun homeNetwork(homeSSID: String): Boolean {
        if (!wifiConnected()) {
            return false
        }

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo

        if (activeNetwork != null) {
            if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

                val wifiInfo = wifiManager.connectionInfo
                val currentSSID = wifiInfo.ssid

                try {
                    if (currentSSID.contains(homeSSID)) {
                        return true
                    }
                } catch (exception: Exception) {
                    val errorString = if (exception.message == null) "HomeSSID failed" else exception.message
                    Logger.instance.error(tag, errorString)
                    return false
                }

            } else {
                Logger.instance.warning(tag, "Active network is not wifi: ${activeNetwork.type}")
            }
        }

        return false
    }

    override fun setWifiState(newWifiState: Boolean) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.isWifiEnabled = newWifiState
    }

    override fun setMobileDataState(newMobileDataState: Boolean) {
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            telephonyManager.javaClass.getDeclaredMethod("setDataEnabled", Boolean::class.javaPrimitiveType)?.invoke(telephonyManager, newMobileDataState)
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
        }
    }

    override fun getWifiSsid(): String {
        if (!wifiConnected()) {
            return ""
        }

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo

        if (activeNetwork != null) {
            if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiInfo = wifiManager.connectionInfo
                return wifiInfo.ssid
            } else {
                Logger.instance.warning(tag, "Active network is not wifi: ${activeNetwork.type}")
            }
        }

        return ""
    }

    override fun getIpAddress(): String {
        val ip = StringBuilder()

        try {
            val enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces()

            while (enumNetworkInterfaces.hasMoreElements()) {
                val networkInterface = enumNetworkInterfaces.nextElement()
                val enumInetAddress = networkInterface.inetAddresses

                while (enumInetAddress.hasMoreElements()) {
                    val inetAddress = enumInetAddress.nextElement()

                    if (inetAddress.isSiteLocalAddress) {
                        ip.append("SiteLocalAddress: ").append(inetAddress.hostAddress).append("\n")
                    }
                }
            }
        } catch (exception: SocketException) {
            Logger.instance.error(tag, exception)
            ip.append("Error: Something Wrong! ").append(exception.toString()).append("\n")
        }

        return ip.toString()
    }

    override fun getWifiDBM(): Int {
        var dbm = 0

        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (wifiManager.isWifiEnabled) {
            val wifiInfo = wifiManager.connectionInfo
            if (wifiInfo != null) {
                dbm = wifiInfo.rssi
            }
        }

        return dbm
    }
}