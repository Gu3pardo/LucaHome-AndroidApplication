package guepardoapps.lucahome.services;

import java.util.Calendar;
import java.util.Locale;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.constants.IDs;
import guepardoapps.library.lucahome.common.constants.ServerActions;
import guepardoapps.library.lucahome.common.constants.SharedPrefConstants;
import guepardoapps.library.lucahome.common.dto.*;
import guepardoapps.library.lucahome.common.enums.*;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.DatabaseController;
import guepardoapps.library.lucahome.controller.LucaNotificationController;
import guepardoapps.library.lucahome.controller.MediaMirrorController;
import guepardoapps.library.lucahome.controller.MenuController;
import guepardoapps.library.lucahome.controller.ServiceController;
import guepardoapps.library.lucahome.converter.json.*;
import guepardoapps.library.lucahome.services.sockets.SocketActionService;

import guepardoapps.library.openweather.common.OWBroadcasts;
import guepardoapps.library.openweather.common.OWBundles;
import guepardoapps.library.openweather.common.OWIds;
import guepardoapps.library.openweather.common.model.ForecastModel;
import guepardoapps.library.openweather.common.model.WeatherModel;
import guepardoapps.library.openweather.controller.OpenWeatherController;

import guepardoapps.library.toolset.beacon.BeaconController;
import guepardoapps.library.toolset.common.classes.NotificationContent;
import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.lucahome.common.constants.Constants;
import guepardoapps.library.lucahome.common.constants.Timeouts;
import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.DialogController;
import guepardoapps.library.toolset.controller.NetworkController;
import guepardoapps.library.toolset.controller.ReceiverController;
import guepardoapps.library.toolset.controller.SharedPrefController;
import guepardoapps.library.toolset.scheduler.ScheduleService;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.views.BirthdayView;
import guepardoapps.lucahome.views.ForecastWeatherView;
import guepardoapps.lucahome.views.SecurityView;
import guepardoapps.lucahome.views.SensorTemperatureView;

