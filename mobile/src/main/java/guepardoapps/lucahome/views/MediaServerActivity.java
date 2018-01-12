package guepardoapps.lucahome.views;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import guepardoapps.lucahome.common.classes.mediaserver.MediaNotificationData;
import guepardoapps.lucahome.common.classes.mediaserver.MediaServerData;
import guepardoapps.lucahome.common.classes.mediaserver.MediaServerInformationData;
import guepardoapps.lucahome.common.classes.mediaserver.PlayedYoutubeVideoData;
import guepardoapps.lucahome.common.classes.mediaserver.RadioStreamData;
import guepardoapps.lucahome.common.classes.mediaserver.SleepTimerData;
import guepardoapps.lucahome.common.classes.mediaserver.YoutubeData;
import guepardoapps.lucahome.common.classes.mediaserver.YoutubeVideoData;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.constants.Keys;
import guepardoapps.lucahome.common.enums.MediaServerAction;
import guepardoapps.lucahome.common.enums.MediaServerSelection;
import guepardoapps.lucahome.common.enums.RSSFeed;
import guepardoapps.lucahome.common.enums.RadioStreams;
import guepardoapps.lucahome.common.enums.YoutubeId;
import guepardoapps.lucahome.common.service.MediaServerService;
import guepardoapps.lucahome.common.tasks.DownloadYoutubeVideoTask;
import guepardoapps.lucahome.service.NavigationService;

