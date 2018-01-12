package guepardoapps.lucahome.common.classes.mediaserver;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.interfaces.classes.IMediaServerClass;

@SuppressWarnings({"unused", "WeakerAccess"})
public class SleepTimerData implements IMediaServerClass, Serializable {
    private static final long serialVersionUID = 2691811435346000099L;

    private static final String TAG = SleepTimerData.class.getSimpleName();
    private static final int COMMUNICATION_ENTRY_LENGTH = 2;

    private static final int INDEX_SLEEP_TIMER_IS_ENABLED = 0;
    private static final int INDEX_SLEEP_TIMER_COUNTDOWN_SEC = 1;

    public static final String SPLIT_CHAR = "::";
    public static final String END_CHAR = ";";

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
        return String.format(Locale.getDefault(), "%s%s%d%s",
                _sleepTimerEnabled, SPLIT_CHAR,
                _sleepTimerCountDownSec, END_CHAR);
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

        _sleepTimerEnabled = entries[INDEX_SLEEP_TIMER_IS_ENABLED].contains("1");
        _sleepTimerCountDownSec = Integer.parseInt(entries[INDEX_SLEEP_TIMER_COUNTDOWN_SEC]);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{%s:{SleepTimerEnabled:%s},{SleepTimerCountDownSec:%d}}",
                TAG, _sleepTimerEnabled, _sleepTimerCountDownSec);
    }
}
