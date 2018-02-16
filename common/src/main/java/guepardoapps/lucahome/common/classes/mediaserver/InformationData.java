package guepardoapps.lucahome.common.classes.mediaserver;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.Locale;

import guepardoapps.lucahome.common.classes.RssFeed;
import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"WeakerAccess"})
public class InformationData implements IMediaServerClass {
    private static final String Tag = InformationData.class.getSimpleName();

    private String _serverVersion;
    private String _centerText;
    private RssFeed _currentRssFeed;
    private int _currentVolume;
    private int _currentBatteryLevel;
    private int _currentScreenBrightness;

    public InformationData(
            @NonNull String serverVersion,
            @NonNull String centerText,
            @NonNull RssFeed currentRssFeed,
            int currentVolume,
            int currentBatteryLevel,
            int currentScreenBrightness) {
        _serverVersion = serverVersion;
        _centerText = centerText;
        _currentRssFeed = currentRssFeed;
        _currentVolume = currentVolume;
        _currentBatteryLevel = currentBatteryLevel;
        _currentScreenBrightness = currentScreenBrightness;
    }

    public InformationData() {
        this("", "", new RssFeed(), -1, -1, -1);
    }

    public String GetServerVersion() {
        return _serverVersion;
    }

    public String GetCenterText() {
        return _centerText;
    }

    public RssFeed GetRSSFeed() {
        return _currentRssFeed;
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
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "CommunicationString is %s", communicationString));

        InformationData tmpInformationData = new Gson().fromJson(communicationString, InformationData.class);
        _serverVersion = tmpInformationData.GetServerVersion();
        _centerText = tmpInformationData.GetCenterText();
        _currentRssFeed = tmpInformationData.GetRSSFeed();
        _currentVolume = tmpInformationData.GetCurrentVolume();
        _currentBatteryLevel = tmpInformationData.GetCurrentBatteryLevel();
        _currentScreenBrightness = tmpInformationData.GetCurrentScreenBrightness();
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"ServerVersion\":\"%s\",\"CenterText\":\"%s\",\"CurrentRssFeed\":\"%s\",\"CurrentVolume\":%d,\"CurrentBatteryLevel\":%d,\"CurrentScreenBrightness\":%d}",
                Tag, _serverVersion, _centerText, _currentRssFeed, _currentVolume, _currentBatteryLevel, _currentScreenBrightness);
    }
}
