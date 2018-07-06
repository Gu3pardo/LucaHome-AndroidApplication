package guepardoapps.lucahome.common.controller

import android.net.NetworkInfo

interface INetworkController {
    fun isInternetConnected(): Pair<NetworkInfo?, Boolean>
    fun getIpAddress(): String
    fun getIp(): String

    fun isWifiConnected(): Pair<NetworkInfo?, Boolean>
    fun isHomeWifiConnected(ssid: String): Boolean
    fun getWifiSsid(): String
    fun getWifiDBM(): Int
    fun setWifiState(newWifiState: Boolean)

    fun isMobileConnected(): Pair<NetworkInfo?, Boolean>
    fun setMobileDataState(newMobileDataState: Boolean)

    fun isBluetoothEnabled(): Boolean
    fun setBluetoothState(enable: Boolean)
}