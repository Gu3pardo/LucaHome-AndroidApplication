package guepardoapps.lucahome.common.classes.mediaserver;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.enums.MediaServerSelection;
import guepardoapps.lucahome.common.interfaces.classes.IMediaServerClass;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MediaServerInformationData implements IMediaServerClass, Serializable {
    private static final long serialVersionUID = 1839400020675481913L;

    private static final String TAG = MediaServerInformationData.class.getSimpleName();

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
        return new Gson().toJson(this);
    }

    @Override
    public void ParseCommunicationString(@NonNull String communicationString) throws Exception {
        if (communicationString.length() == 0) {
            throw new NullPointerException("CommunicationString may not be of length 0!");
        }
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "CommunicationString is %s", communicationString));

        MediaServerInformationData tempMediaServerInformationData = new Gson().fromJson(communicationString, MediaServerInformationData.class);
        _mediaServerSelection = tempMediaServerInformationData.GetMediaServerSelection();
        _serverVersion = tempMediaServerInformationData.GetServerVersion();
        _currentVolume = tempMediaServerInformationData.GetCurrentVolume();
        _currentBatteryLevel = tempMediaServerInformationData.GetCurrentBatteryLevel();
        _currentScreenBrightness = tempMediaServerInformationData.GetCurrentScreenBrightness();
    }

    @Override
    public String toString() {
        return String.format(
                Locale.getDefault(),
                "{%s:{MediaServerSelection:%s},{ServerVersion:%s},{Volume:%d},{BatteryLevel:%d},{ScreenBrightness:%d}}",
                TAG, _mediaServerSelection, _serverVersion, _currentVolume, _currentBatteryLevel, _currentScreenBrightness);
    }
}
