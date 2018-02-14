package guepardoapps.lucahome.common.controller.mediaserver.interfaces;

import java.util.ArrayList;

import guepardoapps.lucahome.common.classes.mediaserver.PlayedYoutubeVideo;
import guepardoapps.lucahome.common.enums.RadioStreamType;
import guepardoapps.lucahome.common.enums.YoutubeIdType;

public interface ICenterViewController {
    String GetCenterText();

    boolean IsRadioStreamPlaying();

    RadioStreamType GetRadioStream();

    boolean IsYoutubePlaying();

    YoutubeIdType GetYoutubeId();

    int GetCurrentPlayPosition();

    int GetYoutubeDuration();

    ArrayList<PlayedYoutubeVideo> GetYoutubeIds();
}
