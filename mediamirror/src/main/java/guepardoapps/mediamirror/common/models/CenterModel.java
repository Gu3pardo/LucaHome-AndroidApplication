package guepardoapps.mediamirror.common.models;

import android.support.annotation.NonNull;

import java.io.Serializable;

import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.enums.RadioStreams;
import guepardoapps.lucahome.common.enums.YoutubeId;

public class CenterModel implements Serializable {
    private static final String TAG = CenterModel.class.getSimpleName();

    private static final String DEFAULT_CENTER_TEXT = "Hello, this is your media server!";
    private static final String DEFAULT_WEB_VIEW_URL = "http://imgur.com/";
    private static final RadioStreams DEFAULT_RADIO_STREAM = RadioStreams.BAYERN_3;
    private static final String DEFAULT_MEDIA_NOTIFICATION_TITLE = "BubbleUPNP is running";

    private boolean _centerVisible;
    private String _centerText;

    private boolean _youtubeVisible;
    private YoutubeId _youtubeId;

    private boolean _webViewVisible;
    private String _webViewUrl;

    private boolean _radioStreamVisible;
    private RadioStreams _radioStream;

    private boolean _mediaNotificationVisible;
    private String _mediaNotificationTitle;

    public CenterModel(
            boolean centerVisible, @NonNull String centerText,
            boolean youtubeVisible, @NonNull YoutubeId youtubeId,
            boolean webViewVisible, @NonNull String webViewUrl,
            boolean radioStreamVisible, @NonNull RadioStreams radioStream,
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

        checkPlausibility();
    }

    public boolean IsCenterVisible() {
        return _centerVisible;
    }

    public String GetCenterText() {
        return _centerText;
    }

    public boolean IsYoutubeVisible() {
        return _youtubeVisible;
    }

    public YoutubeId GetYoutubeId() {
        return _youtubeId;
    }

    public boolean IsWebViewVisible() {
        return _webViewVisible;
    }

    public String GetWebViewUrl() {
        return _webViewUrl;
    }

    public boolean IsRadioStreamVisible() {
        return _radioStreamVisible;
    }

    public RadioStreams GetRadioStream() {
        return _radioStream;
    }

    public boolean IsMediaNotificationVisible() {
        return _mediaNotificationVisible;
    }

    public String GetMediaNotificationTitle() {
        return _mediaNotificationTitle;
    }

    private void checkPlausibility() {
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

            Logger.getInstance().Warning(TAG, "Invalid visibilities!");

            if (_youtubeId != YoutubeId.NULL) {
                _youtubeVisible = true;
                Logger.getInstance().Warning(TAG, "Resetting center, webView, radio and mediaNotification!");

                _centerVisible = false;
                _centerText = "";

                _webViewVisible = false;
                _webViewUrl = "";

                _radioStreamVisible = false;
                _radioStream = RadioStreams.BAYERN_3;

                _mediaNotificationVisible = false;
                _mediaNotificationTitle = "";
            }

            if (_centerText.length() > 0) {
                _youtubeVisible = false;
                _youtubeId = YoutubeId.NULL;

                _centerVisible = true;
                Logger.getInstance().Warning(TAG, "Resetting youtube, webView, radio and mediaNotification!");

                _webViewVisible = false;
                _webViewUrl = "";

                _radioStreamVisible = false;
                _radioStream = RadioStreams.BAYERN_3;

                _mediaNotificationVisible = false;
                _mediaNotificationTitle = "";
            }

            if (_webViewUrl.length() > 0) {
                _youtubeVisible = false;
                _youtubeId = YoutubeId.NULL;

                _centerVisible = false;
                _centerText = "";

                _webViewVisible = true;
                Logger.getInstance().Warning(TAG, "Resetting youtube, center, radio  and mediaNotification!");

                _radioStreamVisible = false;
                _radioStream = RadioStreams.BAYERN_3;

                _mediaNotificationVisible = false;
                _mediaNotificationTitle = "";
            }

            if (_radioStream != null || _radioStream != RadioStreams.BAYERN_3) {
                _youtubeVisible = false;
                _youtubeId = YoutubeId.NULL;

                _centerVisible = false;
                _centerText = "";

                _webViewVisible = false;
                _webViewUrl = "";

                _radioStreamVisible = true;
                Logger.getInstance().Warning(TAG, "Resetting youtube, center, webView and mediaNotification!");

                _mediaNotificationVisible = false;
                _mediaNotificationTitle = "";
            }

            if (_mediaNotificationTitle.length() > 0) {
                _youtubeVisible = false;
                _youtubeId = YoutubeId.NULL;

                _centerVisible = false;
                _centerText = "";

                _webViewVisible = false;
                _webViewUrl = "";

                _radioStreamVisible = false;
                _radioStream = RadioStreams.BAYERN_3;

                _mediaNotificationVisible = true;
                Logger.getInstance().Warning(TAG, "Resetting youtube, center, radio and webView!");
            }
        }

        if (_centerVisible) {
            if (_centerText.length() == 0) {
                Logger.getInstance().Warning(TAG, "Setting center to default: " + DEFAULT_CENTER_TEXT);
                _centerText = DEFAULT_CENTER_TEXT;
            }
        } else if (_youtubeVisible) {
            if (_youtubeId == YoutubeId.NULL) {
                Logger.getInstance().Warning(TAG, "Setting videoUrl to default!");
                _youtubeId = YoutubeId.DEFAULT;
            }
        } else if (_webViewVisible) {
            if (_webViewUrl.length() == 0) {
                Logger.getInstance().Warning(TAG, "Setting webViewUrl to default: " + DEFAULT_WEB_VIEW_URL);
                _webViewUrl = DEFAULT_WEB_VIEW_URL;
            }
        } else if (_radioStreamVisible) {
            if (_radioStream == null || _radioStream == RadioStreams.BAYERN_3) {
                Logger.getInstance().Warning(TAG, "Setting radioStream to default: " + DEFAULT_RADIO_STREAM.toString());
                _radioStream = DEFAULT_RADIO_STREAM;
            }
        } else if (_mediaNotificationVisible) {
            if (_mediaNotificationTitle.length() == 0) {
                Logger.getInstance().Warning(TAG, "Setting mediaNotificationTitle to default: " + DEFAULT_MEDIA_NOTIFICATION_TITLE);
                _mediaNotificationTitle = DEFAULT_MEDIA_NOTIFICATION_TITLE;
            }
        } else {
            _centerVisible = true;
            if (_centerText.length() == 0) {
                Logger.getInstance().Warning(TAG, "Setting center to default!");
                _centerText = DEFAULT_CENTER_TEXT;
            }
        }
    }

    @Override
    public String toString() {
        return TAG
                + ":{CenterVisible:" + String.valueOf(_centerVisible)
                + ";CenterText:" + _centerText
                + ";YoutubeVisible:" + String.valueOf(_youtubeVisible)
                + ";YoutubeId:" + _youtubeId
                + ";WebViewVisible:" + String.valueOf(_webViewVisible)
                + ";WebViewUrl:" + _webViewUrl
                + ";RadioStreamVisible:" + String.valueOf(_radioStreamVisible)
                + ";RadioStream:" + _radioStream.toString()
                + ";MediaNotificationVisible:" + String.valueOf(_mediaNotificationVisible)
                + ";MediaNotificationTitle:" + _mediaNotificationTitle
                + "}";
    }
}
