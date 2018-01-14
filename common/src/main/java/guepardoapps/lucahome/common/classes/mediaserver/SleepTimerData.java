package guepardoapps.lucahome.common.classes.mediaserver;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.interfaces.classes.IMediaServerClass;

@SuppressWarnings({"unused", "WeakerAccess"})
public class SleepTimerData implements IMediaServerClass, Serializable {
    private static final long serialVersionUID = 2691811435346000099L;

    private static final String TAG = SleepTimerData.class.getSimpleName();

    private boolean _sleepTimerEnabled;
    private int _sleepTimerCountDownSec;

    public SleepTimerData(
            boolean sleepTimerEnabled,
            int sleepTimerCountDownSec) {
        _sleepTimerEnabled = sleepTimerEnabled;
        _sleepTimerCountDownSec = sleepTimerCountDownSec;
    }

    public SleepTimerData() {
        _sleepTimerEnabled = false;
        _sleepTimerCountDownSec = 0;
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
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "CommunicationString is %s", communicationString));

        SleepTimerData tempSleepTimerData = new Gson().fromJson(communicationString, SleepTimerData.class);
        _sleepTimerEnabled = tempSleepTimerData.GetSleepTimerEnabled();
        _sleepTimerCountDownSec = tempSleepTimerData.GetSleepTimerCountDownSec();
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{%s:{SleepTimerEnabled:%s},{SleepTimerCountDownSec:%d}}",
                TAG, _sleepTimerEnabled, _sleepTimerCountDownSec);
    }
}
