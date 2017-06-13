package guepardoapps.lucahome.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.io.Serializable;
import java.sql.Date;
import java.util.Calendar;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.constants.Constants;
import guepardoapps.library.lucahome.common.constants.IDs;
import guepardoapps.library.lucahome.common.constants.SharedPrefConstants;
import guepardoapps.library.lucahome.common.constants.Timeouts;
import guepardoapps.library.lucahome.common.dto.ActionDto;
import guepardoapps.library.lucahome.common.dto.BirthdayDto;
import guepardoapps.library.lucahome.common.dto.ChangeDto;
import guepardoapps.library.lucahome.common.dto.InformationDto;
import guepardoapps.library.lucahome.common.dto.ListedMenuDto;
import guepardoapps.library.lucahome.common.dto.MapContentDto;
import guepardoapps.library.lucahome.common.dto.MediaMirrorViewDto;
import guepardoapps.library.lucahome.common.dto.MenuDto;
import guepardoapps.library.lucahome.common.dto.MotionCameraDto;
import guepardoapps.library.lucahome.common.dto.MovieDto;
import guepardoapps.library.lucahome.common.dto.ScheduleDto;
import guepardoapps.library.lucahome.common.dto.ShoppingEntryDto;
import guepardoapps.library.lucahome.common.dto.TemperatureDto;
import guepardoapps.library.lucahome.common.dto.TimerDto;
import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.library.lucahome.common.enums.Command;
import guepardoapps.library.lucahome.common.enums.HomeAutomationAction;
import guepardoapps.library.lucahome.common.enums.LucaServerAction;
import guepardoapps.library.lucahome.common.enums.MediaServerAction;
import guepardoapps.library.lucahome.common.enums.MediaServerSelection;
import guepardoapps.library.lucahome.common.enums.TemperatureType;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.DatabaseController;
import guepardoapps.library.lucahome.controller.LucaDialogController;
import guepardoapps.library.lucahome.controller.LucaNotificationController;
import guepardoapps.library.lucahome.controller.MediaMirrorController;
import guepardoapps.library.lucahome.controller.MenuController;
import guepardoapps.library.lucahome.controller.ServiceController;
import guepardoapps.library.lucahome.converter.json.JsonDataToScheduleConverter;
import guepardoapps.library.lucahome.converter.json.JsonDataToSocketConverter;
import guepardoapps.library.lucahome.converter.json.JsonDataToTemperatureConverter;
import guepardoapps.library.lucahome.converter.json.JsonDataToTimerConverter;
import guepardoapps.library.lucahome.services.sockets.SocketActionService;

import guepardoapps.library.openweather.common.OWBroadcasts;
import guepardoapps.library.openweather.common.OWBundles;
import guepardoapps.library.openweather.common.OWIds;
import guepardoapps.library.openweather.common.model.ForecastModel;
import guepardoapps.library.openweather.common.model.WeatherModel;
import guepardoapps.library.openweather.controller.OpenWeatherController;

import guepardoapps.library.toolset.beacon.BeaconController;
import guepardoapps.library.toolset.common.classes.NotificationContent;
import guepardoapps.library.toolset.common.classes.SerializableDate;
import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.common.classes.SerializableTime;
import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.DialogController;
import guepardoapps.library.toolset.controller.NetworkController;
import guepardoapps.library.toolset.controller.ReceiverController;
import guepardoapps.library.toolset.controller.SharedPrefController;
import guepardoapps.library.toolset.scheduler.ScheduleService;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.enums.NavigationData;
import guepardoapps.lucahome.tasks.ConverterTask;
import guepardoapps.lucahome.views.BirthdayView;
import guepardoapps.lucahome.views.ForecastWeatherView;
import guepardoapps.lucahome.views.SecurityView;
import guepardoapps.lucahome.views.SensorTemperatureView;

public class MainService extends Service {

    public enum MainServiceAction implements Serializable {BOOT}

    private static final String TAG = MainService.class.getSimpleName();
    private LucaHomeLogger _logger;

    public static final int SLEEP_NOTIFICATION_HOUR = 21;
    public static final int SLEEP_NOTIFICATION_MINUTE = 45;
    public static final int SLEEP_NOTIFICATION_TIME_SPAN_MINUTE = 5;

    private static final int CLEAR_NOTIFICATION_DISPLAY_HOUR = 2;
    private static final int CLEAR_NOTIFICATION_DISPLAY_MINUTE = 0;

    private boolean _downloadingData;
    private boolean _schedulesAdded;

    private int _progress = 0;
    private int _downloadCount = Constants.DOWNLOAD_STEPS;

    private Calendar _lastUpdate;

    private WeatherModel _currentWeather;
    private ForecastModel _forecastWeather;

    private SerializableList<BirthdayDto> _birthdayList = null;
    private SerializableList<ChangeDto> _changeList = null;
    private InformationDto _information = null;
    private SerializableList<MapContentDto> _mapContentList = null;
    private SerializableList<MediaMirrorViewDto> _mediaMirrorList = new SerializableList<>();
    private SerializableList<ListedMenuDto> _listedMenu = null;
    private SerializableList<MenuDto> _menu = null;
    private MotionCameraDto _motionCameraDto = null;
    private SerializableList<MovieDto> _movieList = null;
    private SerializableList<ScheduleDto> _scheduleList = null;
    private SerializableList<ShoppingEntryDto> _shoppingList = null;
    private SerializableList<TemperatureDto> _temperatureList = null;
    private SerializableList<TimerDto> _timerList = null;
    private SerializableList<WirelessSocketDto> _wirelessSocketList = null;

    private Handler _timeoutHandler = new Handler();

    private boolean _isInitialized;
    private Context _context;

    private BeaconController _beaconController;
    private BroadcastController _broadcastController;
    private DatabaseController _databaseController;
    private LucaNotificationController _notificationController;
    private MediaMirrorController _mediaMirrorController;
    private MenuController _menuController;
    private NetworkController _networkController;
    private OpenWeatherController _openWeatherController;
    private ReceiverController _receiverController;
    private ScheduleService _scheduleService;
    private ServiceController _serviceController;
    private SharedPrefController _sharedPrefController;

