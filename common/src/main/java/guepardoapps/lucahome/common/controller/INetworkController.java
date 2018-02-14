package guepardoapps.lucahome.common.controller;

import android.support.annotation.NonNull;

public interface INetworkController {
    String WIFIReceiverInHomeNetworkBroadcast = "guepardoapps.lucahome.common.controller.wifi.home_network.yes";
    String WIFIReceiverNoHomeNetworkBroadcast = "guepardoapps.lucahome.common.controller.wifi.home_network.no";

    boolean IsNetworkAvailable();

    boolean IsWifiConnected();

    boolean IsMobileDataEnabled();

    boolean IsNetworkTypeEnabled(int networkType);

    boolean IsHomeNetwork(@NonNull String homeSSID);

    boolean SetWifiState(boolean newWifiState);

    boolean SetMobileDataState(boolean newMobileDataState);

    String GetWifiSsid();

    String GetIpAddress();

    int GetWifiDBM();
}
