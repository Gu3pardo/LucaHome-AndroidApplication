package guepardoapps.lucahome.common.classes.mediaserver;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.interfaces.classes.IMediaServerClass;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MediaNotificationData implements IMediaServerClass, Serializable {
    private static final long serialVersionUID = 1115511435346489002L;

    private static final String TAG = MediaNotificationData.class.getSimpleName();

    private boolean _visible;
    private String _packageName;
    private String _title;
    private String _text;
    private String _tickerText;

    public MediaNotificationData(
            boolean visible,
            @NonNull String packageName,
            @NonNull String title,
            @NonNull String text,
            @NonNull String tickerText) {
        _visible = visible;
        _packageName = packageName;
        _title = title;
        _text = text;
        _tickerText = tickerText;
    }

    public MediaNotificationData() {
        _visible = false;
        _packageName = "";
        _title = "";
        _text = "";
        _tickerText = "";
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
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "CommunicationString is %s", communicationString));

        MediaNotificationData tempMediaNotificationData = new Gson().fromJson(communicationString, MediaNotificationData.class);
        _visible = tempMediaNotificationData.IsVisible();
        _packageName = tempMediaNotificationData.GetPackageName();
        _title = tempMediaNotificationData.GetTitle();
        _text = tempMediaNotificationData.GetText();
        _tickerText = tempMediaNotificationData.GetTickerText();
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{%s:{Visible:%s},{PackageName:%s},{Title:%s},{Text:%s},{TickerText:%s}}",
                TAG, _visible, _packageName, _title, _text, _tickerText);
    }
}
