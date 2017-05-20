package guepardoapps.library.lucahome.services.helper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import guepardoapps.library.lucahome.common.enums.Permissions;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

public class PermissionService {

    private static final String TAG = PermissionService.class.getSimpleName();
    private LucaHomeLogger _logger;

    private Context _context;

    public PermissionService(Context context) {
        _logger = new LucaHomeLogger(TAG);
        _context = context;
    }

    public void RequestPermission(Permissions permission) {
        ActivityCompat.requestPermissions((Activity) _context, new String[]{permission.GetPermissionString()},
                permission.GetRequestId());
    }

    public boolean CheckPermission(Permissions permission) {
        _logger.Debug("Checking permission: " + permission.GetPermissionString());
        int res = _context.checkCallingOrSelfPermission(permission.GetPermissionString());
        boolean hasPermission = res == PackageManager.PERMISSION_GRANTED;
        _logger.Debug("Permission allowed: " + String.valueOf(hasPermission));
        return hasPermission;
    }
}
