package guepardoapps.lucahome.common.controller

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.support.annotation.NonNull
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import guepardoapps.lucahome.common.utils.Logger
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException

@Suppress("DEPRECATION")
class NetworkController(@NonNull private val context: Context) : INetworkController {
    private val tag: String = NetworkController::class.java.simpleName

    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val connectivityManager: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    override fun isInternetConnected(): Pair<NetworkInfo?, Boolean> {
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        return Pair(activeNetwork, activeNetwork?.isConnectedOrConnecting == true)
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

    override fun getIp(): String {
        return InetAddress.getLocalHost().hostAddress
    }

    override fun isWifiConnected(): Pair<NetworkInfo?, Boolean> {
        val networkPair = isInternetConnected()
        val isWiFi: Boolean = networkPair.first?.type == ConnectivityManager.TYPE_WIFI
        return Pair(networkPair.first, networkPair.second && isWiFi)
    }

    override fun isHomeWifiConnected(ssid: String): Boolean {
        val networkPair = isWifiConnected()
        if (!networkPair.second) {
            return false
        }

        if (networkPair.first?.type == ConnectivityManager.TYPE_WIFI) {
            return try {
                wifiManager.connectionInfo.ssid.contains(ssid)
            } catch (exception: Exception) {
                val errorString = if (exception.message == null) "HomeSSID failed" else exception.message
                Logger.instance.error(tag, errorString)
                false
            }

        }

        Logger.instance.warning(tag, "Active network is not wifi: ${networkPair.first?.type}")
        return false
    }

    override fun getWifiSsid(): String {
        val networkPair = isWifiConnected()
        if (!networkPair.second) {
            return ""
        }

        if (networkPair.first?.type == ConnectivityManager.TYPE_WIFI) {
            return wifiManager.connectionInfo.ssid
        }

        Logger.instance.warning(tag, "Active network is not wifi: ${networkPair.first?.type}")
        return ""
    }

    override fun getWifiDBM(): Int {
        var dbm = 0

        if (wifiManager.isWifiEnabled) {
            val wifiInfo = wifiManager.connectionInfo
            if (wifiInfo != null) {
                dbm = wifiInfo.rssi
            }
        }

        return dbm
    }

    override fun setWifiState(newWifiState: Boolean) {
        wifiManager.isWifiEnabled = newWifiState
    }

    override fun isMobileConnected(): Pair<NetworkInfo?, Boolean> {
        val networkPair = isInternetConnected()
        val isMobile: Boolean = networkPair.first?.type == ConnectivityManager.TYPE_MOBILE
        return Pair(networkPair.first, networkPair.second && isMobile)
    }

    override fun setMobileDataState(newMobileDataState: Boolean) {
        try {
            telephonyManager.javaClass.getDeclaredMethod("setDataEnabled", Boolean::class.javaPrimitiveType).invoke(telephonyManager, newMobileDataState)
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
        }
    }

    override fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter.isEnabled
    }

    override fun setBluetoothState(enable: Boolean) {
        if (enable) {
            bluetoothAdapter.enable()
        } else {
            bluetoothAdapter.disable()
        }
    }
}