package guepardoapps.lucahome.common.classes.mediaserver;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.common.enums.RSSFeed;

@SuppressWarnings({"unused"})
public class MediaServerData implements Serializable {
    private static final long serialVersionUID = -6336573950675481913L;

    private static final String TAG = MediaServerData.class.getSimpleName();

    private YoutubeData _youtubeData;
    private String _centerText;
    private RadioStreamData _radioStreamData;
    private MediaNotificationData _mediaNotificationData;
    private SleepTimerData _sleepTimerData;
    private RSSFeed _currentRssFeed;
    private MediaServerInformationData _mediaServerInformationData;
    private boolean _validModel;

    public MediaServerData(
            @NonNull YoutubeData youtubeData,
            @NonNull String centerText,
            @NonNull RadioStreamData radioStreamData,
            @NonNull MediaNotificationData mediaNotificationData,
            @NonNull SleepTimerData sleepTimerData,
            @NonNull RSSFeed currentRssFeed,
            @NonNull MediaServerInformationData mediaServerInformationData,
            boolean validModel) {
        _youtubeData = youtubeData;
        _centerText = centerText;
        _radioStreamData = radioStreamData;
        _mediaNotificationData = mediaNotificationData;
        _sleepTimerData = sleepTimerData;
        _currentRssFeed = currentRssFeed;
        _mediaServerInformationData = mediaServerInformationData;
        _validModel = validModel;
    }

    public void SetYoutubeData(@NonNull YoutubeData youtubeData) {
        _youtubeData = youtubeData;
    }

    public YoutubeData GetYoutubeData() {
        return _youtubeData;
    }

    public void SetCenterText(@NonNull String centerText) {
        _centerText = centerText;
    }

    public String GetCenterText() {
        return _centerText;
    }

    public void SetRadioStreamData(@NonNull RadioStreamData radioStreamData) {
        _radioStreamData = radioStreamData;
    }

    public RadioStreamData GetRadioStreamData() {
        return _radioStreamData;
    }

    public void SetMediaNotificationData(@NonNull MediaNotificationData mediaNotificationData) {
        _mediaNotificationData = mediaNotificationData;
    }

    public MediaNotificationData GetMediaNotificationData() {
        return _mediaNotificationData;
    }

    public void SetSleepTimerData(@NonNull SleepTimerData sleepTimerData) {
        _sleepTimerData = sleepTimerData;
    }

    public SleepTimerData GetSleepTimerData() {
        return _sleepTimerData;
    }

    public void SetRSSFeed(@NonNull RSSFeed currentRssFeed) {
        _currentRssFeed = currentRssFeed;
    }

    public RSSFeed GetRSSFeed() {
        return _currentRssFeed;
    }

    public void SetMediaServerInformationData(@NonNull MediaServerInformationData mediaServerInformationData) {
        _mediaServerInformationData = mediaServerInformationData;
        _validModel = true;
    }

    public MediaServerInformationData GetMediaServerInformationData() {
        return _mediaServerInformationData;
    }

    public boolean IsValidModel() {
        return _validModel;
    }

    @Override
    public String toString() {
        return String.format(
                Locale.getDefault(),
                "{%s:{YoutubeData:%s},{CenterText:%s},{RadioStreamData:%s},{MediaNotificationData:%s},{SleepTimerData:%s},{RSSFeed:%s},{MediaServerInformationData:%s},{ValidModel:%s}}",
                TAG, _youtubeData, _centerText, _radioStreamData, _mediaNotificationData, _sleepTimerData, _currentRssFeed, _mediaServerInformationData, _validModel);
    }
}