public class MediaServerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MediaServerActivity.class.getSimpleName();

    private Context _context;
    private ReceiverController _receiverController;

    /**
     * Enables for selection spinner or seekBars
     * Disable while setting new selection to prevent endless reload
     */
    private boolean _mediaServerSelectionSpinnerEnabled = true;
    private boolean _youtubePlayPositionSeekBarEnabled = true;
    private boolean _radioStreamSelectionSpinnerEnabled = true;
    private boolean _rssFeedSelectionSpinnerEnabled = true;

    private DrawerLayout _drawerLayout;

    /**
     * UI variables for CardView MediaServer Selection, battery and version
     */
    private Spinner _mediaServerSelectionSpinner;
    private TextView _mediaServerBatteryTextView;
    private TextView _mediaServerVersionTextView;

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
     * UI variables for CardView MediaNotification information textView, play and stop
     */
    private CardView _mediaNotificationCardView;
    private TextView _mediaNotificationInformationTextView;
    private FloatingActionButton _mediaNotificationPlayButton;
    private FloatingActionButton _mediaNotificationStopButton;

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
     * UI variables for CardView manipulating text on MediaServer
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
     * UI variables for CardView system reboot and shutdown
     */
    private Button _systemRebootButton;
    private Button _systemShutdownButton;

    /**
     * BroadcastReceiver for receiving response of command
     */
    private BroadcastReceiver _commandResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.getInstance().Debug(TAG, "_commandResponseReceiver onReceive");
            MediaServerService.MediaServerDownloadFinishedContent result = (MediaServerService.MediaServerDownloadFinishedContent) intent.getSerializableExtra(MediaServerService.MediaServerCommandResponseBundle);
            if (result != null) {
                if (result.Success) {
                    displaySuccessSnackBar(Tools.DecompressByteArrayToString(result.Response));
                } else {
                    displayErrorSnackBar(Tools.DecompressByteArrayToString(result.Response));
                }
            } else {
                displayErrorSnackBar("Received command response is null!");
            }
        }
    };

    /**
     * BroadcastReceiver for receiving updates for youtube data
     */
    private BroadcastReceiver _youtubeDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.getInstance().Debug(TAG, "_youtubeDataReceiver onReceive");
            MediaServerData mediaServerData = MediaServerService.getInstance().GetActiveMediaServer();
            updateYoutubeUi(mediaServerData);
        }
    };

    /**
     * BroadcastReceiver for receiving image of playing youtube video
     */
    private BroadcastReceiver _youtubeVideoImageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.getInstance().Debug(TAG, "_youtubeVideoImageReceiver onReceive");
            YoutubeVideoData youtubeVideoData = (YoutubeVideoData) intent.getSerializableExtra(MediaServerService.MediaServerYoutubeVideoDataBundle);
            if (youtubeVideoData != null) {
                Picasso.with(context).load(youtubeVideoData.GetMediumImageUrl()).into(_youtubeVideoImageView);
                _youtubeIdSelectionButton.setText(youtubeVideoData.GetTitle());
            } else {
                Logger.getInstance().Warning(TAG, "youtubeVideo is null");
            }
        }
    };

    /**
     * BroadcastReceiver for receiving updates for center text data
     */
    private BroadcastReceiver _centerTextDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.getInstance().Debug(TAG, "_centerTextDataReceiver onReceive");
            String centerText = MediaServerService.getInstance().GetActiveMediaServer().GetCenterText();
            updateEditTextUi(centerText);
        }
    };

    /**
     * BroadcastReceiver for receiving updates for radio stream data
     */
    private BroadcastReceiver _radioStreamDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.getInstance().Debug(TAG, "_radioStreamDataReceiver onReceive");
            RadioStreamData radioStreamData = MediaServerService.getInstance().GetActiveMediaServer().GetRadioStreamData();
            updateRadioStreamUi(radioStreamData);
        }
    };

    /**
     * BroadcastReceiver for receiving updates for media notification data
     */
    private BroadcastReceiver _mediaNotificationDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.getInstance().Debug(TAG, "_mediaNotificationDataReceiver onReceive");
            MediaNotificationData mediaNotificationData = MediaServerService.getInstance().GetActiveMediaServer().GetMediaNotificationData();
            updateMediaNotificationUi(mediaNotificationData);
        }
    };

    /**
     * BroadcastReceiver for receiving updates for sleep timer data
     */
    private BroadcastReceiver _sleepTimerDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.getInstance().Debug(TAG, "_sleepTimerDataReceiver onReceive");
            MediaServerData mediaServerData = MediaServerService.getInstance().GetActiveMediaServer();
            updateYoutubeUi(mediaServerData);
        }
    };

    /**
     * BroadcastReceiver for receiving updates for rss feed data
     */
    private BroadcastReceiver _rssFeedDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.getInstance().Debug(TAG, "_rssFeedDataReceiver onReceive");
            RSSFeed rssFeed = MediaServerService.getInstance().GetActiveMediaServer().GetRSSFeed();
            updateRssUi(rssFeed);
        }
    };

    /**
     * BroadcastReceiver for receiving updates for information data
     */
    private BroadcastReceiver _informationDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.getInstance().Debug(TAG, "_informationDataReceiver onReceive");
            MediaServerInformationData mediaServerInformationData = MediaServerService.getInstance().GetActiveMediaServer().GetMediaServerInformationData();
            updateInformationUi(mediaServerInformationData);
            updateVolumeUi(mediaServerInformationData);
            updateBrightnessUi(mediaServerInformationData);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_media_server);

        Toolbar toolbar = findViewById(R.id.toolbar_media_server);
        //setSupportActionBar(toolbar);

        _context = this;
        _receiverController = new ReceiverController(_context);

        _drawerLayout = findViewById(R.id.drawer_layout_media_server);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, _drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        _drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view_media_server);
        navigationView.setNavigationItemSelectedListener(this);

        //Define variables for information about MediaServer
        _mediaServerSelectionSpinner = findViewById(R.id.media_server_selection_spinner);
        _mediaServerBatteryTextView = findViewById(R.id.media_server_battery_text_view);
        _mediaServerVersionTextView = findViewById(R.id.media_server_version_text_view);
        setUpInformationUi();

        //Define variables to handle youtube on MediaServer
        _youtubeVideoImageView = findViewById(R.id.youtube_video_image_view);
        _youtubeIdSelectionButton = findViewById(R.id.youtube_id_button);
        _youtubePlayButton = findViewById(R.id.youtube_play_image_button);
        _youtubePauseButton = findViewById(R.id.youtube_pause_image_button);
        _youtubeStopButton = findViewById(R.id.youtube_stop_image_button);
        _youtubePlayPositionSeekBar = findViewById(R.id.youtube_duration_seek_bar);
        _youtubePlayPositionTextView = findViewById(R.id.youtube_video_time_text_view);
        _sleepTimerButton = findViewById(R.id.sleep_timer_button);
        setUpYoutubeUi();

        //Define variables for radio stream
        _radioStreamSelectionSpinner = findViewById(R.id.radio_stream_selection_spinner);
        _radioStreamPlayButton = findViewById(R.id.radio_stream_play_button);
        _radioStreamStopButton = findViewById(R.id.radio_stream_stop_button);
        setUpRadioStreamUi();

        //Define variables for media notification
        _mediaNotificationCardView = findViewById(R.id.media_server_media_notification_card_view);
        _mediaNotificationInformationTextView = findViewById(R.id.media_notification_information_textView);
        _mediaNotificationPlayButton = findViewById(R.id.media_notification_play_button);
        _mediaNotificationStopButton = findViewById(R.id.media_notification_stop_button);
        setUpMediaNotificationUi();

        //Define variables for volume
        _volumeTextView = findViewById(R.id.volume_text_view);
        _volumeIncreaseButton = findViewById(R.id.volume_increase_button);
        _volumeDecreaseButton = findViewById(R.id.volume_decrease_button);
        setUpVolumeUi();

        //Define variables for radio stream
        _rssSelectionSpinner = findViewById(R.id.rss_selection_spinner);
        setUpRssUi();

        //Define variables for editing text on MediaServer
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

        //Define variables for system data
        _systemRebootButton = findViewById(R.id.system_reboot_button);
        _systemShutdownButton = findViewById(R.id.system_shutdown_button);
        setUpSystemUi();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _receiverController.RegisterReceiver(_commandResponseReceiver, new String[]{MediaServerService.MediaServerCommandResponseBroadcast});
        _receiverController.RegisterReceiver(_youtubeDataReceiver, new String[]{MediaServerService.MediaServerYoutubeDataBroadcast});
        _receiverController.RegisterReceiver(_youtubeVideoImageReceiver, new String[]{MediaServerService.MediaServerYoutubeVideoDataBroadcast});
        _receiverController.RegisterReceiver(_centerTextDataReceiver, new String[]{MediaServerService.MediaServerCenterTextDataBroadcast});
        _receiverController.RegisterReceiver(_radioStreamDataReceiver, new String[]{MediaServerService.MediaServerRadioStreamDataBroadcast});
        _receiverController.RegisterReceiver(_mediaNotificationDataReceiver, new String[]{MediaServerService.MediaServerMediaNotificationDataBroadcast});
        _receiverController.RegisterReceiver(_sleepTimerDataReceiver, new String[]{MediaServerService.MediaServerSleepTimerDataBroadcast});
        _receiverController.RegisterReceiver(_rssFeedDataReceiver, new String[]{MediaServerService.MediaServerRssFeedDataBroadcast});
        _receiverController.RegisterReceiver(_informationDataReceiver, new String[]{MediaServerService.MediaServerInformationDataBroadcast});
    }

    @Override
    protected void onPause() {
        super.onPause();
        _receiverController.Dispose();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _receiverController.Dispose();
    }

    @Override
    public void onBackPressed() {
        if (_drawerLayout.isDrawerOpen(GravityCompat.START)) {
            _drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            NavigationService.getInstance().GoBack(this);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        NavigationService.NavigationResult navigationResult = NavigationService.NavigationResult.NULL;

        if (id == R.id.nav_schedule) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, ScheduleActivity.class);
        } else if (id == R.id.nav_timer) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, TimerActivity.class);
        } else if (id == R.id.nav_socket) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, WirelessSocketActivity.class);
        } else if (id == R.id.nav_movie) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, MovieActivity.class);
        } else if (id == R.id.nav_coins) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, CoinActivity.class);
        } else if (id == R.id.nav_menu) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, MenuActivity.class);
        } else if (id == R.id.nav_shopping) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, ShoppingListActivity.class);
        } else if (id == R.id.nav_forecast_weather) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, ForecastWeatherActivity.class);
        } else if (id == R.id.nav_birthday) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, BirthdayActivity.class);
        } else if (id == R.id.nav_security) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, SecurityActivity.class);
        } else if (id == R.id.nav_settings) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, SettingsActivity.class);
        } else if (id == R.id.nav_switch) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, WirelessSwitchActivity.class);
        } else if (id == R.id.nav_meter) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, MeterDataActivity.class);
        } else if (id == R.id.nav_money) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, MoneyMeterDataActivity.class);
        }

        if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
            Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
            displayErrorSnackBar("Failed to navigate! Please contact LucaHome support!");
        }

        _drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Setup for UI
     */

    private void setUpInformationUi() {
        Logger.getInstance().Debug(TAG, "setUpInformationUi");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(_context, android.R.layout.simple_spinner_item, MediaServerService.getInstance().GetServerLocations());
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _mediaServerSelectionSpinner.setAdapter(dataAdapter);

        _mediaServerSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Logger.getInstance().Debug(TAG, "_mediaServerSelectionSpinner onItemSelected _mediaServerSelectionSpinnerEnabled: " + String.valueOf(_mediaServerSelectionSpinnerEnabled));

                if (!_mediaServerSelectionSpinnerEnabled) {
                    Logger.getInstance().Warning(TAG, "_mediaServerSelectionSpinner is disabled!");
                } else {
                    String selectedLocation = MediaServerService.getInstance().GetServerLocations().get(position);
                    MediaServerSelection selectedMediaServer = MediaServerSelection.GetByLocation(selectedLocation);
                    MediaServerService.getInstance().SetActiveMediaServer(selectedMediaServer);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setUpYoutubeUi() {
        Logger.getInstance().Debug(TAG, "setUpYoutubeUi");

        _youtubeIdSelectionButton.setOnClickListener(view -> displayYoutubeIdSelectionDialog());

        _youtubePlayButton.setOnClickListener(view -> MediaServerService.getInstance().SendCommand(MediaServerAction.YOUTUBE_PLAY.toString(), ""));
        _youtubePauseButton.setOnClickListener(view -> MediaServerService.getInstance().SendCommand(MediaServerAction.YOUTUBE_PAUSE.toString(), ""));
        _youtubeStopButton.setOnClickListener(view -> MediaServerService.getInstance().SendCommand(MediaServerAction.YOUTUBE_STOP.toString(), ""));

        _youtubePlayPositionSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                Logger.getInstance().Debug(TAG, "_youtubePlayPositionSeekBar onProgressChanged _youtubePlayPositionSeekBarEnabled: " + String.valueOf(_youtubePlayPositionSeekBarEnabled));

                if (!_youtubePlayPositionSeekBarEnabled) {
                    Logger.getInstance().Warning(TAG, "_youtubePlayPositionSeekBar is disabled!");
                } else {
                    MediaServerService.getInstance().SendCommand(MediaServerAction.YOUTUBE_SET_POSITION.toString(), String.valueOf(progressValue));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        _sleepTimerButton.setOnClickListener(view -> {
            MediaServerData mediaServerData = MediaServerService.getInstance().GetActiveMediaServer();
            SleepTimerData sleepTimerData = mediaServerData.GetSleepTimerData();
            if (sleepTimerData.GetSleepTimerEnabled()) {
                MediaServerService.getInstance().SendCommand(MediaServerAction.SLEEP_SOUND_STOP.toString(), "");
            } else {
                MediaServerService.getInstance().SendCommand(MediaServerAction.SLEEP_SOUND_PLAY.toString(), "");
            }
        });


        _youtubePlayPositionSeekBar.setVisibility(View.GONE);
        _sleepTimerButton.setVisibility(View.GONE);
        _sleepTimerButton.setEnabled(false);
    }

    private void setUpRadioStreamUi() {
        Logger.getInstance().Debug(TAG, "setUpRadioStreamUi");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(_context, android.R.layout.simple_spinner_item, MediaServerService.getInstance().GetRadioStreamTitleList());
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _radioStreamSelectionSpinner.setAdapter(dataAdapter);

        _radioStreamSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Logger.getInstance().Debug(TAG, "_radioStreamSelectionSpinner onItemSelected _radioStreamSelectionSpinnerEnabled: " + String.valueOf(_radioStreamSelectionSpinnerEnabled));

                if (!_radioStreamSelectionSpinnerEnabled) {
                    Logger.getInstance().Warning(TAG, "_radioStreamSelectionSpinner is disabled!");
                } else {
                    String selectedRadioStream = MediaServerService.getInstance().GetRadioStreamTitleList().get(position);
                    RadioStreams radioStream = RadioStreams.GetByTitle(selectedRadioStream);

                    MediaServerService.getInstance().SendCommand(MediaServerAction.RADIO_STREAM_PLAY.toString(), String.valueOf(radioStream.GetId()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        _radioStreamPlayButton.setOnClickListener(view -> MediaServerService.getInstance().SendCommand(MediaServerAction.RADIO_STREAM_PLAY.toString(), ""));
        _radioStreamStopButton.setOnClickListener(view -> MediaServerService.getInstance().SendCommand(MediaServerAction.RADIO_STREAM_STOP.toString(), ""));
    }

    private void setUpMediaNotificationUi() {
        Logger.getInstance().Debug(TAG, "setUpMediaNotificationUi");

        _mediaNotificationPlayButton.setOnClickListener(view -> MediaServerService.getInstance().SendCommand(MediaServerAction.MEDIA_NOTIFICATION_PLAY.toString(), ""));
        _mediaNotificationStopButton.setOnClickListener(view -> MediaServerService.getInstance().SendCommand(MediaServerAction.MEDIA_NOTIFICATION_STOP.toString(), ""));
    }

    private void setUpVolumeUi() {
        Logger.getInstance().Debug(TAG, "setUpVolumeUi");

        _volumeIncreaseButton.setOnClickListener(view -> MediaServerService.getInstance().SendCommand(MediaServerAction.VOLUME_INCREASE.toString(), ""));
        _volumeDecreaseButton.setOnClickListener(view -> MediaServerService.getInstance().SendCommand(MediaServerAction.VOLUME_DECREASE.toString(), ""));
    }

    private void setUpRssUi() {
        Logger.getInstance().Debug(TAG, "setUpRssUi");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(_context, android.R.layout.simple_spinner_item, MediaServerService.getInstance().GetRssFeedTitleList());
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _rssSelectionSpinner.setAdapter(dataAdapter);

        _rssSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Logger.getInstance().Debug(TAG, "_rssSelectionSpinner onItemSelected _rssFeedSelectionSpinnerEnabled: " + _rssFeedSelectionSpinnerEnabled);

                if (!_rssFeedSelectionSpinnerEnabled) {
                    Logger.getInstance().Warning(TAG, "_radioStreamSelectionSpinner is disabled!");
                } else {
                    String selectedRssStream = MediaServerService.getInstance().GetRssFeedTitleList().get(position);
                    RSSFeed rssFeed = RSSFeed.GetByTitle(selectedRssStream);
                    MediaServerService.getInstance().SendCommand(MediaServerAction.RSS_FEED_SET.toString(), String.valueOf(rssFeed.GetId()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setUpEditTextUi() {
        Logger.getInstance().Debug(TAG, "setUpEditTextUi");

        _sendTextButton.setOnClickListener(view -> {
            String sendText = _editText.getText().toString();
            if (sendText.length() < 1 || sendText.length() > 500) {
                Logger.getInstance().Error(TAG, "sendText is null!");
                displayErrorSnackBar("sendText is null!");
                return;
            }
            MediaServerService.getInstance().SendCommand(MediaServerAction.CENTER_TEXT_SET.toString(), sendText);
        });
    }

    private void setUpBrightnessUi() {
        Logger.getInstance().Debug(TAG, "setUpBrightnessUi");

        _brightnessIncreaseButton.setOnClickListener(view -> MediaServerService.getInstance().SendCommand(MediaServerAction.SCREEN_BRIGHTNESS_INCREASE.toString(), ""));
        _brightnessDecreaseButton.setOnClickListener(view -> MediaServerService.getInstance().SendCommand(MediaServerAction.SCREEN_BRIGHTNESS_DECREASE.toString(), ""));
    }

    private void setUpUpdateUi() {
        Logger.getInstance().Debug(TAG, "setUpBrightnessUi");

        _updateCurrentWeatherButton.setOnClickListener(view -> MediaServerService.getInstance().SendCommand(MediaServerAction.UPDATE_CURRENT_WEATHER.toString(), ""));
        _updateForecastWeatherButton.setOnClickListener(view -> MediaServerService.getInstance().SendCommand(MediaServerAction.UPDATE_FORECAST_WEATHER.toString(), ""));
        _updateTemperatureButton.setOnClickListener(view -> MediaServerService.getInstance().SendCommand(MediaServerAction.UPDATE_RASPBERRY_TEMPERATURE.toString(), ""));
        _updateIpButton.setOnClickListener(view -> MediaServerService.getInstance().SendCommand(MediaServerAction.UPDATE_IP_ADDRESS.toString(), ""));
        _updateBirthdaysButton.setOnClickListener(view -> MediaServerService.getInstance().SendCommand(MediaServerAction.UPDATE_BIRTHDAY_ALARM.toString(), ""));
        _updateCalendarButton.setOnClickListener(view -> MediaServerService.getInstance().SendCommand(MediaServerAction.UPDATE_CALENDAR_ALARM.toString(), ""));
    }

    private void setUpSystemUi() {
        Logger.getInstance().Debug(TAG, "setUpSystemUi");

        _systemRebootButton.setOnClickListener(view -> MediaServerService.getInstance().SendCommand(MediaServerAction.SYSTEM_REBOOT.toString(), ""));
        _systemShutdownButton.setOnClickListener(view -> MediaServerService.getInstance().SendCommand(MediaServerAction.SYSTEM_SHUTDOWN.toString(), ""));
    }

    /**
     * Update of UI elements
     */
    private void updateInformationUi(@NonNull MediaServerInformationData mediaServerInformationData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "updateMediaServerInformationUi with MediaServerInformationData %s", mediaServerInformationData));
        _mediaServerSelectionSpinnerEnabled = false;
        _mediaServerBatteryTextView.setText(String.format(Locale.getDefault(), "Battery %d%%", mediaServerInformationData.GetCurrentBatteryLevel()));
        _mediaServerVersionTextView.setText(String.format(Locale.getDefault(), "Version: %s", mediaServerInformationData.GetServerVersion()));
        _mediaServerSelectionSpinner.setSelection(mediaServerInformationData.GetMediaServerSelection().GetId());
        new Handler().postDelayed(() -> _mediaServerSelectionSpinnerEnabled = true, 1000);
    }

    private void updateYoutubeUi(@NonNull MediaServerData mediaServerData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "updateYoutubeMediaServerUi with MediaServerData %s", mediaServerData));

        YoutubeData youtubeData = mediaServerData.GetYoutubeData();
        SleepTimerData sleepTimerData = mediaServerData.GetSleepTimerData();
        MediaServerInformationData mediaServerInformationData = mediaServerData.GetMediaServerInformationData();

        _youtubePlayPositionSeekBarEnabled = false;

        _youtubePlayPositionSeekBar.setVisibility(youtubeData.IsYoutubePlaying() ? View.VISIBLE : View.GONE);
        _youtubePlayPositionTextView.setVisibility(youtubeData.IsYoutubePlaying() ? View.VISIBLE : View.GONE);
        _sleepTimerButton.setVisibility((sleepTimerData.GetSleepTimerEnabled() && mediaServerInformationData.GetMediaServerSelection().IsSleepingServer()) ? View.VISIBLE : View.GONE);

        _youtubePlayPositionSeekBar.setEnabled(youtubeData.IsYoutubePlaying());
        _youtubePlayButton.setEnabled(!youtubeData.IsYoutubePlaying());
        _youtubePauseButton.setEnabled(youtubeData.IsYoutubePlaying());
        _youtubeStopButton.setEnabled(youtubeData.IsYoutubePlaying());
        _sleepTimerButton.setEnabled(sleepTimerData.GetSleepTimerEnabled() && mediaServerInformationData.GetMediaServerSelection().IsSleepingServer());

        if (youtubeData.IsYoutubePlaying()) {
            int currentVideoPlayTime = youtubeData.GetCurrentYoutubeVideoPosition();
            int totalVideoPlayTime = youtubeData.GetCurrentYoutubeVideoDuration();

            loadYoutubeVideoImage(youtubeData);

            if (currentVideoPlayTime == -1 || totalVideoPlayTime == -1
                    || currentVideoPlayTime == 0 || totalVideoPlayTime == 0) {
                Logger.getInstance().Error(TAG, String.format(Locale.getDefault(),
                        "Invalid values: currentVideoPlayTime: %d: totalVideoPlayTime: %d",
                        currentVideoPlayTime, totalVideoPlayTime));

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
                        currentVideoPlayTimeHour, currentVideoPlayTimeMinute, currentVideoPlayTimeSecond,
                        totalVideoPlayTimeHour, totalVideoPlayTimeMinute, totalVideoPlayTimeSecond);

                _youtubePlayPositionTextView.setText(youtubePlayTimeText);
            }
        }

        new Handler().postDelayed(() -> _youtubePlayPositionSeekBarEnabled = true, 1000);
    }

    private void updateRadioStreamUi(@NonNull RadioStreamData radioStreamData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "updateRadioStreamUi with RadioStreamData %s", radioStreamData));
        _radioStreamSelectionSpinnerEnabled = false;
        _radioStreamPlayButton.setEnabled(!radioStreamData.GetRadioStreamIsPlaying());
        _radioStreamStopButton.setEnabled(radioStreamData.GetRadioStreamIsPlaying());
        _radioStreamSelectionSpinner.setSelection(radioStreamData.GetRadioStreamFeed().GetId());
        new Handler().postDelayed(() -> _radioStreamSelectionSpinnerEnabled = true, 1000);
    }

    private void updateMediaNotificationUi(@NonNull MediaNotificationData mediaNotificationData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "updateMediaNotificationUi with MediaNotificationData %s", mediaNotificationData));
        _mediaNotificationInformationTextView.setText(mediaNotificationData.GetInformationString());
        if (mediaNotificationData.IsVisible()) {
            _mediaNotificationCardView.setVisibility(View.VISIBLE);
        } else {
            _mediaNotificationCardView.setVisibility(View.GONE);
        }
    }

    private void updateVolumeUi(@NonNull MediaServerInformationData mediaServerInformationData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "updateVolumeUi with MediaServerInformationData %s", mediaServerInformationData));
        _volumeTextView.setText(String.format(Locale.getDefault(), "Volume: %d", mediaServerInformationData.GetCurrentVolume()));
    }

    private void updateRssUi(@NonNull RSSFeed rssFeed) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "updateRssUi with RSSFeed %s", rssFeed));
        _rssFeedSelectionSpinnerEnabled = false;
        _rssSelectionSpinner.setSelection(rssFeed.GetId());
        new Handler().postDelayed(() -> _rssFeedSelectionSpinnerEnabled = true, 1000);

    }

    private void updateEditTextUi(@NonNull String centerText) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "updateEditTextUi with CenterText %s", centerText));
        _editText.setText(centerText);
    }

    private void updateBrightnessUi(@NonNull MediaServerInformationData mediaServerInformationData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "updateBrightnessUi with MediaServerInformationData %s", mediaServerInformationData));
        _brightnessTextView.setText(String.format(Locale.getDefault(), "Brightness: %d%%", ((mediaServerInformationData.GetCurrentScreenBrightness() * 100) / 255)));
    }

    /**
     * Dialog methods
     */
    private void displayYoutubeIdSelectionDialog() {
        final Dialog dialog = new Dialog(_context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_select_youtube_id);

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
                youtubeIdEditText.setText(selectedYoutubeId.GetYoutubeId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        MediaServerData activeMediaServerData = MediaServerService.getInstance().GetActiveMediaServer();
        YoutubeData youtubeData = activeMediaServerData.GetYoutubeData();
        ArrayList<PlayedYoutubeVideoData> playedYoutubeVideoDataList = youtubeData.GetPlayedYoutubeVideos();

        final ArrayList<String> playedYoutubeIdStrings = new ArrayList<>();
        for (PlayedYoutubeVideoData entry : playedYoutubeVideoDataList) {
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

            MediaServerService.getInstance().SendCommand(MediaServerAction.YOUTUBE_PLAY.toString(), youtubeId);

            dialog.dismiss();
        });

        dialog.setCancelable(true);
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        } else {
            Logger.getInstance().Warning(TAG, "Window is null!");
        }
    }

    private void displayYoutubeIdSearchDialog() {
        final Dialog dialog = new Dialog(_context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edittext);

        TextView titleView = dialog.findViewById(R.id.dialog_title);
        titleView.setText(R.string.searchYoutube);

        TextView promptView = dialog.findViewById(R.id.dialog_prompt);
        promptView.setText(R.string.promptEnterSearch);

        final EditText inputEditText = dialog.findViewById(R.id.dialog_edittext);

        Button closeButton = dialog.findViewById(R.id.dialog_button_close);
        closeButton.setText(R.string.search);
        closeButton.setOnClickListener(v -> {
            String input = inputEditText.getText().toString();

            if (input.length() == 0) {
                Logger.getInstance().Error(TAG, "Input has invalid length 0!");
                Toasty.error(_context, "Input has invalid length 0!", Toast.LENGTH_LONG).show();
                return;
            }

            searchYoutubeVideos(input);
        });

        dialog.setCancelable(true);
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        } else {
            Logger.getInstance().Warning(TAG, "Window is null!");
        }
    }

    /**
     * Youtube data methods
     */
    private void searchYoutubeVideos(@NonNull String searchString) {
        ProgressDialog loadingVideosDialog = ProgressDialog.show(_context, "Loading Videos...", "");
        loadingVideosDialog.setCancelable(false);

        searchString = searchString.replace(" ", "+");
        String url = String.format(Locale.getDefault(), Constants.YOUTUBE_SEARCH, Constants.YOUTUBE_MAX_RESULTS, searchString, Keys.YOUTUBE_API_KEY);

        DownloadYoutubeVideoTask task = new DownloadYoutubeVideoTask(_context, loadingVideosDialog, false, true);
        task.execute(url);
    }

    private void loadYoutubeVideoImage(@NonNull YoutubeData youtubeData) {
        String url = String.format(Locale.getDefault(), Constants.YOUTUBE_SEARCH, 1, youtubeData.GetCurrentYoutubeId(), Keys.YOUTUBE_API_KEY);
        DownloadYoutubeVideoTask task = new DownloadYoutubeVideoTask(_context, null, true, false);
        task.execute(url);
    }

    /**
     * SnackBar methods
     */
    private void displaySuccessSnackBar(@NonNull String message) {
        Snacky.builder()
                .setActivty(MediaServerActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_LONG)
                .setActionText(android.R.string.ok)
                .success()
                .show();
    }

    private void displayErrorSnackBar(@NonNull String message) {
        Snacky.builder()
                .setActivty(MediaServerActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_LONG)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }
}
