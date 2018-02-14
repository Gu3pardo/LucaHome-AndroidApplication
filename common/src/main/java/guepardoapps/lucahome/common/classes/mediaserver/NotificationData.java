package guepardoapps.lucahome.common.classes.mediaserver;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.Locale;

import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"WeakerAccess"})
public class NotificationData implements IMediaServerClass {
    private static final String Tag = NotificationData.class.getSimpleName();

    private boolean _visible;
    private String _packageName;
    private String _title;
    private String _text;
    private String _tickerText;

    public NotificationData(boolean visible, @NonNull String packageName, @NonNull String title, @NonNull String text, @NonNull String tickerText) {
        _visible = visible;
        _packageName = packageName;
        _title = title;
        _text = text;
        _tickerText = tickerText;
    }

    public NotificationData() {
        this(false, "", "", "", "");
    }

    public boolean IsVisible() {
        return _visible;
    }

    public String GetPackageName() {
        return _packageName;
    }

    public String GetTitle() {
        return _title;
    }

    public String GetText() {
        return _text;
    }

    public String GetTickerText() {
        return _tickerText;
    }

    public String GetInformationString() {
        return String.format(Locale.getDefault(), "%s\n%s : %s", _packageName, _title, _text);
    }

    @Override
    public String GetCommunicationString() {
        return new Gson().toJson(this);
    }

    @Override
    public void ParseCommunicationString(@NonNull String communicationString) throws Exception {
        if (communicationString.length() == 0) {
            throw new NullPointerException("CommunicationString may not be of length 0!");
        }
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "CommunicationString is %s", communicationString));

        NotificationData tmpNotificationData = new Gson().fromJson(communicationString, NotificationData.class);
        _visible = tmpNotificationData.IsVisible();
        _packageName = tmpNotificationData.GetPackageName();
        _title = tmpNotificationData.GetTitle();
        _text = tmpNotificationData.GetText();
        _tickerText = tmpNotificationData.GetTickerText();
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"Visible\":\"%s\",\"PackageName\":\"%s\",\"Title\":\"%s\",\"Text\":\"%s\",\"TickerText\":\"%s\"}",
                Tag, _visible, _packageName, _title, _text, _tickerText);
    }
}
