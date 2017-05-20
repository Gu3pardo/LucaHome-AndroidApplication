package guepardoapps.library.lucahome.services.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

public class NavigationService {

    private static final String TAG = NavigationService.class.getSimpleName();
    private LucaHomeLogger _logger;

    private Context _context;

    public NavigationService(Context context) {
        _logger = new LucaHomeLogger(TAG);
        _context = context;
    }

    public void NavigateTo(Class<?> target, Bundle data, boolean finish) {
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

    public void NavigateTo(Class<?> target) {
        // TODO
        // SEND BROADCAST TO MAIN SERVICE AND ASK FOR PERMISSION TO GO THERE
        Intent navigateTo = new Intent(_context, target);
        _context.startActivity(navigateTo);
    }

    public void NavigateTo(Class<?> target, boolean finish) {
        // TODO
        // SEND BROADCAST TO MAIN SERVICE AND ASK FOR PERMISSION TO GO THERE
        Intent navigateTo = new Intent(_context, target);
        _context.startActivity(navigateTo);

        if (finish) {
            ((Activity) _context).finish();
        }
    }
}
