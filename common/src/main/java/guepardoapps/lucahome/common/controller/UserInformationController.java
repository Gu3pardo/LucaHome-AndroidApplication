package guepardoapps.lucahome.common.controller;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.text.format.Formatter;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"deprecation", "WeakerAccess"})
public class UserInformationController implements IUserInformationController {

    private PackageManager _packageManager;
    private WifiManager _wifiManager;

    public UserInformationController(@NonNull Context context) {
        _packageManager = context.getPackageManager();
        _wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public List<ApplicationInfo> GetApkList() {
        return _packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
    }

    @Override
    public ArrayList<String> GetApkPackageNameArrayList() {
        ArrayList<String> apkPackageNameList = new ArrayList<>();
        for (ApplicationInfo applicationInfo : GetApkList()) {
            apkPackageNameList.add(applicationInfo.packageName);
        }
        return apkPackageNameList;
    }

    @Override
    public boolean IsPackageInstalled(@NonNull String packageName) {
        for (ApplicationInfo applicationInfo : GetApkList()) {
            if (applicationInfo.packageName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String GetWifiSSID() {
        return _wifiManager.getConnectionInfo().toString();
    }

    @Override
    public String GetIp() {
        return Formatter.formatIpAddress(_wifiManager.getConnectionInfo().getIpAddress());
    }
}
