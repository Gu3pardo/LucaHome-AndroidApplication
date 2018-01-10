package guepardoapps.lucahome.basic.services;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

import guepardoapps.lucahome.basic.utils.Logger;

@SuppressWarnings("deprecation")
public class NotificationService extends NotificationListenerService {
    private static final String TAG = NotificationService.class.getSimpleName();

    public static final String INTENT_ACTION_NOTIFICATION_POSTED = "NOTIFICATION_SERVICE_INTENT_ACTION_NOTIFICATION_POSTED";
    public static final String INTENT_ACTION_NOTIFICATION_REMOVED = "NOTIFICATION_SERVICE_INTENT_ACTION_NOTIFICATION_REMOVED";

    public static final String EXTRA_KEY_PACKAGE_NAME = "EXTRA_KEY_PACKAGE_NAME";
    public static final String EXTRA_KEY_TICKER_TEXT = "EXTRA_KEY_TICKER_TEXT";
    public static final String EXTRA_KEY_TITLE = "EXTRA_KEY_TITLE";
    public static final String EXTRA_KEY_TEXT = "EXTRA_KEY_TEXT";
    public static final String EXTRA_KEY_NOTIFICATION_ACTIONS = "EXTRA_KEY_NOTIFICATION_ACTIONS";
    public static final String EXTRA_KEY_ICON = "EXTRA_KEY_ICON";

    private Context _context;

    @Override
    public void onCreate() {
        super.onCreate();
        _context = getApplicationContext();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "StatusBarNotification %s posted", statusBarNotification));
        handleStatusBarNotification(statusBarNotification, INTENT_ACTION_NOTIFICATION_POSTED);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "StatusBarNotification %s removed", statusBarNotification));
        handleStatusBarNotification(statusBarNotification, INTENT_ACTION_NOTIFICATION_REMOVED);
    }

    private void handleStatusBarNotification(@NonNull StatusBarNotification statusBarNotification, @NonNull String intentAction) {
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
            Logger.getInstance().Warning(TAG, "textCharSequence is null!");
        }

        Bitmap bitmap = statusBarNotification.getNotification().largeIcon;

        Notification.Action[] notificationActions = statusBarNotification.getNotification().actions;

        Intent messageReceiveIntent = new Intent(intentAction);

        messageReceiveIntent.putExtra(EXTRA_KEY_PACKAGE_NAME, packageName);
        messageReceiveIntent.putExtra(EXTRA_KEY_TICKER_TEXT, tickerText);
        messageReceiveIntent.putExtra(EXTRA_KEY_TITLE, title);
        messageReceiveIntent.putExtra(EXTRA_KEY_TEXT, text);
        messageReceiveIntent.putExtra(EXTRA_KEY_NOTIFICATION_ACTIONS, notificationActions);

        if (bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            messageReceiveIntent.putExtra(EXTRA_KEY_ICON, byteArray);
        }

        LocalBroadcastManager.getInstance(_context).sendBroadcast(messageReceiveIntent);
    }
}