    private BroadcastReceiver _actionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_actionReceiver onReceive");
            HomeAutomationAction action = (HomeAutomationAction) intent.getSerializableExtra(Bundles.HOME_AUTOMATION_ACTION);
            if (action != null) {
                _logger.Debug(String.format("Action is %s", action.toString()));
                _progress = 0;
                switch (action) {
                    case GET_ALL:
                        _broadcastController.SendSerializableArrayBroadcast(
                                Broadcasts.UPDATE_MAP_CONTENT_VIEW,
                                new String[]{Bundles.MAP_CONTENT_LIST, Bundles.SOCKET_LIST, Bundles.SCHEDULE_LIST, Bundles.TIMER_LIST, Bundles.TEMPERATURE_LIST, Bundles.SHOPPING_LIST, Bundles.MENU, Bundles.LISTED_MENU, Bundles.MOTION_CAMERA_DTO},
                                new Object[]{_mapContentList, _wirelessSocketList, _scheduleList, _timerList, _temperatureList, _shoppingList, _menu, _listedMenu, _motionCameraDto});
                        break;
                    case GET_BIRTHDAY_LIST:
                        _broadcastController.SendSerializableArrayBroadcast(
                                Broadcasts.UPDATE_BIRTHDAY,
                                new String[]{Bundles.BIRTHDAY_LIST},
                                new Object[]{_birthdayList});
                        sendBirthdaysToWear();
                        break;
                    case GET_CHANGE_LIST:
                        _broadcastController.SendSerializableArrayBroadcast(
                                Broadcasts.UPDATE_CHANGE,
                                new String[]{Bundles.CHANGE_LIST},
                                new Object[]{_changeList});
                        break;
                    case GET_INFORMATION_LIST:
                        _broadcastController.SendSerializableArrayBroadcast(
                                Broadcasts.UPDATE_INFORMATION,
                                new String[]{Bundles.INFORMATION_SINGLE},
                                new Object[]{_information});
                        break;
                    case GET_MAP_DATA:
                        _broadcastController.SendSerializableArrayBroadcast(
                                Broadcasts.UPDATE_MAP_CONTENT_VIEW,
                                new String[]{Bundles.MAP_CONTENT_LIST, Bundles.SOCKET_LIST, Bundles.SCHEDULE_LIST, Bundles.TIMER_LIST, Bundles.TEMPERATURE_LIST, Bundles.SHOPPING_LIST, Bundles.MENU, Bundles.LISTED_MENU, Bundles.MOTION_CAMERA_DTO},
                                new Object[]{_mapContentList, _wirelessSocketList, _scheduleList, _timerList, _temperatureList, _shoppingList, _menu, _listedMenu, _motionCameraDto});
                        break;
                    case GET_MEDIA_MIRROR_DATA:
                        _broadcastController.SendSerializableArrayBroadcast(
                                Broadcasts.UPDATE_MEDIAMIRROR,
                                new String[]{Bundles.MEDIAMIRROR},
                                new Object[]{_mediaMirrorList});
                        sendMediaMirrorToWear();
                        break;
                    case GET_MENU_LIST:
                        _broadcastController.SendSerializableArrayBroadcast(
                                Broadcasts.UPDATE_MENU_VIEW,
                                new String[]{Bundles.MENU},
                                new Object[]{_menu});
                        _broadcastController.SendSerializableArrayBroadcast(
                                Broadcasts.UPDATE_LISTED_MENU_VIEW,
                                new String[]{Bundles.LISTED_MENU},
                                new Object[]{_listedMenu});
                        sendMenuToWear();
                        break;
                    case GET_MOTION_CAMERA_DATA:
                        _broadcastController.SendSerializableArrayBroadcast(
                                Broadcasts.UPDATE_MOTION_CAMERA_DTO,
                                new String[]{Bundles.MOTION_CAMERA_DTO},
                                new Object[]{_motionCameraDto});
                        break;
                    case GET_MOVIE_LIST:
                        _broadcastController.SendSerializableArrayBroadcast(
                                Broadcasts.UPDATE_MOVIE,
                                new String[]{Bundles.MOVIE_LIST},
                                new Object[]{_movieList});
                        break;
                    case GET_SCHEDULE_LIST:
                        _broadcastController.SendSerializableArrayBroadcast(
                                Broadcasts.UPDATE_SCHEDULE,
                                new String[]{Bundles.SCHEDULE_LIST, Bundles.SOCKET_LIST},
                                new Object[]{_scheduleList, _wirelessSocketList});
                        sendSchedulesToWear();
                        break;
                    case GET_SHOPPING_LIST:
                        _broadcastController.SendSerializableArrayBroadcast(
                                Broadcasts.UPDATE_SHOPPING_LIST,
                                new String[]{Bundles.SHOPPING_LIST},
                                new Object[]{_shoppingList});
                        sendShoppingListToWear();
                        break;
                    case GET_SOCKET_LIST:
                        _broadcastController.SendSerializableArrayBroadcast(
                                Broadcasts.UPDATE_SOCKET_LIST,
                                new String[]{Bundles.SOCKET_LIST},
                                new Object[]{_wirelessSocketList});
                        sendSocketsToWear();
                        break;
                    case GET_TEMPERATURE_LIST:
                        _broadcastController.SendSerializableArrayBroadcast(
                                Broadcasts.UPDATE_TEMPERATURE,
                                new String[]{Bundles.TEMPERATURE_LIST},
                                new Object[]{_temperatureList});
                        sendTemperatureToWear();
                        break;
                    case GET_TIMER_LIST:
                        _broadcastController.SendSerializableArrayBroadcast(
                                Broadcasts.UPDATE_TIMER,
                                new String[]{Bundles.TIMER_LIST, Bundles.SOCKET_LIST},
                                new Object[]{_timerList, _wirelessSocketList});
                        sendTimerToWear();
                        break;
                    case GET_WEATHER_CURRENT:
                        _broadcastController.SendSerializableArrayBroadcast(
                                Broadcasts.UPDATE_WEATHER_VIEW,
                                new String[]{Bundles.WEATHER_CURRENT},
                                new Object[]{_currentWeather});
                        sendWeatherToWear();
                        break;
                    case GET_WEATHER_FORECAST:
                        _broadcastController.SendSerializableArrayBroadcast(
                                Broadcasts.UPDATE_FORECAST_VIEW,
                                new String[]{Bundles.WEATHER_FORECAST},
                                new Object[]{_forecastWeather});
                        break;

                    case ENABLE_HEATING:
                        enableHeatingSocket(false);
                        break;
                    case ENABLE_HEATING_AND_SOUND:
                        enableHeatingSocket(true);
                        break;
                    case ENABLE_SEA_SOUND:
                        enableSoundSchedule(30);
                        break;
                    case DISABLE_SEA_SOUND:
                        for (MediaServerSelection entry : MediaServerSelection.values()) {
                            if (entry.IsSleepingMirror()) {
                                _mediaMirrorController.SendCommand(
                                        entry.GetIp(),
                                        MediaServerAction.STOP_SEA_SOUND.toString(),
                                        "");
                                break;
                            }
                        }
                        break;

                    case SHOW_NOTIFICATION_SOCKET:
                        if (_wirelessSocketList != null) {
                            _notificationController.CreateSocketNotification(_wirelessSocketList);
                        }
                        break;
                    case SHOW_NOTIFICATION_TEMPERATURE:
                        if (_temperatureList != null && _currentWeather != null) {
                            _notificationController.CreateTemperatureNotification(
                                    SensorTemperatureView.class,
                                    _temperatureList,
                                    _currentWeather);
                        }
                        break;
                    case SHOW_NOTIFICATION_WEATHER:
                        _downloadForecastWeatherRunnable.run();
                        break;

                    case DISABLE_SECURITY_CAMERA:
                        _serviceController.StartRestService(
                                Bundles.MOTION_CAMERA_DTO,
                                LucaServerAction.STOP_MOTION.toString(),
                                Broadcasts.RELOAD_MOTION_CAMERA_DTO);
                        break;

                    case BEACON_SCANNING_START:
                        _beaconController.StartScanning();
                        break;
                    case BEACON_SCANNING_STOP:
                        _beaconController.StopScanning();
                        break;

                    default:
                        _logger.Warn("action is not supported! " + action.toString());
                        break;
                }
            } else {
                _logger.Warn("action is null!");
            }
        }

        private void enableHeatingSocket(boolean enableSound) {
            _logger.Debug("enableHeatingSocket");
            if (_wirelessSocketList == null) {
                _logger.Warn("SocketList is null!");
                return;
            }

            for (int socketIndex = 0; socketIndex < _wirelessSocketList.getSize(); socketIndex++) {
                if (_wirelessSocketList.getValue(socketIndex).GetName().contains("Heating")) {
                    Intent serviceIntent = new Intent(_context, SocketActionService.class);

                    Bundle serviceData = new Bundle();
                    serviceData.putSerializable(Bundles.SOCKET_DATA, _wirelessSocketList.getValue(socketIndex));
                    serviceIntent.putExtras(serviceData);

                    _context.startService(serviceIntent);

                    createDeactivatingSchedule(_wirelessSocketList.getValue(socketIndex).GetName(), enableSound);

                    return;
                }
            }

            _logger.Warn("Found no socket for heating!");
            Toasty.warning(_context, "Found no socket for heating!", Toast.LENGTH_LONG).show();
        }

        private void createDeactivatingSchedule(String socketName, boolean enableSound) {
            _logger.Debug("createDeactivatingSchedule");

            int timerMinute = _sharedPrefController.LoadIntegerValueFromSharedPreferences(SharedPrefConstants.TIMER_MIN);
            _logger.Debug(String.format(Locale.getDefault(), "Loaded time value is %d sec", timerMinute));

            if (timerMinute == 0) {
                _logger.Error(String.format("timerMinute and timerHour is 0! Setting timerMinute to default %s!", SharedPrefConstants.DEFAULT_TIMER_MIN));
                timerMinute = SharedPrefConstants.DEFAULT_TIMER_MIN;
            }

            Calendar currentTime = Calendar.getInstance();
            int currentDay = currentTime.get(Calendar.DAY_OF_WEEK) - 1;
            _logger.Debug(String.format("Current time is %s", currentTime));

            int scheduleMinute = currentTime.get(Calendar.MINUTE) + timerMinute;
            int scheduleHour = currentTime.get(Calendar.HOUR_OF_DAY);
            _logger.Debug(String.format("schedule time is %s:%s", scheduleHour, scheduleMinute));

            if (scheduleMinute > 59) {
                scheduleMinute -= 60;
                scheduleHour++;
            }
            if (scheduleHour > 23) {
                scheduleHour -= 24;
                currentDay++;
            }
            if (currentDay > 6) {
                currentDay -= 7;
            }

            String addHeatingTimerAction = LucaServerAction.ADD_SCHEDULE.toString()
                    + socketName + "_" + String.valueOf(scheduleHour) + "_" + String.valueOf(scheduleMinute)
                    + "&socket=" + socketName
                    + "&gpio=&weekday=" + currentDay
                    + "&hour=" + scheduleHour
                    + "&minute=" + scheduleMinute
                    + "&onoff=0&isTimer=1&playSound=0&playRaspberry=1";
            _logger.Debug(String.format(Locale.getDefault(), "Heating action is %s", addHeatingTimerAction));

            _serviceController.StartRestService(
                    "",
                    addHeatingTimerAction,
                    Broadcasts.RELOAD_TIMER);

            if (enableSound) {
                enableSoundSchedule(30);
            }

            _downloadSocketRunnable.run();
            _downloadScheduleRunnable.run();
        }

        private void enableSoundSchedule(int playLength) {
            _logger.Debug("enableSoundSchedule");

            for (MediaServerSelection entry : MediaServerSelection.values()) {
                if (entry.IsSleepingMirror()) {
                    _mediaMirrorController.SendCommand(
                            entry.GetIp(),
                            MediaServerAction.PLAY_SEA_SOUND.toString(),
                            String.valueOf(playLength));
                    break;
                }
            }
        }
    };

    private BroadcastReceiver _addReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_addReceiver onReceive");

            String action = intent.getStringExtra(Bundles.ACTION);
            if (action == null) {
                _logger.Error("_addReceiver received data with null action!");
                return;
            }

            String bundle;
            String broadcast;

            if (action.contains(LucaServerAction.ADD_BIRTHDAY.toString())) {
                bundle = Bundles.BIRTHDAY_DOWNLOAD;
                broadcast = Broadcasts.ADD_BIRTHDAY;
            } else if (action.contains(LucaServerAction.ADD_MOVIE.toString())) {
                bundle = Bundles.MOVIE_DOWNLOAD;
                broadcast = Broadcasts.ADD_MOVIE;
            } else if (action.contains(LucaServerAction.ADD_SCHEDULE.toString())) {
                bundle = Bundles.SCHEDULE_DOWNLOAD;
                broadcast = Broadcasts.ADD_SCHEDULE;
            } else if (action.contains(LucaServerAction.ADD_SOCKET.toString())) {
                bundle = Bundles.SOCKET_DOWNLOAD;
                broadcast = Broadcasts.ADD_SOCKET;
            } else {
                _logger.Error(String.format(Locale.getDefault(), "Invalid action %s!", action));
                return;
            }

            _serviceController.StartRestService(
                    bundle,
                    action,
                    broadcast);
        }
    };

    private BroadcastReceiver _startDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_startDownloadReceiver onReceive");

            LucaDialogController.LucaObject lucaObject = (LucaDialogController.LucaObject) intent.getSerializableExtra(Bundles.LUCA_OBJECT);
            if (lucaObject == null) {
                _logger.Error("_downloadReceiver received data with null lucaObject!");
                return;
            }

            String action = intent.getStringExtra(Bundles.ACTION);
            if (action != null) {
                _logger.Error("_startDownloadReceiver received data with action! Not performing download!");
                return;
            }

            switch (lucaObject) {
                case BIRTHDAY:
                    String[] birthdayAnswerArray = intent.getStringArrayExtra(Bundles.BIRTHDAY_DOWNLOAD);
                    if (birthdayAnswerArray == null) {
                        _logger.Error("birthdayAnswerArray is null!");
                        return;
                    }

                    if (actionWasSuccessful(birthdayAnswerArray)) {
                        _downloadBirthdayRunnable.run();
                    } else {
                        _logger.Warn("Add of birthday failed!");
                        Toasty.error(_context, "Add of birthday failed!", Toast.LENGTH_LONG).show();
                    }
                    break;

                case MENU:
                    String[] menuAnswerArray = intent.getStringArrayExtra(Bundles.MENU_DOWNLOAD);
                    if (menuAnswerArray == null) {
                        _logger.Error("menuAnswerArray is null!");
                        return;
                    }

                    if (actionWasSuccessful(menuAnswerArray)) {
                        _downloadMenuRunnable.run();
                    } else {
                        _logger.Warn("Add of menu failed!");
                        Toasty.error(_context, "Add of menu failed!", Toast.LENGTH_LONG).show();
                    }
                    break;

                case MOVIE:
                    String[] movieAnswerArray = intent.getStringArrayExtra(Bundles.MOVIE_DOWNLOAD);
                    if (movieAnswerArray == null) {
                        _logger.Error("movieAnswerArray is null!");
                        return;
                    }

                    if (actionWasSuccessful(movieAnswerArray)) {
                        _downloadMovieRunnable.run();
                    } else {
                        _logger.Warn("Add of movie failed!");
                        Toasty.error(_context, "Add of movie failed!", Toast.LENGTH_LONG).show();
                    }
                    break;

                case SCHEDULE:
                case TIMER:
                    String[] scheduleAnswerArray = intent.getStringArrayExtra(Bundles.SCHEDULE_DOWNLOAD);
                    if (scheduleAnswerArray == null) {
                        _logger.Error("scheduleAnswerArray is null!");
                        return;
                    }

                    if (actionWasSuccessful(scheduleAnswerArray)) {
                        _downloadScheduleRunnable.run();
                    } else {
                        _logger.Warn("Add of schedule failed!");
                        Toasty.error(_context, "Add of schedule failed!", Toast.LENGTH_LONG).show();
                    }
                    break;

                case SOCKET:
                    String[] socketAnswerArray = intent.getStringArrayExtra(Bundles.SOCKET_DOWNLOAD);
                    if (socketAnswerArray == null) {
                        _logger.Error("socketAnswerArray is null!");
                        return;
                    }

                    if (actionWasSuccessful(socketAnswerArray)) {
                        _downloadSocketRunnable.run();
                    } else {
                        _logger.Warn("Add of socket failed!");
                        Toasty.error(_context, "Add of socket failed!", Toast.LENGTH_LONG).show();
                    }
                    break;

                default:
                    _logger.Error("Cannot start download object: " + lucaObject.toString());
                    break;
            }
        }

        private boolean actionWasSuccessful(@NonNull String[] answerArray) {
            boolean actionSuccess = true;

            for (String answer : answerArray) {
                _logger.Debug(answer);
                if (!answer.endsWith("1")) {
                    actionSuccess = false;
                    break;
                }
            }

            return actionSuccess;
        }
    };

    private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateReceiver onReceive");

            LucaDialogController.LucaObject lucaServerObject = (LucaDialogController.LucaObject) intent.getSerializableExtra(Bundles.LUCA_OBJECT);
            if (lucaServerObject == null) {
                _logger.Error("_updateReceiver received data with null lucaServerObject!");
                return;
            }

            String action = intent.getStringExtra(Bundles.ACTION);
            if (action == null) {
                _logger.Error("_updateReceiver received data with null action!");
                return;
            }

            switch (lucaServerObject) {
                case BIRTHDAY:
                    _serviceController.StartRestService(
                            Bundles.BIRTHDAY_DOWNLOAD,
                            action,
                            Broadcasts.UPDATE_BIRTHDAY);
                    break;
                case MENU:
                    _serviceController.StartRestService(
                            Bundles.MENU_DOWNLOAD,
                            action,
                            Broadcasts.UPDATE_MENU);
                    break;
                case MOVIE:
                    _serviceController.StartRestService(
                            Bundles.MOVIE_DOWNLOAD,
                            action,
                            Broadcasts.UPDATE_MOVIE);
                    break;
                case SCHEDULE:
                case TIMER:
                    _serviceController.StartRestService(
                            Bundles.SCHEDULE_DOWNLOAD,
                            action,
                            Broadcasts.UPDATE_SCHEDULE);
                    break;
                case SOCKET:
                    _serviceController.StartRestService(
                            Bundles.SOCKET_DOWNLOAD,
                            action,
                            Broadcasts.UPDATE_SOCKET_LIST);
                    break;
                default:
                    _logger.Error("Cannot update object: " + lucaServerObject.toString());
                    break;
            }
        }
    };

    private BroadcastReceiver _birthdayConvertedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_birthdayConvertedReceiver onReceive");

            SerializableList<BirthdayDto> newBirthdayList = (SerializableList<BirthdayDto>) intent.getSerializableExtra(Bundles.BIRTHDAY_LIST);
            if (newBirthdayList != null) {
                _birthdayList = newBirthdayList;
                _broadcastController.SendSerializableArrayBroadcast(
                        Broadcasts.UPDATE_BIRTHDAY,
                        new String[]{Bundles.BIRTHDAY_LIST},
                        new Object[]{_birthdayList});

                checkForBirthday();
                sendBirthdaysToWear();
            } else {
                _logger.Warn("birthdayStringArray is null");
            }
        }

        private void checkForBirthday() {
            for (int index = 0; index < _birthdayList.getSize(); index++) {
                BirthdayDto birthday = _birthdayList.getValue(index);
                if (birthday.HasBirthday()) {
                    int age = birthday.GetAge();
                    _logger.Debug("It is " + birthday.GetName() + "'s " + String.valueOf(age) + "th birthday!");
                    _notificationController.CreateBirthdayNotification(
                            BirthdayView.class,
                            R.drawable.birthday,
                            birthday.GetName(),
                            birthday.GetNotificationBody(age),
                            true);
                }
            }
        }
    };

    private BroadcastReceiver _birthdayDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_birthdayDownloadReceiver onReceive");

            String[] birthdayStringArray = intent.getStringArrayExtra(Bundles.BIRTHDAY_DOWNLOAD);
            if (birthdayStringArray != null) {
                ConverterTask converterTask = new ConverterTask(
                        birthdayStringArray,
                        Bundles.BIRTHDAY_LIST,
                        null,
                        _broadcastController,
                        _databaseController,
                        _sharedPrefController
                );
                converterTask.execute();
            } else {
                _logger.Warn("birthdayStringArray is null");
            }

            updateDownloadCount();
        }
    };

    private BroadcastReceiver _changeConvertedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_changeConvertedReceiver onReceive");

            SerializableList<ChangeDto> newChangeList = (SerializableList<ChangeDto>) intent.getSerializableExtra(Bundles.CHANGE_LIST);
            if (newChangeList != null) {
                _changeList = newChangeList;
                _broadcastController.SendSerializableArrayBroadcast(
                        Broadcasts.UPDATE_CHANGE,
                        new String[]{Bundles.CHANGE_LIST},
                        new Object[]{_changeList});

                _databaseController.ClearDatabaseChange();
                for (int index = 0; index < _changeList.getSize(); index++) {
                    _databaseController.SaveChange(_changeList.getValue(index));
                }

                checkLastLoadedData();
            } else {
                _logger.Warn("newChangeList is null");
            }
        }

        private void checkLastLoadedData() {
            _logger.Debug("checkLastLoadedData");

            check(SharedPrefConstants.LAST_LOADED_BIRTHDAY_TIME,
                    "Birthdays",
                    new Runnable[]{_downloadBirthdayRunnable});

            check(SharedPrefConstants.LAST_LOADED_MAP_CONTENT_TIME,
                    "MapContent",
                    new Runnable[]{_downloadMapContentRunnable});

            check(SharedPrefConstants.LAST_LOADED_MENU_TIME,
                    "Menu",
                    new Runnable[]{_downloadListedMenuRunnable, _downloadMenuRunnable});

            check(SharedPrefConstants.LAST_LOADED_MOVIE_TIME,
                    "Movies",
                    new Runnable[]{_downloadMovieRunnable});

            check(SharedPrefConstants.LAST_LOADED_SETTINGS_TIME,
                    "Settings",
                    new Runnable[]{_downloadSocketRunnable});

            check(SharedPrefConstants.LAST_LOADED_SHOPPING_LIST_TIME,
                    "ShoppingList",
                    new Runnable[]{_downloadShoppingListRunnable});
        }

        private void check(
                @NonNull String sharedPrefKey,
                @NonNull String changeEntry,
                @NonNull Runnable[] runnableArray) {
            _logger.Debug(String.format(Locale.getDefault(), "Checking key %s with changeEntry %s and runnableArray %s", sharedPrefKey, changeEntry, runnableArray));

            String savedData = _sharedPrefController.LoadStringValueFromSharedPreferences(sharedPrefKey);
            String[] savedDataArray = savedData.split("\\-");
            if (savedDataArray.length == 5) {
                SerializableTime time = convertDataToTime(savedDataArray);
                _logger.Debug(String.format(Locale.getDefault(), "Converted time is %s", time));

                Date date = convertDataToDate(savedDataArray);
                _logger.Debug(String.format(Locale.getDefault(), "Converted date is %s", date));

                boolean found = false;
                boolean needsUpdate = false;

                _logger.Debug("Checking changeList");
                for (int index = 0; index < _changeList.getSize(); index++) {
                    ChangeDto entry = _changeList.getValue(index);
                    _logger.Debug(String.format(Locale.getDefault(), "Checking changeEntry %s", entry));

                    if (entry.GetType().contains(changeEntry)) {
                        _logger.Debug(String.format(Locale.getDefault(), "Found entry %s", entry));

                        if (entry.GetDate().after(date)) {
                            _logger.Debug(String.format(Locale.getDefault(), "Date %s of entry %s is after date %s", entry.GetDate(), entry, date));

                            needsUpdate = true;
                            for (Runnable runnable : runnableArray) {
                                runnable.run();
                            }
                        } else {
                            if (entry.GetDate().compareTo(date) >= 0) {
                                if (time.isAfter(entry.GetTime())) {
                                    _logger.Debug(String.format(Locale.getDefault(), "Time %s of entry %s is after time %s", entry.GetTime(), entry, time));

                                    needsUpdate = true;
                                    for (Runnable runnable : runnableArray) {
                                        runnable.run();
                                    }
                                }
                            }
                        }

                        found = true;
                        break;
                    }
                }

                if (!found) {
                    _logger.Warn(String.format(Locale.getDefault(), "Did not found entry %s in _changeList! Performing update!", changeEntry));
                    for (Runnable runnable : runnableArray) {
                        runnable.run();
                    }
                    needsUpdate = true;
                }

                if (!needsUpdate) {
                    switch (changeEntry) {
                        case "Birthdays":
                            _birthdayList = _databaseController.GetBirthdayList();
                            _broadcastController.SendSerializableArrayBroadcast(
                                    Broadcasts.UPDATE_BIRTHDAY,
                                    new String[]{Bundles.BIRTHDAY_LIST},
                                    new Object[]{_birthdayList});
                            sendBirthdaysToWear();
                            updateDownloadCount();
                            break;
                        case "MapContent":
                            _mapContentList = _databaseController.GetMapContent();
                            break;
                        case "Menu":
                            _listedMenu = _databaseController.GetListedMenuList();
                            _broadcastController.SendSerializableArrayBroadcast(
                                    Broadcasts.UPDATE_LISTED_MENU_VIEW,
                                    new String[]{Bundles.LISTED_MENU},
                                    new Object[]{_listedMenu});
                            updateDownloadCount();
                            _menu = _databaseController.GetMenuList();
                            _broadcastController.SendSerializableArrayBroadcast(
                                    Broadcasts.UPDATE_MENU_VIEW,
                                    new String[]{Bundles.MENU},
                                    new Object[]{_menu});
                            sendMenuToWear();
                            updateDownloadCount();
                            break;
                        case "Movies":
                            _movieList = _databaseController.GetMovieList();
                            _broadcastController.SendSerializableArrayBroadcast(
                                    Broadcasts.UPDATE_MOVIE,
                                    new String[]{Bundles.MOVIE_LIST},
                                    new Object[]{_movieList});
                            updateDownloadCount();
                            break;
                        case "Settings":
                            _wirelessSocketList = _databaseController.GetSocketList();
                            _broadcastController.SendSerializableArrayBroadcast(
                                    Broadcasts.UPDATE_SOCKET_LIST,
                                    new String[]{Bundles.SOCKET_LIST},
                                    new Object[]{_wirelessSocketList});
                            sendSocketsToWear();
                            updateDownloadCount();

                            _scheduleList = _databaseController.GetScheduleList(_wirelessSocketList);
                            _broadcastController.SendSerializableArrayBroadcast(
                                    Broadcasts.UPDATE_SCHEDULE_LIST,
                                    new String[]{Bundles.SCHEDULE_LIST},
                                    new Object[]{_scheduleList});
                            sendSchedulesToWear();
                            updateDownloadCount();

                            _timerList = _databaseController.GetTimerList(_wirelessSocketList);
                            _broadcastController.SendSerializableArrayBroadcast(
                                    Broadcasts.UPDATE_TIMER_LIST,
                                    new String[]{Bundles.TIMER_LIST},
                                    new Object[]{_timerList});
                            sendTimerToWear();
                            updateDownloadCount();

                            _broadcastController.SendSerializableArrayBroadcast(
                                    Broadcasts.UPDATE_MAP_CONTENT_VIEW,
                                    new String[]{Bundles.MAP_CONTENT_LIST, Bundles.SOCKET_LIST, Bundles.SCHEDULE_LIST, Bundles.TIMER_LIST, Bundles.TEMPERATURE_LIST, Bundles.SHOPPING_LIST, Bundles.MENU, Bundles.LISTED_MENU, Bundles.MOTION_CAMERA_DTO},
                                    new Object[]{_mapContentList, _wirelessSocketList, _scheduleList, _timerList, _temperatureList, _shoppingList, _menu, _listedMenu, _motionCameraDto});

                            break;
                        case "ShoppingList":
                            _shoppingList = _databaseController.GetShoppingList();
                            _broadcastController.SendSerializableArrayBroadcast(
                                    Broadcasts.UPDATE_SHOPPING_LIST,
                                    new String[]{Bundles.SHOPPING_LIST},
                                    new Object[]{_shoppingList});
                            sendShoppingListToWear();
                            updateDownloadCount();
                            break;
                        default:
                            _logger.Warn(String.format(Locale.getDefault(), "ChangeEntry %s is not supported", changeEntry));
                            break;
                    }
                }
            } else {
                _logger.Warn(String.format(Locale.getDefault(), "savedDataArray has invalid length %d", savedDataArray.length));
            }
        }

        private SerializableTime convertDataToTime(@NonNull String[] stringArray) {
            _logger.Debug(String.format(Locale.getDefault(), "convertDataToTime with  stringArray %s", stringArray));

            String hourString = stringArray[0].replace("-", "");
            int hour = Integer.parseInt(hourString);
            String minuteString = stringArray[1].replace("-", "");
            int minute = Integer.parseInt(minuteString);

            return new SerializableTime(hour, minute, 0, 0);
        }

        private Date convertDataToDate(@NonNull String[] stringArray) {
            _logger.Debug(String.format(Locale.getDefault(), "convertDataToDate with  stringArray %s", stringArray));

            String dayString = stringArray[2].replace("-", "");
            int day = Integer.parseInt(dayString);
            String monthString = stringArray[3].replace("-", "");
            int month = Integer.parseInt(monthString) - 1;
            String yearString = stringArray[4].replace("-", "");
            int year = Integer.parseInt(yearString) - 1900;

            return new Date(year, month, day);
        }
    };

    private BroadcastReceiver _changeDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_changeDownloadReceiver onReceive");

            String[] changeStringArray = intent.getStringArrayExtra(Bundles.CHANGE_DOWNLOAD);
            if (changeStringArray != null) {
                ConverterTask converterTask = new ConverterTask(
                        changeStringArray,
                        Bundles.CHANGE_LIST,
                        null,
                        _broadcastController,
                        _databaseController,
                        _sharedPrefController
                );
                converterTask.execute();
            } else {
                _logger.Warn("changeStringArray is null");
            }

            updateDownloadCount();
        }
    };

    private BroadcastReceiver _currentWeatherDownloadModelReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_weatherModelReceiver onReceive");

            WeatherModel newWeather = (WeatherModel) intent.getSerializableExtra(OWBundles.EXTRA_WEATHER_MODEL);
            if (newWeather != null) {
                _currentWeather = newWeather;
                sendWeatherToWear();
            }

            if (_sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_TEMPERATURE_NOTIFICATION)) {
                _notificationController.CreateTemperatureNotification(SensorTemperatureView.class, _temperatureList, _currentWeather);
            }

            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.UPDATE_WEATHER_VIEW,
                    new String[]{Bundles.WEATHER_CURRENT},
                    new Object[]{_currentWeather});

            updateDownloadCount();
        }
    };

    private BroadcastReceiver _forecastWeatherDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_forecastModelReceiver onReceive");

            ForecastModel newForecastWeather = (ForecastModel) intent.getSerializableExtra(OWBundles.EXTRA_FORECAST_MODEL);
            if (newForecastWeather != null) {
                _forecastWeather = newForecastWeather;

                NotificationContent _message = _forecastWeather.TellForecastWeather();
                if (_message != null) {
                    _notificationController.CreateForecastWeatherNotification(
                            ForecastWeatherView.class,
                            _message.GetIcon(),
                            _message.GetBigIcon(),
                            _message.GetTitle(),
                            _message.GetText(),
                            OWIds.FORECAST_NOTIFICATION_ID);
                } else {
                    _logger.Error("_message is null!");
                }
            } else {
                _logger.Warn("newForecastWeather is null");
            }

            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.UPDATE_FORECAST_VIEW,
                    new String[]{Bundles.WEATHER_FORECAST},
                    new Object[]{_forecastWeather});

            updateDownloadCount();
        }
    };

    private BroadcastReceiver _informationConvertedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_informationConvertedReceiver onReceive");

            InformationDto newInformation = (InformationDto) intent.getSerializableExtra(Bundles.INFORMATION_SINGLE);
            if (newInformation != null) {
                _information = newInformation;
                _broadcastController.SendSerializableArrayBroadcast(
                        Broadcasts.UPDATE_INFORMATION,
                        new String[]{Bundles.INFORMATION_SINGLE},
                        new Object[]{_information});
            } else {
                _logger.Warn("newInformation is null");
            }
        }
    };

    private BroadcastReceiver _informationDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_informationDownloadReceiver onReceive");

            String[] informationStringArray = intent.getStringArrayExtra(Bundles.INFORMATION_DOWNLOAD);
            if (informationStringArray != null) {
                ConverterTask converterTask = new ConverterTask(
                        informationStringArray,
                        Bundles.INFORMATION_SINGLE,
                        null,
                        _broadcastController,
                        _databaseController,
                        _sharedPrefController
                );
                converterTask.execute();
            } else {
                _logger.Warn("informationStringArray is null");
            }

            updateDownloadCount();
        }
    };

    private BroadcastReceiver _mapContentConvertedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_mapContentConvertedReceiver onReceive");

            SerializableList<MapContentDto> newMapContentList = (SerializableList<MapContentDto>) intent.getSerializableExtra(Bundles.MAP_CONTENT_LIST);
            if (newMapContentList != null) {
                _mapContentList = newMapContentList;
            }
        }
    };

    private BroadcastReceiver _mapContentDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_mapContentDownloadReceiver onReceive");

            String[] mapContentStringArray = intent.getStringArrayExtra(Bundles.MAP_CONTENT_DOWNLOAD);
            if (mapContentStringArray != null) {
                ConverterTask converterTask = new ConverterTask(
                        mapContentStringArray,
                        Bundles.MAP_CONTENT_LIST,
                        null,
                        _broadcastController,
                        _databaseController,
                        _sharedPrefController
                );
                converterTask.execute();
            } else {
                _logger.Warn("mapContentStringArray is null!");
            }

            updateDownloadCount();
        }
    };

    private BroadcastReceiver _listedMenuDtoConvertedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_listedMenuDtoConvertedReceiver onReceive");

            SerializableList<ListedMenuDto> newListedMenuList = (SerializableList<ListedMenuDto>) intent.getSerializableExtra(Bundles.LISTED_MENU);
            if (newListedMenuList != null) {
                _listedMenu = newListedMenuList;
            } else {
                _logger.Warn("newListedMenuList is null");
            }
        }
    };

    private BroadcastReceiver _listedMenuDtoDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_listedMenuDtoDownloadReceiver onReceive");

            String[] listedMenuStringArray = intent.getStringArrayExtra(Bundles.LISTED_MENU_DOWNLOAD);
            if (listedMenuStringArray != null) {
                ConverterTask converterTask = new ConverterTask(
                        listedMenuStringArray,
                        Bundles.LISTED_MENU,
                        null,
                        _broadcastController,
                        _databaseController,
                        _sharedPrefController
                );
                converterTask.execute();
            } else {
                _logger.Warn("listedMenuStringArray is null");
            }

            updateDownloadCount();
        }
    };

    private BroadcastReceiver _mediaMirrorViewDtoDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_mediaMirrorViewDtoDownloadReceiver onReceive");

            MediaMirrorViewDto mediaMirrorViewDto = (MediaMirrorViewDto) intent.getSerializableExtra(Bundles.MEDIAMIRROR_VIEW_DTO);

            if (mediaMirrorViewDto != null) {
                _logger.Debug("New Dto is: " + mediaMirrorViewDto.toString());
                boolean foundEntry = false;
                for (int index = 0; index < _mediaMirrorList.getSize(); index++) {
                    if (_mediaMirrorList.getValue(index).GetMediaServerSelection().GetId() == mediaMirrorViewDto.GetMediaServerSelection().GetId()) {
                        _mediaMirrorList.setValue(index, mediaMirrorViewDto);
                        foundEntry = true;
                        break;
                    }
                }

                if (!foundEntry) {
                    _mediaMirrorList.addValue(mediaMirrorViewDto);
                }
            } else {
                _logger.Warn("Received null MediaMirrorViewDto...!");
            }

            updateDownloadCount();
            sendMediaMirrorToWear();
        }
    };

    private BroadcastReceiver _menuDtoConvertedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_menuDtoConvertedReceiver onReceive");

            SerializableList<MenuDto> newMenuList = (SerializableList<MenuDto>) intent.getSerializableExtra(Bundles.MENU);
            if (newMenuList != null) {
                _menu = newMenuList;
                _menuController.CheckMenuDto(_menu);
            } else {
                _logger.Warn("newMenuList is null");
            }

            sendMenuToWear();
        }
    };

    private BroadcastReceiver _menuDtoDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_menuDtoDownloadReceiver onReceive");

            String[] menuStringArray = intent.getStringArrayExtra(Bundles.MENU_DOWNLOAD);
            if (menuStringArray != null) {
                ConverterTask converterTask = new ConverterTask(
                        menuStringArray,
                        Bundles.MENU,
                        null,
                        _broadcastController,
                        _databaseController,
                        _sharedPrefController
                );
                converterTask.execute();
            } else {
                _logger.Warn("menuStringArray is null");
            }

            updateDownloadCount();
        }
    };

    private BroadcastReceiver _motionCameraDtoConvertedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_motionCameraDtoConvertedReceiver onReceive");

            SerializableList<MotionCameraDto> motionCameraDtoList = (SerializableList<MotionCameraDto>) intent.getSerializableExtra(Bundles.MOTION_CAMERA_DTO);
            if (motionCameraDtoList != null) {
                if (motionCameraDtoList.getSize() != 1) {
                    _logger.Error(String.format(Locale.getDefault(), "MotionCameraDtoList has wrong size %d!", motionCameraDtoList.getSize()));
                    return;
                }

                _motionCameraDto = motionCameraDtoList.getValue(0);
                _broadcastController.SendSerializableArrayBroadcast(
                        Broadcasts.UPDATE_MOTION_CAMERA_DTO,
                        new String[]{Bundles.MOTION_CAMERA_DTO},
                        new Object[]{_motionCameraDto});

                if (_motionCameraDto.GetCameraState()) {
                    _notificationController.CreateCameraNotification(SecurityView.class);
                } else {
                    _notificationController.CloseNotification(IDs.NOTIFICATION_CAMERA);
                }
            } else {
                _logger.Warn("motionCameraDtoList is null");
            }
        }
    };

    private BroadcastReceiver _motionCameraDtoDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_motionCameraDtoDownloadReceiver onReceive");

            String[] motionCameraDtoStringArray = intent.getStringArrayExtra(Bundles.MOTION_CAMERA_DTO_DOWNLOAD);
            if (motionCameraDtoStringArray != null) {
                ConverterTask converterTask = new ConverterTask(
                        motionCameraDtoStringArray,
                        Bundles.MOTION_CAMERA_DTO,
                        null,
                        _broadcastController,
                        _databaseController,
                        _sharedPrefController
                );
                converterTask.execute();
            } else {
                _logger.Warn("motionCameraDtoStringArray is null");
            }

            updateDownloadCount();
        }
    };

    private BroadcastReceiver _movieConvertedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_movieConvertedReceiver onReceive");

            SerializableList<MovieDto> newMovieList = (SerializableList<MovieDto>) intent.getSerializableExtra(Bundles.MOVIE_LIST);
            if (newMovieList != null) {
                _movieList = newMovieList;
                _broadcastController.SendSerializableArrayBroadcast(
                        Broadcasts.UPDATE_MOVIE,
                        new String[]{Bundles.MOVIE_LIST},
                        new Object[]{_movieList});
            } else {
                _logger.Warn("newMovieList is null");
            }
        }
    };

    private BroadcastReceiver _movieDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_movieDownloadReceiver onReceive");

            String[] movieStringArray = intent.getStringArrayExtra(Bundles.MOVIE_DOWNLOAD);
            if (movieStringArray != null) {
                ConverterTask converterTask = new ConverterTask(
                        movieStringArray,
                        Bundles.MOVIE_LIST,
                        null,
                        _broadcastController,
                        _databaseController,
                        _sharedPrefController
                );
                converterTask.execute();
            } else {
                _logger.Warn("movieStringArray is null");
            }

            updateDownloadCount();
        }
    };

    private BroadcastReceiver _scheduleDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_scheduleDownloadReceiver onReceive");

            String[] scheduleStringArray = intent.getStringArrayExtra(Bundles.SCHEDULE_DOWNLOAD);
            if (scheduleStringArray != null && _wirelessSocketList != null) {

                SerializableList<ScheduleDto> newScheduleList = JsonDataToScheduleConverter.GetList(scheduleStringArray, _wirelessSocketList);
                if (newScheduleList != null) {
                    _scheduleList = newScheduleList;
                    _broadcastController.SendSerializableArrayBroadcast(
                            Broadcasts.UPDATE_SCHEDULE,
                            new String[]{Bundles.SCHEDULE_LIST, Bundles.SOCKET_LIST},
                            new Object[]{_scheduleList, _wirelessSocketList});

                    _databaseController.ClearDatabaseSchedule();
                    for (int index = 0; index < _scheduleList.getSize(); index++) {
                        _databaseController.SaveSchedule(_scheduleList.getValue(index));
                    }

                    sendSchedulesToWear();
                } else {
                    _logger.Warn("newScheduleList is null");
                }

                SerializableList<TimerDto> newTimerList = JsonDataToTimerConverter.GetList(scheduleStringArray, _wirelessSocketList);
                if (newTimerList != null) {
                    _timerList = newTimerList;
                    _broadcastController.SendSerializableArrayBroadcast(
                            Broadcasts.UPDATE_TIMER,
                            new String[]{Bundles.TIMER_LIST, Bundles.SOCKET_LIST},
                            new Object[]{_timerList, _wirelessSocketList});

                    _databaseController.ClearDatabaseTimer();
                    for (int index = 0; index < _timerList.getSize(); index++) {
                        _databaseController.SaveTimer(_timerList.getValue(index));
                    }
                } else {
                    _logger.Warn("newTimerList is null");
                }
            }
            updateDownloadCount();
        }
    };

    private BroadcastReceiver _shoppingListConvertedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_shoppingListConvertedReceiver onReceive");

            SerializableList<ShoppingEntryDto> newShoppingList = (SerializableList<ShoppingEntryDto>) intent.getSerializableExtra(Bundles.SHOPPING_LIST);
            if (newShoppingList != null) {
                _shoppingList = newShoppingList;
                _broadcastController.SendSerializableArrayBroadcast(
                        Broadcasts.UPDATE_SHOPPING_LIST,
                        new String[]{Bundles.SHOPPING_LIST},
                        new Object[]{_shoppingList});

                sendShoppingListToWear();
            } else {
                _logger.Warn("newShoppingList is null");
            }
        }
    };

    private BroadcastReceiver _shoppingListDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_shoppingListDownloadReceiver onReceive");

            String[] shoppingListStringArray = intent.getStringArrayExtra(Bundles.SHOPPING_LIST_DOWNLOAD);
            if (shoppingListStringArray != null) {
                ConverterTask converterTask = new ConverterTask(
                        shoppingListStringArray,
                        Bundles.SHOPPING_LIST,
                        null,
                        _broadcastController,
                        _databaseController,
                        _sharedPrefController
                );
                converterTask.execute();
            } else {
                _logger.Warn("shoppingListStringArray is null");
            }

            updateDownloadCount();
        }
    };

    private BroadcastReceiver _socketDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_socketDownloadReceiver onReceive");

            String[] socketStringArray = intent.getStringArrayExtra(Bundles.SOCKET_DOWNLOAD);
            if (socketStringArray != null) {

                SerializableList<WirelessSocketDto> newWirelessSocketList = JsonDataToSocketConverter.GetList(socketStringArray);
                if (newWirelessSocketList != null) {
                    _wirelessSocketList = newWirelessSocketList;
                    installNotificationSettings();
                } else {
                    _logger.Warn("newWirelessSocketList is null");
                }

                if (_wirelessSocketList != null) {
                    _broadcastController.SendSerializableArrayBroadcast(
                            Broadcasts.UPDATE_SOCKET_LIST,
                            new String[]{Bundles.SOCKET_LIST},
                            new Object[]{_wirelessSocketList});

                    _broadcastController.SendSerializableArrayBroadcast(
                            Broadcasts.UPDATE_MAP_CONTENT_VIEW,
                            new String[]{Bundles.MAP_CONTENT_LIST, Bundles.SOCKET_LIST, Bundles.SCHEDULE_LIST, Bundles.TIMER_LIST, Bundles.TEMPERATURE_LIST, Bundles.SHOPPING_LIST, Bundles.MENU, Bundles.LISTED_MENU, Bundles.MOTION_CAMERA_DTO},
                            new Object[]{_mapContentList, _wirelessSocketList, _scheduleList, _timerList, _temperatureList, _shoppingList, _menu, _listedMenu, _motionCameraDto});

                    sendSocketsToWear();

                    if (_sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_SOCKET_NOTIFICATION)) {
                        _notificationController.CreateSocketNotification(_wirelessSocketList);
                    }

                    _databaseController.ClearDatabaseSocket();
                    for (int index = 0; index < _wirelessSocketList.getSize(); index++) {
                        if (!_databaseController.SaveSocket(_wirelessSocketList.getValue(index))) {
                            _logger.Warn(
                                    String.format(
                                            Locale.getDefault(),
                                            "Failed to save socket %s to database!",
                                            _wirelessSocketList.getValue(index).GetName()));
                        }
                    }

                    SerializableTime time = new SerializableTime();
                    SerializableDate date = new SerializableDate();
                    _sharedPrefController.SaveStringValue(
                            SharedPrefConstants.LAST_LOADED_SETTINGS_TIME,
                            String.format(Locale.getDefault(), "%s-%s-%s-%s-%s", time.HH(), time.MM(), date.DD(), date.MM(), date.YYYY()));
                } else {
                    _logger.Warn("_wirelessSocketList is null");
                }
            }

            updateDownloadCount();
            _downloadScheduleRunnable.run();
        }
    };

    private BroadcastReceiver _temperatureDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_temperatureDownloadReceiver onReceive");

            String[] temperatureStringArray = intent.getStringArrayExtra(Bundles.TEMPERATURE_DOWNLOAD);
            if (temperatureStringArray != null) {

                SerializableList<TemperatureDto> newTemperatureList = JsonDataToTemperatureConverter.GetList(temperatureStringArray);
                if (newTemperatureList != null) {
                    _temperatureList = newTemperatureList;

                    if (_temperatureList.getSize() >= 1) {
                        sendTemperatureToWear();
                    } else {
                        _logger.Warn("TemperatureList has size " + String.valueOf(_temperatureList.getSize()));
                    }
                } else {
                    _logger.Warn("newTemperatureList is null!");
                }

                if (_temperatureList != null) {
                    _broadcastController.SendSerializableArrayBroadcast(
                            Broadcasts.UPDATE_TEMPERATURE,
                            new String[]{Bundles.TEMPERATURE_LIST},
                            new Object[]{_temperatureList});
                }

                if (_sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_TEMPERATURE_NOTIFICATION)) {
                    _notificationController.CreateTemperatureNotification(SensorTemperatureView.class, _temperatureList, _currentWeather);
                }
            }
            updateDownloadCount();
        }
    };

    private BroadcastReceiver _reloadAllReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_reloadAllReceiver onReceive");
            download();
        }
    };

    private BroadcastReceiver _reloadBirthdayReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_reloadBirthdayReceiver onReceive");
            _downloadBirthdayRunnable.run();
        }
    };

    private BroadcastReceiver _reloadChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_reloadChangeReceiver onReceive");
            _downloadChangeRunnable.run();
        }
    };

    private BroadcastReceiver _reloadCurrentWeatherReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_reloadCurrentWeatherReceiver onReceive");
            _downloadCurrentWeatherRunnable.run();
        }
    };

    private BroadcastReceiver _reloadForecastWeatherReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_reloadForecastWeatherReceiver onReceive");
            _downloadForecastWeatherRunnable.run();
        }
    };

    private BroadcastReceiver _reloadListedMenuReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_reloadListedMenuReceiver onReceive");
            _downloadListedMenuRunnable.run();
        }
    };

    private BroadcastReceiver _reloadMediaMirrorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_reloadMediaMirrorReceiver onReceive");
            _downloadMediaMirrorRunnable.run();
        }
    };

    private BroadcastReceiver _reloadMenuReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_reloadMenuReceiver onReceive");
            _downloadMenuRunnable.run();
        }
    };

    private BroadcastReceiver _reloadMotionCameraDtoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_reloadMotionCameraDtoReceiver onReceive");
            _downloadMotionCameraDtoRunnable.run();
        }
    };

    private BroadcastReceiver _reloadMovieReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_reloadMovieReceiver onReceive");
            _downloadMovieRunnable.run();
        }
    };

    private BroadcastReceiver _reloadScheduleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_reloadScheduleReceiver onReceive");
            _downloadScheduleRunnable.run();
        }
    };

    private BroadcastReceiver _reloadShoppingListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_reloadShoppingListReceiver onReceive");
            _downloadShoppingListRunnable.run();
        }
    };

    private BroadcastReceiver _reloadSocketReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_reloadSocketReceiver onReceive");
            _downloadSocketRunnable.run();
        }
    };

    private BroadcastReceiver _reloadTemperatureReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_reloadTemperatureReceiver onReceive");
            _downloadTemperatureRunnable.run();
        }
    };

    private BroadcastReceiver _updateBoughtShoppingListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateBoughtShoppingListReceiver onReceive");

            String data = intent.getStringExtra(Bundles.SHOPPING_LIST);
            if (data != null) {
                String[] content = data.split("\\:");

                if (content.length == 2) {
                    String name = content[0];
                    _logger.Debug(String.format("_name is %s", name));

                    String boughtString = content[1];
                    boughtString = boughtString.replace(":", "");
                    boolean bought = boughtString.contains("1");
                    _logger.Debug(String.format("bought is %s", bought));

                    for (int shoppingIndex = 0; shoppingIndex < _shoppingList.getSize(); shoppingIndex++) {
                        ShoppingEntryDto entry = _shoppingList.getValue(shoppingIndex);
                        if (entry.GetName().contains(name)) {
                            _logger.Debug("Found matching entry at with name " + name);

                            entry.SetBought(bought);

                            _broadcastController.SendSerializableArrayBroadcast(
                                    Broadcasts.UPDATE_SHOPPING_LIST,
                                    new String[]{Bundles.SHOPPING_LIST},
                                    new Object[]{_shoppingList});

                            _databaseController.ClearDatabaseShoppingList();
                            for (int index = 0; index < _shoppingList.getSize(); index++) {
                                _databaseController.SaveShoppingEntry(_shoppingList.getValue(index));
                            }

                            sendShoppingListToWear();

                            break;
                        }
                    }
                    _logger.Warn(String.format(Locale.getDefault(), "Found no matching entry with name %s to update!", name));
                } else {
                    _logger.Warn(String.format(Locale.getDefault(), "Invalid length %s for content %s", content.length, content));
                }
            } else {
                _logger.Warn("Received null data in _updateBoughtShoppingListReceiver");
            }
        }
    };

    // Receiver used for receiving the current battery state of the phone and sending the state to the watch
    private BroadcastReceiver _batteryChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_batteryChangedReceiver onReceive");

            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            if (level != -1) {
                _logger.Debug("new battery level: " + String.valueOf(level));
                String message = "PhoneBattery:" + String.valueOf(level) + "%";
                _serviceController.SendMessageToWear(message);
            }
        }
    };

    // Receiver used for receiving the current time and check if time is ready
    // to display sleep notification or set a flag if not or to delete this flag
    private BroadcastReceiver _timeTickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            _logger.Debug("action: " + action);

            if (action.equals(Intent.ACTION_TIME_TICK)) {
                Calendar calendar = Calendar.getInstance();

                // Check, if the time is ready for displaying the notification
                // time needs to be NOTIFICATION_HOUR:NOTIFICATION_MINUTE
                if ((int) calendar.get(Calendar.HOUR_OF_DAY) == SLEEP_NOTIFICATION_HOUR) {
                    if (calendar.get(Calendar.MINUTE) > SLEEP_NOTIFICATION_MINUTE - SLEEP_NOTIFICATION_TIME_SPAN_MINUTE
                            && calendar.get(Calendar.MINUTE) < SLEEP_NOTIFICATION_MINUTE + SLEEP_NOTIFICATION_TIME_SPAN_MINUTE) {
                        _logger.Debug("We are in the timeSpan!");

                        // Check also if we are in the home network, otherwise
                        // it's senseless to display the notification, but we
                        // set a flag to display notification later
                        if (_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
                            _logger.Debug("Showing notification go to sleep!");
                            _notificationController.CreateSleepHeatingNotification();
                        } else {
                            _logger.Debug("Saving flag to display notification later!");
                            _sharedPrefController.SaveBooleanValue(SharedPrefConstants.DISPLAY_SLEEP_NOTIFICATION_ACTIVE, true);
                        }
                    } else {
                        _logger.Debug("We are NOT in the timeSpan!");
                    }
                }
                // Check if time is ready to delete the flag to display the
                // sleep notification
                else if ((int) calendar.get(Calendar.HOUR_OF_DAY) == CLEAR_NOTIFICATION_DISPLAY_HOUR) {
                    if ((int) calendar.get(Calendar.MINUTE) == CLEAR_NOTIFICATION_DISPLAY_MINUTE) {
                        if (_sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_SLEEP_NOTIFICATION_ACTIVE)) {
                            _logger.Debug("We entered home and flag is active to display sleep notification");
                            _sharedPrefController.SaveBooleanValue(SharedPrefConstants.DISPLAY_SLEEP_NOTIFICATION_ACTIVE, false);
                        }
                    }
                }
            }
        }
    };

    private Runnable _downloadBirthdayRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_downloadBirthdayRunnable run");

            if (!_networkController.IsNetworkAvailable()) {
                _logger.Warn("No network available!");
                loadFromDatabase();
                return;
            }

            if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
                _logger.Warn("No LucaHome network! ...");
                loadFromDatabase();
                return;
            }

            _serviceController.StartRestService(
                    Bundles.BIRTHDAY_DOWNLOAD,
                    LucaServerAction.GET_BIRTHDAYS.toString(),
                    Broadcasts.DOWNLOAD_BIRTHDAY_FINISHED);
        }

        private void loadFromDatabase() {
            _logger.Debug("loadFromDatabase");

            _birthdayList = _databaseController.GetBirthdayList();
            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.UPDATE_BIRTHDAY,
                    new String[]{Bundles.BIRTHDAY_LIST},
                    new Object[]{_birthdayList});

            updateDownloadCount();
            sendBirthdaysToWear();
        }
    };

    private Runnable _downloadChangeRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_downloadChangeRunnable run");

            if (!_networkController.IsNetworkAvailable()) {
                _logger.Warn("No network available!");
                loadFromDatabase();
                return;
            }

            if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
                _logger.Warn("No LucaHome network! ...");
                loadFromDatabase();
                return;
            }

            _serviceController.StartRestService(
                    Bundles.CHANGE_DOWNLOAD,
                    LucaServerAction.GET_CHANGES.toString(),
                    Broadcasts.DOWNLOAD_CHANGE_FINISHED);
        }

        private void loadFromDatabase() {
            _logger.Debug("loadFromDatabase");

            _changeList = _databaseController.GetChangeList();
            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.UPDATE_CHANGE,
                    new String[]{Bundles.CHANGE_LIST},
                    new Object[]{_changeList});

            updateDownloadCount();
        }
    };

    private Runnable _downloadCurrentWeatherRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_downloadCurrentWeatherRunnable run");

            if (!_networkController.IsNetworkAvailable()) {
                _logger.Warn("No network available!");
                return;
            }

            _openWeatherController.LoadCurrentWeather();
        }
    };

    private Runnable _downloadForecastWeatherRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_downloadForecastWeatherRunnable run");

            if (!_networkController.IsNetworkAvailable()) {
                _logger.Warn("No network available!");
                return;
            }

            if (_sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_WEATHER_NOTIFICATION)) {
                _openWeatherController.LoadForecastWeather();
            }
        }
    };

    private Runnable _downloadInformationRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_downloadInformationRunnable run");

            if (!_networkController.IsNetworkAvailable()) {
                _logger.Warn("No network available!");
                return;
            }

            if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
                _logger.Warn("No LucaHome network! ...");
                return;
            }

            _serviceController.StartRestService(
                    Bundles.INFORMATION_DOWNLOAD,
                    LucaServerAction.GET_INFORMATIONS.toString(),
                    Broadcasts.DOWNLOAD_INFORMATION_FINISHED);
        }
    };

    private Runnable _downloadListedMenuRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_downloadListedMenuRunnable run");

            if (!_networkController.IsNetworkAvailable()) {
                _logger.Warn("No network available!");
                loadFromDatabase();
                return;
            }

            if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
                _logger.Warn("No LucaHome network! ...");
                loadFromDatabase();
                return;
            }

            _serviceController.StartRestService(
                    Bundles.LISTED_MENU_DOWNLOAD,
                    LucaServerAction.GET_LISTED_MENU.toString(),
                    Broadcasts.DOWNLOAD_LISTED_MENU_FINISHED);
        }

        private void loadFromDatabase() {
            _logger.Debug("loadFromDatabase");

            _listedMenu = _databaseController.GetListedMenuList();
            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.UPDATE_LISTED_MENU_VIEW,
                    new String[]{Bundles.LISTED_MENU},
                    new Object[]{_listedMenu});

            updateDownloadCount();
        }
    };

    private Runnable _downloadMapContentRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_downloadMapContentRunnable run");

            if (!_networkController.IsNetworkAvailable()) {
                _logger.Warn("No network available!");
                loadFromDatabase();
                return;
            }

            if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
                _logger.Warn("No LucaHome network! ...");
                loadFromDatabase();
                return;
            }

            _serviceController.StartRestService(
                    Bundles.MAP_CONTENT_DOWNLOAD,
                    LucaServerAction.GET_MAP_CONTENTS.toString(),
                    Broadcasts.DOWNLOAD_MAP_CONTENT_FINISHED);
        }

        private void loadFromDatabase() {
            _logger.Debug("loadFromDatabase");

            _mapContentList = _databaseController.GetMapContent();
            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.UPDATE_MAP_CONTENT_VIEW,
                    new String[]{Bundles.MAP_CONTENT_LIST},
                    new Object[]{_mapContentList});

            updateDownloadCount();
        }
    };

    private Runnable _downloadMediaMirrorRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_downloadMediaMirrorRunnable run");

            if (!_networkController.IsNetworkAvailable()) {
                _logger.Warn("No network available!");
                return;
            }

            if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
                _logger.Warn("No LucaHome network! ...");
                return;
            }

            for (final MediaServerSelection entry : MediaServerSelection.values()) {
                if (entry.GetId() == 0) {
                    continue;
                }

                _logger.Debug(String.format(Locale.getDefault(), "Trying serverIp %s", entry.GetIp()));
                _mediaMirrorController.SendCommand(entry.GetIp(), MediaServerAction.GET_MEDIAMIRROR_DTO.toString(), "");
            }
        }
    };

    private Runnable _downloadMenuRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_downloadMenuRunnable run");

            if (!_networkController.IsNetworkAvailable()) {
                _logger.Warn("No network available!");
                loadFromDatabase();
                return;
            }

            if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
                _logger.Warn("No LucaHome network! ...");
                loadFromDatabase();
                return;
            }

            _serviceController.StartRestService(
                    Bundles.MENU_DOWNLOAD,
                    LucaServerAction.GET_MENU.toString(),
                    Broadcasts.DOWNLOAD_MENU_FINISHED);
        }

        private void loadFromDatabase() {
            _logger.Debug("loadFromDatabase");

            _menu = _databaseController.GetMenuList();
            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.UPDATE_MENU_VIEW,
                    new String[]{Bundles.MENU},
                    new Object[]{_menu});

            updateDownloadCount();
            sendMenuToWear();
        }
    };

    private Runnable _downloadMotionCameraDtoRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_downloadMotionCameraDtoRunnable run");

            if (!_networkController.IsNetworkAvailable()) {
                _logger.Warn("No network available!");
                return;
            }

            if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
                _logger.Warn("No LucaHome network! ...");
                return;
            }

            _serviceController.StartRestService(
                    Bundles.MOTION_CAMERA_DTO_DOWNLOAD,
                    LucaServerAction.GET_MOTION_DATA.toString(),
                    Broadcasts.DOWNLOAD_MOTION_CAMERA_DTO_FINISHED);
        }
    };

    private Runnable _downloadMovieRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_downloadMovieRunnable run");

            if (!_networkController.IsNetworkAvailable()) {
                _logger.Warn("No network available!");
                loadFromDatabase();
                return;
            }

            if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
                _logger.Warn("No LucaHome network! ...");
                loadFromDatabase();
                return;
            }

            _serviceController.StartRestService(
                    Bundles.MOVIE_DOWNLOAD,
                    LucaServerAction.GET_MOVIES.toString(),
                    Broadcasts.DOWNLOAD_MOVIE_FINISHED);
        }

        private void loadFromDatabase() {
            _logger.Debug("loadFromDatabase");

            _movieList = _databaseController.GetMovieList();
            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.UPDATE_MOVIE,
                    new String[]{Bundles.MOVIE_LIST},
                    new Object[]{_movieList});

            updateDownloadCount();
        }
    };

    private Runnable _downloadScheduleRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_downloadScheduleRunnable run");

            if (!_networkController.IsNetworkAvailable()) {
                _logger.Warn("No network available!");
                loadFromDatabase();
                return;
            }

            if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
                _logger.Warn("No LucaHome network! ...");
                loadFromDatabase();
                return;
            }

            _serviceController.StartRestService(
                    Bundles.SCHEDULE_DOWNLOAD,
                    LucaServerAction.GET_SCHEDULES.toString(),
                    Broadcasts.DOWNLOAD_SCHEDULE_FINISHED);
        }

        private void loadFromDatabase() {
            _logger.Debug("loadFromDatabase");

            _scheduleList = _databaseController.GetScheduleList(_wirelessSocketList);
            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.UPDATE_SCHEDULE_LIST,
                    new String[]{Bundles.SCHEDULE_LIST},
                    new Object[]{_scheduleList});
            sendSchedulesToWear();

            _timerList = _databaseController.GetTimerList(_wirelessSocketList);
            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.UPDATE_TIMER_LIST,
                    new String[]{Bundles.TIMER_LIST},
                    new Object[]{_timerList});
            sendTimerToWear();

            updateDownloadCount();
        }
    };

    private Runnable _downloadShoppingListRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_downloadShoppingListRunnable run");

            if (!_networkController.IsNetworkAvailable()) {
                _logger.Warn("No network available!");
                loadFromDatabase();
                return;
            }

            if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
                _logger.Warn("No LucaHome network! ...");
                loadFromDatabase();
                return;
            }

            if (_shoppingList != null) {
                for (int index = 0; index < _shoppingList.getSize(); index++) {
                    ShoppingEntryDto entry = _shoppingList.getValue(index);
                    if (entry.IsBought()) {
                        _serviceController.StartRestService(
                                Bundles.SHOPPING_LIST,
                                entry.GetCommandDelete(),
                                "");
                    }
                }
            }

            _serviceController.StartRestService(
                    Bundles.SHOPPING_LIST_DOWNLOAD,
                    LucaServerAction.GET_SHOPPING_LIST.toString(),
                    Broadcasts.DOWNLOAD_SHOPPING_LIST_FINISHED);
        }

        private void loadFromDatabase() {
            _logger.Debug("loadFromDatabase");

            _shoppingList = _databaseController.GetShoppingList();
            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.UPDATE_SHOPPING_LIST,
                    new String[]{Bundles.SHOPPING_LIST},
                    new Object[]{_shoppingList});

            updateDownloadCount();
            sendShoppingListToWear();
        }
    };

    private Runnable _downloadSocketRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_downloadSocketRunnable run");

            if (!_networkController.IsNetworkAvailable()) {
                _logger.Warn("No network available!");
                loadFromDatabase();
                return;
            }

            if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
                _logger.Warn("No LucaHome network! ...");
                loadFromDatabase();
                return;
            }

            _serviceController.StartRestService(
                    Bundles.SOCKET_DOWNLOAD,
                    LucaServerAction.GET_SOCKETS.toString(),
                    Broadcasts.DOWNLOAD_SOCKET_FINISHED);
        }

        private void loadFromDatabase() {
            _logger.Debug("loadFromDatabase");
            _wirelessSocketList = _databaseController.GetSocketList();

            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.UPDATE_SOCKET_LIST,
                    new String[]{Bundles.SOCKET_LIST},
                    new Object[]{_wirelessSocketList});

            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.UPDATE_MAP_CONTENT_VIEW,
                    new String[]{Bundles.MAP_CONTENT_LIST, Bundles.SOCKET_LIST, Bundles.SCHEDULE_LIST, Bundles.TIMER_LIST, Bundles.TEMPERATURE_LIST, Bundles.SHOPPING_LIST, Bundles.MENU, Bundles.LISTED_MENU, Bundles.MOTION_CAMERA_DTO},
                    new Object[]{_mapContentList, _wirelessSocketList, _scheduleList, _timerList, _temperatureList, _shoppingList, _menu, _listedMenu, _motionCameraDto});

            sendSocketsToWear();

            if (_sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_SOCKET_NOTIFICATION)) {
                _notificationController.CreateSocketNotification(_wirelessSocketList);
            }

            updateDownloadCount();
        }
    };

    private Runnable _downloadTemperatureRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_downloadTemperatureRunnable run");

            if (!_networkController.IsNetworkAvailable()) {
                _logger.Warn("No network available!");
                return;
            }

            _downloadCurrentWeatherRunnable.run();

            if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
                _logger.Warn("No LucaHome network! ...");
                return;
            }

            _serviceController.StartRestService(
                    Bundles.TEMPERATURE_DOWNLOAD,
                    LucaServerAction.GET_TEMPERATURES.toString(),
                    Broadcasts.DOWNLOAD_TEMPERATURE_FINISHED);
        }
    };

    private Runnable _timeoutCheck = new Runnable() {
        public void run() {
            _logger.Warn("_timeoutCheck received!");
            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.COMMAND,
                    new String[]{Bundles.COMMAND, Bundles.NAVIGATE_DATA},
                    new Object[]{Command.NAVIGATE, NavigationData.FINISH});
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        if (_logger != null) {
            _logger.Debug("onCreate");
        }

        if (!_isInitialized) {
            _logger = new LucaHomeLogger(TAG);
            _logger.Debug("onCreate");

            _birthdayList = new SerializableList<>();
            _changeList = new SerializableList<>();
            _listedMenu = new SerializableList<>();
            _menu = new SerializableList<>();
            _movieList = new SerializableList<>();
            _scheduleList = new SerializableList<>();
            _temperatureList = new SerializableList<>();
            _timerList = new SerializableList<>();
            _wirelessSocketList = new SerializableList<>();

            _context = this;

            _beaconController = new BeaconController(_context);
            _broadcastController = new BroadcastController(_context);
            _databaseController = DatabaseController.getSingleton();
            _databaseController.Initialize(_context);
            _mediaMirrorController = new MediaMirrorController(_context);
            _mediaMirrorController.Initialize();
            _menuController = new MenuController(_context);
            _networkController = new NetworkController(_context,
                    new DialogController(
                            _context,
                            ContextCompat.getColor(_context, R.color.TextIcon),
                            ContextCompat.getColor(_context, R.color.Background)));
            _notificationController = new LucaNotificationController(_context);
            _openWeatherController = new OpenWeatherController(_context, Constants.CITY);
            _receiverController = new ReceiverController(_context);
            _scheduleService = ScheduleService.getInstance();
            _serviceController = new ServiceController(_context);
            _sharedPrefController = new SharedPrefController(_context, SharedPrefConstants.SHARED_PREF_NAME);

            registerReceiver();

            _progress = 0;
            _downloadCount = Constants.DOWNLOAD_STEPS;

            boot();

            if (_sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.USE_BEACONS)) {
                _beaconController.StartScanning();
            }

            _isInitialized = true;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (_logger == null) {
            _logger = new LucaHomeLogger(TAG);
        }
        _logger.Debug("onStartCommand");

        try {
            Bundle data = intent.getExtras();
            MainServiceAction action = (MainServiceAction) data.getSerializable(Bundles.ACTION);

            if (action != null) {
                _logger.Debug("Received action is: " + action.toString());

                switch (action) {
                    case BOOT:
                        _progress = 0;
                        _downloadCount = Constants.DOWNLOAD_STEPS;
                        boot();
                        break;

                    default:
                        _logger.Warn("Action not supported! " + action.toString());
                        break;
                }
            } else {
                _logger.Warn("Action is null!");
            }
        } catch (Exception e) {
            _logger.Error(e.toString());
        }

        checkDatabase();
        checkSleepNotificationFlag();

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        if (_logger == null) {
            _logger = new LucaHomeLogger(TAG);
        }
        _logger.Debug("onBind");
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (_logger == null) {
            _logger = new LucaHomeLogger(TAG);
        }
        _logger.Debug("onDestroy");

        _databaseController.Dispose();
        _mediaMirrorController.Dispose();
        _receiverController.Dispose();
        _scheduleService.Dispose();

        _isInitialized = false;
    }

    private void registerReceiver() {
        _receiverController.RegisterReceiver(_actionReceiver, new String[]{Broadcasts.HOME_AUTOMATION_COMMAND});
        _receiverController.RegisterReceiver(_addReceiver, new String[]{
                Broadcasts.ADD_BIRTHDAY,
                Broadcasts.ADD_MOVIE,
                Broadcasts.ADD_SCHEDULE,
                Broadcasts.ADD_SOCKET});
        _receiverController.RegisterReceiver(_updateReceiver, new String[]{
                Broadcasts.UPDATE_BIRTHDAY,
                Broadcasts.UPDATE_MOVIE,
                Broadcasts.UPDATE_SCHEDULE,
                Broadcasts.UPDATE_SOCKET_LIST});
        _receiverController.RegisterReceiver(_startDownloadReceiver, new String[]{
                Broadcasts.ADD_BIRTHDAY,
                Broadcasts.ADD_MOVIE,
                Broadcasts.ADD_SCHEDULE,
                Broadcasts.ADD_SOCKET,
                Broadcasts.UPDATE_BIRTHDAY,
                Broadcasts.UPDATE_MENU,
                Broadcasts.UPDATE_MOVIE,
                Broadcasts.UPDATE_SCHEDULE,
                Broadcasts.UPDATE_SOCKET_LIST});

        _receiverController.RegisterReceiver(_birthdayConvertedReceiver, new String[]{Broadcasts.CONVERT_BIRTHDAY_FINISHED});
        _receiverController.RegisterReceiver(_changeConvertedReceiver, new String[]{Broadcasts.CONVERT_CHANGE_FINISHED});
        _receiverController.RegisterReceiver(_informationConvertedReceiver, new String[]{Broadcasts.CONVERT_INFORMATION_FINISHED});
        _receiverController.RegisterReceiver(_mapContentConvertedReceiver, new String[]{Broadcasts.CONVERT_MAP_CONTENT_FINISHED});
        _receiverController.RegisterReceiver(_listedMenuDtoConvertedReceiver, new String[]{Broadcasts.CONVERT_LISTED_MENU_FINISHED});
        _receiverController.RegisterReceiver(_menuDtoConvertedReceiver, new String[]{Broadcasts.CONVERT_MENU_FINISHED});
        _receiverController.RegisterReceiver(_motionCameraDtoConvertedReceiver, new String[]{Broadcasts.CONVERT_MOTION_CAMERA_DTO_FINISHED});
        _receiverController.RegisterReceiver(_movieConvertedReceiver, new String[]{Broadcasts.CONVERT_MOVIE_FINISHED});
        _receiverController.RegisterReceiver(_shoppingListConvertedReceiver, new String[]{Broadcasts.CONVERT_SHOPPING_LIST_FINISHED});

        _receiverController.RegisterReceiver(_birthdayDownloadReceiver, new String[]{Broadcasts.DOWNLOAD_BIRTHDAY_FINISHED});
        _receiverController.RegisterReceiver(_changeDownloadReceiver, new String[]{Broadcasts.DOWNLOAD_CHANGE_FINISHED});
        _receiverController.RegisterReceiver(_currentWeatherDownloadModelReceiver, new String[]{OWBroadcasts.CURRENT_WEATHER_JSON_FINISHED});
        _receiverController.RegisterReceiver(_forecastWeatherDownloadReceiver, new String[]{OWBroadcasts.FORECAST_WEATHER_JSON_FINISHED});
        _receiverController.RegisterReceiver(_informationDownloadReceiver, new String[]{Broadcasts.DOWNLOAD_INFORMATION_FINISHED});
        _receiverController.RegisterReceiver(_mapContentDownloadReceiver, new String[]{Broadcasts.DOWNLOAD_MAP_CONTENT_FINISHED});
        _receiverController.RegisterReceiver(_mediaMirrorViewDtoDownloadReceiver, new String[]{Broadcasts.MEDIAMIRROR_VIEW_DTO});
        _receiverController.RegisterReceiver(_listedMenuDtoDownloadReceiver, new String[]{Broadcasts.DOWNLOAD_LISTED_MENU_FINISHED});
        _receiverController.RegisterReceiver(_menuDtoDownloadReceiver, new String[]{Broadcasts.DOWNLOAD_MENU_FINISHED});
        _receiverController.RegisterReceiver(_motionCameraDtoDownloadReceiver, new String[]{Broadcasts.DOWNLOAD_MOTION_CAMERA_DTO_FINISHED});
        _receiverController.RegisterReceiver(_movieDownloadReceiver, new String[]{Broadcasts.DOWNLOAD_MOVIE_FINISHED});
        _receiverController.RegisterReceiver(_scheduleDownloadReceiver, new String[]{Broadcasts.DOWNLOAD_SCHEDULE_FINISHED});
        _receiverController.RegisterReceiver(_shoppingListDownloadReceiver, new String[]{Broadcasts.DOWNLOAD_SHOPPING_LIST_FINISHED});
        _receiverController.RegisterReceiver(_socketDownloadReceiver, new String[]{Broadcasts.DOWNLOAD_SOCKET_FINISHED});
        _receiverController.RegisterReceiver(_temperatureDownloadReceiver, new String[]{Broadcasts.DOWNLOAD_TEMPERATURE_FINISHED});

        _receiverController.RegisterReceiver(_reloadAllReceiver, new String[]{Broadcasts.RELOAD_ALL});
        _receiverController.RegisterReceiver(_reloadBirthdayReceiver, new String[]{Broadcasts.RELOAD_BIRTHDAY});
        _receiverController.RegisterReceiver(_reloadChangeReceiver, new String[]{
                Broadcasts.RELOAD_BIRTHDAY,
                Broadcasts.RELOAD_MOVIE,
                Broadcasts.RELOAD_SCHEDULE,
                Broadcasts.RELOAD_SOCKETS,
                Broadcasts.RELOAD_TIMER});
        _receiverController.RegisterReceiver(_reloadCurrentWeatherReceiver, new String[]{Broadcasts.RELOAD_CURRENT_WEATHER});
        _receiverController.RegisterReceiver(_reloadForecastWeatherReceiver, new String[]{Broadcasts.RELOAD_FORECAST_WEATHER});
        _receiverController.RegisterReceiver(_reloadListedMenuReceiver, new String[]{Broadcasts.RELOAD_LISTED_MENU});
        _receiverController.RegisterReceiver(_reloadMediaMirrorReceiver, new String[]{Broadcasts.RELOAD_MEDIAMIRROR});
        _receiverController.RegisterReceiver(_reloadMenuReceiver, new String[]{Broadcasts.RELOAD_MENU});
        _receiverController.RegisterReceiver(_reloadMotionCameraDtoReceiver, new String[]{Broadcasts.RELOAD_MOTION_CAMERA_DTO});
        _receiverController.RegisterReceiver(_reloadMovieReceiver, new String[]{Broadcasts.RELOAD_MOVIE});
        _receiverController.RegisterReceiver(_reloadScheduleReceiver, new String[]{Broadcasts.RELOAD_SCHEDULE, Broadcasts.RELOAD_TIMER});
        _receiverController.RegisterReceiver(_reloadShoppingListReceiver, new String[]{Broadcasts.RELOAD_SHOPPING_LIST});
        _receiverController.RegisterReceiver(_reloadSocketReceiver, new String[]{Broadcasts.RELOAD_SOCKETS});
        _receiverController.RegisterReceiver(_reloadTemperatureReceiver, new String[]{Broadcasts.RELOAD_TEMPERATURE});

        _receiverController.RegisterReceiver(_updateBoughtShoppingListReceiver, new String[]{Broadcasts.UPDATE_BOUGHT_SHOPPING_LIST});

        _receiverController.RegisterReceiver(_batteryChangedReceiver, new String[]{Intent.ACTION_BATTERY_CHANGED});
        _receiverController.RegisterReceiver(_timeTickReceiver, new String[]{Intent.ACTION_TIME_TICK});
    }

    private void addSchedules() {
        _scheduleService.AddSchedule("UpdateBirthday", _downloadBirthdayRunnable, 4 * 60 * 60 * 1000, true);
        _scheduleService.AddSchedule("UpdateChanges", _downloadChangeRunnable, 30 * 60 * 1000, true);
        _scheduleService.AddSchedule("UpdateCurrent", _downloadCurrentWeatherRunnable, 15 * 60 * 1000, true);
        _scheduleService.AddSchedule("UpdateForecast", _downloadForecastWeatherRunnable, 30 * 60 * 1000, true);
        _scheduleService.AddSchedule("UpdateListedMenu", _downloadListedMenuRunnable, 3 * 60 * 60 * 1000, true);
        _scheduleService.AddSchedule("UpdateMediaMirror", _downloadMediaMirrorRunnable, 5 * 60 * 1000, true);
        _scheduleService.AddSchedule("UpdateMenu", _downloadMenuRunnable, 3 * 60 * 60 * 1000, true);
        _scheduleService.AddSchedule("UpdateMotionCamera", _downloadMotionCameraDtoRunnable, 5 * 60 * 1000, true);
        _scheduleService.AddSchedule("UpdateMovie", _downloadMovieRunnable, 60 * 60 * 1000, true);
        _scheduleService.AddSchedule("UpdateShoppingList", _downloadShoppingListRunnable, 30 * 60 * 1000, true);
        _scheduleService.AddSchedule("UpdateSockets", _downloadSocketRunnable, 15 * 60 * 1000, true);
        _scheduleService.AddSchedule("UpdateTemperature", _downloadTemperatureRunnable, 5 * 60 * 1000, true);
        _schedulesAdded = true;
    }

    private void boot() {
        if (!_sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.SHARED_PREF_INSTALLED)) {
            install();
        }

        if (!_downloadingData) {
            if (!_sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.USER_DATA_ENTERED)) {
                _broadcastController.SendSerializableArrayBroadcast(
                        Broadcasts.COMMAND,
                        new String[]{Bundles.COMMAND},
                        new Object[]{Command.SHOW_USER_LOGIN_DIALOG});
            } else {
                Calendar now = Calendar.getInstance();
                if (_lastUpdate != null) {
                    long difference = Calendar.getInstance().getTimeInMillis() - _lastUpdate.getTimeInMillis();

                    if (difference < 60 * 1000) {
                        _logger.Warn("Just updated the data!");

                        _broadcastController.SendSerializableArrayBroadcast(
                                Broadcasts.COMMAND,
                                new String[]{Bundles.COMMAND, Bundles.NAVIGATE_DATA},
                                new Object[]{Command.NAVIGATE, NavigationData.MAIN});

                        return;
                    }
                }
                _lastUpdate = now;

                if (!_schedulesAdded) {
                    addSchedules();
                }

                download();
            }
        }
    }

    private void download() {
        _logger.Debug("download");

        _downloadingData = true;
        _progress = 0;
        _downloadCount = Constants.DOWNLOAD_STEPS;

        startTimeout();

        _downloadCurrentWeatherRunnable.run();
        _downloadForecastWeatherRunnable.run();

        _downloadChangeRunnable.run();
        _downloadInformationRunnable.run();
        _downloadMediaMirrorRunnable.run();
        _downloadMotionCameraDtoRunnable.run();
        _downloadTemperatureRunnable.run();
    }

    private void updateDownloadCount() {
        _logger.Debug("updateDownloadCount");

        _progress++;
        _broadcastController.SendIntBroadcast(
                Broadcasts.UPDATE_PROGRESSBAR,
                Bundles.VIEW_PROGRESS,
                _progress);

        if (_progress >= _downloadCount) {
            stopTimeout();
            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.COMMAND,
                    new String[]{Bundles.COMMAND, Bundles.NAVIGATE_DATA},
                    new Object[]{Command.NAVIGATE, NavigationData.MAIN});
        }
    }

    private void install() {
        _logger.Debug("install");

        if (!_sharedPrefController.Contains(SharedPrefConstants.USER_NAME)
                || !_sharedPrefController.Contains(SharedPrefConstants.USER_PASSPHRASE)) {
            _broadcastController.SendSerializableBroadcast(Broadcasts.COMMAND, Bundles.COMMAND, Command.SHOW_USER_LOGIN_DIALOG);
        }

        _sharedPrefController.SaveBooleanValue(SharedPrefConstants.SHARED_PREF_INSTALLED, true);

        checkInstallSharedPrefBoolean(SharedPrefConstants.DISPLAY_SOCKET_NOTIFICATION, true);
        checkInstallSharedPrefBoolean(SharedPrefConstants.DISPLAY_WEATHER_NOTIFICATION, true);
        checkInstallSharedPrefBoolean(SharedPrefConstants.DISPLAY_TEMPERATURE_NOTIFICATION, true);
        checkInstallSharedPrefBoolean(SharedPrefConstants.DISPLAY_BIRTHDAY_NOTIFICATION, true);
        checkInstallSharedPrefBoolean(SharedPrefConstants.DISPLAY_SLEEP_NOTIFICATION, true);
        checkInstallSharedPrefBoolean(SharedPrefConstants.DISPLAY_CAMERA_NOTIFICATION, true);

        checkInstallSharedPrefBoolean(SharedPrefConstants.START_AUDIO_APP, true);
        checkInstallSharedPrefBoolean(SharedPrefConstants.START_OSMC_APP, true);

        checkInstallSharedPrefBoolean(SharedPrefConstants.USE_BEACONS, false);

        checkInstallSharedPrefString(SharedPrefConstants.LAST_LOADED_BIRTHDAY_TIME, "00-00-00-00-0000");
        checkInstallSharedPrefString(SharedPrefConstants.LAST_LOADED_MAP_CONTENT_TIME, "00-00-00-00-0000");
        checkInstallSharedPrefString(SharedPrefConstants.LAST_LOADED_MENU_TIME, "00-00-00-00-0000");
        checkInstallSharedPrefString(SharedPrefConstants.LAST_LOADED_MOVIE_TIME, "00-00-00-00-0000");
        checkInstallSharedPrefString(SharedPrefConstants.LAST_LOADED_SETTINGS_TIME, "00-00-00-00-0000");
        checkInstallSharedPrefString(SharedPrefConstants.LAST_LOADED_SHOPPING_LIST_TIME, "00-00-00-00-0000");
    }

    private void checkInstallSharedPrefBoolean(String key, boolean value) {
        if (!_sharedPrefController.Contains(key)) {
            _sharedPrefController.SaveBooleanValue(key, value);
        }
    }

    private void checkInstallSharedPrefString(String key, String value) {
        if (!_sharedPrefController.Contains(key)) {
            _sharedPrefController.SaveStringValue(key, value);
        }
    }

    private void installNotificationSettings() {
        _logger.Debug("installNotificationSettings");

        if (_wirelessSocketList == null) {
            _logger.Warn("_wirelessSocketList is null!");
            return;
        }

        for (int index = 0; index < _wirelessSocketList.getSize(); index++) {
            String key = _wirelessSocketList.getValue(index).GetNotificationVisibilitySharedPrefKey();
            if (!_sharedPrefController.Contains(key)) {
                _logger.Info(String.format(Locale.getDefault(), "Installing key %s", key));
                _sharedPrefController.SaveBooleanValue(key, true);
            } else {
                _logger.Info(String.format(Locale.getDefault(), "Key %s already exists and is %s", key,
                        _sharedPrefController.LoadBooleanValueFromSharedPreferences(key)));
            }
        }
    }

    private void startTimeout() {
        _logger.Debug("Starting timeoutController...");
        _timeoutHandler.postDelayed(_timeoutCheck, Timeouts.MAX_DOWNLOAD_SEC);
    }

    private void stopTimeout() {
        _logger.Debug("Stopping timeoutController...");
        _downloadingData = false;
        _timeoutHandler.removeCallbacks(_timeoutCheck);
    }

    private void sendBirthdaysToWear() {
        if (_birthdayList == null) {
            _logger.Warn("_birthdayList is null!");
            return;
        }

        String messageText = "Birthdays:";
        for (int index = 0; index < _birthdayList.getSize(); index++) {
            messageText += _birthdayList.getValue(index).GetName() + ":"
                    + String.valueOf(_birthdayList.getValue(index).GetBirthday().get(Calendar.YEAR)) + ":"
                    + String.valueOf(_birthdayList.getValue(index).GetBirthday().get(Calendar.MONTH)) + ":"
                    + String.valueOf(_birthdayList.getValue(index).GetBirthday().get(Calendar.DAY_OF_MONTH)) + "&";
        }
        _logger.Info("message is " + messageText);
        _serviceController.SendMessageToWear(messageText);
    }

    private void sendMediaMirrorToWear() {
        if (_mediaMirrorList == null) {
            _logger.Warn("_mediaMirrorList is null!");
            return;
        }

        String messageText = "MediaMirror:";
        for (int index = 0; index < _mediaMirrorList.getSize(); index++) {
            messageText += _mediaMirrorList.getValue(index).GetMediaServerSelection().GetIp() + ":"
                    + String.valueOf(_mediaMirrorList.getValue(index).GetBatteryLevel()) + ":"
                    + String.valueOf(_mediaMirrorList.getValue(index).GetVolume()) + ":"
                    + _mediaMirrorList.getValue(index).GetYoutubeId() + ":"
                    + _mediaMirrorList.getValue(index).GetServerVersion() + "&";
        }
        _logger.Info("message is " + messageText);
        _serviceController.SendMessageToWear(messageText);
    }

    private void sendMenuToWear() {
        if (_menu == null) {
            _logger.Warn("_menu is null!");
            return;
        }

        String messageText = "Menu:";
        for (int index = 0; index < _menu.getSize(); index++) {
            messageText += _menu.getValue(index).GetWeekday() + ":"
                    + String.valueOf(_menu.getValue(index).GetDay()) + ":"
                    + String.valueOf(_menu.getValue(index).GetMonth()) + ":"
                    + String.valueOf(_menu.getValue(index).GetYear()) + ":"
                    + _menu.getValue(index).GetTitle() + ":"
                    + _menu.getValue(index).GetDescription() + ":&";
        }

        _logger.Info("message is " + messageText);
        _serviceController.SendMessageToWear(messageText);
    }

    private void sendSchedulesToWear() {
        if (_scheduleList == null) {
            _logger.Warn("_scheduleList is null!");
            return;
        }

        String messageText = "Schedules:";
        for (int index = 0; index < _scheduleList.getSize(); index++) {
            String information = _scheduleList.getValue(index).GetWeekday().toString() + ", "
                    + _scheduleList.getValue(index).GetTime().toString() + " set "
                    + _scheduleList.getValue(index).GetSocket().GetName() + " to "
                    + String.valueOf(_scheduleList.getValue(index).GetAction());
            messageText += _scheduleList.getValue(index).GetName() + ":" + information + ":"
                    + (_scheduleList.getValue(index).IsActive() ? "1" : "0") + "&";
        }
        _logger.Info("message is " + messageText);
        _serviceController.SendMessageToWear(messageText);
    }

    private void sendSocketsToWear() {
        if (_wirelessSocketList == null) {
            _logger.Warn("_wirelessSocketList is null!");
            return;
        }

        String messageText = "Sockets:";
        for (int index = 0; index < _wirelessSocketList.getSize(); index++) {
            messageText += _wirelessSocketList.getValue(index).GetName() + ":"
                    + (_wirelessSocketList.getValue(index).IsActivated() ? "1" : "0") + "&";
        }
        _logger.Info("message is " + messageText);
        _serviceController.SendMessageToWear(messageText);
    }

    private void sendShoppingListToWear() {
        if (_shoppingList == null) {
            _logger.Warn("_shoppingList is null!");
            return;
        }

        String messageText = "ShoppingList:";
        for (int index = 0; index < _shoppingList.getSize(); index++) {
            ShoppingEntryDto entry = _shoppingList.getValue(index);
            messageText += entry.GetName() + ":"
                    + entry.GetGroup().toString() + ":"
                    + entry.GetQuantity() + ":"
                    + (entry.IsBought() ? "1" : "0") + "&";
        }
        _logger.Info("message is " + messageText);
        _serviceController.SendMessageToWear(messageText);
    }

    private void sendTemperatureToWear() {
        if (_temperatureList == null) {
            _logger.Warn("_temperatureList is null!");
            return;
        }

        String messageText = "RaspberryTemperature:";

        int found = 0;
        for (int index = 0; index < _temperatureList.getSize(); index++) {
            TemperatureDto entry = _temperatureList.getValue(index);
            if (entry.GetTemperatureType() == TemperatureType.RASPBERRY) {
                messageText += "TEMPERATURE:" + entry.GetTemperatureString();
                messageText += "&AREA:" + entry.GetArea();
                messageText += "&LASTUPDATE:" + entry.GetLastUpdate().HHMM();
                found++;
            }
        }

        _logger.Info("messageText for temperature is " + messageText);
        if (found == 1) {
            _serviceController.SendMessageToWear(messageText);
        } else {
            _logger.Warn("found " + String.valueOf(found) + " different temperatures!");
        }
    }

    private void sendTimerToWear() {
        if (_timerList == null) {
            _logger.Warn("_timerList is null!");
            return;
        }

        String messageText = "Timer:";
        for (int index = 0; index < _timerList.getSize(); index++) {
            String information = _timerList.getValue(index).GetWeekday().toString() + ", "
                    + _timerList.getValue(index).GetTime().toString() + " set "
                    + _timerList.getValue(index).GetSocket().GetName() + " to "
                    + String.valueOf(_timerList.getValue(index).GetAction());

            messageText += _timerList.getValue(index).GetName() + ":"
                    + information + ":"
                    + (_timerList.getValue(index).IsActive() ? "1" : "0") + "&";
        }
        _logger.Info("message is " + messageText);
        _serviceController.SendMessageToWear(messageText);
    }

    private void sendWeatherToWear() {
        if (_currentWeather == null) {
            _logger.Warn("_currentWeather is null!");
            return;
        }

        String messageText = "CurrentWeather:DESCRIPTION:" + _currentWeather.GetDescription()
                + "&TEMPERATURE:" + _currentWeather.GetTemperatureString()
                + "&LASTUPDATE:" + _currentWeather.GetLastUpdate().toMilliSecond()
                + "&SUNRISE:" + String.valueOf(_currentWeather.GetSunriseTime().toMilliSecond())
                + "&SUNSET:" + String.valueOf(_currentWeather.GetSunsetTime().toMilliSecond());

        _serviceController.SendMessageToWear(messageText);
    }

    private void checkDatabase() {
        _logger.Debug("checkDatabase");

        if (!_networkController.IsNetworkAvailable()) {
            _logger.Warn("No network available!");
            return;
        }

        if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
            _logger.Warn("No LucaHome network! ...");
            return;
        }

        SerializableList<ActionDto> storedActions = _databaseController.GetActions();
        if (storedActions.getSize() > 0) {
            for (int index = 0; index < storedActions.getSize(); index++) {
                ActionDto entry = storedActions.getValue(index);
                _databaseController.DeleteAction(entry);
                _scheduleService.DeleteSchedule(entry.GetName());
                _serviceController.StartRestService(
                        entry.GetName(),
                        entry.GetAction(),
                        entry.GetBroadcast());
            }
        } else {
            _logger.Debug("No actions stored!");
        }
    }

    private void checkSleepNotificationFlag() {

        if (!_networkController.IsNetworkAvailable()) {
            _logger.Warn("No network available!");
            return;
        }

        if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
            _logger.Warn("No LucaHome network! ...");
            return;
        }

        if (_sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_SLEEP_NOTIFICATION_ACTIVE)) {
            _notificationController.CreateSleepHeatingNotification();
            _sharedPrefController.SaveBooleanValue(SharedPrefConstants.DISPLAY_SLEEP_NOTIFICATION_ACTIVE, false);
        }
    }
}