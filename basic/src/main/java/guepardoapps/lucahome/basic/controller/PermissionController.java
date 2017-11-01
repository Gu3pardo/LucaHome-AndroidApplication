package guepardoapps.lucahome.basic.controller;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;
import guepardoapps.lucahome.basic.utils.Logger;

public class PermissionController {
    private static final String TAG = PermissionController.class.getSimpleName();
    protected Logger _logger;

    protected Context _context;

    public PermissionController(@NonNull Context context) {
        _logger = new Logger(TAG);
        _logger.Debug("Created new " + TAG + "...");
        _context = context;
    }

    public void CheckPermissions(int callbackId, @NonNull String... permissionsId) {
        boolean hasPermission = true;

        for (String permission : permissionsId) {
            hasPermission = hasPermission && (ContextCompat.checkSelfPermission(_context, permission) == PackageManager.PERMISSION_GRANTED);
        }

        if (!hasPermission) {
            try {
                ActivityCompat.requestPermissions((Activity) _context, permissionsId, callbackId);
            } catch (Exception ex) {
                _logger.Error(ex.toString());
                Toasty.error(_context, "Failed to ask for permissions!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
