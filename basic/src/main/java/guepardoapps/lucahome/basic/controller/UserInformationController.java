package guepardoapps.lucahome.basic.controller;

import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.text.format.Formatter;

import guepardoapps.lucahome.basic.utils.Logger;

@SuppressWarnings({"deprecation", "unused"})
public class UserInformationController {
    private static final String TAG = UserInformationController.class.getSimpleName();

    private PackageManager _packageManager;
    private WifiManager _wifiManager;

    public UserInformationController(@NonNull Context context) {
        _packageManager = context.getPackageManager();
        _wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public List<ApplicationInfo> GetApkList() {
        List<ApplicationInfo> apkList = _packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo entry : apkList) {
            Logger.getInstance().Debug(TAG, entry.toString());
        }
        return apkList;
    }

    public String GetWifiSSID() {
        return _wifiManager.getConnectionInfo().toString();
    }

    public String GetIp() {
        return Formatter.formatIpAddress(_wifiManager.getConnectionInfo().getIpAddress());
    }
}
