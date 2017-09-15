package guepardoapps.lucahome.basic.controller;

import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.text.format.Formatter;

import guepardoapps.lucahome.basic.utils.Logger;

public class UserInformationController {
    private static final String TAG = UserInformationController.class.getSimpleName();
    private Logger _logger;

    private PackageManager _packageManager;
    private WifiManager _wifiManager;

    public UserInformationController(@NonNull Context context) {
        _logger = new Logger(TAG);
        _logger.Debug("Created new " + TAG + "...");

        _packageManager = context.getPackageManager();
        _wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public List<ApplicationInfo> GetApkList() {
        _logger.Debug("GetApkList");
        List<ApplicationInfo> apkList = _packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo entry : apkList) {
            _logger.Debug(entry.toString());
        }
        return apkList;
    }

    public String GetWifiSSID() {
        _logger.Debug("GetWifiSSID");
        String wifiSSID = _wifiManager.getConnectionInfo().toString();
        _logger.Debug(wifiSSID);
        return wifiSSID;
    }

    @SuppressWarnings("deprecation")
    public String GetIp() {
        _logger.Debug("GetIp");
        String ip = Formatter.formatIpAddress(_wifiManager.getConnectionInfo().getIpAddress());
        _logger.Debug(ip);
        return ip;
    }
}
