package guepardoapps.lucahome.common.listener;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

import guepardoapps.lucahome.common.utils.Logger;

public class ListenerNotificationService extends android.service.notification.NotificationListenerService implements IListenerNotificationService {
    private static final String Tag = ListenerNotificationService.class.getSimpleName();

    private Context _context;

    @Override
    public void onCreate() {
        super.onCreate();
        _context = getApplicationContext();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "StatusBarNotification %s posted", statusBarNotification));
        HandleStatusBarNotification(statusBarNotification, IntentActionNotificationPosted);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "StatusBarNotification %s removed", statusBarNotification));
        HandleStatusBarNotification(statusBarNotification, IntentActionNotificationRemoved);
    }

    @Override
    public void HandleStatusBarNotification(StatusBarNotification statusBarNotification, @NonNull String intentAction) {
        if (statusBarNotification == null) {
            Logger.getInstance().Warning(Tag, "statusBarNotification is null!");
            return;
        }

        String packageName = statusBarNotification.getPackageName();

        String tickerText = "";
        if (statusBarNotification.getNotification().tickerText != null) {
            tickerText = statusBarNotification.getNotification().tickerText.toString();
        }

        Bundle extras = statusBarNotification.getNotification().extras;
        String title = extras.getString("android.title");

        String text = "";
        CharSequence textCharSequence = extras.getCharSequence("android.text");
        if (textCharSequence != null) {
            text = textCharSequence.toString();
        } else {
            Logger.getInstance().Warning(Tag, "textCharSequence is null!");
        }

        Notification.Action[] notificationActions = statusBarNotification.getNotification().actions;

        Intent messageReceiveIntent = new Intent(intentAction);

        messageReceiveIntent.putExtra(ExtraKeyPackageName, packageName);
        messageReceiveIntent.putExtra(ExtraKeyTickerText, tickerText);
        messageReceiveIntent.putExtra(ExtraKeyTitle, title);
        messageReceiveIntent.putExtra(ExtraKeyText, text);
        messageReceiveIntent.putExtra(ExtraKeyNotificationActions, notificationActions);

        try {
            Icon icon = statusBarNotification.getNotification().getLargeIcon();
            if (icon != null) {
                Drawable drawable = icon.loadDrawable(_context);
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                messageReceiveIntent.putExtra(ExtraKeyIcon, byteArray);
            }
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.toString());
        }

        LocalBroadcastManager.getInstance(_context).sendBroadcast(messageReceiveIntent);
    }
}
