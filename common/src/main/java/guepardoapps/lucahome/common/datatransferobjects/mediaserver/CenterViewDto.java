package guepardoapps.lucahome.common.datatransferobjects.mediaserver;

import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.lucahome.common.enums.RadioStreamType;
import guepardoapps.lucahome.common.enums.YoutubeIdType;
import guepardoapps.lucahome.common.utils.Logger;

public class CenterViewDto implements ICenterViewDto {
    private static final String Tag = CenterViewDto.class.getSimpleName();

    private static final String DefaultCenterText = "Hello, this is your media server!";
    private static final String DefaultWebViewUrl = "http://imgur.com/";
    private static final RadioStreamType DefaultRadioStreamType = RadioStreamType.BAYERN_3;
    private static final String DefaultMediaNotificationTitle = "BubbleUPNP is running";

    private boolean _centerVisible;
    private String _centerText;

    private boolean _youtubeVisible;
    private YoutubeIdType _youtubeId;

    private boolean _webViewVisible;
    private String _webViewUrl;

    private boolean _radioStreamVisible;
    private RadioStreamType _radioStream;

    private boolean _mediaNotificationVisible;
    private String _mediaNotificationTitle;

    public CenterViewDto(
            boolean centerVisible, @NonNull String centerText,
            boolean youtubeVisible, @NonNull YoutubeIdType youtubeId,
            boolean webViewVisible, @NonNull String webViewUrl,
            boolean radioStreamVisible, @NonNull RadioStreamType radioStream,
            boolean mediaNotificationVisible, @NonNull String mediaNotificationTitle) {
        _centerVisible = centerVisible;
        _centerText = centerText;

        _youtubeVisible = youtubeVisible;
        _youtubeId = youtubeId;

        _webViewVisible = webViewVisible;
        _webViewUrl = webViewUrl;

        _radioStreamVisible = radioStreamVisible;
        _radioStream = radioStream;

        _mediaNotificationVisible = mediaNotificationVisible;
        _mediaNotificationTitle = mediaNotificationTitle;

        CheckPlausibility();
    }

    @Override
    public boolean IsCenterVisible() {
        return _centerVisible;
    }

    @Override
    public String GetCenterText() {
        return _centerText;
    }

    @Override
    public boolean IsYoutubeVisible() {
        return _youtubeVisible;
    }

    @Override
    public YoutubeIdType GetYoutubeId() {
        return _youtubeId;
    }

    @Override
    public boolean IsWebViewVisible() {
        return _webViewVisible;
    }

    @Override
    public String GetWebViewUrl() {
        return _webViewUrl;
    }

    @Override
    public boolean IsRadioStreamVisible() {
        return _radioStreamVisible;
    }

    @Override
    public RadioStreamType GetRadioStream() {
        return _radioStream;
    }

    @Override
    public boolean IsMediaNotificationVisible() {
        return _mediaNotificationVisible;
    }

    @Override
    public String GetMediaNotificationTitle() {
        return _mediaNotificationTitle;
    }

