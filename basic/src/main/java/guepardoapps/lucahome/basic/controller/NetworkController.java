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

import guepardoapps.lucahome.basic.utils.Logger;

public class NetworkController {

    private static final String TAG = NetworkController.class.getSimpleName();
    private Logger _logger;

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
        _logger = new Logger(TAG);
        _logger.Debug("Created new " + TAG + "...");
        _context = context;
    }

    public boolean IsNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public boolean IsWifiConnected() {
        if (!IsNetworkAvailable()) {
            return false;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
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
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                WifiManager wifiManager = (WifiManager) _context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                String currentSSID = wifiInfo.getSSID();
                _logger.Debug("currentSSID: " + currentSSID);

                try {
                    if (currentSSID.contains(homeSSID)) {
                        return true;
                    }
                } catch (Exception exception) {
                    String errorString = (exception.getMessage() == null) ? "HomeSSID failed" : exception.getMessage();
                    _logger.Error(errorString);
                    return false;
                }
            } else {
                _logger.Warning("Active network is not wifi: " + String.valueOf(activeNetwork.getType()));
            }
        } else {
            _logger.Warning("activeNetwork is null!");
        }

        return false;
    }

    public String GetIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();

                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "SiteLocalAddress: " + inetAddress.getHostAddress() + "\n";
                    }
                }
            }
        } catch (SocketException e) {
            _logger.Error(e.toString());
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }

    public int GetWifiDBM() {
        int dbm = 0;

        WifiManager wifiManager = (WifiManager) _context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                dbm = wifiInfo.getRssi();
            }
        }

        return dbm;
    }
}