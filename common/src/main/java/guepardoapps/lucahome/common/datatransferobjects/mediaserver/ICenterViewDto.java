package guepardoapps.lucahome.common.datatransferobjects.mediaserver;

import guepardoapps.lucahome.common.enums.RadioStreamType;
import guepardoapps.lucahome.common.enums.YoutubeIdType;

public interface ICenterViewDto {
    boolean IsCenterVisible();

    String GetCenterText();

    boolean IsYoutubeVisible();

    YoutubeIdType GetYoutubeId();

    boolean IsWebViewVisible();

    String GetWebViewUrl();

    boolean IsRadioStreamVisible();

    RadioStreamType GetRadioStream();

    boolean IsMediaNotificationVisible();

    String GetMediaNotificationTitle();

    void CheckPlausibility();
}