    @Override
    public void CheckPlausibility() {
        if ((_centerVisible && _youtubeVisible)
                || (_centerVisible && _webViewVisible)
                || (_centerVisible && _radioStreamVisible)
                || (_centerVisible && _mediaNotificationVisible)
                || (_youtubeVisible && _webViewVisible)
                || (_youtubeVisible && _radioStreamVisible)
                || (_youtubeVisible && _mediaNotificationVisible)
                || (_webViewVisible && _radioStreamVisible)
                || (_webViewVisible && _mediaNotificationVisible)
                || (_radioStreamVisible && _mediaNotificationVisible)) {

            Logger.getInstance().Warning(Tag, "Invalid visibilities!");

            if (_youtubeId != YoutubeIdType.NULL) {
                _youtubeVisible = true;
                Logger.getInstance().Warning(Tag, "Resetting center, webView, radio and mediaNotification!");

                _centerVisible = false;
                _centerText = "";

                _webViewVisible = false;
                _webViewUrl = "";

                _radioStreamVisible = false;
                _radioStream = RadioStreamType.BAYERN_3;

                _mediaNotificationVisible = false;
                _mediaNotificationTitle = "";
            }

            if (_centerText.length() > 0) {
                _youtubeVisible = false;
                _youtubeId = YoutubeIdType.NULL;

                _centerVisible = true;
                Logger.getInstance().Warning(Tag, "Resetting youtube, webView, radio and mediaNotification!");

                _webViewVisible = false;
                _webViewUrl = "";

                _radioStreamVisible = false;
                _radioStream = RadioStreamType.BAYERN_3;

                _mediaNotificationVisible = false;
                _mediaNotificationTitle = "";
            }

            if (_webViewUrl.length() > 0) {
                _youtubeVisible = false;
                _youtubeId = YoutubeIdType.NULL;

                _centerVisible = false;
                _centerText = "";

                _webViewVisible = true;
                Logger.getInstance().Warning(Tag, "Resetting youtube, center, radio  and mediaNotification!");

                _radioStreamVisible = false;
                _radioStream = RadioStreamType.BAYERN_3;

                _mediaNotificationVisible = false;
                _mediaNotificationTitle = "";
            }

            if (_radioStream != null) {
                _youtubeVisible = false;
                _youtubeId = YoutubeIdType.NULL;

                _centerVisible = false;
                _centerText = "";

                _webViewVisible = false;
                _webViewUrl = "";

                _radioStreamVisible = true;
                Logger.getInstance().Warning(Tag, "Resetting youtube, center, webView and mediaNotification!");

                _mediaNotificationVisible = false;
                _mediaNotificationTitle = "";
            }

            if (_mediaNotificationTitle.length() > 0) {
                _youtubeVisible = false;
                _youtubeId = YoutubeIdType.NULL;

                _centerVisible = false;
                _centerText = "";

                _webViewVisible = false;
                _webViewUrl = "";

                _radioStreamVisible = false;
                _radioStream = RadioStreamType.BAYERN_3;

                _mediaNotificationVisible = true;
                Logger.getInstance().Warning(Tag, "Resetting youtube, center, radio and webView!");
            }
        }

        if (_centerVisible) {
            if (_centerText.length() == 0) {
                Logger.getInstance().Warning(Tag, "Setting center to default: " + DefaultCenterText);
                _centerText = DefaultCenterText;
            }
        } else if (_youtubeVisible) {
            if (_youtubeId == YoutubeIdType.NULL) {
                Logger.getInstance().Warning(Tag, "Setting videoUrl to default!");
                _youtubeId = YoutubeIdType.DEFAULT;
            }
        } else if (_webViewVisible) {
            if (_webViewUrl.length() == 0) {
                Logger.getInstance().Warning(Tag, "Setting webViewUrl to default: " + DefaultWebViewUrl);
                _webViewUrl = DefaultWebViewUrl;
            }
        } else if (_radioStreamVisible) {
            if (_radioStream == null || _radioStream == RadioStreamType.BAYERN_3) {
                Logger.getInstance().Warning(Tag, "Setting radioStream to default: " + DefaultRadioStreamType.toString());
                _radioStream = DefaultRadioStreamType;
            }
        } else if (_mediaNotificationVisible) {
            if (_mediaNotificationTitle.length() == 0) {
                Logger.getInstance().Warning(Tag, "Setting mediaNotificationTitle to default: " + DefaultMediaNotificationTitle);
                _mediaNotificationTitle = DefaultMediaNotificationTitle;
            }
        } else {
            _centerVisible = true;
            if (_centerText.length() == 0) {
                Logger.getInstance().Warning(Tag, "Setting center to default!");
                _centerText = DefaultCenterText;
            }
        }
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"CenterVisible\":\"%s\",\"CenterText\":\"%s\",\"YoutubeVisible\":\"%s\",\"YoutubeId\":\"%s\",\"WebViewVisible\":\"%s\",\"WebViewUrl\":\"%s\",\"RadioStreamVisible\":\"%s\",\"RadioStream\":\"%s\",\"MediaNotificationVisible\":\"%s\",\"MediaNotificationTitle\":\"%s\"}",
                Tag, _centerVisible, _centerText, _youtubeVisible, _youtubeId, _webViewVisible, _webViewUrl, _radioStreamVisible, _radioStream, _mediaNotificationVisible, _mediaNotificationTitle);
    }
}
