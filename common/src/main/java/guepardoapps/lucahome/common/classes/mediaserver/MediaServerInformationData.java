package guepardoapps.lucahome.common.classes.mediaserver;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.enums.MediaServerSelection;
import guepardoapps.lucahome.common.interfaces.classes.IMediaServerClass;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MediaServerInformationData implements IMediaServerClass, Serializable {
    private static final long serialVersionUID = 1839400020675481913L;

    private static final String TAG = MediaServerInformationData.class.getSimpleName();
    private static final int COMMUNICATION_ENTRY_LENGTH = 5;

    private static final int INDEX_MEDIA_SERVER_SELECTION_ID = 0;
    private static final int INDEX_SERVER_VERSION = 1;
    private static final int INDEX_CURRENT_VOLUME = 2;
    private static final int INDEX_CURRENT_BATTERY_LEVEL = 3;
    private static final int INDEX_CURRENT_SCREEN_BRIGHTNESS = 4;

    public static final String SPLIT_CHAR = "::";
    public static final String END_CHAR = ";";

    private MediaServerSelection _mediaServerSelection;

    private String _serverVersion;

    private int _currentVolume;
    private int _currentBatteryLevel;
    private int _currentScreenBrightness;

    public MediaServerInformationData(
            @NonNull MediaServerSelection mediaServerSelection,
            @NonNull String serverVersion,
            int currentVolume,
            int currentBatteryLevel,
            int currentScreenBrightness) {
        _mediaServerSelection = mediaServerSelection;
        _serverVersion = serverVersion;
        _currentVolume = currentVolume;
        _currentBatteryLevel = currentBatteryLevel;
        _currentScreenBrightness = currentScreenBrightness;
    }

    public MediaServerInformationData() {
        _mediaServerSelection = MediaServerSelection.NULL;
        _serverVersion = "";
        _currentVolume = 0;
        _currentBatteryLevel = 0;
        _currentScreenBrightness = 0;
    }

    public void SetMediaServerSelection(@NonNull MediaServerSelection mediaServerSelection) {
        _mediaServerSelection = mediaServerSelection;
    }

    public MediaServerSelection GetMediaServerSelection() {
        return _mediaServerSelection;
    }

    public String GetServerVersion() {
        return _serverVersion;
    }

    public int GetCurrentVolume() {
        return _currentVolume;
    }

    public int GetCurrentBatteryLevel() {
        return _currentBatteryLevel;
    }

    public int GetCurrentScreenBrightness() {
        return _currentScreenBrightness;
    }

    @Override
    public String GetCommunicationString() {
        return String.format(Locale.getDefault(), "%d%s%s%s%d%s%d%s%d%s",
                _mediaServerSelection.GetId(), SPLIT_CHAR,
                _serverVersion, SPLIT_CHAR,
                _currentVolume, SPLIT_CHAR,
                _currentBatteryLevel, SPLIT_CHAR,
                _currentScreenBrightness, END_CHAR);
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

        _mediaServerSelection = MediaServerSelection.GetById(Integer.parseInt(entries[INDEX_MEDIA_SERVER_SELECTION_ID]));
        _serverVersion = entries[INDEX_SERVER_VERSION];
        _currentVolume = Integer.parseInt(entries[INDEX_CURRENT_VOLUME]);
        _currentBatteryLevel = Integer.parseInt(entries[INDEX_CURRENT_BATTERY_LEVEL]);
        _currentScreenBrightness = Integer.parseInt(entries[INDEX_CURRENT_SCREEN_BRIGHTNESS]);
    }

    @Override
    public String toString() {
        return String.format(
                Locale.getDefault(),
                "{%s:{MediaServerSelection:%s},{ServerVersion:%s},{Volume:%d},{BatteryLevel:%d},{ScreenBrightness:%d}}",
                TAG, _mediaServerSelection, _serverVersion, _currentVolume, _currentBatteryLevel, _currentScreenBrightness);
    }
}
