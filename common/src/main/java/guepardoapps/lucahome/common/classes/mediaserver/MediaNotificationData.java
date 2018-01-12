package guepardoapps.lucahome.common.classes.mediaserver;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.interfaces.classes.IMediaServerClass;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MediaNotificationData implements IMediaServerClass, Serializable {
    private static final long serialVersionUID = 1115511435346489002L;

    private static final String TAG = MediaNotificationData.class.getSimpleName();
    private static final int COMMUNICATION_ENTRY_LENGTH = 5;

    private static final int INDEX_VISIBLE = 0;
    private static final int INDEX_PACKAGE_NAME = 1;
    private static final int INDEX_TITLE = 2;
    private static final int INDEX_TEXT = 3;
    private static final int INDEX_TICKER_TEXT = 4;

    public static final String SPLIT_CHAR = "::";
    public static final String END_CHAR = ";";

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
        return String.format(Locale.getDefault(), "%s%s%s%s%s%s%s%s%s%s",
                _visible, SPLIT_CHAR,
                _packageName, SPLIT_CHAR,
                _title, SPLIT_CHAR,
                _text, SPLIT_CHAR,
                _tickerText, END_CHAR);
    }

    @Override
    public void ParseCommunicationString(@NonNull String communicationString) throws Exception {
        if (communicationString.length() == 0) {
            throw new NullPointerException("CommunicationString may not be of length 0!");
        }
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "CommunicationString is %s", communicationString));
        communicationString = communicationString.replace(END_CHAR, "");

        String[] entries = communicationString.split(SPLIT_CHAR);
        if (entries.length != COMMUNICATION_ENTRY_LENGTH) {
            throw new IndexOutOfBoundsException(String.format(Locale.getDefault(), "Invalid length %d for entries in %s!", entries.length, TAG));
        }

        _visible = Boolean.getBoolean(entries[INDEX_VISIBLE]);
        _packageName = entries[INDEX_PACKAGE_NAME];
        _title = entries[INDEX_TITLE];
        _text = entries[INDEX_TEXT];
        _tickerText = entries[INDEX_TICKER_TEXT];
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{%s:{Visible:%s},{PackageName:%s},{Title:%s},{Text:%s},{TickerText:%s}}",
                TAG, _visible, _packageName, _title, _text, _tickerText);
    }
}
