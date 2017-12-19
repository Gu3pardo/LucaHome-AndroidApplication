package guepardoapps.mediamirror.controller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.rey.material.widget.FloatingActionButton;

import java.util.ArrayList;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.constants.Keys;
import guepardoapps.lucahome.common.enums.RadioStreams;
import guepardoapps.lucahome.common.enums.YoutubeId;
import guepardoapps.lucahome.common.tasks.DownloadYoutubeVideoTask;
import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.common.models.CenterModel;
import guepardoapps.mediamirror.common.models.YoutubeDatabaseModel;
import guepardoapps.mediamirror.interfaces.IViewController;

public class CenterViewController implements IViewController, YouTubePlayer.OnInitializedListener {
    private static final String TAG = CenterViewController.class.getSimpleName();
    private Logger _logger;

    private static final CenterViewController SINGLETON = new CenterViewController();

    private boolean _isInitialized;
    private boolean _screenEnabled;

    private Context _context;
    private DatabaseController _databaseController;
    private MediaVolumeController _mediaVolumeController;
    private ReceiverController _receiverController;

    private YouTubePlayerView _youTubePlayerView;
    private TextView _centerTextView;
    private LinearLayout _centerRadioStreamLinearLayout;
    private FloatingActionButton _floatingActionButtonRadioStreamPlay;
    private FloatingActionButton _floatingActionButtonRadioStreamStop;
    private WebView _centerWebView;

    private TextView _currentPlayingTextView;

    private boolean _youTubePlayerIsInitialized;
    private YouTubePlayer _youtubePlayer;
    private boolean _loadingVideo;
    private String _youtubeId = YoutubeId.DEFAULT.GetYoutubeId();

    private RadioStreams _radioStream = RadioStreams.BAYERN_3;
    private MediaPlayer _radioPlayer;

    private boolean _loadingUrl;

