package guepardoapps.lucahome.services;

import java.util.Calendar;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.constants.*;
import guepardoapps.lucahome.view.BirthdayView;
import guepardoapps.lucahome.view.SensorTemperatureView;
import guepardoapps.lucahomelibrary.common.classes.*;
import guepardoapps.lucahomelibrary.common.constants.IDs;
import guepardoapps.lucahomelibrary.common.constants.ServerActions;
import guepardoapps.lucahomelibrary.common.controller.LucaDialogController;
import guepardoapps.lucahomelibrary.common.controller.LucaNotificationController;
import guepardoapps.lucahomelibrary.common.controller.ServiceController;
import guepardoapps.lucahomelibrary.common.converter.json.*;
import guepardoapps.lucahomelibrary.common.dto.*;
import guepardoapps.lucahomelibrary.common.enums.*;
import guepardoapps.lucahomelibrary.common.tools.LucaHomeLogger;
import guepardoapps.lucahomelibrary.mediamirror.client.ClientTask;
import guepardoapps.lucahomelibrary.mediamirror.common.enums.ServerAction;
import guepardoapps.lucahomelibrary.services.sockets.SocketActionService;
import guepardoapps.lucahomelibrary.view.controller.MediaMirrorController;

import guepardoapps.toolset.controller.BroadcastController;
import guepardoapps.toolset.controller.DialogController;
import guepardoapps.toolset.controller.NetworkController;
import guepardoapps.toolset.controller.ReceiverController;
import guepardoapps.toolset.controller.SharedPrefController;

import guepardoapps.toolset.openweather.OpenWeatherController;
import guepardoapps.toolset.openweather.common.OpenWeatherConstants;
import guepardoapps.toolset.openweather.model.ForecastModel;
import guepardoapps.toolset.openweather.model.WeatherModel;

import guepardoapps.toolset.scheduler.ScheduleService;

public class MainService extends Service {

	private static final String TAG = MainService.class.getName();
	private LucaHomeLogger _logger;

	private boolean _isInitialized;
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
	private SerializableList<MovieDto> _movieList = null;
	private SerializableList<ScheduleDto> _scheduleList = null;
	private SerializableList<TemperatureDto> _temperatureList = null;
	private SerializableList<TimerDto> _timerList = null;
	private SerializableList<WirelessSocketDto> _wirelessSocketList = null;

	private Handler _timeoutHandler = new Handler();
	private int _timeCheck = 30000;

	private Context _context;

	private BroadcastController _broadcastController;
	private LucaDialogController _dialogController;
	private LucaNotificationController _notificationController;
	private NetworkController _networkController;
	private OpenWeatherController _openWeatherController;
	private ReceiverController _receiverController;
	private ScheduleService _scheduleService;
	private ServiceController _serviceController;
	private SharedPrefController _sharedPrefController;

