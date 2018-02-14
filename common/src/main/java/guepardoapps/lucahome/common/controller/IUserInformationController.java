package guepardoapps.lucahome.common.controller;

import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public interface IUserInformationController {
    List<ApplicationInfo> GetApkList();

    ArrayList<String> GetApkPackageNameArrayList();

    boolean IsPackageInstalled(@NonNull String packageName);

    String GetWifiSSID();

    String GetIp();
}
