package guepardoapps.library.lucahome.services.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

public class NavigationService {

    private static final String TAG = NavigationService.class.getSimpleName();
    private LucaHomeLogger _logger;

    private Context _context;

    public NavigationService(@NonNull Context context) {
        _logger = new LucaHomeLogger(TAG);
        _context = context;
    }

    public void NavigateTo(
            @NonNull Class<?> target,
            Bundle data,
            boolean finish) {
        _logger.Debug("Navigate to " + target.toString());

        Intent navigateTo = new Intent(_context, target);
        if (data != null) {
            _logger.Debug("data is not null!");
            navigateTo.putExtras(data);
        }
        _context.startActivity(navigateTo);

        if (finish) {
            ((Activity) _context).finish();
        }
    }

    public void NavigateTo(@NonNull Class<?> target) {
        Intent navigateTo = new Intent(_context, target);
        _context.startActivity(navigateTo);
    }

    public void NavigateTo(
            @NonNull Class<?> target,
            boolean finish) {
        Intent navigateTo = new Intent(_context, target);
        _context.startActivity(navigateTo);

        if (finish) {
            ((Activity) _context).finish();
        }
    }
}
