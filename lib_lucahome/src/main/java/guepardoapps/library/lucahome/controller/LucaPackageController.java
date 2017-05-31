package guepardoapps.library.lucahome.controller;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.annotation.NonNull;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.controller.PackageController;

public class LucaPackageController extends PackageController {

    private static final String TAG = LucaPackageController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private Context _context;

    public LucaPackageController(@NonNull Context context) {
        super(context);
        _logger = new LucaHomeLogger(TAG);
        _context = context;
    }

    public boolean IsLatestAppVersionInstalled(@NonNull String loadedAndroidVersion) {
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