    private BroadcastReceiver _pauseVideoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_pauseVideoReceiver onReceive");

            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            pauseVideo();
        }
    };

    private BroadcastReceiver _playBirthdaySongReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_playBirthdaySongReceiver onReceive");

            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            startVideo(YoutubeId.BIRTHDAY_SONG.toString());
        }
    };

    private BroadcastReceiver _playRadioStreamReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_playRadioStreamReceiver onReceive");

            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            String radioStreamId = intent.getStringExtra(Bundles.RADIO_STREAM_ID);
            if (radioStreamId != null) {
                if (radioStreamId.length() > 0) {
                    try {
                        int id = Integer.parseInt(radioStreamId);
                        RadioStreams radioStream = RadioStreams.GetById(id);
                        _radioStream = radioStream;
                    } catch (Exception exception) {
                        _logger.Error(exception.toString());
                        _radioStream = RadioStreams.BAYERN_3;
                    }
                }
            }

            stopVideo();
            stopWebViewLoading();

            startRadioPlaying();
        }
    };

    private BroadcastReceiver _playVideoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_playVideoReceiver onReceive");

            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            String youtubeId = intent.getStringExtra(Bundles.YOUTUBE_ID);
            if (youtubeId != null) {
                if (youtubeId.length() > 0) {
                    _youtubeId = youtubeId;
                }
            }

            stopRadioPlaying();
            stopWebViewLoading();

            startVideo(_youtubeId);
        }
    };

    private BroadcastReceiver _screenDisableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_screenDisableReceiver onReceive");

            _screenEnabled = false;

            pauseVideo();
            _youtubePlayer.release();
            _youTubePlayerIsInitialized = false;

            stopRadioPlaying();
        }
    };

    private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
        @SuppressLint("SetJavaScriptEnabled")
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_screenEnableReceiver onReceive");
            _screenEnabled = true;
            initializeWebView();
            activateYoutube();
        }
    };

    private BroadcastReceiver _stopRadioStreamReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_stopRadioStreamReceiver onReceive");

            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            stopRadioPlaying();
        }
    };

    private BroadcastReceiver _stopVideoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_stopVideoReceiver onReceive");

            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            stopVideo();
        }
    };

    private BroadcastReceiver _updateViewReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateViewReceiver onReceive");

            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            CenterModel model = (CenterModel) intent.getSerializableExtra(Bundles.CENTER_MODEL);

            if (model != null) {
                _logger.Debug(model.toString());

                if (model.IsCenterVisible()) {
                    stopWebViewLoading();
                    stopVideo();
                    stopRadioPlaying();

                    _youTubePlayerView.setVisibility(View.GONE);
                    _centerWebView.setVisibility(View.GONE);
                    _centerTextView.setVisibility(View.VISIBLE);
                    _centerRadioStreamLinearLayout.setVisibility(View.GONE);

                    _centerTextView.setText(model.GetCenterText());

                    _currentPlayingTextView.setText("");
                } else if (model.IsYoutubeVisible()) {
                    stopWebViewLoading();
                    stopRadioPlaying();

                    _youTubePlayerView.setVisibility(View.VISIBLE);
                    _centerWebView.setVisibility(View.GONE);
                    _centerTextView.setVisibility(View.GONE);
                    _centerRadioStreamLinearLayout.setVisibility(View.GONE);

                    _youtubeId = model.GetYoutubeId();
                    startVideo(_youtubeId);

                    _currentPlayingTextView.setText(String.format(Locale.getDefault(),"YoutubeId: %s", _youtubeId));
                } else if (model.IsWebViewVisible()) {
                    stopWebViewLoading();
                    stopVideo();
                    stopRadioPlaying();

                    _youTubePlayerView.setVisibility(View.GONE);
                    _centerWebView.setVisibility(View.VISIBLE);
                    _centerTextView.setVisibility(View.GONE);
                    _centerRadioStreamLinearLayout.setVisibility(View.GONE);

                    _loadingUrl = true;
                    _centerWebView.loadUrl(model.GetWebViewUrl());

                    _currentPlayingTextView.setText("");
                } else if (model.IsRadioStreamVisible()) {
                    stopWebViewLoading();
                    stopVideo();
                    stopRadioPlaying();

                    _youTubePlayerView.setVisibility(View.GONE);
                    _centerWebView.setVisibility(View.GONE);
                    _centerTextView.setVisibility(View.VISIBLE);
                    _centerRadioStreamLinearLayout.setVisibility(View.VISIBLE);

                    RadioStreams radioStream = model.GetRadioStream();
                    if (radioStream != null) {
                        _radioStream = radioStream;
                        _centerTextView.setText(_radioStream.GetTitle());
                        startRadioPlaying();
                        _currentPlayingTextView.setText(String.format(Locale.getDefault(),"RadioStream: %s", _radioStream.GetTitle()));
                    } else {
                        _logger.Error("RadioStream is null!");
                        _centerTextView.setText(_context.getString(R.string.errorRadioStream));
                        _currentPlayingTextView.setText("");
                    }
                } else {
                    if (_loadingUrl) {
                        _centerWebView.stopLoading();
                    }

                    _youTubePlayerView.setVisibility(View.INVISIBLE);
                    _centerWebView.setVisibility(View.INVISIBLE);
                    _centerTextView.setVisibility(View.VISIBLE);

                    _centerTextView.setText(R.string.errorCenterModel);

                    _currentPlayingTextView.setText("");
                }
            } else {
                _logger.Warning("model is null!");
            }
        }
    };

    private BroadcastReceiver _videoPositionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            _logger.Debug("_videoPositionReceiver onReceive");

            int positionPercent = intent.getIntExtra(Bundles.VIDEO_POSITION_PERCENT, -1);
            if (positionPercent != -1) {
                _logger.Debug("Setting video to position of percentage " + String.valueOf(positionPercent));

                if (_youtubePlayer.isPlaying()) {
                    int duration = _youtubePlayer.getDurationMillis();
                    _youtubePlayer.seekToMillis((duration * positionPercent) / 100);
                }
            }
        }
    };

    private BroadcastReceiver _youtubeIdReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_youtubeIdReceiver onReceive");

            String youtubeId = intent.getStringExtra(Bundles.YOUTUBE_ID);
            if (youtubeId != null) {
                _logger.Debug("received youtubeId: " + youtubeId);
                _youtubeId = youtubeId;

                startVideo(_youtubeId);
            }
        }
    };

    private YouTubePlayer.PlaybackEventListener _playbackEventListener = new YouTubePlayer.PlaybackEventListener() {

        @Override
        public void onBuffering(boolean buffering) {
        }

        @Override
        public void onPaused() {
        }

        @Override
        public void onPlaying() {
        }

        @Override
        public void onSeekTo(int position) {
        }

        @Override
        public void onStopped() {
        }

    };

    private YouTubePlayer.PlayerStateChangeListener _playerStateChangeListener = new YouTubePlayer.PlayerStateChangeListener() {

        @Override
        public void onAdStarted() {
            _mediaVolumeController.MuteVolume();
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {
            _logger.Error(errorReason.toString());

            if (errorReason == YouTubePlayer.ErrorReason.USER_DECLINED_RESTRICTED_CONTENT) {
                if (YoutubeId.GetByYoutubeId(_youtubeId) == YoutubeId.THE_GOOD_LIFE_STREAM) {
                    _logger.Debug("Stream is " + YoutubeId.THE_GOOD_LIFE_STREAM.GetTitle() + "! Searching other id!");

                    String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&q=The+Good+Life+24+7&key=" + Keys.YOUTUBE_API_KEY;

                    DownloadYoutubeVideoTask task = new DownloadYoutubeVideoTask(_context, null, "", false, false);
                    task.execute(url);
                }
            }
        }

        @Override
        public void onLoaded(String arg0) {
            _loadingVideo = false;
            _youtubePlayer.play();
            _currentPlayingTextView.setText(String.format(Locale.getDefault(),"YoutubeId: %s", _youtubeId));
        }

        @Override
        public void onLoading() {
        }

        @Override
        public void onVideoEnded() {
            if (_youtubePlayer.hasNext()) {
                _youtubePlayer.next();
            } else {
                _youTubePlayerView.setVisibility(View.GONE);
                _centerWebView.setVisibility(View.GONE);
                _centerTextView.setVisibility(View.VISIBLE);
                _centerTextView.setText(R.string.madeByGuepardoApps);
                _centerRadioStreamLinearLayout.setVisibility(View.GONE);
                _currentPlayingTextView.setText("");
            }
        }

        @Override
        public void onVideoStarted() {
            _mediaVolumeController.UnMuteVolume();
        }
    };

    private CenterViewController() {
        _logger = new Logger(TAG);
    }

    public static CenterViewController getInstance() {
        return SINGLETON;
    }

    public void Initialize(@NonNull Context context) {
        _context = context;
        _databaseController = DatabaseController.getSingleton();
        _databaseController.Initialize(_context);
        _mediaVolumeController = MediaVolumeController.getInstance();
        _receiverController = new ReceiverController(_context);
    }

    @Override
    public void onCreate() {
        _logger.Debug("onCreate");

        _screenEnabled = true;

        _youTubePlayerView = ((Activity) _context).findViewById(R.id.centerYoutubePlayer);
        _centerTextView = ((Activity) _context).findViewById(R.id.centerTextView);
        _centerRadioStreamLinearLayout = ((Activity) _context).findViewById(R.id.centerRadioStreamLinearLayout);
        _floatingActionButtonRadioStreamPlay = ((Activity) _context).findViewById(R.id.floating_action_button_radio_stream_play);
        _floatingActionButtonRadioStreamStop = ((Activity) _context).findViewById(R.id.floating_action_button_radio_stream_stop);
        _centerWebView = ((Activity) _context).findViewById(R.id.centerWebView);

        _currentPlayingTextView = ((Activity) _context).findViewById(R.id.currentPlayingTextView);

        initializeMediaPlayer();
        initializeButtons();
        initializeWebView();

        activateYoutube();
    }

    @Override
    public void onStart() {
        _logger.Debug("onStart");
    }

    @Override
    public void onResume() {
        _logger.Debug("onResume");

        if (!_isInitialized) {
            _receiverController.RegisterReceiver(_playBirthdaySongReceiver, new String[]{Broadcasts.PLAY_BIRTHDAY_SONG});
            _receiverController.RegisterReceiver(_playRadioStreamReceiver, new String[]{Broadcasts.PLAY_RADIO_STREAM});
            _receiverController.RegisterReceiver(_playVideoReceiver, new String[]{Broadcasts.PLAY_VIDEO});
            _receiverController.RegisterReceiver(_pauseVideoReceiver, new String[]{Broadcasts.PAUSE_VIDEO});
            _receiverController.RegisterReceiver(_screenDisableReceiver, new String[]{Broadcasts.SCREEN_OFF});
            _receiverController.RegisterReceiver(_screenEnableReceiver, new String[]{Broadcasts.SCREEN_ENABLED});
            _receiverController.RegisterReceiver(_stopRadioStreamReceiver, new String[]{Broadcasts.STOP_RADIO_STREAM});
            _receiverController.RegisterReceiver(_stopVideoReceiver, new String[]{Broadcasts.STOP_VIDEO});
            _receiverController.RegisterReceiver(_updateViewReceiver, new String[]{Broadcasts.SHOW_CENTER_MODEL});
            _receiverController.RegisterReceiver(_videoPositionReceiver, new String[]{Broadcasts.SET_VIDEO_POSITION});
            _receiverController.RegisterReceiver(_youtubeIdReceiver, new String[]{Broadcasts.YOUTUBE_ID});

            _isInitialized = true;
            _logger.Debug("Initializing!");
        } else {
            _logger.Warning("Is ALREADY initialized!");
        }
    }

    @Override
    public void onPause() {
        _logger.Debug("onPause");

        stopWebViewLoading();
        stopVideo();
        stopRadioPlaying();
    }

    @Override
    public void onDestroy() {
        _logger.Debug("onDestroy");

        _receiverController.Dispose();
        _isInitialized = false;

        stopWebViewLoading();
        stopVideo();
        stopRadioPlaying();
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult result) {
        videoError("Failed to initialize YoutubePlayer!" + result);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
        if (!_screenEnabled) {
            _logger.Debug("Screen is not enabled!");
            return;
        }

        if (!_youTubePlayerIsInitialized) {
            _youtubePlayer = player;

            _youtubePlayer.setPlayerStateChangeListener(_playerStateChangeListener);
            _youtubePlayer.setPlaybackEventListener(_playbackEventListener);

            _youTubePlayerIsInitialized = true;
        }

        if (!wasRestored) {
            startVideo(_youtubeId);
        }
    }

    public boolean IsRadioStreamPlaying() {
        return _radioPlayer.isPlaying();
    }

    public int GetRadioStreamId() {
        return _radioStream.GetId();
    }

    public boolean IsYoutubePlaying() {
        return _youtubePlayer.isPlaying();
    }

    public ArrayList<YoutubeDatabaseModel> GetYoutubeIds() {
        return _databaseController.GetYoutubeIds();
    }

    public int GetCurrentPlayPosition() {
        if (!_youtubePlayer.isPlaying()) {
            return -1;
        }

        return _youtubePlayer.getCurrentTimeMillis() / 1000;
    }

    public int GetYoutubeDuration() {
        if (!_youtubePlayer.isPlaying()) {
            return -1;
        }

        return _youtubePlayer.getDurationMillis() / 1000;
    }

    private void startVideo(@NonNull String youtubeId) {
        _logger.Debug(String.format("trying to start video %s", youtubeId));

        if (!_screenEnabled) {
            _logger.Debug("Screen is not enabled!");
            return;
        }

        if (!_youTubePlayerIsInitialized) {
            _logger.Error("YouTubePlayer is not initialized!");
            return;
        }

        if (_loadingVideo) {
            _logger.Warning("Already loading a video!");
            return;
        }

        if (_youtubePlayer.isPlaying()) {
            Toasty.info(_context, "Stopping current played video!", Toast.LENGTH_SHORT).show();
            _logger.Warning("Stopping current played video!");
            stopVideo();
        }

        if (_youtubePlayer != null) {
            _databaseController.SaveYoutubeId(new YoutubeDatabaseModel(_databaseController.GetHighestId() + 1, youtubeId, 0));
            _youtubePlayer.cueVideo(youtubeId);
        }

        _youTubePlayerView.setVisibility(View.VISIBLE);
        _centerWebView.setVisibility(View.GONE);
        _centerTextView.setVisibility(View.GONE);
        _centerRadioStreamLinearLayout.setVisibility(View.GONE);
    }

    private void pauseVideo() {
        _logger.Debug("pauseVideo");

        if (!_screenEnabled) {
            _logger.Debug("Screen is not enabled!");
            return;
        }

        if (!_youTubePlayerIsInitialized) {
            _logger.Error("YouTubePlayer is not initialized!");
            return;
        }

        if (!_youtubePlayer.isPlaying()) {
            _logger.Warning("Not playing a video!");
            return;
        }

        _youtubePlayer.pause();
    }

    private void stopVideo() {
        _logger.Debug("stopVideo");

        if (!_screenEnabled) {
            _logger.Debug("Screen is not enabled!");
            return;
        }

        if (!_youTubePlayerIsInitialized) {
            _logger.Error("YouTubePlayer is not initialized!");
            return;
        }

        if (!_youtubePlayer.isPlaying()) {
            _logger.Warning("Not playing a video!");
            return;
        }

        _youtubePlayer.pause();
        _youtubePlayer.seekToMillis(0);

        _youTubePlayerView.setVisibility(View.INVISIBLE);
    }

    private void videoError(@NonNull String error) {
        if (!_screenEnabled) {
            _logger.Debug("Screen is not enabled!");
            return;
        }

        _loadingVideo = false;
        _logger.Error("Video Play Error :" + error);

        _youTubePlayerView.setVisibility(View.GONE);

        _centerTextView.setVisibility(View.VISIBLE);
        _centerTextView.setText(String.format(Locale.getDefault(), "Video Play Error : %s", error));
    }

    private void initializeMediaPlayer() {
        _logger.Debug("initializeMediaPlayer");

        _radioPlayer = new MediaPlayer();

        try {
            _radioPlayer.setDataSource(_radioStream.GetUrl());
            _logger.Debug(String.format(Locale.getDefault(), "Set DataSource to %s", _radioStream.GetUrl()));
        } catch (Exception e) {
            _logger.Error(e.toString());
            Toasty.error(_context, "An error appeared settings url for radio player!", Toast.LENGTH_LONG).show();
        }

        _radioPlayer.setOnBufferingUpdateListener((mediaPlayer, percent) -> _logger.Information(String.format(Locale.getDefault(), "Buffered to %d%%", percent)));
        _radioPlayer.setOnPreparedListener(mediaPlayer -> {
            _logger.Debug("onPreparedListener...");
            _radioPlayer.start();
        });
    }

    private void initializeButtons() {
        _logger.Debug("initializeButtons");

        _floatingActionButtonRadioStreamPlay.setOnClickListener(view -> {
            _logger.Debug("_floatingActionButtonRadioStreamPlay onClick");
            startRadioPlaying();
        });

        _floatingActionButtonRadioStreamStop.setOnClickListener(view -> {
            _logger.Debug("_floatingActionButtonRadioStreamStop onClick");
            stopRadioPlaying();
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initializeWebView() {
        _logger.Debug("initializeWebView");

        _centerWebView = ((Activity) _context).findViewById(R.id.centerWebView);
        _centerWebView.getSettings().setBuiltInZoomControls(true);
        _centerWebView.getSettings().setSupportZoom(true);
        _centerWebView.getSettings().setJavaScriptEnabled(true);
        _centerWebView.getSettings().setLoadWithOverviewMode(true);
        _centerWebView.setWebViewClient(new WebViewClient());
        _centerWebView.setWebChromeClient(new WebChromeClient());
        _centerWebView.setInitialScale(100);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(false);
        _centerWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                _loadingUrl = false;
            }
        });
    }

    private void activateYoutube() {
        _logger.Debug("activateYoutube");
        if (Keys.YOUTUBE_API_KEY.length() != 0) {
            _youTubePlayerView.initialize(Keys.YOUTUBE_API_KEY, this);
        } else {
            _logger.Warning("Please enter your youtube api key!");
            Toasty.error(_context, "Please enter your youtube api key!", Toast.LENGTH_LONG).show();
        }
    }

    private void startRadioPlaying() {
        _logger.Debug("startRadioPlaying");

        _floatingActionButtonRadioStreamPlay.setEnabled(false);
        _floatingActionButtonRadioStreamStop.setEnabled(true);

        try {
            _radioPlayer.prepareAsync();
        } catch (Exception exception) {
            _logger.Error(exception.toString());
        }
    }

    private void stopRadioPlaying() {
        _logger.Debug("stopRadioPlaying");

        if (_radioPlayer.isPlaying()) {
            _radioPlayer.stop();
        }
        _radioPlayer.release();
        initializeMediaPlayer();

        _floatingActionButtonRadioStreamPlay.setEnabled(true);
        _floatingActionButtonRadioStreamStop.setEnabled(false);
    }

    private void stopWebViewLoading() {
        _logger.Warning("stopWebViewLoading");

        if (_loadingUrl) {
            _logger.Warning("WebView is loading a website! Cancel loading!");
            _centerWebView.stopLoading();
            _loadingUrl = false;
        }
    }
}