	private Runnable _startDownloadRunnable = new Runnable() {
		@Override
		public void run() {
			_logger.Debug("_startDownloadRunnable run");

			if (!_networkController.IsNetworkAvailable()) {
				_logger.Warn("No network available!");
				return;
			}

			if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
				_logger.Warn("No LucaHome network! ...");
				return;
			}

			startDownloadAll();
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
				_context.startService(new Intent(_context, OpenWeatherService.class));
			}
		}
	};

	private Runnable _downloadIsSoundPlayingRunnable = new Runnable() {
		@Override
		public void run() {
			_logger.Debug("_downloadIsSoundPlayingRunnable run");

			if (!_networkController.IsNetworkAvailable()) {
				_logger.Warn("No network available!");
				return;
			}

			if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
				_logger.Warn("No LucaHome network! ...");
				return;
			}

			startDownloadIsSoundPlaying();
		}
	};

	private Runnable _downloadSocketRunnable = new Runnable() {
		@Override
		public void run() {
			_logger.Debug("_downloadSocketRunnable run");

			if (!_networkController.IsNetworkAvailable()) {
				_logger.Warn("No network available!");
				return;
			}

			if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
				_logger.Warn("No LucaHome network! ...");
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
					new String[] { Bundles.COMMAND, Bundles.NAVIGATE_DATA },
					new Object[] { Command.NAVIGATE, NavigateData.FINISH });
		}
	};

	private BroadcastReceiver _actionReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_actionReceiver onReceive");
			MainServiceAction action = (MainServiceAction) intent.getSerializableExtra(Bundles.MAIN_SERVICE_ACTION);
			if (action != null) {
				_progress = 0;
				switch (action) {
				case DOWNLOAD_ALL:
					_downloadCount = Constants.DOWNLOAD_STEPS;
					startDownloadAll();
					break;
				case DOWLOAD_SOCKETS:
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
				case GET_ALL:
					_broadcastController.SendSerializableArrayBroadcast(Broadcasts.GET_ALL,
							new String[] { Bundles.BIRTHDAY_LIST, Bundles.CHANGE_LIST, Bundles.INFORMATION_SINGLE,
									Bundles.MAP_CONTENT_LIST, Bundles.MOVIE_LIST, Bundles.SCHEDULE_LIST,
									Bundles.SOCKET_LIST, Bundles.TEMPERATURE_LIST, Bundles.TIMER_LIST,
									Bundles.WEATHER_CURRENT, Bundles.WEATHER_FORECAST },
							new Object[] { _birthdayList, _changeList, _information, _mapContentList, _movieList,
									_scheduleList, _wirelessSocketList, _temperatureList, _timerList, _currentWeather,
									_forecastWeather });
					break;
				case GET_BIRTHDAYS:
					_broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_BIRTHDAY,
							new String[] { Bundles.BIRTHDAY_LIST }, new Object[] { _birthdayList });
					sendBirthdaysToWear();
					break;
				case GET_CHANGES:
					_broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_CHANGE,
							new String[] { Bundles.CHANGE_LIST }, new Object[] { _changeList });
					break;
				case GET_INFORMATIONS:
					_logger.Debug("GET_INFORMATIONS");
					if (_information != null) {
						_logger.Debug(_information.toString());
					} else {
						_logger.Warn("Information is null!");
					}
					_broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_INFORMATION,
							new String[] { Bundles.INFORMATION_SINGLE }, new Object[] { _information });
					break;
				case GET_MOVIES:
					_broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_MOVIE,
							new String[] { Bundles.MOVIE_LIST }, new Object[] { _movieList });
					break;
				case GET_SCHEDULES:
					_broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_SCHEDULE,
							new String[] { Bundles.SCHEDULE_LIST, Bundles.SOCKET_LIST },
							new Object[] { _scheduleList, _wirelessSocketList });
					sendSchedulesToWear();
					break;
				case GET_SOCKETS:
					_broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_SOCKET,
							new String[] { Bundles.SOCKET_LIST }, new Object[] { _wirelessSocketList });
					sendSocketsToWear();
					break;
				case GET_TEMPERATURE:
					_broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_TEMPERATURE,
							new String[] { Bundles.TEMPERATURE_LIST }, new Object[] { _temperatureList });
					break;
				case GET_TIMER:
					_broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_TIMER,
							new String[] { Bundles.TIMER_LIST, Bundles.SOCKET_LIST },
							new Object[] { _timerList, _wirelessSocketList });
					break;
				case GET_WEATHER_CURRENT:
					_broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_WEATHER_VIEW,
							new String[] { Bundles.WEATHER_CURRENT }, new Object[] { _currentWeather });
					break;
				case GET_WEATHER_FORECAST:
					_broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_FORECAST_VIEW,
							new String[] { Bundles.WEATHER_FORECAST }, new Object[] { _forecastWeather });
					break;
				case GET_MAP_CONTENT:
					_broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_MAP_CONTENT_VIEW,
							new String[] { Bundles.MAP_CONTENT_LIST, Bundles.SOCKET_LIST, Bundles.SCHEDULE_LIST,
									Bundles.TIMER_LIST, Bundles.TEMPERATURE_LIST },
							new Object[] { _mapContentList, _wirelessSocketList, _scheduleList, _timerList,
									_temperatureList });
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
						_context.startService(new Intent(_context, OpenWeatherService.class));
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
				_logger.Warn("Socketlist is null!");
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
			int timerHour = _sharedPrefController.LoadIntegerValueFromSharedPreferences(SharedPrefConstants.TIMER_HOUR);
			_logger.Debug(String.format("Loaded time values are %s:%s", timerHour, timerMinute));
			if (timerMinute == 0 && timerHour == 0) {
				_logger.Error(String.format("timerMinute and timerHour is 0! Setting timerMinute to default %s!",
						SharedPrefConstants.DEFAULT_TIMER_MIN));
				timerMinute = SharedPrefConstants.DEFAULT_TIMER_MIN;
			}

			Calendar currentTime = Calendar.getInstance();
			int currentDay = currentTime.get(Calendar.DAY_OF_WEEK) - 1;
			_logger.Debug(String.format("Current time is %s", currentTime));

			int scheduleMinute = currentTime.get(Calendar.MINUTE) + timerMinute;
			int scheduleHour = currentTime.get(Calendar.HOUR_OF_DAY) + timerHour;
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
			ClientTask clientTask = new ClientTask(_context,
					MediaMirrorController.SERVER_IPS.get(MediaMirrorController.SLEEP_MEDIA_SERVER_INDEX),
					MediaMirrorController.SERVERPORT);
			clientTask.SetCommunication(
					"ACTION:" + ServerAction.PLAY_SEA_SOUND.toString() + "&DATA:" + String.valueOf(playLength));
			clientTask.execute();
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
							new String[] { Bundles.BIRTHDAY_LIST }, new Object[] { _birthdayList });

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
					_notificationController.CreateBirthdayNotification(BirthdayView.class, birthday.GetNotificationId(),
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
							new String[] { Bundles.CHANGE_LIST }, new Object[] { _changeList });
				} else {
					_logger.Warn("newChangeList is null");
				}
			} else {
				_logger.Warn("changeStringArray is null");
			}

			updateDownloadCount();
		}
	};

	private BroadcastReceiver _forecastModelReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_forecastModelReceiver onReceive");

			ForecastModel newForecastWeather = (ForecastModel) intent
					.getSerializableExtra(OpenWeatherConstants.BUNDLE_EXTRA_FORECAST_MODEL);
			if (newForecastWeather != null) {
				_forecastWeather = newForecastWeather;
			} else {
				_logger.Warn("newForecastWeather is null");
			}

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
							new String[] { Bundles.INFORMATION_SINGLE }, new Object[] { _information });
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

			String[] mapcontentStringArray = intent.getStringArrayExtra(Bundles.MAP_CONTENT_DOWNLOAD);
			if (mapcontentStringArray != null) {

				SerializableList<MapContentDto> newMapContentList = JsonDataToMapContentConverter
						.GetList(mapcontentStringArray);
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
				_logger.Warn("mapcontentStringArray is null!");
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
							new String[] { Bundles.MOVIE_LIST }, new Object[] { _movieList });
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
							new String[] { Bundles.SCHEDULE_LIST, Bundles.SOCKET_LIST },
							new Object[] { _scheduleList, _wirelessSocketList });

					sendSchedulesToWear();
				} else {
					_logger.Warn("newScheduleList is null");
				}

				SerializableList<TimerDto> newTimerList = JsonDataToTimerConverter.GetList(scheduleStringArray,
						_wirelessSocketList);
				if (newTimerList != null) {
					_timerList = newTimerList;
					_broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_TIMER,
							new String[] { Bundles.TIMER_LIST, Bundles.SOCKET_LIST },
							new Object[] { _timerList, _wirelessSocketList });
				} else {
					_logger.Warn("newTimerList is null");
				}
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
							new String[] { Bundles.SOCKET_LIST }, new Object[] { _wirelessSocketList });
					_broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_MAP_CONTENT_VIEW,
							new String[] { Bundles.MAP_CONTENT_LIST, Bundles.SOCKET_LIST, Bundles.SCHEDULE_LIST,
									Bundles.TIMER_LIST, Bundles.TEMPERATURE_LIST },
							new Object[] { _mapContentList, _wirelessSocketList, _scheduleList, _timerList,
									_temperatureList });

					sendSocketsToWear();

					if (_sharedPrefController
							.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_SOCKET_NOTIFICATION)) {
						_notificationController.CreateSocketNotification(_wirelessSocketList);
					}
				} else {
					_logger.Warn("_wirelessSocketList is null");
				}
			}

			updateDownloadCount();
			startDownloadSchedule();
		}
	};

	private BroadcastReceiver _soundDownloadReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_soundDownloadReceiver onReceive");
			String[] isPlayingStringArray = intent.getStringArrayExtra(TAG);

			if (isPlayingStringArray != null) {
				if (isPlayingStringArray[0] != null) {
					_logger.Debug(isPlayingStringArray[0]);
					String[] data = isPlayingStringArray[0].split("\\};");

					String isPlayingString = data[0].replace("{IsPlaying:", "").replace("};", "");
					if (isPlayingString.contains("1")) {
						if (data.length == 4) {
							String playingFile = data[1].replace("{PlayingFile:", "").replace("};", "");
							int raspberry = Integer.parseInt(data[3].replace("{Raspberry:", "").replace("};", ""));
							_notificationController.CreateSoundNotification(playingFile,
									RaspberrySelection.GetById(raspberry).toString(),
									IDs.NOTIFICATION_SONG + raspberry);
						} else {
							_logger.Warn("data has wrong size!");
						}
					}
				}

				if (isPlayingStringArray.length > 1) {
					if (isPlayingStringArray[1] != null) {
						_logger.Debug(isPlayingStringArray[1]);
						String[] data = isPlayingStringArray[1].split("\\};");

						String isPlayingString = data[0].replace("{IsPlaying:", "").replace("};", "");
						if (isPlayingString.contains("1")) {
							if (data.length == 4) {
								String playingFile = data[1].replace("{PlayingFile:", "").replace("};", "");
								int raspberry = Integer.parseInt(data[3].replace("{Raspberry:", "").replace("};", ""));
								_notificationController.CreateSoundNotification(playingFile,
										RaspberrySelection.GetById(raspberry).toString(),
										IDs.NOTIFICATION_SONG + raspberry);
							} else {
								_logger.Warn("data has wrong size!");
							}
						}
					}
				}
			}
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
						String messageText = "RaspberryTemperature:";

						int found = 0;
						for (int index = 0; index < _temperatureList.getSize(); index++) {
							TemperatureDto entry = _temperatureList.getValue(index);
							if (entry.GetTemperatureType() == TemperatureType.RASPBERRY) {
								messageText += "TEMPERATURE:" + entry.GetTemperatureString();
								messageText += "&AREA:" + entry.GetArea();
								messageText += "&LASTUPDATE:" + entry.GetLastUpdate().toString();
								found++;
							}
						}

						_logger.Info("messageText for temperature is " + messageText);
						if (found == 1) {
							_serviceController.SendMessageToWear(messageText);
						} else {
							_logger.Warn("found " + String.valueOf(found) + " different temperatures!");
						}
					} else {
						_logger.Warn("Temperaturelist has size " + String.valueOf(_temperatureList.getSize()));
					}
				} else {
					_logger.Warn("newTemperatureList is null!");
				}

				if (_temperatureList != null) {
					_broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_TEMPERATURE,
							new String[] { Bundles.TEMPERATURE_LIST }, new Object[] { _temperatureList });
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

	private BroadcastReceiver _weatherModelReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_weatherModelReceiver onReceive");

			WeatherModel newWeather = (WeatherModel) intent
					.getSerializableExtra(OpenWeatherConstants.BUNDLE_EXTRA_WEATHER_MODEL);
			if (newWeather != null) {
				_currentWeather = newWeather;
				_serviceController.SendMessageToWear("CurrentWeather:DESCRIPTION:" + _currentWeather.GetDescription()
						+ "&TEMPERATURE:" + _currentWeather.GetTemperatureString() + "&LASTUPDATE:"
						+ _currentWeather.GetLastUpdate().toString());
			}

			if (_sharedPrefController
					.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_TEMPERATURE_NOTIFICATION)) {
				_notificationController.CreateTemperatureNotification(SensorTemperatureView.class, _temperatureList,
						_currentWeather);
			}
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

	private BroadcastReceiver _reloadBirthdayReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_reloadBirthdayReceiver onReceive");
			startDownloadBirthday();
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

	@Override
	public void onCreate() {
		super.onCreate();
		if (_logger != null) {
			_logger.Debug("onCreate");
		}

		if (!_isInitialized) {
			_logger = new LucaHomeLogger(TAG);
			_logger.Debug("onCreate");

			_birthdayList = new SerializableList<BirthdayDto>();
			_changeList = new SerializableList<ChangeDto>();
			_movieList = new SerializableList<MovieDto>();
			_scheduleList = new SerializableList<ScheduleDto>();
			_temperatureList = new SerializableList<TemperatureDto>();
			_timerList = new SerializableList<TimerDto>();
			_wirelessSocketList = new SerializableList<WirelessSocketDto>();

			_context = this;

			_broadcastController = new BroadcastController(_context);
			_dialogController = new LucaDialogController(_context);
			_networkController = new NetworkController(_context,
					new DialogController(_context, ContextCompat.getColor(_context, R.color.TextIcon),
							ContextCompat.getColor(_context, R.color.Background)));
			_notificationController = new LucaNotificationController(_context);
			_openWeatherController = new OpenWeatherController(_context, Constants.CITY);
			_receiverController = new ReceiverController(_context);
			_scheduleService = new ScheduleService();
			_serviceController = new ServiceController(_context);
			_sharedPrefController = new SharedPrefController(_context, SharedPrefConstants.SHARED_PREF_NAME);

			registerReceiver();

			_progress = 0;
			_downloadCount = Constants.DOWNLOAD_STEPS;
			boot();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		if (_logger != null) {
			_logger.Debug("onStartCommand");
		}

		try {
			Bundle data = intent.getExtras();
			MainServiceAction action = (MainServiceAction) data.getSerializable(Bundles.MAIN_SERVICE_ACTION);
			if (action != null) {
				if (_logger != null) {
					_logger.Debug("Received action is: " + action.toString());
				}
				switch (action) {
				case BOOT:
					_progress = 0;
					_downloadCount = Constants.DOWNLOAD_STEPS;
					boot();
					break;
				default:
					if (_logger != null) {
						_logger.Warn("Action not supported! " + action.toString());
					}
					break;
				}
			} else {
				if (_logger != null) {
					_logger.Warn("Action is null!");
				}
			}
		} catch (Exception e) {
			if (_logger != null) {
				_logger.Error(e.toString());
			}
		}

		return 0;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		if (_logger != null) {
			_logger.Debug("onBind");
		}
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (_logger != null) {
			_logger.Debug("onDestroy");
		}
		_scheduleService.Dispose();
		unregisterReceiver();
	}

	private void registerReceiver() {
		_receiverController.RegisterReceiver(_actionReceiver, new String[] { Broadcasts.MAIN_SERVICE_COMMAND });
		_receiverController.RegisterReceiver(_birthdayDownloadReceiver,
				new String[] { Broadcasts.DOWNLOAD_BIRTHDAY_FINISHED });
		_receiverController.RegisterReceiver(_changeDownloadReceiver,
				new String[] { Broadcasts.DOWNLOAD_CHANGE_FINISHED });
		_receiverController.RegisterReceiver(_forecastModelReceiver,
				new String[] { OpenWeatherConstants.BROADCAST_GET_FORECAST_WEATHER_JSON_FINISHED });
		_receiverController.RegisterReceiver(_informationDownloadReceiver,
				new String[] { Broadcasts.DOWNLOAD_INFORMATION_FINISHED });
		_receiverController.RegisterReceiver(_mapContentDownloadReceiver,
				new String[] { Broadcasts.DOWNLOAD_MAP_CONTENT_FINISHED });
		_receiverController.RegisterReceiver(_movieDownloadReceiver,
				new String[] { Broadcasts.DOWNLOAD_MOVIE_FINISHED });
		_receiverController.RegisterReceiver(_scheduleDownloadReceiver,
				new String[] { Broadcasts.DOWNLOAD_SCHEDULE_FINISHED });
		_receiverController.RegisterReceiver(_socketDownloadReceiver,
				new String[] { Broadcasts.DOWNLOAD_SOCKET_FINISHED });
		_receiverController.RegisterReceiver(_soundDownloadReceiver, new String[] { Broadcasts.IS_SOUND_PLAYING });
		_receiverController.RegisterReceiver(_temperatureDownloadReceiver,
				new String[] { Broadcasts.DOWNLOAD_TEMPERATURE_FINISHED });
		_receiverController.RegisterReceiver(_weatherModelReceiver,
				new String[] { OpenWeatherConstants.BROADCAST_GET_CURRENT_WEATHER_JSON_FINISHED });

		_receiverController.RegisterReceiver(_addReceiver, new String[] { Broadcasts.ADD_BIRTHDAY, Broadcasts.ADD_MOVIE,
				Broadcasts.ADD_SCHEDULE, Broadcasts.ADD_SOCKET });
		_receiverController.RegisterReceiver(_updateReceiver, new String[] { Broadcasts.UPDATE_BIRTHDAY,
				Broadcasts.UPDATE_MOVIE, Broadcasts.UPDATE_SCHEDULE, Broadcasts.UPDATE_SOCKET });
		_receiverController.RegisterReceiver(_startDownloadReceiver,
				new String[] { Broadcasts.ADD_BIRTHDAY, Broadcasts.ADD_MOVIE, Broadcasts.ADD_SCHEDULE,
						Broadcasts.ADD_SOCKET, Broadcasts.UPDATE_BIRTHDAY, Broadcasts.UPDATE_MOVIE,
						Broadcasts.UPDATE_SCHEDULE, Broadcasts.UPDATE_SOCKET });

		_receiverController.RegisterReceiver(_reloadBirthdayReceiver, new String[] { Broadcasts.RELOAD_BIRTHDAY });
		_receiverController.RegisterReceiver(_reloadMovieReceiver, new String[] { Broadcasts.RELOAD_MOVIE });
		_receiverController.RegisterReceiver(_reloadScheduleReceiver,
				new String[] { Broadcasts.RELOAD_SCHEDULE, Broadcasts.RELOAD_TIMER });
		_receiverController.RegisterReceiver(_reloadSocketReceiver, new String[] { Broadcasts.RELOAD_SOCKETS });

		_receiverController.RegisterReceiver(_reloadChangeReceiver,
				new String[] { Broadcasts.RELOAD_BIRTHDAY, Broadcasts.RELOAD_MOVIE, Broadcasts.RELOAD_SCHEDULE,
						Broadcasts.RELOAD_TIMER, Broadcasts.RELOAD_SOCKETS });
	}

	private void unregisterReceiver() {
		_receiverController.UnregisterReceiver(_actionReceiver);
		_receiverController.UnregisterReceiver(_birthdayDownloadReceiver);
		_receiverController.UnregisterReceiver(_changeDownloadReceiver);
		_receiverController.UnregisterReceiver(_forecastModelReceiver);
		_receiverController.UnregisterReceiver(_informationDownloadReceiver);
		_receiverController.UnregisterReceiver(_mapContentDownloadReceiver);
		_receiverController.UnregisterReceiver(_movieDownloadReceiver);
		_receiverController.UnregisterReceiver(_scheduleDownloadReceiver);
		_receiverController.UnregisterReceiver(_socketDownloadReceiver);
		_receiverController.UnregisterReceiver(_soundDownloadReceiver);
		_receiverController.UnregisterReceiver(_temperatureDownloadReceiver);
		_receiverController.UnregisterReceiver(_weatherModelReceiver);

		_receiverController.UnregisterReceiver(_addReceiver);
		_receiverController.UnregisterReceiver(_updateReceiver);
		_receiverController.UnregisterReceiver(_startDownloadReceiver);

		_receiverController.UnregisterReceiver(_reloadBirthdayReceiver);
		_receiverController.UnregisterReceiver(_reloadMovieReceiver);
		_receiverController.UnregisterReceiver(_reloadScheduleReceiver);
		_receiverController.UnregisterReceiver(_reloadSocketReceiver);

		_receiverController.UnregisterReceiver(_reloadChangeReceiver);
	}

	private void addSchedules() {
		_scheduleService.AddSchedule("UpdateBirthday", _downloadBirthdayRunnable, 240 * 60 * 1000);
		_scheduleService.AddSchedule("UpdateForecast", _downloadForecastWeatherRunnable, 30 * 60 * 1000);
		_scheduleService.AddSchedule("UpdateIsSoundPlaying", _downloadIsSoundPlayingRunnable, 5 * 60 * 1000);
		_scheduleService.AddSchedule("UpdateSockets", _downloadSocketRunnable, 15 * 60 * 1000);
		_scheduleService.AddSchedule("UpdateTemperature", _downloadTemperatureRunnable, 5 * 60 * 1000);
		_schedulesAdded = true;
	}

	private void boot() {
		if (!_sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.SHARED_PREF_INSTALLED)) {
			install();
			return;
		}

		if (!_schedulesAdded) {
			addSchedules();
		}

		if (!_networkController.IsNetworkAvailable()) {
			_logger.Warn("No network available!");
			return;
		}

		if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
			_logger.Warn("No LucaHome network! ...");
			Toasty.warning(_context, "No LucaHome network! ...", Toast.LENGTH_LONG).show();
			return;
		}

		if (!_downloadingData) {
			if (!_sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.USER_DATA_ENTERED)) {
				_dialogController.ShowUserCredentialsDialog(null, _startDownloadRunnable, false);
			} else {
				Calendar now = Calendar.getInstance();
				if (_lastUpdate != null) {
					long difference = Calendar.getInstance().getTimeInMillis() - _lastUpdate.getTimeInMillis();

					if (difference < 60 * 1000) {
						_logger.Warn("Just updated the data!");

						_broadcastController.SendSerializableArrayBroadcast(Broadcasts.COMMAND,
								new String[] { Bundles.COMMAND, Bundles.NAVIGATE_DATA },
								new Object[] { Command.NAVIGATE, NavigateData.HOME });

						return;
					}
				}
				_lastUpdate = now;

				startDownloadAll();
			}
		}
	}

	private void install() {
		_broadcastController.SendSerializableBroadcast(Broadcasts.COMMAND, Bundles.COMMAND,
				Command.SHOW_USER_LOGIN_DIALOG);

		_sharedPrefController.SaveBooleanValue(SharedPrefConstants.SHARED_PREF_INSTALLED, true);

		_sharedPrefController.SaveBooleanValue(SharedPrefConstants.DISPLAY_SOCKET_NOTIFICATION, true);
		_sharedPrefController.SaveBooleanValue(SharedPrefConstants.DISPLAY_WEATHER_NOTIFICATION, true);
		_sharedPrefController.SaveBooleanValue(SharedPrefConstants.DISPLAY_TEMPERATURE_NOTIFICATION, true);

		_sharedPrefController.SaveBooleanValue(SharedPrefConstants.START_AUDIO_APP, true);
		_sharedPrefController.SaveBooleanValue(SharedPrefConstants.START_OSMC_APP, true);

		_sharedPrefController.SaveIntegerValue(SharedPrefConstants.SOUND_RASPBERRY_SELECTION,
				RaspberrySelection.RASPBERRY_1.GetInt());
	}

	private void startDownloadAll() {
		_downloadingData = true;
		_progress = 0;
		_downloadCount = Constants.DOWNLOAD_STEPS;

		startDownloadBirthday();
		startDownloadChange();
		startDownloadCurrentWeather();
		startDownloadForecastWeather();
		startDownloadInformation();
		startDownloadMapContent();
		startDownloadMovie();
		startDownloadSocket();
		startDownloadIsSoundPlaying();
		startDownloadTemperature();

		if (_sharedPrefController
				.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_WEATHER_NOTIFICATION)) {
			_context.startService(new Intent(_context, OpenWeatherService.class));
		}

		startTimeout();
	}

	private void updateDownloadCount() {
		_progress++;
		_broadcastController.SendIntBroadcast(Broadcasts.UPDATE_PROGRESSBAR, Bundles.VIEW_PROGRESS, _progress);
		if (_progress >= _downloadCount) {
			stopTimeout();
			_broadcastController.SendSerializableArrayBroadcast(Broadcasts.COMMAND,
					new String[] { Bundles.COMMAND, Bundles.NAVIGATE_DATA },
					new Object[] { Command.NAVIGATE, NavigateData.HOME });
		}
	}

	private void installNotificationSettings() {
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
		_serviceController.StartRestService(Bundles.BIRTHDAY_DOWNLOAD, ServerActions.GET_BIRTHDAYS,
				Broadcasts.DOWNLOAD_BIRTHDAY_FINISHED, LucaObject.BIRTHDAY, RaspberrySelection.BOTH);
	}

	private void startDownloadChange() {
		_serviceController.StartRestService(Bundles.CHANGE_DOWNLOAD, ServerActions.GET_CHANGES,
				Broadcasts.DOWNLOAD_CHANGE_FINISHED, LucaObject.CHANGE, RaspberrySelection.BOTH);
	}

	private void startDownloadCurrentWeather() {
		_openWeatherController.loadCurrentWeather();
	}

	private void startDownloadForecastWeather() {
		_openWeatherController.loadForecastWeather();
	}

	private void startDownloadInformation() {
		_serviceController.StartRestService(Bundles.INFORMATION_DOWNLOAD, ServerActions.GET_INFORMATIONS,
				Broadcasts.DOWNLOAD_INFORMATION_FINISHED, LucaObject.INFORMATION, RaspberrySelection.BOTH);
	}

	private void startDownloadMapContent() {
		_serviceController.StartRestService(Bundles.MAP_CONTENT_DOWNLOAD, ServerActions.GET_MAP_CONTENTS,
				Broadcasts.DOWNLOAD_MAP_CONTENT_FINISHED, LucaObject.MAP_CONTENT, RaspberrySelection.BOTH);
	}

	private void startDownloadMovie() {
		_serviceController.StartRestService(Bundles.MOVIE_DOWNLOAD, ServerActions.GET_MOVIES,
				Broadcasts.DOWNLOAD_MOVIE_FINISHED, LucaObject.MOVIE, RaspberrySelection.BOTH);
	}

	private void startDownloadSchedule() {
		_serviceController.StartRestService(Bundles.SCHEDULE_DOWNLOAD, ServerActions.GET_SCHEDULES,
				Broadcasts.DOWNLOAD_SCHEDULE_FINISHED, LucaObject.SCHEDULE, RaspberrySelection.BOTH);
	}

	private void startDownloadSocket() {
		_serviceController.StartRestService(Bundles.SOCKET_DOWNLOAD, ServerActions.GET_SOCKETS,
				Broadcasts.DOWNLOAD_SOCKET_FINISHED, LucaObject.WIRELESS_SOCKET, RaspberrySelection.BOTH);
	}

	private void startDownloadIsSoundPlaying() {
		_serviceController.StartRestService(TAG, ServerActions.IS_SOUND_PLAYING, Broadcasts.IS_SOUND_PLAYING,
				LucaObject.SOUND, RaspberrySelection.BOTH);
	}

	private void startDownloadTemperature() {
		_serviceController.StartRestService(Bundles.TEMPERATURE_DOWNLOAD, ServerActions.GET_TEMPERATURES,
				Broadcasts.DOWNLOAD_TEMPERATURE_FINISHED, LucaObject.TEMPERATURE, RaspberrySelection.BOTH);
	}

	private void startTimeout() {
		_logger.Debug("Starting timeoutController...");
		_timeoutHandler.postDelayed(_timeoutCheck, _timeCheck);
	}

	private void stopTimeout() {
		_logger.Debug("Stopping timeoutController...");
		_downloadingData = false;
		_timeoutHandler.removeCallbacks(_timeoutCheck);
	}

	private void sendBirthdaysToWear() {
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

	private void sendSchedulesToWear() {
		String messageText = "Schedules:";
		for (int index = 0; index < _scheduleList.getSize(); index++) {
			String information = _scheduleList.getValue(index).GetWeekday().toString() + ", "
					+ _scheduleList.getValue(index).GetTime().toString() + " set "
					+ _scheduleList.getValue(index).GetSocket().GetName() + " to "
					+ String.valueOf(_scheduleList.getValue(index).GetAction());
			messageText += _scheduleList.getValue(index).GetName() + ":" + information + ":"
					+ (_scheduleList.getValue(index).GetIsActive() ? "1" : "0") + "&";
		}
		_logger.Info("message is " + messageText);
		_serviceController.SendMessageToWear(messageText);
	}

	private void sendSocketsToWear() {
		String messageText = "Sockets:";
		for (int index = 0; index < _wirelessSocketList.getSize(); index++) {
			messageText += _wirelessSocketList.getValue(index).GetName() + ":"
					+ (_wirelessSocketList.getValue(index).GetIsActivated() ? "1" : "0") + "&";
		}
		_logger.Info("message is " + messageText);
		_serviceController.SendMessageToWear(messageText);
	}
}