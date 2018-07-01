package guepardoapps.lucahome.common.controller.deprecated;

import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused"})
public interface IUserInformationController {
    List<ApplicationInfo> GetApkList();

    ArrayList<String> GetApkPackageNameArrayList();

    boolean IsPackageInstalled(@NonNull String packageName);

    String GetWifiSSID();

    String GetIp() throws Exception;
}
