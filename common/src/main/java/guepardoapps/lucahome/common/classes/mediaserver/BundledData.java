package guepardoapps.lucahome.common.classes.mediaserver;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.Locale;

import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"WeakerAccess"})
public class BundledData implements IMediaServerClass {
    private static final String Tag = BundledData.class.getSimpleName();

    private InformationData _informationData;
    private NotificationData _notificationData;
    private RadioStreamData _radioStreamData;
    private SleepTimerData _sleepTimerData;
    private YoutubeData _youtubeData;
    private boolean _validModel;

    public BundledData(
            InformationData informationData,
            NotificationData notificationData,
            RadioStreamData radioStreamData,
            SleepTimerData sleepTimerData,
            YoutubeData youtubeData,
            boolean validModel) {
        _informationData = informationData;
        _notificationData = notificationData;
        _radioStreamData = radioStreamData;
        _sleepTimerData = sleepTimerData;
        _youtubeData = youtubeData;
        _validModel = validModel;
    }

    public BundledData() {
        this(new InformationData(), new NotificationData(), new RadioStreamData(), new SleepTimerData(), new YoutubeData(), false);
    }

    public void SetInformationData(@NonNull InformationData informationData) {
        _informationData = informationData;
    }

    public InformationData GetInformationData() {
        return _informationData;
    }

    public void SetNotificationData(@NonNull NotificationData notificationData) {
        _notificationData = notificationData;
    }

    public NotificationData GetNotificationData() {
        return _notificationData;
    }

    public void SetRadioStreamData(@NonNull RadioStreamData radioStreamData) {
        _radioStreamData = radioStreamData;
    }

    public RadioStreamData GetRadioStreamData() {
        return _radioStreamData;
    }

    public void SetSleepTimerData(@NonNull SleepTimerData sleepTimerData) {
        _sleepTimerData = sleepTimerData;
    }

    public SleepTimerData GetSleepTimerData() {
        return _sleepTimerData;
    }

    public void SetYoutubeData(@NonNull YoutubeData youtubeData) {
        _youtubeData = youtubeData;
    }

    public YoutubeData GetYoutubeData() {
        return _youtubeData;
    }

    public boolean IsValidModel() {
        return _validModel;
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

        BundledData tmpBundledData = new Gson().fromJson(communicationString, BundledData.class);
        _informationData = tmpBundledData.GetInformationData();
        _notificationData = tmpBundledData.GetNotificationData();
        _radioStreamData = tmpBundledData.GetRadioStreamData();
        _sleepTimerData = tmpBundledData.GetSleepTimerData();
        _youtubeData = tmpBundledData.GetYoutubeData();
        _validModel = tmpBundledData.IsValidModel();
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"InformationData\":\"%s\",\"NotificationData\":\"%s\",\"RadioStreamData\":\"%s\",\"SleepTimerData\":\"%s\",\"YoutubeData\":\"%s\",\"ValidModel\":\"%s\"}",
                Tag, _informationData, _notificationData, _radioStreamData, _sleepTimerData, _youtubeData, _validModel);
    }
}
