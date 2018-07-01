package guepardoapps.lucahome.common.controller

interface INetworkController {
    fun networkAvailable(): Boolean

    fun wifiConnected(): Boolean

    fun mobileDataEnabled(): Boolean

    fun networkTypeEnabled(networkType: Int): Boolean

    fun homeNetwork(homeSSID: String): Boolean

    fun setWifiState(newWifiState: Boolean)

    fun setMobileDataState(newMobileDataState: Boolean)

    fun getWifiSsid(): String

    fun getIpAddress(): String

    fun getWifiDBM(): Int
}