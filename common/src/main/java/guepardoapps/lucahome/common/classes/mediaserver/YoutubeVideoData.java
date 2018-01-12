package guepardoapps.lucahome.common.classes.mediaserver;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.interfaces.classes.IMediaServerClass;

@SuppressWarnings({"unused", "WeakerAccess"})
public class YoutubeVideoData implements IMediaServerClass, Serializable {
    private static final long serialVersionUID = 2691810003647772566L;

    private static final String TAG = YoutubeVideoData.class.getSimpleName();
    private static final int COMMUNICATION_ENTRY_LENGTH = 4;

    private static final int INDEX_YOUTUBE_ID = 0;
    private static final int INDEX_TITLE = 1;
    private static final int INDEX_DESCRIPTION = 2;
    private static final int INDEX_MEDIUM_IMAGE_URL = 3;

    public static final String SPLIT_CHAR = "::";
    public static final String END_CHAR = ";";

    private String _youtubeId;
    private String _title;
    private String _description;
    private String _mediumImageUrl;

    public YoutubeVideoData(
            @NonNull String youtubeId,
            @NonNull String title,
            @NonNull String description,
            @NonNull String mediumImageUrl) {
        _youtubeId = youtubeId;
        _title = title;
        _description = description;
        _mediumImageUrl = mediumImageUrl;
    }

    public String GetYoutubeId() {
        return _youtubeId;
    }

    public String GetTitle() {
        return _title;
    }

    public String GetDescription() {
        return _description;
    }

    public String GetMediumImageUrl() {
        return _mediumImageUrl;
    }

    @Override
    public String GetCommunicationString() {
        return String.format(Locale.getDefault(), "%s%s%s%s%s%s%s%s",
                _youtubeId, SPLIT_CHAR,
                _title, SPLIT_CHAR,
                _description, SPLIT_CHAR,
                _mediumImageUrl, END_CHAR);
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

        _youtubeId = entries[INDEX_YOUTUBE_ID];
        _title = entries[INDEX_TITLE];
        _description = entries[INDEX_DESCRIPTION];
        _mediumImageUrl = entries[INDEX_MEDIUM_IMAGE_URL];
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%s:{YoutubeId: %s},{Title: %s},{Description: %s},{MediumImageUrl: %s}", TAG, _youtubeId, _title, _description, _mediumImageUrl);
    }
}
