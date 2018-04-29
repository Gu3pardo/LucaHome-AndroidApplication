package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.mediaserver.IMediaServerClass;
import guepardoapps.lucahome.common.enums.LucaServerActionTypes;
import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"unused", "WeakerAccess"})
public class YoutubeVideo implements ILucaClass, IMediaServerClass {
    private static final String Tag = YoutubeVideo.class.getSimpleName();

    private UUID _uuid;
    private String _title;
    private String _youtubeId;
    private int _playCount;

    private String _description;
    private String _mediumImageUrl;

    private boolean _isOnServer;
    private ILucaClass.LucaServerDbAction _serverDbAction;

    public YoutubeVideo(
            @NonNull UUID uuid,
            @NonNull String title,
            @NonNull String youtubeId,
            int playCount,
            @NonNull String description,
            @NonNull String mediumImageUrl,
            boolean isOnServer,
            @NonNull ILucaClass.LucaServerDbAction serverDbAction) {
        _uuid = uuid;
        _title = title;
        _youtubeId = youtubeId;
        _playCount = playCount;
        _description = description;
        _mediumImageUrl = mediumImageUrl;
        _isOnServer = isOnServer;
        _serverDbAction = serverDbAction;
    }

    public YoutubeVideo(
            @NonNull UUID uuid,
            @NonNull String title,
            @NonNull String youtubeId,
            int playCount,
            @NonNull String description,
            @NonNull String mediumImageUrl) {
        this(uuid, title, youtubeId, playCount, description, mediumImageUrl, true, ILucaClass.LucaServerDbAction.Null);
    }

    public YoutubeVideo(
            @NonNull UUID uuid,
            @NonNull String title,
            @NonNull String youtubeId,
            int playCount) {
        this(uuid, title, youtubeId, playCount, "", "");
    }

    @Override
    public UUID GetUuid() {
        return _uuid;
    }

    @Override
    public UUID GetRoomUuid() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetRoomUuid not implemented for " + Tag);
    }

    public String GetTitle() {
        return _title;
    }

    public String GetYoutubeId() {
        return _youtubeId;
    }

    public int GetPlayCount() {
        return _playCount;
    }

    public void SetDescription(@NonNull String description) {
        _description = description;
    }

    public String GetDescription() {
        return _description;
    }

    public void SetMediumImageUrl(@NonNull String mediumImageUrl) {
        _mediumImageUrl = mediumImageUrl;
    }

    public String GetMediumImageUrl() {
        return _mediumImageUrl;
    }

    @Override
    public String GetCommunicationString() {
        return new Gson().toJson(this);
    }

    @Override
    public void ParseCommunicationString(@NonNull String communicationString) throws NullPointerException {
        if (communicationString.length() == 0) {
            throw new NullPointerException("CommunicationString may not be of length 0!");
        }
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "CommunicationString is %s", communicationString));

        YoutubeVideo tmpYoutubeVideoData = new Gson().fromJson(communicationString, YoutubeVideo.class);
        _uuid = tmpYoutubeVideoData.GetUuid();
        _youtubeId = tmpYoutubeVideoData.GetYoutubeId();
        _playCount = tmpYoutubeVideoData.GetPlayCount();
        _title = tmpYoutubeVideoData.GetTitle();
        _description = tmpYoutubeVideoData.GetDescription();
        _mediumImageUrl = tmpYoutubeVideoData.GetMediumImageUrl();
    }

    @Override
    public String GetCommandAdd() {
        return String.format(Locale.getDefault(),
                "%s%s&title=%s&youtubeid=%s&playcount=%d",
                LucaServerActionTypes.ADD_YOUTUBE_VIDEO.toString(), _uuid, _title, _youtubeId, _playCount);
    }

    @Override
    public String GetCommandUpdate() {
        return String.format(Locale.getDefault(),
                "%s%s&title=%s&youtubeid=%s&playcount=%d",
                LucaServerActionTypes.UPDATE_YOUTUBE_VIDEO.toString(), _uuid, _title, _youtubeId, _playCount);
    }

    @Override
    public String GetCommandDelete() {
        return String.format(Locale.getDefault(),
                "%s%s",
                LucaServerActionTypes.DELETE_YOUTUBE_VIDEO.toString(), _uuid);
    }

    @Override
    public void SetIsOnServer(boolean isOnServer) {
        _isOnServer = isOnServer;
    }

    @Override
    public boolean GetIsOnServer() {
        return _isOnServer;
    }

    @Override
    public void SetServerDbAction(@NonNull ILucaClass.LucaServerDbAction serverDbAction) {
        _serverDbAction = serverDbAction;
    }

    @Override
    public ILucaClass.LucaServerDbAction GetServerDbAction() {
        return _serverDbAction;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"Title\":\"%s\",\"YoutubeId\":\"%s\",\"PlayCount\":%d,\"Description\":\"%s\",\"MediumImageUrl\":\"%s\"}",
                Tag, _uuid, _title, _youtubeId, _playCount, _description, _mediumImageUrl);
    }
}
