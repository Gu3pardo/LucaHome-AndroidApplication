package guepardoapps.lucahome.common.services;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.MediaServer;

@SuppressWarnings({"unused"})
public interface IMediaServerClientService extends ILucaService<MediaServer> {
    String MediaServerDataDownloadFinishedBroadcast = "guepardoapps.lucahome.common.services.change.download.finished";
    String MediaServerCommandResponseBroadcast = "guepardoapps.lucahome.common.services.mediaServer.command_response";
    String MediaServerInformationDataBroadcast = "guepardoapps.lucahome.common.services.mediaServer.information_data";
    String MediaServerNotificationDataBroadcast = "guepardoapps.lucahome.common.services.mediaServer.notification_data";
    String MediaServerRadioStreamDataBroadcast = "guepardoapps.lucahome.common.services.mediaServer.radio_stream_data";
    String MediaServerSleepTimerDataBroadcast = "guepardoapps.lucahome.common.services.mediaServer.sleep_timer_data";
    String MediaServerYoutubeDataBroadcast = "guepardoapps.lucahome.common.services.mediaServer.youtube_data";

    String MediaServerDataDownloadFinishedBundle = "MediaServerDataDownloadFinishedBundle";
    String MediaServerCommandResponseBundle = "MediaServerCommandResponseBundle";
    String MediaServerInformationDataBundle = "MediaServerInformationDataBundle";
    String MediaServerNotificationDataBundle = "MediaServerNotificationDataBundle";
    String MediaServerRadioStreamDataBundle = "MediaServerRadioStreamDataBundle";
    String MediaServerSleepTimerDataBundle = "MediaServerSleepTimerDataBundle";
    String MediaServerYoutubeDataBundle = "MediaServerYoutubeDataBundle";

    int IndexCommandAction = 0;
    int IndexCommandData = 1;
    int CommandDataSize = 2;

    int IndexResponseFlag = 0;
    int IndexResponseMessage = 1;
    int IndexResponseAction = 2;
    int IndexResponseData = 3;
    int ResponseDataSize = 4;

    void SetActiveMediaServer(@NonNull MediaServer mediaServer);

    MediaServer GetActiveMediaServer();

    MediaServer GetMediaServerByIp(@NonNull String ip);

    ArrayList<UUID> GetRoomUuidList();

    ArrayList<String> GetRadioStreamTitleList();

    ArrayList<String> GetRssFeedTitleList();

    void SendCommand(@NonNull String command, @NonNull String data);
}
