package guepardoapps.lucahome.basic.controller;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;

import guepardoapps.lucahome.basic.utils.Logger;

public class NetworkController {
    private static final String TAG = NetworkController.class.getSimpleName();

    public static final String WIFIReceiverInHomeNetworkBroadcast = "guepardoapps.lucahome.receiver.wifi.home_network.yes";
    public static final String WIFIReceiverNoHomeNetworkBroadcast = "guepardoapps.lucahome.receiver.wifi.home_network.no";

    private Context _context;

    public Runnable StartNetwork = new Runnable() {
        @Override
        public void run() {
            Intent settingsIntent = new Intent();
            settingsIntent.setClassName("com.android.settings", "com.android.settings.Settings");
            _context.startActivity(settingsIntent);
        }
    };

    public NetworkController(@NonNull Context context) {
        _context = context;
    }

    public boolean IsNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public boolean IsWifiConnected() {
        if (!IsNetworkAvailable()) {
            return false;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            }
        }

        return false;
    }

    public boolean IsHomeNetwork(@NonNull String homeSSID) {
        if (!IsWifiConnected()) {
            return false;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                WifiManager wifiManager = (WifiManager) _context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager == null) {
                    return false;
                }

                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String currentSSID = wifiInfo.getSSID();

                try {
                    if (currentSSID.contains(homeSSID)) {
                        return true;
                    }
                } catch (Exception exception) {
                    String errorString = (exception.getMessage() == null) ? "HomeSSID failed" : exception.getMessage();
                    Logger.getInstance().Error(TAG, errorString);
                    return false;
                }
            } else {
                Logger.getInstance().Warning(TAG, "Active network is not wifi: " + String.valueOf(activeNetwork.getType()));
            }
        }

        return false;
    }

    public String GetIpAddress() {
        StringBuilder ip = new StringBuilder();
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();

                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip.append("SiteLocalAddress: ").append(inetAddress.getHostAddress()).append("\n");
                    }
                }
            }
        } catch (SocketException e) {
            Logger.getInstance().Error(TAG, e.toString());
            ip.append("Something Wrong! ").append(e.toString()).append("\n");
        }

        return ip.toString();
    }

    public int GetWifiDBM() {
        int dbm = 0;

        WifiManager wifiManager = (WifiManager) _context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            return -1;
        }

        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                dbm = wifiInfo.getRssi();
            }
        }

        return dbm;
    }
}