package guepardoapps.library.lucahome.services.helper;

import java.io.Serializable;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

public class PackageService implements Serializable {

    private static final long serialVersionUID = -3162735517640750340L;

    private static final String TAG = PackageService.class.getSimpleName();
    private LucaHomeLogger _logger;

    private Context _context;

    public PackageService(Context context) {
        _logger = new LucaHomeLogger(TAG);
        _context = context;
    }

    public boolean IsPackageInstalled(String packageName) {
        PackageManager packageManager = _context.getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            _logger.Debug(packageName + " is installed!");
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            _logger.Debug(e.toString());
            return false;
        }
    }

    public boolean StartApplication(String packageName) {
        PackageManager packageManager = _context.getPackageManager();
        try {
            Intent startAppIntent = packageManager.getLaunchIntentForPackage(packageName);
            if (startAppIntent == null) {
                _logger.Error("Cannot start " + packageName + "!");
                return false;
            }
            startAppIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            _logger.Debug("Starting " + packageName + "!");
            _context.startActivity(startAppIntent);
            return true;
        } catch (Exception e) {
            _logger.Error(e.toString());
            return false;
        }
    }

    public boolean IsLatestAppVersionInstalled(String loadedAndroidVersion) {
        PackageInfo packageInfo;

        try {
            packageInfo = _context.getPackageManager().getPackageInfo(_context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            _logger.Error(e.toString());
            return false;
        }

        int latestAppVersionInt = 0;

        String appVersion = packageInfo.versionName;
        int appVersionCode = packageInfo.versionCode;

        if (appVersionCode > latestAppVersionInt) {
            Toasty.info(_context, "Please contact the LucaHome server admin to update the server!",
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if (appVersionCode < latestAppVersionInt) {
            Toasty.info(_context, "There is a new version of LucaHome! Your version is " + appVersion
                    + ", but latest version is " + loadedAndroidVersion, Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }
}
