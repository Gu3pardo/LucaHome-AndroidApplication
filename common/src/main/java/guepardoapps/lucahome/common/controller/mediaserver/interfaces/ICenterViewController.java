package guepardoapps.lucahome.common.controller.mediaserver.interfaces;

import java.util.ArrayList;

import guepardoapps.lucahome.common.classes.RadioStream;
import guepardoapps.lucahome.common.classes.YoutubeVideo;
import guepardoapps.lucahome.common.enums.YoutubeIdType;

public interface ICenterViewController {
    String GetCenterText();

    boolean IsRadioStreamPlaying();

    RadioStream GetRadioStream();

    boolean IsYoutubePlaying();

    YoutubeIdType GetYoutubeId();

    int GetCurrentPlayPosition();

    int GetYoutubeDuration();

    ArrayList<YoutubeVideo> GetYoutubeVideoList();
}
