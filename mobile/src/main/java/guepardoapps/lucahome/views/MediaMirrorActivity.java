package guepardoapps.lucahome.views;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rey.material.widget.Button;
import com.rey.material.widget.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import de.mateware.snacky.Snacky;
import es.dmoral.toasty.Toasty;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.MediaMirrorData;
import guepardoapps.lucahome.common.classes.PlayedYoutubeVideo;
import guepardoapps.lucahome.common.classes.YoutubeVideo;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.constants.Keys;
import guepardoapps.lucahome.common.enums.MediaServerAction;
import guepardoapps.lucahome.common.enums.MediaServerSelection;
import guepardoapps.lucahome.common.enums.RSSFeed;
import guepardoapps.lucahome.common.enums.RadioStreams;
import guepardoapps.lucahome.common.enums.YoutubeId;
import guepardoapps.lucahome.common.service.MediaMirrorService;
import guepardoapps.lucahome.common.tasks.DownloadYoutubeVideoTask;
import guepardoapps.lucahome.service.NavigationService;

public class MediaMirrorActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MediaMirrorActivity.class.getSimpleName();
    private Logger _logger;

    private Context _context;

    private ReceiverController _receiverController;

    private MediaMirrorService _mediaMirrorService;
    private NavigationService _navigationService;

    /**
     * Variable for received MediaMirrorData
     */
    private MediaMirrorData _mediaMirrorData;

    /**
     * Enables for selection spinner or seekBars
     * Disable while setting new selection to prevent endless reload
     */
    private boolean _mediaMirrorSelectionSpinnerEnabled = true;
    private boolean _youtubePlayPositionSeekBarEnabled = true;
    private boolean _radioStreamSelectionSpinnerEnabled = true;

    /**
     * UI variables for CardView MediaMirror Selection, battery and version
     */
    private Spinner _mediaMirrorSelectionSpinner;
    private TextView _mediaMirrorBatteryTextView;
    private TextView _mediaMirrorVersionTextView;

    /**
     * UI variables for CardView Youtube data
     * ImageView current played video
     * Button id selection
     * FloatingActionButton Play, Pause, Stop
     * SeekBar PlayPosition selection
     * TextView PlayPosition
     * Button SleepTimer
     */
    private ImageView _youtubeVideoImageView;
    private Button _youtubeIdSelectionButton;
    private FloatingActionButton _youtubePlayButton;
    private FloatingActionButton _youtubePauseButton;
    private FloatingActionButton _youtubeStopButton;
    private SeekBar _youtubePlayPositionSeekBar;
    private TextView _youtubePlayPositionTextView;
    private Button _sleepTimerButton;

    /**
     * UI variables for CardView RadioStream selection, play and stop
     */
    private Spinner _radioStreamSelectionSpinner;
    private FloatingActionButton _radioStreamPlayButton;
    private FloatingActionButton _radioStreamStopButton;

    /**
     * UI variables for CardView Volume display, increase and decrease
     */
    private TextView _volumeTextView;
    private FloatingActionButton _volumeIncreaseButton;
    private FloatingActionButton _volumeDecreaseButton;

    /**
     * UI variables for CardView RSS selection
     */
    private Spinner _rssSelectionSpinner;

    /**
     * UI variables for CardView manipulating text on MediaMirror
     */
    private EditText _editText;
    private FloatingActionButton _sendTextButton;

    /**
     * UI variables for CardView Screen brightness, increase and decrease
     */
    private TextView _brightnessTextView;
    private FloatingActionButton _brightnessIncreaseButton;
    private FloatingActionButton _brightnessDecreaseButton;

    /**
     * UI variables for CardView Update current and forecast weather, temperature, ip, birthdays and calendar
     */
    private Button _updateCurrentWeatherButton;
    private Button _updateForecastWeatherButton;
    private Button _updateTemperatureButton;
    private Button _updateIpButton;
    private Button _updateBirthdaysButton;
    private Button _updateCalendarButton;

    /**
     * BroadcastReceiver for receiving update of MediaMirrorData
     */
    private BroadcastReceiver _mediaMirrorUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_mediaMirrorUpdateReceiver onReceive");
            MediaMirrorService.MediaMirrorDownloadFinishedContent result = (MediaMirrorService.MediaMirrorDownloadFinishedContent) intent.getSerializableExtra(MediaMirrorService.MediaMirrorDownloadFinishedBundle);
            if (result != null) {
                if (result.Success) {
                    if (result.MediaMirror != null) {
                        _mediaMirrorData = result.MediaMirror;

                        displayCommonMediaMirrorUi(_mediaMirrorData);
                        displayYoutubeMediaMirrorUi(_mediaMirrorData);
                        displayRadioStreamUi(_mediaMirrorData);
                        displayVolumeUi(_mediaMirrorData);
                        displayBrightnessUi(_mediaMirrorData);

                        return;
                    }
                }

                displayErrorSnackBar(Tools.DecompressByteArrayToString(result.Response));
                return;
            }

            displayErrorSnackBar("Result is null!");
        }
    };

    /**
     * BroadcastReceiver for receiving image of playing youtube video
     */
    private BroadcastReceiver _youtubeVideoImageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_youtubeVideoImageReceiver onReceive");

            YoutubeVideo youtubeVideo = (YoutubeVideo) intent.getSerializableExtra(MediaMirrorService.MediaMirrorYoutubeVideoBundle);
            if (youtubeVideo != null) {
                Picasso.with(context).load(youtubeVideo.GetMediumImageUrl()).into(_youtubeVideoImageView);
                _youtubeIdSelectionButton.setText(youtubeVideo.GetTitle());
            } else {
                _logger.Warning("youtubeVideo is null");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _logger = new Logger(TAG);
        _logger.Debug("onCreate");

        setContentView(R.layout.activity_mediamirror);

        Toolbar toolbar = findViewById(R.id.toolbar_media_mirror);
        //setSupportActionBar(toolbar);

        _context = this;

        _receiverController = new ReceiverController(_context);

        _mediaMirrorService = MediaMirrorService.getInstance();
        _navigationService = NavigationService.getInstance();

        DrawerLayout drawer = findViewById(R.id.drawer_layout_mediamirror);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view_mediamirror);
        navigationView.setNavigationItemSelectedListener(this);

        //Define variables for common MediaMirror
        _mediaMirrorSelectionSpinner = findViewById(R.id.media_mirror_selection_spinner);
        _mediaMirrorBatteryTextView = findViewById(R.id.media_mirror_battery_text_view);
        _mediaMirrorVersionTextView = findViewById(R.id.media_mirror_version_text_view);
        setUpCommonMediaMirrorUi();

        //Define variables to handle youtube on MediaMirror
        _youtubeVideoImageView = findViewById(R.id.youtube_video_image_view);
        _youtubeIdSelectionButton = findViewById(R.id.youtube_id_button);
        _youtubePlayButton = findViewById(R.id.youtube_play_image_button);
        _youtubePauseButton = findViewById(R.id.youtube_pause_image_button);
        _youtubeStopButton = findViewById(R.id.youtube_stop_image_button);
        _youtubePlayPositionSeekBar = findViewById(R.id.youtube_duration_seek_bar);
        _youtubePlayPositionTextView = findViewById(R.id.youtube_video_time_text_view);
        _sleepTimerButton = findViewById(R.id.sleep_timer_button);
        setUpYoutubeMediaMirrorUi();

        //Define variables for radio stream
        _radioStreamSelectionSpinner = findViewById(R.id.radio_stream_selection_spinner);
        _radioStreamPlayButton = findViewById(R.id.radio_stream_play_button);
        _radioStreamStopButton = findViewById(R.id.radio_stream_stop_button);
        setUpRadioStreamUi();

        //Define variables for volume
        _volumeTextView = findViewById(R.id.volume_text_view);
        _volumeIncreaseButton = findViewById(R.id.volume_increase_button);
        _volumeDecreaseButton = findViewById(R.id.volume_decrease_button);
        setUpVolumeUi();

        //Define variables for radio stream
        _rssSelectionSpinner = findViewById(R.id.rss_selection_spinner);
        setUpRssUi();

        //Define variables for editing text on MediaMirror
        _editText = findViewById(R.id.text_edit_view);
        _sendTextButton = findViewById(R.id.send_text_button);
        setUpEditTextUi();

        //Define variables for volume
        _brightnessTextView = findViewById(R.id.brightness_text_view);
        _brightnessIncreaseButton = findViewById(R.id.brightness_increase_button);
        _brightnessDecreaseButton = findViewById(R.id.brightness_decrease_button);
        setUpBrightnessUi();

        //Define variables for updating data
        _updateCurrentWeatherButton = findViewById(R.id.update_current_weather_button);
        _updateForecastWeatherButton = findViewById(R.id.update_forecast_weather_button);
        _updateTemperatureButton = findViewById(R.id.update_temperature_button);
        _updateIpButton = findViewById(R.id.update_ip_button);
        _updateBirthdaysButton = findViewById(R.id.update_birthdays_button);
        _updateCalendarButton = findViewById(R.id.update_calendar_button);
        setUpUpdateUi();
    }

    @Override
    protected void onStart() {
        super.onStart();
        _logger.Debug("onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        _logger.Debug("onResume");
        _receiverController.RegisterReceiver(_mediaMirrorUpdateReceiver, new String[]{MediaMirrorService.MediaMirrorDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_youtubeVideoImageReceiver, new String[]{MediaMirrorService.MediaMirrorYoutubeVideoBroadcast});
    }

    @Override
    protected void onPause() {
        super.onPause();
        _logger.Debug("onPause");
        _receiverController.Dispose();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _logger.Debug("onDestroy");
        _receiverController.Dispose();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_mediamirror);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            _navigationService.GoBack(this);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        NavigationService.NavigationResult navigationResult = NavigationService.NavigationResult.NULL;

        if (id == R.id.nav_schedule) {
            navigationResult = _navigationService.NavigateToActivity(_context, ScheduleActivity.class);
        } else if (id == R.id.nav_timer) {
            navigationResult = _navigationService.NavigateToActivity(_context, TimerActivity.class);
        } else if (id == R.id.nav_socket) {
            navigationResult = _navigationService.NavigateToActivity(_context, WirelessSocketActivity.class);
        } else if (id == R.id.nav_movie) {
            navigationResult = _navigationService.NavigateToActivity(_context, MovieActivity.class);
        } else if (id == R.id.nav_coins) {
            navigationResult = _navigationService.NavigateToActivity(_context, CoinActivity.class);
        } else if (id == R.id.nav_menu) {
            navigationResult = _navigationService.NavigateToActivity(_context, MenuActivity.class);
        } else if (id == R.id.nav_shopping) {
            navigationResult = _navigationService.NavigateToActivity(_context, ShoppingListActivity.class);
        } else if (id == R.id.nav_forecast_weather) {
            navigationResult = _navigationService.NavigateToActivity(_context, ForecastWeatherActivity.class);
        } else if (id == R.id.nav_birthday) {
            navigationResult = _navigationService.NavigateToActivity(_context, BirthdayActivity.class);
        } else if (id == R.id.nav_security) {
            navigationResult = _navigationService.NavigateToActivity(_context, SecurityActivity.class);
        } else if (id == R.id.nav_settings) {
            navigationResult = _navigationService.NavigateToActivity(_context, SettingsActivity.class);
        }

        if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
            _logger.Error(String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
            displayErrorSnackBar("Failed to navigate! Please contact LucaHome support!");
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_mediamirror);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void displayErrorSnackBar(@NonNull String message) {
        Snacky.builder()
                .setActivty(MediaMirrorActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_LONG)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void setUpCommonMediaMirrorUi() {
        _logger.Debug("setUpCommonMediaMirrorUi");

        final ArrayList<String> serverLocations = new ArrayList<>();
        for (MediaServerSelection entry : MediaServerSelection.values()) {
            if (entry != MediaServerSelection.NULL) {
                serverLocations.add(entry.GetLocation());
            }
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(_context, android.R.layout.simple_spinner_item, serverLocations);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        _mediaMirrorSelectionSpinner.setAdapter(dataAdapter);
        _mediaMirrorSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!_mediaMirrorSelectionSpinnerEnabled) {
                    _logger.Warning("_mediaMirrorSelectionSpinner is disabled!");
                    return;
                }

                String selectedLocation = serverLocations.get(position);
                String selectedIp = MediaServerSelection.GetByLocation(selectedLocation).GetIp();

                _mediaMirrorService.SendCommand(selectedIp, MediaServerAction.GET_MEDIAMIRROR_DTO.toString(), "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void displayCommonMediaMirrorUi(@NonNull MediaMirrorData mediaMirrorData) {
        _mediaMirrorSelectionSpinnerEnabled = false;
        _mediaMirrorSelectionSpinner.setSelection(mediaMirrorData.GetMediaServerSelection().GetId(), true);
        _mediaMirrorBatteryTextView.setText(String.format(Locale.getDefault(), "Battery %d%%", mediaMirrorData.GetBatteryLevel()));
        _mediaMirrorVersionTextView.setText(String.format(Locale.getDefault(), "Version: %s", mediaMirrorData.GetServerVersion()));
        _mediaMirrorSelectionSpinnerEnabled = true;
    }

    private void setUpYoutubeMediaMirrorUi() {
        _logger.Debug("setUpYoutubeMediaMirrorUi");

        _youtubeIdSelectionButton.setOnClickListener(view -> {
            if (_mediaMirrorData == null) {
                _logger.Error("_mediaMirrorData is null!");
                displayErrorSnackBar("_mediaMirrorData is null!");
                return;
            }

            displayYoutubeIdSelectionDialog();
        });

        _youtubePlayButton.setOnClickListener(view -> {
            if (_mediaMirrorData == null) {
                _logger.Error("_mediaMirrorData is null!");
                displayErrorSnackBar("_mediaMirrorData is null!");
                return;
            }

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.PLAY_YOUTUBE_VIDEO.toString(),
                    "");

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.GET_MEDIAMIRROR_DTO.toString(),
                    "");
        });

        _youtubePauseButton.setOnClickListener(view -> {
            if (_mediaMirrorData == null) {
                _logger.Error("_mediaMirrorData is null!");
                displayErrorSnackBar("_mediaMirrorData is null!");
                return;
            }

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.PAUSE_YOUTUBE_VIDEO.toString(),
                    "");

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.GET_MEDIAMIRROR_DTO.toString(),
                    "");
        });

        _youtubeStopButton.setOnClickListener(view -> {
            if (_mediaMirrorData == null) {
                _logger.Error("_mediaMirrorData is null!");
                displayErrorSnackBar("_mediaMirrorData is null!");
                return;
            }

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.STOP_YOUTUBE_VIDEO.toString(),
                    "");

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.GET_MEDIAMIRROR_DTO.toString(),
                    "");
        });

        _youtubePlayPositionSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                if (!_youtubePlayPositionSeekBarEnabled) {
                    _logger.Warning("_youtubePlayPositionSeekBar is disabled!");
                    return;
                }

                if (_mediaMirrorData == null) {
                    _logger.Error("_mediaMirrorData is null!");
                    displayErrorSnackBar("_mediaMirrorData is null!");
                    return;
                }

                _mediaMirrorService.SendCommand(
                        _mediaMirrorData.GetMediaServerSelection().GetIp(),
                        MediaServerAction.SET_YOUTUBE_PLAY_POSITION.toString(),
                        String.valueOf(progressValue));

                _mediaMirrorService.SendCommand(
                        _mediaMirrorData.GetMediaServerSelection().GetIp(),
                        MediaServerAction.GET_MEDIAMIRROR_DTO.toString(),
                        "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        _youtubePlayPositionSeekBar.setVisibility(View.GONE);

        _sleepTimerButton.setOnClickListener(view -> {
            if (_mediaMirrorData == null) {
                _logger.Error("_mediaMirrorData is null!");
                displayErrorSnackBar("_mediaMirrorData is null!");
                return;
            }

            if (_mediaMirrorData.IsSleepTimerEnabled()) {
                _mediaMirrorService.SendCommand(
                        _mediaMirrorData.GetMediaServerSelection().GetIp(),
                        MediaServerAction.STOP_SEA_SOUND.toString(),
                        "");
            } else {
                _mediaMirrorService.SendCommand(
                        _mediaMirrorData.GetMediaServerSelection().GetIp(),
                        MediaServerAction.PLAY_SEA_SOUND.toString(),
                        "");
            }

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.GET_MEDIAMIRROR_DTO.toString(),
                    "");
        });
        _sleepTimerButton.setVisibility(View.GONE);
        _sleepTimerButton.setEnabled(false);
    }

    private void displayYoutubeMediaMirrorUi(@NonNull MediaMirrorData mediaMirrorData) {
        _youtubePlayPositionSeekBarEnabled = false;

        _youtubePlayPositionSeekBar.setVisibility(mediaMirrorData.IsYoutubePlaying() ? View.VISIBLE : View.GONE);
        _youtubePlayPositionSeekBar.setEnabled(mediaMirrorData.IsYoutubePlaying());

        _youtubePlayButton.setEnabled(!mediaMirrorData.IsYoutubePlaying());
        _youtubePauseButton.setEnabled(mediaMirrorData.IsYoutubePlaying());
        _youtubeStopButton.setEnabled(mediaMirrorData.IsYoutubePlaying());

        _youtubePlayPositionTextView.setVisibility(mediaMirrorData.IsYoutubePlaying() ? View.VISIBLE : View.GONE);

        _sleepTimerButton.setVisibility((mediaMirrorData.IsSleepTimerEnabled() && mediaMirrorData.GetMediaServerSelection().IsSleepingMirror()) ? View.VISIBLE : View.GONE);
        _sleepTimerButton.setEnabled(mediaMirrorData.IsSleepTimerEnabled() && mediaMirrorData.GetMediaServerSelection().IsSleepingMirror());

        if (mediaMirrorData.IsYoutubePlaying()) {
            int currentVideoPlayTime = mediaMirrorData.GetYoutubeVideoCurrentPlayTime();
            int totalVideoPlayTime = mediaMirrorData.GetYoutubeVideoDuration();

            loadYoutubeVideoImage(_mediaMirrorData);

            if (currentVideoPlayTime == -1 || totalVideoPlayTime == -1 || currentVideoPlayTime == 0 || totalVideoPlayTime == 0) {
                _logger.Error(String.format(Locale.getDefault(), "Invalid values: currentVideoPlayTime: %d: totalVideoPlayTime: %d", currentVideoPlayTime, totalVideoPlayTime));
                _youtubePlayPositionSeekBar.setVisibility(View.GONE);
                _youtubePlayPositionTextView.setVisibility(View.GONE);

            } else {
                int progress = (currentVideoPlayTime * 100) / totalVideoPlayTime;
                _youtubePlayPositionSeekBar.setProgress(progress);

                int currentVideoPlayTimeHour = currentVideoPlayTime / 3600;
                int currentVideoPlayTimeMinute = currentVideoPlayTime % 60;
                int currentVideoPlayTimeSecond = currentVideoPlayTime % 3600;

                int totalVideoPlayTimeHour = totalVideoPlayTime / 3600;
                int totalVideoPlayTimeMinute = totalVideoPlayTime % 60;
                int totalVideoPlayTimeSecond = totalVideoPlayTime % 3600;

                String youtubePlayTimeText = String.format(Locale.getDefault(),
                        "%02d:%02d:%02d / %02d:%02d:%02d",
                        currentVideoPlayTimeHour, currentVideoPlayTimeMinute, currentVideoPlayTimeSecond, totalVideoPlayTimeHour, totalVideoPlayTimeMinute, totalVideoPlayTimeSecond);
                _youtubePlayPositionTextView.setText(youtubePlayTimeText);
            }
        }

        _youtubePlayPositionSeekBarEnabled = true;
    }

    private void setUpRadioStreamUi() {
        _logger.Debug("setUpYoutubeMediaMirrorUi");

        final ArrayList<String> radioStreams = new ArrayList<>();
        for (RadioStreams entry : RadioStreams.values()) {
            radioStreams.add(entry.GetTitle());
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(_context, android.R.layout.simple_spinner_item, radioStreams);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        _radioStreamSelectionSpinner.setAdapter(dataAdapter);
        _radioStreamSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!_radioStreamSelectionSpinnerEnabled) {
                    _logger.Warning("_radioStreamSelectionSpinner is disabled!");
                    return;
                }

                if (_mediaMirrorData == null) {
                    _logger.Error("_mediaMirrorData is null!");
                    displayErrorSnackBar("_mediaMirrorData is null!");
                    return;
                }

                String selectedRadioStream = radioStreams.get(position);
                RadioStreams radioStream = RadioStreams.GetByTitle(selectedRadioStream);

                _mediaMirrorService.SendCommand(
                        _mediaMirrorData.GetMediaServerSelection().GetIp(),
                        MediaServerAction.SHOW_RADIO_STREAM.toString(),
                        String.valueOf(radioStream.GetId()));

                _mediaMirrorService.SendCommand(
                        _mediaMirrorData.GetMediaServerSelection().GetIp(),
                        MediaServerAction.GET_MEDIAMIRROR_DTO.toString(),
                        "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        _radioStreamPlayButton.setOnClickListener(view -> {
            if (_mediaMirrorData == null) {
                _logger.Error("_mediaMirrorData is null!");
                displayErrorSnackBar("_mediaMirrorData is null!");
                return;
            }

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.PLAY_RADIO_STREAM.toString(),
                    "");

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.GET_MEDIAMIRROR_DTO.toString(),
                    "");
        });

        _radioStreamStopButton.setOnClickListener(view -> {
            if (_mediaMirrorData == null) {
                _logger.Error("_mediaMirrorData is null!");
                displayErrorSnackBar("_mediaMirrorData is null!");
                return;
            }

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.STOP_RADIO_STREAM.toString(),
                    "");

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.GET_MEDIAMIRROR_DTO.toString(),
                    "");
        });
    }

    private void displayRadioStreamUi(@NonNull MediaMirrorData mediaMirrorData) {
        _radioStreamSelectionSpinnerEnabled = false;

        _radioStreamSelectionSpinner.setSelection(mediaMirrorData.GetRadioStreamId(), true);
        _radioStreamPlayButton.setEnabled(!mediaMirrorData.IsRadioStreamPlaying());
        _radioStreamStopButton.setEnabled(mediaMirrorData.IsRadioStreamPlaying());

        _radioStreamSelectionSpinnerEnabled = true;
    }

    private void setUpVolumeUi() {
        _logger.Debug("setUpVolumeUi");

        _volumeIncreaseButton.setOnClickListener(view -> {
            if (_mediaMirrorData == null) {
                _logger.Error("_mediaMirrorData is null!");
                displayErrorSnackBar("_mediaMirrorData is null!");
                return;
            }

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.INCREASE_VOLUME.toString(),
                    "");

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.GET_MEDIAMIRROR_DTO.toString(),
                    "");
        });

        _volumeDecreaseButton.setOnClickListener(view -> {
            if (_mediaMirrorData == null) {
                _logger.Error("_mediaMirrorData is null!");
                displayErrorSnackBar("_mediaMirrorData is null!");
                return;
            }

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.DECREASE_VOLUME.toString(),
                    "");

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.GET_MEDIAMIRROR_DTO.toString(),
                    "");
        });
    }

    private void displayVolumeUi(@NonNull MediaMirrorData mediaMirrorData) {
        _volumeTextView.setText(String.format(Locale.getDefault(), "Volume: %d", mediaMirrorData.GetVolume()));
    }

    private void setUpRssUi() {
        _logger.Debug("setUpRssUi");

        final ArrayList<String> rssStreams = new ArrayList<>();
        for (RSSFeed entry : RSSFeed.values()) {
            if (entry.GetId() > 1) {
                rssStreams.add(entry.GetTitle());
            }
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(_context, android.R.layout.simple_spinner_item, rssStreams);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        _rssSelectionSpinner.setAdapter(dataAdapter);
        _rssSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (_mediaMirrorData == null) {
                    _logger.Error("_mediaMirrorData is null!");
                    displayErrorSnackBar("_mediaMirrorData is null!");
                    return;
                }

                String selectedRssStream = rssStreams.get(position);
                RSSFeed rssFeed = RSSFeed.GetByTitle(selectedRssStream);

                _mediaMirrorService.SendCommand(
                        _mediaMirrorData.GetMediaServerSelection().GetIp(),
                        MediaServerAction.SET_RSS_FEED.toString(),
                        String.valueOf(rssFeed.GetId()));

                _mediaMirrorService.SendCommand(
                        _mediaMirrorData.GetMediaServerSelection().GetIp(),
                        MediaServerAction.GET_MEDIAMIRROR_DTO.toString(),
                        "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setUpEditTextUi() {
        _logger.Debug("setUpEditTextUi");

        _sendTextButton.setOnClickListener(view -> {
            if (_mediaMirrorData == null) {
                _logger.Error("_mediaMirrorData is null!");
                displayErrorSnackBar("_mediaMirrorData is null!");
                return;
            }

            String sendText = _editText.getText().toString();
            if (sendText.length() < 1 || sendText.length() > 500) {
                _logger.Error("sendText is null!");
                displayErrorSnackBar("sendText is null!");
                return;
            }

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.SHOW_CENTER_TEXT.toString(),
                    sendText);

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.GET_MEDIAMIRROR_DTO.toString(),
                    "");
        });
    }

    private void setUpBrightnessUi() {
        _logger.Debug("setUpBrightnessUi");

        _brightnessIncreaseButton.setOnClickListener(view -> {
            if (_mediaMirrorData == null) {
                _logger.Error("_mediaMirrorData is null!");
                displayErrorSnackBar("_mediaMirrorData is null!");
                return;
            }

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.INCREASE_SCREEN_BRIGHTNESS.toString(),
                    "");

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.GET_MEDIAMIRROR_DTO.toString(),
                    "");
        });

        _brightnessDecreaseButton.setOnClickListener(view -> {
            if (_mediaMirrorData == null) {
                _logger.Error("_mediaMirrorData is null!");
                displayErrorSnackBar("_mediaMirrorData is null!");
                return;
            }

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.DECREASE_SCREEN_BRIGHTNESS.toString(),
                    "");

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.GET_MEDIAMIRROR_DTO.toString(),
                    "");
        });
    }

    private void displayBrightnessUi(@NonNull MediaMirrorData mediaMirrorData) {
        _brightnessTextView.setText(String.format(Locale.getDefault(), "Brightness: %d%%", ((mediaMirrorData.GetScreenBrightness() * 100) / 255)));
    }

    private void setUpUpdateUi() {
        _logger.Debug("setUpBrightnessUi");

        _updateCurrentWeatherButton.setOnClickListener(view -> {
            if (_mediaMirrorData == null) {
                _logger.Error("_mediaMirrorData is null!");
                displayErrorSnackBar("_mediaMirrorData is null!");
                return;
            }

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.UPDATE_CURRENT_WEATHER.toString(),
                    "");
        });

        _updateForecastWeatherButton.setOnClickListener(view -> {
            if (_mediaMirrorData == null) {
                _logger.Error("_mediaMirrorData is null!");
                displayErrorSnackBar("_mediaMirrorData is null!");
                return;
            }

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.UPDATE_FORECAST_WEATHER.toString(),
                    "");
        });

        _updateTemperatureButton.setOnClickListener(view -> {
            if (_mediaMirrorData == null) {
                _logger.Error("_mediaMirrorData is null!");
                displayErrorSnackBar("_mediaMirrorData is null!");
                return;
            }

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.UPDATE_RASPBERRY_TEMPERATURE.toString(),
                    "");
        });

        _updateIpButton.setOnClickListener(view -> {
            if (_mediaMirrorData == null) {
                _logger.Error("_mediaMirrorData is null!");
                displayErrorSnackBar("_mediaMirrorData is null!");
                return;
            }

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.UPDATE_IP_ADDRESS.toString(),
                    "");
        });

        _updateBirthdaysButton.setOnClickListener(view -> {
            if (_mediaMirrorData == null) {
                _logger.Error("_mediaMirrorData is null!");
                displayErrorSnackBar("_mediaMirrorData is null!");
                return;
            }

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.UPDATE_BIRTHDAY_ALARM.toString(),
                    "");
        });

        _updateCalendarButton.setOnClickListener(view -> {
            if (_mediaMirrorData == null) {
                _logger.Error("_mediaMirrorData is null!");
                displayErrorSnackBar("_mediaMirrorData is null!");
                return;
            }

            _mediaMirrorService.SendCommand(
                    _mediaMirrorData.GetMediaServerSelection().GetIp(),
                    MediaServerAction.UPDATE_CALENDAR_ALARM.toString(),
                    "");
        });
    }

    private void loadYoutubeVideoImage(@NonNull MediaMirrorData mediaMirrorData) {
        String url = String.format(Locale.getDefault(), Constants.YOUTUBE_SEARCH, 1, mediaMirrorData.GetYoutubeId(), Keys.YOUTUBE_API_KEY);
        DownloadYoutubeVideoTask task = new DownloadYoutubeVideoTask(
                _context,
                null,
                mediaMirrorData.GetMediaServerSelection().GetIp(),
                true,
                false);
        task.execute(url);
    }

    private void searchYoutubeVideos(@NonNull MediaMirrorData mediaMirrorData, @NonNull String searchString) {
        ProgressDialog loadingVideosDialog = ProgressDialog.show(_context, "Loading Videos...", "");
        loadingVideosDialog.setCancelable(false);

        searchString = searchString.replace(" ", "+");
        String url = String.format(Locale.getDefault(), Constants.YOUTUBE_SEARCH, Constants.YOUTUBE_MAX_RESULTS, searchString, Keys.YOUTUBE_API_KEY);
        DownloadYoutubeVideoTask task = new DownloadYoutubeVideoTask(
                _context,
                loadingVideosDialog,
                mediaMirrorData.GetMediaServerSelection().GetIp(),
                false,
                true);
        task.execute(url);
    }

    private void displayYoutubeIdSelectionDialog() {
        final Dialog dialog = new Dialog(_context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_select_youtube_id);

        final CheckBox playOnAllMirror = dialog.findViewById(R.id.dialog_select_youtube_play_on_all_mirror);

        ImageButton youtubeSearchButton = dialog.findViewById(R.id.dialog_enter_youtube_search_button);
        youtubeSearchButton.setOnClickListener(view -> {
            dialog.dismiss();
            displayYoutubeIdSearchDialog();
        });

        final EditText youtubeIdEditText = dialog.findViewById(R.id.dialog_enter_youtube_id_editText);

        List<YoutubeId> youtubeIds = Arrays.asList(YoutubeId.values());
        ArrayAdapter<YoutubeId> youtubeIdDataAdapter = new ArrayAdapter<>(_context, android.R.layout.simple_spinner_item, youtubeIds);
        youtubeIdDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final Spinner predefinedYoutubeIdsSpinner = dialog.findViewById(R.id.dialog_select_youtube_predefined_spinner);
        predefinedYoutubeIdsSpinner.setAdapter(youtubeIdDataAdapter);
        predefinedYoutubeIdsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                YoutubeId selectedYoutubeId = YoutubeId.GetById(position);
                _logger.Debug(String.format("selectedYoutubeId is %s", selectedYoutubeId));
                youtubeIdEditText.setText(selectedYoutubeId.GetYoutubeId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        final ArrayList<String> playedYoutubeIdStrings = new ArrayList<>();
        for (PlayedYoutubeVideo entry : _mediaMirrorData.GetPlayedYoutubeIds()) {
            playedYoutubeIdStrings.add(entry.GetYoutubeId());
        }

        ArrayAdapter<String> playedYoutubeIdDataAdapter = new ArrayAdapter<>(_context, android.R.layout.simple_spinner_item, playedYoutubeIdStrings);
        playedYoutubeIdDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final Spinner alreadyPlayedYoutubeIdsSpinner = dialog.findViewById(R.id.dialog_select_youtube_played_spinner);
        alreadyPlayedYoutubeIdsSpinner.setAdapter(playedYoutubeIdDataAdapter);
        alreadyPlayedYoutubeIdsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedYoutubeId = playedYoutubeIdStrings.get(position);
                _logger.Debug(String.format("selectedYoutubeId is %s", selectedYoutubeId));
                youtubeIdEditText.setText(selectedYoutubeId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        Button btnPlay = dialog.findViewById(R.id.dialog_select_youtube_play);
        btnPlay.setOnClickListener(view -> {
            String youtubeId = youtubeIdEditText.getText().toString();
            if (youtubeId.length() < 6) {
                Toasty.error(_context, "YoutubeId invalid!", Toast.LENGTH_LONG).show();
                return;
            }
            youtubeId = youtubeId.replace(" ", "");

            if (playOnAllMirror.isChecked()) {
                for (MediaServerSelection entry : MediaServerSelection.values()) {
                    if (entry.GetId() > 0) {
                        _mediaMirrorService.SendCommand(entry.GetIp(), MediaServerAction.PLAY_YOUTUBE_VIDEO.toString(), youtubeId);
                    }
                }
            } else {
                _mediaMirrorService.SendCommand(_mediaMirrorData.GetMediaServerSelection().GetIp(), MediaServerAction.PLAY_YOUTUBE_VIDEO.toString(), youtubeId);
            }

            _mediaMirrorService.SendCommand(_mediaMirrorData.GetMediaServerSelection().GetIp(), MediaServerAction.GET_MEDIAMIRROR_DTO.toString(), "");

            dialog.dismiss();
        });

        dialog.setCancelable(true);
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        } else {
            _logger.Warning("Window is null!");
        }
    }

    private void displayYoutubeIdSearchDialog() {
        final Dialog dialog = new Dialog(_context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edittext);

        TextView titleView = dialog.findViewById(R.id.dialog_title);
        titleView.setText("Search youtube");

        TextView promptView = dialog.findViewById(R.id.dialog_prompt);
        promptView.setText("Enter your search below");

        final EditText inputEditText = dialog.findViewById(R.id.dialog_edittext);

        Button closeButton = dialog.findViewById(R.id.dialog_button_close);
        closeButton.setText("Search");
        closeButton.setOnClickListener(v -> {
            if (_mediaMirrorData == null) {
                _logger.Error("_mediaMirrorData is null!");
                displayErrorSnackBar("_mediaMirrorData is null!");
                return;
            }

            String input = inputEditText.getText().toString();

            if (input.length() == 0) {
                _logger.Error("Input has invalid length 0!");
                Toasty.error(_context, "Input has invalid length 0!", Toast.LENGTH_LONG).show();
                return;
            }

            searchYoutubeVideos(_mediaMirrorData, input);
        });

        dialog.setCancelable(true);
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        } else {
            _logger.Warning("Window is null!");
        }
    }
}
