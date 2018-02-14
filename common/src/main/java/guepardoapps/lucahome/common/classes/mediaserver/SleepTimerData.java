package guepardoapps.lucahome.common.classes.mediaserver;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.Locale;

import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"WeakerAccess"})
public class SleepTimerData implements IMediaServerClass {
    private static final String Tag = SleepTimerData.class.getSimpleName();

    private boolean _sleepTimerEnabled;
    private int _sleepTimerCountDownSec;

    public SleepTimerData(
            boolean sleepTimerEnabled,
            int sleepTimerCountDownSec) {
        _sleepTimerEnabled = sleepTimerEnabled;
        _sleepTimerCountDownSec = sleepTimerCountDownSec;
    }

    public SleepTimerData() {
        this(false, -1);
    }

    public boolean GetSleepTimerEnabled() {
        return _sleepTimerEnabled;
    }

    public int GetSleepTimerCountDownSec() {
        return _sleepTimerCountDownSec;
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

        SleepTimerData tmpSleepTimerData = new Gson().fromJson(communicationString, SleepTimerData.class);
        _sleepTimerEnabled = tmpSleepTimerData.GetSleepTimerEnabled();
        _sleepTimerCountDownSec = tmpSleepTimerData.GetSleepTimerCountDownSec();
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"SleepTimerEnabled\":\"%s\",\"SleepTimerCountDownSec\":%d}",
                Tag, _sleepTimerEnabled, _sleepTimerCountDownSec);
    }
}