public class MainService extends Service {

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
            MainServiceAction action = (MainServiceAction) intent.getSerializableExtra(Bundles.MAIN_SERVICE_ACTION);
            if (action != null) {
                _logger.Debug(String.format("Action is %s", action.toString()));
                _progress = 0;
                switch (action) {
                    case DOWNLOAD_ALL:
                        _downloadCount = Constants.DOWNLOAD_STEPS;
                        prepareDownloadAll();
                        startDownloadLucaHomeAll();
                        startDownloadOtherAll();
                        break;
                    case DOWNLOAD_SOCKETS:
                        _downloadCount = 1;
                        startDownloadSocket();
                        break;
                    case DOWNLOAD_TEMPERATURE:
                        _downloadCount = 1;
                        startDownloadTemperature();
                        break;
                    case DOWNLOAD_WEATHER_CURRENT:
                        _downloadCount = 1;
                        startDownloadCurrentWeather();
                        break;
                    case DOWNLOAD_SHOPPING_LIST:
                        _downloadCount = 1;
                        startDownloadShoppingList();
                        break;
                    case DOWNLOAD_MENU:
                        _downloadCount = 2;
                        startDownloadMenu();
                        startDownloadListedMenu();
                        break;
                    case DOWNLOAD_MOTION_CAMERA_DTO:
                        _downloadCount = 1;
                        startDownloadMotionCameraDto();
                        break;
                    case GET_ALL:
                        _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_MAP_CONTENT_VIEW,
                                new String[]{Bundles.MAP_CONTENT_LIST, Bundles.SOCKET_LIST, Bundles.SCHEDULE_LIST,
                                        Bundles.TIMER_LIST, Bundles.TEMPERATURE_LIST, Bundles.SHOPPING_LIST, Bundles.MENU,
                                        Bundles.LISTED_MENU, Bundles.MOTION_CAMERA_DTO},
                                new Object[]{_mapContentList, _wirelessSocketList, _scheduleList, _timerList,
                                        _temperatureList, _shoppingList, _menu, _listedMenu, _motionCameraDto});
                        break;
                    case GET_BIRTHDAYS:
                        _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_BIRTHDAY,
                                new String[]{Bundles.BIRTHDAY_LIST}, new Object[]{_birthdayList});
                        sendBirthdaysToWear();
                        break;
                    case GET_CHANGES:
                        _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_CHANGE,
                                new String[]{Bundles.CHANGE_LIST}, new Object[]{_changeList});
                        break;
                    case GET_INFORMATIONS:
                        _logger.Debug("GET_INFORMATIONS");
                        if (_information != null) {
                            _logger.Debug(_information.toString());
                        } else {
                            _logger.Warn("Information is null!");
                        }
                        _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_INFORMATION,
                                new String[]{Bundles.INFORMATION_SINGLE}, new Object[]{_information});
                        break;
                    case GET_MENU:
                        _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_MENU_VIEW,
                                new String[]{Bundles.MENU}, new Object[]{_menu});
                        _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_LISTED_MENU_VIEW,
                                new String[]{Bundles.LISTED_MENU}, new Object[]{_listedMenu});
                        sendMenuToWear();
                        break;
                    case GET_MOTION_CAMERA_DTO:
                        _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_MOTION_CAMERA_DTO,
                                new String[]{Bundles.MOTION_CAMERA_DTO}, new Object[]{_motionCameraDto});
                        break;
                    case GET_MOVIES:
                        _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_MOVIE,
                                new String[]{Bundles.MOVIE_LIST}, new Object[]{_movieList});
                        break;
                    case GET_SCHEDULES:
                        _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_SCHEDULE,
                                new String[]{Bundles.SCHEDULE_LIST, Bundles.SOCKET_LIST},
                                new Object[]{_scheduleList, _wirelessSocketList});
                        sendSchedulesToWear();
                        break;
                    case GET_SOCKETS:
                        _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_SOCKET,
                                new String[]{Bundles.SOCKET_LIST}, new Object[]{_wirelessSocketList});
                        sendSocketsToWear();
                        break;
                    case GET_TEMPERATURE:
                        _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_TEMPERATURE,
                                new String[]{Bundles.TEMPERATURE_LIST}, new Object[]{_temperatureList});
                        sendTemperatureToWear();
                        break;
                    case GET_TIMER:
                        _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_TIMER,
                                new String[]{Bundles.TIMER_LIST, Bundles.SOCKET_LIST},
                                new Object[]{_timerList, _wirelessSocketList});
                        sendTimerToWear();
                        break;
                    case GET_WEATHER_CURRENT:
                        _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_WEATHER_VIEW,
                                new String[]{Bundles.WEATHER_CURRENT}, new Object[]{_currentWeather});
                        sendWeatherToWear();
                        break;
                    case GET_WEATHER_FORECAST:
                        _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_FORECAST_VIEW,
                                new String[]{Bundles.WEATHER_FORECAST}, new Object[]{_forecastWeather});
                        break;
                    case GET_MAP_CONTENT:
                        _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_MAP_CONTENT_VIEW,
                                new String[]{Bundles.MAP_CONTENT_LIST, Bundles.SOCKET_LIST, Bundles.SCHEDULE_LIST,
                                        Bundles.TIMER_LIST, Bundles.TEMPERATURE_LIST, Bundles.SHOPPING_LIST, Bundles.MENU,
                                        Bundles.LISTED_MENU, Bundles.MOTION_CAMERA_DTO},
                                new Object[]{_mapContentList, _wirelessSocketList, _scheduleList, _timerList,
                                        _temperatureList, _shoppingList, _menu, _listedMenu, _motionCameraDto});
                        break;
                    case GET_SHOPPING_LIST:
                        _broadcastController.SendSerializableArrayBroadcast(
                                guepardoapps.library.lucahome.common.constants.Broadcasts.UPDATE_SHOPPING_LIST,
                                new String[]{guepardoapps.library.lucahome.common.constants.Bundles.SHOPPING_LIST},
                                new Object[]{_shoppingList});
                        sendShoppingListToWear();
                        break;
                    case GET_MEDIAMIRROR:
                        _broadcastController.SendSerializableArrayBroadcast(
                                guepardoapps.library.lucahome.common.constants.Broadcasts.UPDATE_MEDIAMIRROR,
                                new String[]{guepardoapps.library.lucahome.common.constants.Bundles.MEDIAMIRROR},
                                new Object[]{_mediaMirrorList});
                        sendMediaMirrorToWear();
                        break;
                    case SHOW_NOTIFICATION_SOCKET:
                        if (_wirelessSocketList != null) {
                            _notificationController.CreateSocketNotification(_wirelessSocketList);
                        }
                        break;
                    case SHOW_NOTIFICATION_TEMPERATURE:
                        if (_temperatureList != null && _currentWeather != null) {
                            _notificationController.CreateTemperatureNotification(SensorTemperatureView.class,
                                    _temperatureList, _currentWeather);
                        }
                        break;
                    case SHOW_NOTIFICATION_WEATHER:
                        if (_sharedPrefController
                                .LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_WEATHER_NOTIFICATION)) {
                            _openWeatherController.LoadForecastWeather();
                        }
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
                        for (MediaMirrorSelection entry : MediaMirrorSelection.values()) {
                            if (entry.IsSleepingMirror()) {
                                _mediaMirrorController.SendCommand(entry.GetIp(), ServerAction.STOP_SEA_SOUND.toString(),
                                        "");
                                break;
                            }
                        }
                        break;
                    case BEACON_SCANNING_START:
                        _beaconController.StartScanning();
                        break;
                    case BEACON_SCANNING_STOP:
                        _beaconController.StopScanning();
                        break;
                    case DISABLE_SECURITY_CAMERA:
                        _serviceController.StartRestService(Bundles.MOTION_CAMERA_DTO, ServerActions.STOP_MOTION,
                                Broadcasts.RELOAD_MOTION_CAMERA_DTO, LucaObject.MOTION_CAMERA_DTO, RaspberrySelection.BOTH);
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

            int timerMinute = _sharedPrefController
                    .LoadIntegerValueFromSharedPreferences(SharedPrefConstants.TIMER_MIN);
            _logger.Debug(String.format(Locale.GERMAN, "Loaded time value is %d sec", timerMinute));
            if (timerMinute == 0) {
                _logger.Error(String.format("timerMinute and timerHour is 0! Setting timerMinute to default %s!",
                        SharedPrefConstants.DEFAULT_TIMER_MIN));
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

            String addHeatingTimerAction = ServerActions.ADD_SCHEDULE + socketName + "_" + String.valueOf(scheduleHour)
                    + "_" + String.valueOf(scheduleMinute) + "&socket=" + socketName + "&gpio=&weekday=" + currentDay
                    + "&hour=" + scheduleHour + "&minute=" + scheduleMinute
                    + "&onoff=0&isTimer=1&playSound=0&playRaspberry=1";
            _logger.Debug(String.format("Heating action is %s", addHeatingTimerAction));

            _serviceController.StartRestService("", addHeatingTimerAction, Broadcasts.RELOAD_TIMER, LucaObject.TIMER,
                    RaspberrySelection.BOTH);

            if (enableSound) {
                enableSoundSchedule(30);
            }

            startDownloadSocket();
            startDownloadSchedule();
        }

        private void enableSoundSchedule(int playLength) {
            _logger.Debug("enableSoundSchedule");

            for (MediaMirrorSelection entry : MediaMirrorSelection.values()) {
                if (entry.IsSleepingMirror()) {
                    _mediaMirrorController.SendCommand(entry.GetIp(), ServerAction.PLAY_SEA_SOUND.toString(),
                            String.valueOf(playLength));
                    break;
                }
            }
        }
    };

    // Receiver used for receiving the current battery state of the phone and
    // sending the state to the watch
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

    private BroadcastReceiver _birthdayDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_birthdayDownloadReceiver onReceive");

            String[] birthdayStringArray = intent.getStringArrayExtra(Bundles.BIRTHDAY_DOWNLOAD);
            if (birthdayStringArray != null) {

                SerializableList<BirthdayDto> newBirthdayList = JsonDataToBirthdayConverter
                        .GetList(birthdayStringArray);
                if (newBirthdayList != null) {
                    _birthdayList = newBirthdayList;
                    _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_BIRTHDAY,
                            new String[]{Bundles.BIRTHDAY_LIST}, new Object[]{_birthdayList});

                    checkForBirthday();
                    sendBirthdaysToWear();
                } else {
                    _logger.Warn("GetList is null");
                }
            } else {
                _logger.Warn("birthdayStringArray is null");
            }

            updateDownloadCount();
        }

        private void checkForBirthday() {
            for (int index = 0; index < _birthdayList.getSize(); index++) {
                BirthdayDto birthday = _birthdayList.getValue(index);
                if (birthday.HasBirthday()) {
                    int age = birthday.GetAge();
                    _logger.Debug("It is " + birthday.GetName() + "'s " + String.valueOf(age) + "th birthday!");
                    _notificationController.CreateBirthdayNotification(BirthdayView.class, R.drawable.birthday,
                            birthday.GetName(), birthday.GetNotificationBody(age), true);
                }
            }
        }
    };

    private BroadcastReceiver _changeDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_changeDownloadReceiver onReceive");

            String[] changeStringArray = intent.getStringArrayExtra(Bundles.CHANGE_DOWNLOAD);
            if (changeStringArray != null) {

                SerializableList<ChangeDto> newChangeList = JsonDataToChangeConverter.GetList(changeStringArray);
                if (newChangeList != null) {
                    _changeList = newChangeList;
                    _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_CHANGE,
                            new String[]{Bundles.CHANGE_LIST}, new Object[]{_changeList});
                } else {
                    _logger.Warn("newChangeList is null");
                }
            } else {
                _logger.Warn("changeStringArray is null");
            }

            updateDownloadCount();
        }
    };

    private BroadcastReceiver _forecastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _receiverController.UnregisterReceiver(_forecastReceiver);

            ForecastModel forecastModel = (ForecastModel) intent.getSerializableExtra(OWBundles.EXTRA_FORECAST_MODEL);
            if (forecastModel != null) {
                _logger.Debug(forecastModel.toString());

                NotificationContent _message = forecastModel.TellForecastWeather();

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
                _logger.Error("_forecastModel is null!");
            }
        }
    };

    private BroadcastReceiver _forecastModelReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_forecastModelReceiver onReceive");

            ForecastModel newForecastWeather = (ForecastModel) intent
                    .getSerializableExtra(OWBundles.EXTRA_FORECAST_MODEL);
            if (newForecastWeather != null) {
                _forecastWeather = newForecastWeather;
            } else {
                _logger.Warn("newForecastWeather is null");
            }

            _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_FORECAST_VIEW,
                    new String[]{Bundles.WEATHER_FORECAST}, new Object[]{_forecastWeather});

            updateDownloadCount();
        }
    };

    private BroadcastReceiver _informationDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_informationDownloadReceiver onReceive");

            String[] informationStringArray = intent.getStringArrayExtra(Bundles.INFORMATION_DOWNLOAD);
            if (informationStringArray != null) {

                InformationDto newInformation = JsonDataToInformationConverter.Get(informationStringArray);
                if (newInformation != null) {
                    _information = newInformation;
                    _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_INFORMATION,
                            new String[]{Bundles.INFORMATION_SINGLE}, new Object[]{_information});
                } else {
                    _logger.Warn("newInformation is null");
                }
            } else {
                _logger.Warn("informationStringArray is null");
            }

            updateDownloadCount();
        }
    };

    private BroadcastReceiver _mapContentDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_mapContentDownloadReceiver onReceive");

            String[] mapContentStringArray = intent.getStringArrayExtra(Bundles.MAP_CONTENT_DOWNLOAD);
            if (mapContentStringArray != null) {

                SerializableList<MapContentDto> newMapContentList = JsonDataToMapContentConverter
                        .GetList(mapContentStringArray);
                if (newMapContentList != null) {
                    _mapContentList = newMapContentList;
                }

                if (_mapContentList != null) {
                    for (int index = 0; index < _mapContentList.getSize(); index++) {
                        _logger.Info(_mapContentList.getValue(index).toString());
                    }
                } else {
                    _logger.Warn("_mapContentList is null!");
                }
            } else {
                _logger.Warn("mapContentStringArray is null!");
            }
            updateDownloadCount();
        }
    };

    private BroadcastReceiver _listedMenuDtoDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_listedMenuDtoDownloadReceiver onReceive");

            String[] listedMenuStringArray = intent.getStringArrayExtra(Bundles.LISTED_MENU_DOWNLOAD);
            if (listedMenuStringArray != null) {

                SerializableList<ListedMenuDto> newListedMenuList = JsonDataToListedMenuConverter
                        .GetList(listedMenuStringArray);
                if (newListedMenuList != null) {
                    _listedMenu = newListedMenuList;
                    _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_LISTED_MENU_VIEW,
                            new String[]{Bundles.LISTED_MENU}, new Object[]{_listedMenu});

                    _databaseController.ClearDatabaseListedMenu();
                    for (int index = 0; index < _listedMenu.getSize(); index++) {
                        _databaseController.SaveListedMenu(_listedMenu.getValue(index));
                    }
                } else {
                    _logger.Warn("newListedMenuList is null");
                }
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

            MediaMirrorViewDto mediaMirrorViewDto = (MediaMirrorViewDto) intent
                    .getSerializableExtra(guepardoapps.library.lucahome.common.constants.Bundles.MEDIAMIRROR_VIEW_DTO);

            if (mediaMirrorViewDto != null) {
                _logger.Debug("New Dto is: " + mediaMirrorViewDto.toString());
                boolean foundEntry = false;
                for (int index = 0; index < _mediaMirrorList.getSize(); index++) {
                    if (_mediaMirrorList.getValue(index).GetMediaMirrorSelection().GetId() == mediaMirrorViewDto
                            .GetMediaMirrorSelection().GetId()) {
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

    private BroadcastReceiver _menuDtoDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_menuDtoDownloadReceiver onReceive");

            String[] menuStringArray = intent.getStringArrayExtra(Bundles.MENU_DOWNLOAD);
            if (menuStringArray != null) {

                SerializableList<MenuDto> newMenuList = JsonDataToMenuConverter.GetList(menuStringArray);
                if (newMenuList != null) {
                    _menu = newMenuList;
                    _menuController.CheckMenuDto(_menu);
                    _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_MENU_VIEW,
                            new String[]{Bundles.MENU}, new Object[]{_menu});

                    _databaseController.ClearDatabaseMenu();
                    for (int index = 0; index < _menu.getSize(); index++) {
                        _databaseController.SaveMenu(_menu.getValue(index));
                    }
                } else {
                    _logger.Warn("newMenuList is null");
                }
            } else {
                _logger.Warn("menuStringArray is null");
            }

            updateDownloadCount();
            sendMenuToWear();
        }
    };

    private BroadcastReceiver _motionCameraDtoDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_motionCameraDtoDownloadReceiver onReceive");

            String[] motionCameraDtoStringArray = intent.getStringArrayExtra(Bundles.MOTION_CAMERA_DTO_DOWNLOAD);
            if (motionCameraDtoStringArray != null) {

                SerializableList<MotionCameraDto> motionCameraDtoList = JsonDataToMotionCameraDtoConverter
                        .GetList(motionCameraDtoStringArray);
                if (motionCameraDtoList != null) {
                    if (motionCameraDtoList.getSize() != 1) {
                        _logger.Error(
                                String.format(Locale.GERMAN,
                                        "MotionCameraDtoList has wrong size %d!",
                                        motionCameraDtoList.getSize()));
                        return;
                    }

                    _motionCameraDto = motionCameraDtoList.getValue(0);
                    _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_MOTION_CAMERA_DTO,
                            new String[]{Bundles.MOTION_CAMERA_DTO}, new Object[]{_motionCameraDto});

                    if (_motionCameraDto.GetCameraState()) {
                        _notificationController.CreateCameraNotification(SecurityView.class);
                    } else {
                        _notificationController.CloseNotification(IDs.NOTIFICATION_CAMERA);
                    }
                } else {
                    _logger.Warn("motionCameraDtoList is null");
                }
            } else {
                _logger.Warn("motionCameraDtoStringArray is null");
            }

            updateDownloadCount();
        }
    };

    private BroadcastReceiver _movieDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_movieDownloadReceiver onReceive");

            String[] movieStringArray = intent.getStringArrayExtra(Bundles.MOVIE_DOWNLOAD);
            if (movieStringArray != null) {

                SerializableList<MovieDto> newMovieList = JsonDataToMovieConverter.GetList(movieStringArray);
                if (newMovieList != null) {
                    _movieList = newMovieList;
                    _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_MOVIE,
                            new String[]{Bundles.MOVIE_LIST}, new Object[]{_movieList});
                } else {
                    _logger.Warn("newMovieList is null");
                }
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

                SerializableList<ScheduleDto> newScheduleList = JsonDataToScheduleConverter.GetList(scheduleStringArray,
                        _wirelessSocketList);
                if (newScheduleList != null) {
                    _scheduleList = newScheduleList;
                    _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_SCHEDULE,
                            new String[]{Bundles.SCHEDULE_LIST, Bundles.SOCKET_LIST},
                            new Object[]{_scheduleList, _wirelessSocketList});

                    sendSchedulesToWear();
                } else {
                    _logger.Warn("newScheduleList is null");
                }

                SerializableList<TimerDto> newTimerList = JsonDataToTimerConverter.GetList(scheduleStringArray,
                        _wirelessSocketList);
                if (newTimerList != null) {
                    _timerList = newTimerList;
                    _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_TIMER,
                            new String[]{Bundles.TIMER_LIST, Bundles.SOCKET_LIST},
                            new Object[]{_timerList, _wirelessSocketList});
                } else {
                    _logger.Warn("newTimerList is null");
                }
            }
            updateDownloadCount();
        }
    };

    private BroadcastReceiver _shoppingListDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_shoppingListDownloadReceiver onReceive");

            String[] shoppingListStringArray = intent.getStringArrayExtra(Bundles.SHOPPING_LIST_DOWNLOAD);
            if (shoppingListStringArray != null) {

                SerializableList<ShoppingEntryDto> newShoppingList = JsonDataToShoppingListConverter
                        .GetList(shoppingListStringArray);
                if (newShoppingList != null) {
                    _shoppingList = newShoppingList;
                    _broadcastController.SendSerializableArrayBroadcast(
                            guepardoapps.library.lucahome.common.constants.Broadcasts.UPDATE_SHOPPING_LIST,
                            new String[]{guepardoapps.library.lucahome.common.constants.Bundles.SHOPPING_LIST},
                            new Object[]{_shoppingList});

                    _databaseController.ClearDatabaseShoppingList();
                    for (int index = 0; index < _shoppingList.getSize(); index++) {
                        _databaseController.SaveShoppingEntry(_shoppingList.getValue(index));
                    }

                    sendShoppingListToWear();
                } else {
                    _logger.Warn("newShoppingList is null");
                }
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

                SerializableList<WirelessSocketDto> newWirelessSocketList = JsonDataToSocketConverter
                        .GetList(socketStringArray);
                if (newWirelessSocketList != null) {
                    _wirelessSocketList = newWirelessSocketList;
                    installNotificationSettings();
                } else {
                    _logger.Warn("newWirelessSocketList is null");
                }

                if (_wirelessSocketList != null) {
                    _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_SOCKET,
                            new String[]{Bundles.SOCKET_LIST}, new Object[]{_wirelessSocketList});
                    _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_MAP_CONTENT_VIEW,
                            new String[]{Bundles.MAP_CONTENT_LIST, Bundles.SOCKET_LIST, Bundles.SCHEDULE_LIST,
                                    Bundles.TIMER_LIST, Bundles.TEMPERATURE_LIST, Bundles.SHOPPING_LIST, Bundles.MENU,
                                    Bundles.LISTED_MENU, Bundles.MOTION_CAMERA_DTO},
                            new Object[]{_mapContentList, _wirelessSocketList, _scheduleList, _timerList,
                                    _temperatureList, _shoppingList, _menu, _listedMenu, _motionCameraDto});

                    sendSocketsToWear();

                    if (_sharedPrefController
                            .LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_SOCKET_NOTIFICATION)) {
                        _notificationController.CreateSocketNotification(_wirelessSocketList);
                    }

                    _databaseController.ClearDatabaseSocket();
                    for (int index = 0; index < _wirelessSocketList.getSize(); index++) {
                        _databaseController.SaveSocket(_wirelessSocketList.getValue(index));
                    }
                } else {
                    _logger.Warn("_wirelessSocketList is null");
                }
            }

            updateDownloadCount();
            startDownloadSchedule();
        }
    };

    private BroadcastReceiver _temperatureDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_temperatureDownloadReceiver onReceive");

            String[] temperatureStringArray = intent.getStringArrayExtra(Bundles.TEMPERATURE_DOWNLOAD);
            if (temperatureStringArray != null) {

                SerializableList<TemperatureDto> newTemperatureList = JsonDataToTemperatureConverter
                        .GetList(temperatureStringArray);
                if (newTemperatureList != null) {
                    _temperatureList = newTemperatureList;

                    if (_temperatureList.getSize() >= 1) {
                        sendTemperatureToWear();
                    } else {
                        _logger.Warn("Temperaturelist has size " + String.valueOf(_temperatureList.getSize()));
                    }
                } else {
                    _logger.Warn("newTemperatureList is null!");
                }

                if (_temperatureList != null) {
                    _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_TEMPERATURE,
                            new String[]{Bundles.TEMPERATURE_LIST}, new Object[]{_temperatureList});
                }

                if (_sharedPrefController
                        .LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_TEMPERATURE_NOTIFICATION)) {
                    _notificationController.CreateTemperatureNotification(SensorTemperatureView.class, _temperatureList,
                            _currentWeather);
                }
            }
            updateDownloadCount();
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
                            && calendar.get(Calendar.MINUTE) < SLEEP_NOTIFICATION_MINUTE
                            + SLEEP_NOTIFICATION_TIME_SPAN_MINUTE) {
                        _logger.Debug("We are in the timeSpan!");

                        // Check also if we are in the home network, otherwise
                        // it's senseless to display the notification, but we
                        // set a flag to display notification later
                        if (_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
                            _logger.Debug("Showing notification go to sleep!");
                            _notificationController.CreateSleepHeatingNotification();
                        } else {
                            _logger.Debug("Saving flag to display notification later!");
                            _sharedPrefController
                                    .SaveBooleanValue(SharedPrefConstants.DISPLAY_SLEEP_NOTIFICATION_ACTIVE, true);
                        }
                    } else {
                        _logger.Debug("We are NOT in the timeSpan!");
                    }
                }
                // Check if time is ready to delete the flag to display the
                // sleep notification
                else if ((int) calendar.get(Calendar.HOUR_OF_DAY) == CLEAR_NOTIFICATION_DISPLAY_HOUR) {
                    if ((int) calendar.get(Calendar.MINUTE) == CLEAR_NOTIFICATION_DISPLAY_MINUTE) {
                        if (_sharedPrefController.LoadBooleanValueFromSharedPreferences(
                                SharedPrefConstants.DISPLAY_SLEEP_NOTIFICATION_ACTIVE)) {
                            _logger.Debug("We entered home and flag is active to display sleep notification");
                            _sharedPrefController
                                    .SaveBooleanValue(SharedPrefConstants.DISPLAY_SLEEP_NOTIFICATION_ACTIVE, false);
                        }
                    }
                }
            }
        }
    };

    private BroadcastReceiver _weatherModelReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_weatherModelReceiver onReceive");

            WeatherModel newWeather = (WeatherModel) intent.getSerializableExtra(OWBundles.EXTRA_WEATHER_MODEL);
            if (newWeather != null) {
                _currentWeather = newWeather;
                sendWeatherToWear();
            }

            if (_sharedPrefController
                    .LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_TEMPERATURE_NOTIFICATION)) {
                _notificationController.CreateTemperatureNotification(SensorTemperatureView.class, _temperatureList,
                        _currentWeather);
            }

            _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_WEATHER_VIEW,
                    new String[]{Bundles.WEATHER_CURRENT}, new Object[]{_currentWeather});

            updateDownloadCount();
        }
    };

    private BroadcastReceiver _addReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_addReceiver onReceive");

            LucaObject lucaObject = (LucaObject) intent.getSerializableExtra(Bundles.LUCA_OBJECT);
            if (lucaObject == null) {
                _logger.Error("_addReceiver received data with null lucaobject!");
                return;
            }

            String action = intent.getStringExtra(Bundles.ACTION);
            if (action == null) {
                _logger.Error("_addReceiver received data with null action!");
                return;
            }

            switch (lucaObject) {
                case BIRTHDAY:
                    _serviceController.StartRestService(Bundles.BIRTHDAY_DOWNLOAD, action, Broadcasts.ADD_BIRTHDAY,
                            lucaObject, RaspberrySelection.BOTH);
                    break;
                case MOVIE:
                    _serviceController.StartRestService(Bundles.MOVIE_DOWNLOAD, action, Broadcasts.ADD_MOVIE, lucaObject,
                            RaspberrySelection.BOTH);
                    break;
                case SCHEDULE:
                case TIMER:
                    _serviceController.StartRestService(Bundles.SCHEDULE_DOWNLOAD, action, Broadcasts.ADD_SCHEDULE,
                            lucaObject, RaspberrySelection.BOTH);
                    break;
                case WIRELESS_SOCKET:
                    _serviceController.StartRestService(Bundles.SOCKET_DOWNLOAD, action, Broadcasts.ADD_SOCKET, lucaObject,
                            RaspberrySelection.BOTH);
                    break;
                default:
                    _logger.Error("Cannot add object: " + lucaObject.toString());
                    break;
            }
        }
    };

    private BroadcastReceiver _reloadBirthdayReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_reloadBirthdayReceiver onReceive");
            startDownloadBirthday();
        }
    };

    private BroadcastReceiver _reloadMediaMirrorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_reloadMediaMirrorReceiver onReceive");
            startDownloadMediaMirror();
        }
    };

    private BroadcastReceiver _reloadListedMenuReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_reloadListedMenuReceiver onReceive");
            startDownloadListedMenu();
        }
    };

    private BroadcastReceiver _reloadMenuReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_reloadMenuReceiver onReceive");
            startDownloadMenu();
        }
    };

    private BroadcastReceiver _reloadMotionCameraDtoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_reloadMotionCameraDtoReceiver onReceive");
            startDownloadMotionCameraDto();
        }
    };

    private BroadcastReceiver _reloadMovieReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_reloadMovieReceiver onReceive");
            startDownloadMovie();
        }
    };

    private BroadcastReceiver _reloadScheduleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_reloadScheduleReceiver onReceive");
            startDownloadSchedule();
        }
    };

    private BroadcastReceiver _reloadSocketReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_reloadSocketReceiver onReceive");
            startDownloadSocket();
        }
    };

    private BroadcastReceiver _reloadChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_reloadChangeReceiver onReceive");
            startDownloadChange();
        }
    };

    private BroadcastReceiver _reloadShoppingListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_reloadShoppingListReceiver onReceive");
            startDownloadShoppingList();
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

    private BroadcastReceiver _startDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_startDownloadReceiver onReceive");

            LucaObject lucaObject = (LucaObject) intent.getSerializableExtra(Bundles.LUCA_OBJECT);
            if (lucaObject == null) {
                _logger.Error("_downloadReceiver received data with null lucaobject!");
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
                    boolean addBirthdaySuccess = true;
                    for (String answer : birthdayAnswerArray) {
                        _logger.Debug(answer);
                        if (!answer.endsWith("1")) {
                            addBirthdaySuccess = false;
                            break;
                        }
                    }
                    if (addBirthdaySuccess) {
                        startDownloadBirthday();
                    } else {
                        _logger.Warn("Add of birthday failed!");
                        Toasty.error(_context, "Add of birthday failed!", Toast.LENGTH_LONG).show();
                    }
                    break;
                case MENU:
                    String[] menuAnswerArray = intent.getStringArrayExtra(Bundles.MENU_DOWNLOAD);
                    boolean addMenuSuccess = true;
                    for (String answer : menuAnswerArray) {
                        _logger.Debug(answer);
                        if (!answer.endsWith("1")) {
                            addMenuSuccess = false;
                            break;
                        }
                    }
                    if (addMenuSuccess) {
                        startDownloadMenu();
                    } else {
                        _logger.Warn("Add of menu failed!");
                        Toasty.error(_context, "Add of menu failed!", Toast.LENGTH_LONG).show();
                    }
                    break;
                case MOVIE:
                    String[] movieAnswerArray = intent.getStringArrayExtra(Bundles.MOVIE_DOWNLOAD);
                    boolean addMovieSuccess = true;
                    for (String answer : movieAnswerArray) {
                        _logger.Debug(answer);
                        if (!answer.endsWith("1")) {
                            addMovieSuccess = false;
                            break;
                        }
                    }
                    if (addMovieSuccess) {
                        startDownloadMovie();
                    } else {
                        _logger.Warn("Add of movie failed!");
                        Toasty.error(_context, "Add of movie failed!", Toast.LENGTH_LONG).show();
                    }
                    break;
                case SCHEDULE:
                case TIMER:
                    String[] scheduleAnswerArray = intent.getStringArrayExtra(Bundles.SCHEDULE_DOWNLOAD);
                    boolean addScheduleSuccess = true;
                    for (String answer : scheduleAnswerArray) {
                        _logger.Debug(answer);
                        if (!answer.endsWith("1")) {
                            addScheduleSuccess = false;
                            break;
                        }
                    }
                    if (addScheduleSuccess) {
                        startDownloadSchedule();
                    } else {
                        _logger.Warn("Add of schedule failed!");
                        Toasty.error(_context, "Add of schedule failed!", Toast.LENGTH_LONG).show();
                    }
                    break;
                case WIRELESS_SOCKET:
                    String[] socketAnswerArray = intent.getStringArrayExtra(Bundles.SOCKET_DOWNLOAD);
                    boolean addSocketSuccess = true;
                    for (String answer : socketAnswerArray) {
                        _logger.Debug(answer);
                        if (!answer.endsWith("1")) {
                            addSocketSuccess = false;
                            break;
                        }
                    }
                    if (addSocketSuccess) {
                        startDownloadSocket();
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
    };

    private BroadcastReceiver _updateBoughtShoppingListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateBoughtShoppingListReceiver onReceive");

            String data = intent.getStringExtra(guepardoapps.library.lucahome.common.constants.Bundles.SHOPPING_LIST);
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
                                    guepardoapps.library.lucahome.common.constants.Broadcasts.UPDATE_SHOPPING_LIST,
                                    new String[]{
                                            guepardoapps.library.lucahome.common.constants.Bundles.SHOPPING_LIST},
                                    new Object[]{_shoppingList});

                            _databaseController.ClearDatabaseShoppingList();
                            for (int index = 0; index < _shoppingList.getSize(); index++) {
                                _databaseController.SaveShoppingEntry(_shoppingList.getValue(index));
                            }

                            sendShoppingListToWear();

                            break;
                        }
                    }
                    _logger.Warn(String.format("Found no matching entry with name %s to update!", name));
                } else {
                    _logger.Warn(String.format("Invalid length %s for content %s", content.length, content));
                }
            } else {
                _logger.Warn("Received null data in _updateBoughtShoppingListReceiver");
            }
        }
    };

    private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateReceiver onReceive");

            LucaObject lucaObject = (LucaObject) intent.getSerializableExtra(Bundles.LUCA_OBJECT);
            if (lucaObject == null) {
                _logger.Error("_updateReceiver received data with null lucaobject!");
                return;
            }

            String action = intent.getStringExtra(Bundles.ACTION);
            if (action == null) {
                _logger.Error("_updateReceiver received data with null action!");
                return;
            }

            switch (lucaObject) {
                case BIRTHDAY:
                    _serviceController.StartRestService(Bundles.BIRTHDAY_DOWNLOAD, action, Broadcasts.UPDATE_BIRTHDAY,
                            lucaObject, RaspberrySelection.BOTH);
                    break;
                case MENU:
                    _serviceController.StartRestService(Bundles.MENU_DOWNLOAD, action, Broadcasts.UPDATE_MENU, lucaObject,
                            RaspberrySelection.BOTH);
                    break;
                case MOVIE:
                    _serviceController.StartRestService(Bundles.MOVIE_DOWNLOAD, action, Broadcasts.UPDATE_MOVIE, lucaObject,
                            RaspberrySelection.BOTH);
                    break;
                case SCHEDULE:
                case TIMER:
                    _serviceController.StartRestService(Bundles.SCHEDULE_DOWNLOAD, action, Broadcasts.UPDATE_SCHEDULE,
                            lucaObject, RaspberrySelection.BOTH);
                    break;
                case WIRELESS_SOCKET:
                    _serviceController.StartRestService(Bundles.SOCKET_DOWNLOAD, action, Broadcasts.UPDATE_SOCKET,
                            lucaObject, RaspberrySelection.BOTH);
                    break;
                default:
                    _logger.Error("Cannot update object: " + lucaObject.toString());
                    break;
            }
        }
    };

    private Runnable _downloadBirthdayRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_downloadBirthdayRunnable run");

            if (!_networkController.IsNetworkAvailable()) {
                _logger.Warn("No network available!");
                return;
            }

            if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
                _logger.Warn("No LucaHome network! ...");
                return;
            }

            startDownloadBirthday();
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

            startDownloadCurrentWeather();
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

            if (_sharedPrefController
                    .LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_WEATHER_NOTIFICATION)) {
                _openWeatherController.LoadForecastWeather();
            }
        }
    };

    private Runnable _downloadListedMenuRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_downloadListedMenuRunnable run");

            if (!_networkController.IsNetworkAvailable()) {
                _logger.Warn("No network available!");
                _listedMenu = _databaseController.GetListedMenuList();
                return;
            }

            if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
                _logger.Warn("No LucaHome network! ...");
                _listedMenu = _databaseController.GetListedMenuList();
                return;
            }

            startDownloadListedMenu();
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

            startDownloadMediaMirror();
        }
    };

    private Runnable _downloadMenuRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_downloadMenuRunnable run");

            if (!_networkController.IsNetworkAvailable()) {
                _logger.Warn("No network available!");
                _menu = _databaseController.GetMenuList();
                return;
            }

            if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
                _logger.Warn("No LucaHome network! ...");
                _menu = _databaseController.GetMenuList();
                return;
            }

            startDownloadMenu();
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

            startDownloadMotionCameraDto();
        }
    };

    private Runnable _downloadShoppingListRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_downloadShoppingListRunnable run");

            if (!_networkController.IsNetworkAvailable()) {
                _logger.Warn("No network available!");
                _shoppingList = _databaseController.GetShoppingList();
                return;
            }

            if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
                _logger.Warn("No LucaHome network! ...");
                _shoppingList = _databaseController.GetShoppingList();
                return;
            }

            startDownloadShoppingList();
        }
    };

    private Runnable _downloadSocketRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_downloadSocketRunnable run");

            if (!_networkController.IsNetworkAvailable()) {
                _logger.Warn("No network available!");
                _wirelessSocketList = _databaseController.GetSocketList();
                return;
            }

            if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
                _logger.Warn("No LucaHome network! ...");
                _wirelessSocketList = _databaseController.GetSocketList();
                return;
            }

            startDownloadSocket();
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

            startDownloadCurrentWeather();

            if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
                _logger.Warn("No LucaHome network! ...");
                return;
            }

            startDownloadTemperature();
        }
    };

    private Runnable _timeoutCheck = new Runnable() {
        public void run() {
            _logger.Warn("_timeoutCheck received!");
            _broadcastController.SendSerializableArrayBroadcast(Broadcasts.COMMAND,
                    new String[]{Bundles.COMMAND, Bundles.NAVIGATE_DATA},
                    new Object[]{Command.NAVIGATE, NavigateData.FINISH});
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
            _databaseController = new DatabaseController(_context);
            _databaseController.Initialize();
            _mediaMirrorController = new MediaMirrorController(_context);
            _mediaMirrorController.Initialize();
            _menuController = new MenuController(_context);
            _networkController = new NetworkController(_context,
                    new DialogController(_context, ContextCompat.getColor(_context, R.color.TextIcon),
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
            MainServiceAction action = (MainServiceAction) data.getSerializable(Bundles.MAIN_SERVICE_ACTION);
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
        _receiverController.RegisterReceiver(_actionReceiver, new String[]{Broadcasts.MAIN_SERVICE_COMMAND});
        _receiverController.RegisterReceiver(_birthdayDownloadReceiver,
                new String[]{Broadcasts.DOWNLOAD_BIRTHDAY_FINISHED});
        _receiverController.RegisterReceiver(_changeDownloadReceiver,
                new String[]{Broadcasts.DOWNLOAD_CHANGE_FINISHED});
        _receiverController.RegisterReceiver(_forecastModelReceiver,
                new String[]{OWBroadcasts.FORECAST_WEATHER_JSON_FINISHED});
        _receiverController.RegisterReceiver(_informationDownloadReceiver,
                new String[]{Broadcasts.DOWNLOAD_INFORMATION_FINISHED});
        _receiverController.RegisterReceiver(_mapContentDownloadReceiver,
                new String[]{Broadcasts.DOWNLOAD_MAP_CONTENT_FINISHED});
        _receiverController.RegisterReceiver(_mediaMirrorViewDtoDownloadReceiver,
                new String[]{guepardoapps.library.lucahome.common.constants.Broadcasts.MEDIAMIRROR_VIEW_DTO});
        _receiverController.RegisterReceiver(_listedMenuDtoDownloadReceiver,
                new String[]{Broadcasts.DOWNLOAD_LISTED_MENU_FINISHED});
        _receiverController.RegisterReceiver(_menuDtoDownloadReceiver,
                new String[]{Broadcasts.DOWNLOAD_MENU_FINISHED});
        _receiverController.RegisterReceiver(_motionCameraDtoDownloadReceiver,
                new String[]{Broadcasts.DOWNLOAD_MOTION_CAMERA_DTO_FINISHED});
        _receiverController.RegisterReceiver(_movieDownloadReceiver,
                new String[]{Broadcasts.DOWNLOAD_MOVIE_FINISHED});
        _receiverController.RegisterReceiver(_scheduleDownloadReceiver,
                new String[]{Broadcasts.DOWNLOAD_SCHEDULE_FINISHED});
        _receiverController.RegisterReceiver(_socketDownloadReceiver,
                new String[]{Broadcasts.DOWNLOAD_SOCKET_FINISHED});
        _receiverController.RegisterReceiver(_temperatureDownloadReceiver,
                new String[]{Broadcasts.DOWNLOAD_TEMPERATURE_FINISHED});
        _receiverController.RegisterReceiver(_weatherModelReceiver,
                new String[]{OWBroadcasts.CURRENT_WEATHER_JSON_FINISHED});
        _receiverController.RegisterReceiver(_shoppingListDownloadReceiver, new String[]{
                guepardoapps.library.lucahome.common.constants.Broadcasts.DOWNLOAD_SHOPPING_LIST_FINISHED});

        _receiverController.RegisterReceiver(_addReceiver, new String[]{Broadcasts.ADD_BIRTHDAY, Broadcasts.ADD_MOVIE,
                Broadcasts.ADD_SCHEDULE, Broadcasts.ADD_SOCKET});
        _receiverController.RegisterReceiver(_updateReceiver, new String[]{Broadcasts.UPDATE_BIRTHDAY,
                Broadcasts.UPDATE_MOVIE, Broadcasts.UPDATE_SCHEDULE, Broadcasts.UPDATE_SOCKET});
        _receiverController.RegisterReceiver(_startDownloadReceiver,
                new String[]{Broadcasts.ADD_BIRTHDAY, Broadcasts.ADD_MOVIE, Broadcasts.ADD_SCHEDULE,
                        Broadcasts.ADD_SOCKET, Broadcasts.UPDATE_BIRTHDAY, Broadcasts.UPDATE_MENU,
                        Broadcasts.UPDATE_MOVIE, Broadcasts.UPDATE_SCHEDULE, Broadcasts.UPDATE_SOCKET});

        _receiverController.RegisterReceiver(_reloadBirthdayReceiver, new String[]{Broadcasts.RELOAD_BIRTHDAY});
        _receiverController.RegisterReceiver(_reloadMediaMirrorReceiver,
                new String[]{guepardoapps.library.lucahome.common.constants.Broadcasts.RELOAD_MEDIAMIRROR});
        _receiverController.RegisterReceiver(_reloadListedMenuReceiver, new String[]{Broadcasts.RELOAD_LISTED_MENU});
        _receiverController.RegisterReceiver(_reloadMenuReceiver, new String[]{Broadcasts.RELOAD_MENU});
        _receiverController.RegisterReceiver(_reloadMotionCameraDtoReceiver,
                new String[]{Broadcasts.RELOAD_MOTION_CAMERA_DTO});
        _receiverController.RegisterReceiver(_reloadMovieReceiver, new String[]{Broadcasts.RELOAD_MOVIE});
        _receiverController.RegisterReceiver(_reloadScheduleReceiver,
                new String[]{Broadcasts.RELOAD_SCHEDULE, Broadcasts.RELOAD_TIMER});
        _receiverController.RegisterReceiver(_reloadSocketReceiver, new String[]{Broadcasts.RELOAD_SOCKETS});
        _receiverController.RegisterReceiver(_reloadChangeReceiver,
                new String[]{Broadcasts.RELOAD_BIRTHDAY, Broadcasts.RELOAD_MOVIE, Broadcasts.RELOAD_SCHEDULE,
                        Broadcasts.RELOAD_TIMER, Broadcasts.RELOAD_SOCKETS});
        _receiverController.RegisterReceiver(_reloadShoppingListReceiver,
                new String[]{guepardoapps.library.lucahome.common.constants.Broadcasts.RELOAD_SHOPPING_LIST});
        _receiverController.RegisterReceiver(_reloadCurrentWeatherReceiver,
                new String[]{Broadcasts.RELOAD_CURRENT_WEATHER});
        _receiverController.RegisterReceiver(_reloadForecastWeatherReceiver,
                new String[]{Broadcasts.RELOAD_FORECAST_WEATHER});

        _receiverController.RegisterReceiver(_updateBoughtShoppingListReceiver,
                new String[]{guepardoapps.library.lucahome.common.constants.Broadcasts.UPDATE_BOUGHT_SHOPPING_LIST});

        _receiverController.RegisterReceiver(_batteryChangedReceiver,
                new String[]{Intent.ACTION_BATTERY_CHANGED});
        _receiverController.RegisterReceiver(_timeTickReceiver, new String[]{Intent.ACTION_TIME_TICK});

        _receiverController.RegisterReceiver(_forecastReceiver,
                new String[]{OWBroadcasts.FORECAST_WEATHER_JSON_FINISHED});
    }

    private void addSchedules() {
        _scheduleService.AddSchedule("UpdateBirthday", _downloadBirthdayRunnable, 4 * 60 * 60 * 1000, true);
        _scheduleService.AddSchedule("UpdateForecast", _downloadCurrentWeatherRunnable, 15 * 60 * 1000, true);
        _scheduleService.AddSchedule("UpdateForecast", _downloadForecastWeatherRunnable, 30 * 60 * 1000, true);
        _scheduleService.AddSchedule("UpdateMediaMirror", _downloadMediaMirrorRunnable, 5 * 60 * 1000, true);
        _scheduleService.AddSchedule("UpdateMenu", _downloadMenuRunnable, 3 * 60 * 60 * 1000, true);
        _scheduleService.AddSchedule("UpdateListedMenu", _downloadListedMenuRunnable, 3 * 60 * 60 * 1000, true);
        _scheduleService.AddSchedule("UpdateMotionCameraDto", _downloadMotionCameraDtoRunnable, 5 * 60 * 1000, true);
        _scheduleService.AddSchedule("UpdateSockets", _downloadSocketRunnable, 15 * 60 * 1000, true);
        _scheduleService.AddSchedule("UpdateTemperature", _downloadTemperatureRunnable, 5 * 60 * 1000, true);
        _scheduleService.AddSchedule("UpdateShoppingList", _downloadShoppingListRunnable, 30 * 60 * 1000, true);
        _schedulesAdded = true;
    }

    private void boot() {
        if (!_sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.SHARED_PREF_INSTALLED)) {
            install();
        }

        if (!_downloadingData) {
            if (!_sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.USER_DATA_ENTERED)) {
                _broadcastController.SendSerializableArrayBroadcast(Broadcasts.COMMAND,
                        new String[]{Bundles.COMMAND}, new Object[]{Command.SHOW_USER_LOGIN_DIALOG});
            } else {
                Calendar now = Calendar.getInstance();
                if (_lastUpdate != null) {
                    long difference = Calendar.getInstance().getTimeInMillis() - _lastUpdate.getTimeInMillis();

                    if (difference < 60 * 1000) {
                        _logger.Warn("Just updated the data!");

                        _broadcastController.SendSerializableArrayBroadcast(Broadcasts.COMMAND,
                                new String[]{Bundles.COMMAND, Bundles.NAVIGATE_DATA},
                                new Object[]{Command.NAVIGATE, NavigateData.HOME});

                        return;
                    }
                }
                _lastUpdate = now;

                if (!_schedulesAdded) {
                    addSchedules();
                }

                prepareDownloadAll();
                startDownloadLucaHomeAll();
                startDownloadOtherAll();
            }
        }
    }

    private void prepareDownloadAll() {
        _logger.Debug("prepareDownloadAll");

        _downloadingData = true;
        _progress = 0;
        _downloadCount = Constants.DOWNLOAD_STEPS;

        startTimeout();
    }

    private void startDownloadLucaHomeAll() {
        _logger.Debug("startDownloadLucaHomeAll");

        startDownloadBirthday();
        startDownloadChange();
        startDownloadInformation();
        startDownloadMapContent();
        startDownloadMediaMirror();
        startDownloadListedMenu();
        startDownloadMenu();
        startDownloadMotionCameraDto();
        startDownloadMovie();
        startDownloadSocket();
        startDownloadTemperature();
        startDownloadShoppingList();
    }

    private void startDownloadOtherAll() {
        _logger.Debug("startDownloadOtherAll");

        startDownloadCurrentWeather();
        startDownloadForecastWeather();

        if (_sharedPrefController
                .LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_WEATHER_NOTIFICATION)) {
            _openWeatherController.LoadForecastWeather();
        }
    }

    private void updateDownloadCount() {
        _logger.Debug("updateDownloadCount");

        _progress++;
        _broadcastController.SendIntBroadcast(Broadcasts.UPDATE_PROGRESSBAR, Bundles.VIEW_PROGRESS, _progress);
        if (_progress >= _downloadCount) {
            stopTimeout();
            _broadcastController.SendSerializableArrayBroadcast(Broadcasts.COMMAND,
                    new String[]{Bundles.COMMAND, Bundles.NAVIGATE_DATA},
                    new Object[]{Command.NAVIGATE, NavigateData.HOME});
        }
    }

    private void install() {
        _logger.Debug("install");

        _broadcastController.SendSerializableBroadcast(Broadcasts.COMMAND, Bundles.COMMAND,
                Command.SHOW_USER_LOGIN_DIALOG);

        _sharedPrefController.SaveBooleanValue(SharedPrefConstants.SHARED_PREF_INSTALLED, true);

        _sharedPrefController.SaveBooleanValue(SharedPrefConstants.DISPLAY_SOCKET_NOTIFICATION, true);
        _sharedPrefController.SaveBooleanValue(SharedPrefConstants.DISPLAY_WEATHER_NOTIFICATION, true);
        _sharedPrefController.SaveBooleanValue(SharedPrefConstants.DISPLAY_TEMPERATURE_NOTIFICATION, true);
        _sharedPrefController.SaveBooleanValue(SharedPrefConstants.DISPLAY_BIRTHDAY_NOTIFICATION, true);
        _sharedPrefController.SaveBooleanValue(SharedPrefConstants.DISPLAY_SLEEP_NOTIFICATION, true);
        _sharedPrefController.SaveBooleanValue(SharedPrefConstants.DISPLAY_CAMERA_NOTIFICATION, true);

        _sharedPrefController.SaveBooleanValue(SharedPrefConstants.START_AUDIO_APP, true);
        _sharedPrefController.SaveBooleanValue(SharedPrefConstants.START_OSMC_APP, true);

        _sharedPrefController.SaveIntegerValue(SharedPrefConstants.SOUND_RASPBERRY_SELECTION,
                RaspberrySelection.RASPBERRY_1.GetInt());

        _sharedPrefController.SaveBooleanValue(SharedPrefConstants.USE_BEACONS, false);
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
                _logger.Info(String.format("Installing key %s", key));
                _sharedPrefController.SaveBooleanValue(key, true);
            } else {
                _logger.Info(String.format("Key %s already exists and is %s", key,
                        _sharedPrefController.LoadBooleanValueFromSharedPreferences(key)));
            }
        }
    }

    private void startDownloadBirthday() {
        _logger.Debug("startDownloadBirthday");
        _serviceController.StartRestService(Bundles.BIRTHDAY_DOWNLOAD, ServerActions.GET_BIRTHDAYS,
                Broadcasts.DOWNLOAD_BIRTHDAY_FINISHED, LucaObject.BIRTHDAY, RaspberrySelection.BOTH);
    }

    private void startDownloadChange() {
        _logger.Debug("startDownloadChange");
        _serviceController.StartRestService(Bundles.CHANGE_DOWNLOAD, ServerActions.GET_CHANGES,
                Broadcasts.DOWNLOAD_CHANGE_FINISHED, LucaObject.CHANGE, RaspberrySelection.BOTH);
    }

    private void startDownloadCurrentWeather() {
        _logger.Debug("startDownloadCurrentWeather");
        _openWeatherController.LoadCurrentWeather();
    }

    private void startDownloadForecastWeather() {
        _logger.Debug("startDownloadForecastWeather");
        _openWeatherController.LoadForecastWeather();
    }

    private void startDownloadInformation() {
        _logger.Debug("startDownloadInformation");
        _serviceController.StartRestService(Bundles.INFORMATION_DOWNLOAD, ServerActions.GET_INFORMATIONS,
                Broadcasts.DOWNLOAD_INFORMATION_FINISHED, LucaObject.INFORMATION, RaspberrySelection.BOTH);
    }

    private void startDownloadMapContent() {
        _logger.Debug("startDownloadMapContent");
        _serviceController.StartRestService(Bundles.MAP_CONTENT_DOWNLOAD, ServerActions.GET_MAP_CONTENTS,
                Broadcasts.DOWNLOAD_MAP_CONTENT_FINISHED, LucaObject.MAP_CONTENT, RaspberrySelection.BOTH);
    }

    private void startDownloadMediaMirror() {
        _logger.Debug("startDownloadMediaMirror");
        for (final MediaMirrorSelection entry : MediaMirrorSelection.values()) {
            if (entry.GetId() == 0) {
                continue;
            }

            _logger.Debug(String.format("Trying serverIp %s", entry.GetIp()));
            _mediaMirrorController.SendCommand(entry.GetIp(), ServerAction.GET_MEDIAMIRROR_DTO.toString(), "");
        }
    }

    private void startDownloadListedMenu() {
        _logger.Debug("startDownloadListedMenu");
        _serviceController.StartRestService(Bundles.LISTED_MENU_DOWNLOAD, ServerActions.GET_LISTED_MENU,
                Broadcasts.DOWNLOAD_LISTED_MENU_FINISHED, LucaObject.LISTEDMENU, RaspberrySelection.BOTH);
    }

    private void startDownloadMenu() {
        _logger.Debug("startDownloadMenu");
        _serviceController.StartRestService(Bundles.MENU_DOWNLOAD, ServerActions.GET_MENU,
                Broadcasts.DOWNLOAD_MENU_FINISHED, LucaObject.MENU, RaspberrySelection.BOTH);
    }

    private void startDownloadMotionCameraDto() {
        _logger.Debug("startDownloadMotionCameraDto");
        _serviceController.StartRestService(Bundles.MOTION_CAMERA_DTO_DOWNLOAD, ServerActions.GET_MOTION_DATA,
                Broadcasts.DOWNLOAD_MOTION_CAMERA_DTO_FINISHED, LucaObject.MOTION_CAMERA_DTO, RaspberrySelection.BOTH);
    }

    private void startDownloadMovie() {
        _logger.Debug("startDownloadMovie");
        _serviceController.StartRestService(Bundles.MOVIE_DOWNLOAD, ServerActions.GET_MOVIES,
                Broadcasts.DOWNLOAD_MOVIE_FINISHED, LucaObject.MOVIE, RaspberrySelection.BOTH);
    }

    private void startDownloadSchedule() {
        _logger.Debug("startDownloadSchedule");
        _serviceController.StartRestService(Bundles.SCHEDULE_DOWNLOAD, ServerActions.GET_SCHEDULES,
                Broadcasts.DOWNLOAD_SCHEDULE_FINISHED, LucaObject.SCHEDULE, RaspberrySelection.BOTH);
    }

    private void startDownloadShoppingList() {
        _logger.Debug("startDownloadShoppingList");
        _serviceController.StartRestService(Bundles.SHOPPING_LIST_DOWNLOAD, ServerActions.GET_SHOPPING_LIST,
                guepardoapps.library.lucahome.common.constants.Broadcasts.DOWNLOAD_SHOPPING_LIST_FINISHED,
                LucaObject.SHOPPING_ENTRY, RaspberrySelection.BOTH);
    }

    private void startDownloadSocket() {
        _logger.Debug("startDownloadSocket");
        _serviceController.StartRestService(Bundles.SOCKET_DOWNLOAD, ServerActions.GET_SOCKETS,
                Broadcasts.DOWNLOAD_SOCKET_FINISHED, LucaObject.WIRELESS_SOCKET, RaspberrySelection.BOTH);
    }

    private void startDownloadTemperature() {
        _logger.Debug("startDownloadTemperature");
        _serviceController.StartRestService(Bundles.TEMPERATURE_DOWNLOAD, ServerActions.GET_TEMPERATURES,
                Broadcasts.DOWNLOAD_TEMPERATURE_FINISHED, LucaObject.TEMPERATURE, RaspberrySelection.BOTH);
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
            messageText += _mediaMirrorList.getValue(index).GetMediaMirrorSelection().GetIp() + ":"
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
            messageText += entry.GetName() + ":" + entry.GetGroup().toString() + ":" + entry.GetQuantity() + ":"
                    + (entry.GetBought() ? "1" : "0") + "&";
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
            messageText += _timerList.getValue(index).GetName() + ":" + information + ":"
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

        String messageText = "CurrentWeather:DESCRIPTION:" + _currentWeather.GetDescription() + "&TEMPERATURE:"
                + _currentWeather.GetTemperatureString() + "&LASTUPDATE:"
                + _currentWeather.GetLastUpdate().toMilliSecond() + "&SUNRISE:"
                + String.valueOf(_currentWeather.GetSunriseTime().toMilliSecond()) + "&SUNSET:"
                + String.valueOf(_currentWeather.GetSunsetTime().toMilliSecond());

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
                _serviceController.StartRestService(entry.GetName(), entry.GetAction(), entry.GetBroadcast(),
                        LucaObject.WIRELESS_SOCKET, RaspberrySelection.BOTH);
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

        if (_sharedPrefController
                .LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_SLEEP_NOTIFICATION_ACTIVE)) {
            _notificationController.CreateSleepHeatingNotification();
            _sharedPrefController.SaveBooleanValue(SharedPrefConstants.DISPLAY_SLEEP_NOTIFICATION_ACTIVE, false);
        }
    }
}