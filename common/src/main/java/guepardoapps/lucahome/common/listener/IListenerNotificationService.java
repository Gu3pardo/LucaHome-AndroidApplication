package guepardoapps.lucahome.common.listener;

import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;

public interface IListenerNotificationService {
    String IntentActionNotificationPosted = "IntentActionNotificationPosted";
    String IntentActionNotificationRemoved = "IntentActionNotificationRemoved";

    String ExtraKeyPackageName = "ExtraKeyPackageName";
    String ExtraKeyTickerText = "ExtraKeyTickerText";
    String ExtraKeyTitle = "ExtraKeyTitle";
    String ExtraKeyText = "ExtraKeyText";
    String ExtraKeyNotificationActions = "ExtraKeyNotificationActions";
    String ExtraKeyIcon = "ExtraKeyIcon";

    void HandleStatusBarNotification(StatusBarNotification statusBarNotification, @NonNull String intentAction);
}
