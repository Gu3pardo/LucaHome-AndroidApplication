package guepardoapps.lucahome.services;

import java.util.Calendar;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.Constants;
import guepardoapps.lucahome.common.LucaHomeLogger;
import guepardoapps.lucahome.common.classes.*;
import guepardoapps.lucahome.common.controller.*;
import guepardoapps.lucahome.common.converter.json.*;
import guepardoapps.lucahome.common.enums.*;
import guepardoapps.lucahome.dto.*;
import guepardoapps.lucahome.dto.sensor.TemperatureDto;
import guepardoapps.lucahome.services.helper.DialogService;
import guepardoapps.lucahome.viewcontroller.BirthdayController;
import guepardoapps.toolset.controller.DialogController;
import guepardoapps.toolset.controller.NetworkController;
import guepardoapps.toolset.controller.SharedPrefController;

import guepardoapps.toolset.openweather.OpenWeatherController;
import guepardoapps.toolset.openweather.common.OpenWeatherConstants;
import guepardoapps.toolset.openweather.model.ForecastModel;
import guepardoapps.toolset.openweather.model.WeatherModel;

import guepardoapps.toolset.scheduler.ScheduleService;

import guepardoapps.toolset.controller.BroadcastController;
import guepardoapps.toolset.controller.ReceiverController;

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

	private BirthdayController _birthdayController;
	private BroadcastController _broadcastController;
	private DialogService _dialogService;
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

			if (_sharedPrefController.LoadBooleanValueFromSharedPreferences(Constants.DISPLAY_WEATHER_NOTIFICATION)) {
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

			if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
				_logger.Warn("No LucaHome network! ...");
				return;
			}

			startDownloadCurrentWeather();
			startDownloadTemperature();
		}
	};

	private Runnable _timeoutCheck = new Runnable() {
		public void run() {
			_logger.Warn("_timeoutCheck received!");
			_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_COMMAND,
					new String[] { Constants.BUNDLE_COMMAND, Constants.BUNDLE_NAVIGATE_DATA },
					new Object[] { Command.NAVIGATE, NavigateData.FINISH });
		}
	};

	private BroadcastReceiver _actionReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_actionReceiver onReceive");
			MainServiceAction action = (MainServiceAction) intent
					.getSerializableExtra(Constants.BUNDLE_MAIN_SERVICE_ACTION);
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
					_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_GET_ALL,
							new String[] { Constants.BUNDLE_BIRTHDAY_LIST, Constants.BUNDLE_CHANGE_LIST,
									Constants.BUNDLE_INFORMATION_SINGLE, Constants.BUNDLE_MAP_CONTENT_LIST,
									Constants.BUNDLE_MOVIE_LIST, Constants.BUNDLE_SCHEDULE_LIST,
									Constants.BUNDLE_SOCKET_LIST, Constants.BUNDLE_TEMPERATURE_LIST,
									Constants.BUNDLE_TIMER_LIST, Constants.BUNDLE_WEATHER_CURRENT,
									Constants.BUNDLE_WEATHER_FORECAST },
							new Object[] { _birthdayList, _changeList, _information, _mapContentList, _movieList,
									_scheduleList, _wirelessSocketList, _temperatureList, _timerList, _currentWeather,
									_forecastWeather });
					break;
				case GET_BIRTHDAYS:
					_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_UPDATE_BIRTHDAY,
							new String[] { Constants.BUNDLE_BIRTHDAY_LIST }, new Object[] { _birthdayList });
					sendBirthdaysToWear();
					break;
				case GET_CHANGES:
					_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_UPDATE_CHANGE,
							new String[] { Constants.BUNDLE_CHANGE_LIST }, new Object[] { _changeList });
					break;
				case GET_INFORMATIONS:
					_logger.Debug("GET_INFORMATIONS");
					if (_information != null) {
						_logger.Debug(_information.toString());
					} else {
						_logger.Warn("Information is null!");
					}
					_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_UPDATE_INFORMATION,
							new String[] { Constants.BUNDLE_INFORMATION_SINGLE }, new Object[] { _information });
					break;
				case GET_MOVIES:
					_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_UPDATE_MOVIE,
							new String[] { Constants.BUNDLE_MOVIE_LIST }, new Object[] { _movieList });
					break;
				case GET_SCHEDULES:
					_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_UPDATE_SCHEDULE,
							new String[] { Constants.BUNDLE_SCHEDULE_LIST, Constants.BUNDLE_SOCKET_LIST },
							new Object[] { _scheduleList, _wirelessSocketList });
					sendSchedulesToWear();
					break;
				case GET_SOCKETS:
					_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_UPDATE_SOCKET,
							new String[] { Constants.BUNDLE_SOCKET_LIST }, new Object[] { _wirelessSocketList });
					sendSocketsToWear();
					break;
				case GET_TEMPERATURE:
					_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_UPDATE_TEMPERATURE,
							new String[] { Constants.BUNDLE_TEMPERATURE_LIST }, new Object[] { _temperatureList });
					break;
				case GET_TIMER:
					_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_UPDATE_TIMER,
							new String[] { Constants.BUNDLE_TIMER_LIST, Constants.BUNDLE_SOCKET_LIST },
							new Object[] { _timerList, _wirelessSocketList });
					break;
				case GET_WEATHER_CURRENT:
					_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_UPDATE_WEATHER_VIEW,
							new String[] { Constants.BUNDLE_WEATHER_CURRENT }, new Object[] { _currentWeather });
					break;
				case GET_WEATHER_FORECAST:
					_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_UPDATE_FORECAST_VIEW,
							new String[] { Constants.BUNDLE_WEATHER_FORECAST }, new Object[] { _forecastWeather });
					break;
				case GET_MAP_CONTENT:
					_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_UPDATE_MAP_CONTENT_VIEW,
							new String[] { Constants.BUNDLE_MAP_CONTENT_LIST, Constants.BUNDLE_SOCKET_LIST,
									Constants.BUNDLE_SCHEDULE_LIST, Constants.BUNDLE_TIMER_LIST,
									Constants.BUNDLE_TEMPERATURE_LIST },
							new Object[] { _mapContentList, _wirelessSocketList, _scheduleList, _timerList,
									_temperatureList });
					break;
				case SHOW_NOTIFICATION_SOCKET:
					if (_wirelessSocketList != null) {
						_serviceController.StartSocketNotificationService(Constants.ID_NOTIFICATION_WEAR,
								_wirelessSocketList);
					}
					break;
				case SHOW_NOTIFICATION_TEMPERATURE:
					if (_temperatureList != null && _currentWeather != null) {
						_serviceController.StartTemperatureNotificationService(Constants.ID_NOTIFICATION_TEMPERATURE,
								_temperatureList, _currentWeather);
					}
					break;
				case SHOW_NOTIFICATION_WEATHER:
					if (_sharedPrefController
							.LoadBooleanValueFromSharedPreferences(Constants.DISPLAY_WEATHER_NOTIFICATION)) {
						_context.startService(new Intent(_context, OpenWeatherService.class));
					}
					break;
				default:
					_logger.Warn("action is not supported! " + action.toString());
					break;
				}
			} else {
				_logger.Warn("action is null!");
			}
		}
	};

	private BroadcastReceiver _birthdayDownloadReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_birthdayDownloadReceiver onReceive");

			String[] birthdayStringArray = intent.getStringArrayExtra(Constants.BIRTHDAY_DOWNLOAD);
			if (birthdayStringArray != null) {

				SerializableList<BirthdayDto> newBirthdayList = JsonDataToBirthdayConverter
						.GetList(birthdayStringArray);
				if (newBirthdayList != null) {
					_birthdayList = newBirthdayList;
					_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_UPDATE_BIRTHDAY,
							new String[] { Constants.BUNDLE_BIRTHDAY_LIST }, new Object[] { _birthdayList });

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
				if (_birthdayController.HasBirthday(birthday)) {
					int age = _birthdayController.GetAge(birthday);
					_logger.Debug("It is " + birthday.GetName() + "'s " + String.valueOf(age) + "th birthday!");
					_serviceController.StartNotificationService(birthday.GetName(), birthday.GetNotificationBody(age),
							birthday.GetNotificationId(), LucaObject.BIRTHDAY);
				}
			}
		}
	};

	private BroadcastReceiver _changeDownloadReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_changeDownloadReceiver onReceive");

			String[] changeStringArray = intent.getStringArrayExtra(Constants.CHANGE_DOWNLOAD);
			if (changeStringArray != null) {

				SerializableList<ChangeDto> newChangeList = JsonDataToChangeConverter.GetList(changeStringArray);
				if (newChangeList != null) {
					_changeList = newChangeList;
					_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_UPDATE_CHANGE,
							new String[] { Constants.BUNDLE_CHANGE_LIST }, new Object[] { _changeList });
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

			String[] informationStringArray = intent.getStringArrayExtra(Constants.INFORMATION_DOWNLOAD);
			if (informationStringArray != null) {

				InformationDto newInformation = JsonDataToInformationConverter.Get(informationStringArray);
				if (newInformation != null) {
					_information = newInformation;
					_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_UPDATE_INFORMATION,
							new String[] { Constants.BUNDLE_INFORMATION_SINGLE }, new Object[] { _information });
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

			String[] mapcontentStringArray = intent.getStringArrayExtra(Constants.MAP_CONTENT_DOWNLOAD);
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

			String[] movieStringArray = intent.getStringArrayExtra(Constants.MOVIE_DOWNLOAD);
			if (movieStringArray != null) {

				SerializableList<MovieDto> newMovieList = JsonDataToMovieConverter.GetList(movieStringArray);
				if (newMovieList != null) {
					_movieList = newMovieList;
					_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_UPDATE_MOVIE,
							new String[] { Constants.BUNDLE_MOVIE_LIST }, new Object[] { _movieList });
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

			String[] scheduleStringArray = intent.getStringArrayExtra(Constants.SCHEDULE_DOWNLOAD);
			if (scheduleStringArray != null && _wirelessSocketList != null) {

				SerializableList<ScheduleDto> newScheduleList = JsonDataToScheduleConverter.GetList(scheduleStringArray,
						_wirelessSocketList);
				if (newScheduleList != null) {
					_scheduleList = newScheduleList;
					_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_UPDATE_SCHEDULE,
							new String[] { Constants.BUNDLE_SCHEDULE_LIST, Constants.BUNDLE_SOCKET_LIST },
							new Object[] { _scheduleList, _wirelessSocketList });

					sendSchedulesToWear();
				} else {
					_logger.Warn("newScheduleList is null");
				}

				SerializableList<TimerDto> newTimerList = JsonDataToTimerConverter.GetList(scheduleStringArray,
						_wirelessSocketList);
				if (newTimerList != null) {
					_timerList = newTimerList;
					_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_UPDATE_TIMER,
							new String[] { Constants.BUNDLE_TIMER_LIST, Constants.BUNDLE_SOCKET_LIST },
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

			String[] socketStringArray = intent.getStringArrayExtra(Constants.SOCKET_DOWNLOAD);
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
					_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_UPDATE_SOCKET,
							new String[] { Constants.BUNDLE_SOCKET_LIST }, new Object[] { _wirelessSocketList });
					_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_UPDATE_MAP_CONTENT_VIEW,
							new String[] { Constants.BUNDLE_MAP_CONTENT_LIST, Constants.BUNDLE_SOCKET_LIST,
									Constants.BUNDLE_SCHEDULE_LIST, Constants.BUNDLE_TIMER_LIST,
									Constants.BUNDLE_TEMPERATURE_LIST },
							new Object[] { _mapContentList, _wirelessSocketList, _scheduleList, _timerList,
									_temperatureList });

					sendSocketsToWear();

					if (_sharedPrefController
							.LoadBooleanValueFromSharedPreferences(Constants.DISPLAY_SOCKET_NOTIFICATION)) {
						_serviceController.StartSocketNotificationService(Constants.ID_NOTIFICATION_WEAR,
								_wirelessSocketList);
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
							_serviceController.StartNotificationService(RaspberrySelection.RASPBERRY_1.toString(),
									playingFile, Constants.ID_NOTIFICATION_SONG + raspberry, LucaObject.SOUND);
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
								_serviceController.StartNotificationService(RaspberrySelection.RASPBERRY_2.toString(),
										playingFile, Constants.ID_NOTIFICATION_SONG + raspberry, LucaObject.SOUND);
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

			String[] temperatureStringArray = intent.getStringArrayExtra(Constants.TEMPERATURE_DOWNLOAD);
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

				if (_temperatureList != null && _currentWeather != null) {
					_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_UPDATE_TEMPERATURE,
							new String[] { Constants.BUNDLE_TEMPERATURE_LIST }, new Object[] { _temperatureList });
					if (_sharedPrefController
							.LoadBooleanValueFromSharedPreferences(Constants.DISPLAY_TEMPERATURE_NOTIFICATION)) {
						_serviceController.StartTemperatureNotificationService(Constants.ID_NOTIFICATION_TEMPERATURE,
								_temperatureList, _currentWeather);
					}
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

			if (_temperatureList != null && _currentWeather != null) {
				if (_sharedPrefController
						.LoadBooleanValueFromSharedPreferences(Constants.DISPLAY_TEMPERATURE_NOTIFICATION)) {
					_serviceController.StartTemperatureNotificationService(Constants.ID_NOTIFICATION_TEMPERATURE,
							_temperatureList, _currentWeather);
				}
			}
			updateDownloadCount();
		}
	};

	private BroadcastReceiver _addReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_addReceiver onReceive");

			LucaObject lucaObject = (LucaObject) intent.getSerializableExtra(Constants.BUNDLE_LUCA_OBJECT);
			if (lucaObject == null) {
				_logger.Error("_addReceiver received data with null lucaobject!");
				return;
			}

			String action = intent.getStringExtra(Constants.BUNDLE_ACTION);
			if (action == null) {
				_logger.Error("_addReceiver received data with null action!");
				return;
			}

			switch (lucaObject) {
			case BIRTHDAY:
				_serviceController.StartRestService(Constants.BIRTHDAY_DOWNLOAD, action,
						Constants.BROADCAST_ADD_BIRTHDAY, lucaObject, RaspberrySelection.BOTH);
				break;
			case MOVIE:
				_serviceController.StartRestService(Constants.MOVIE_DOWNLOAD, action, Constants.BROADCAST_ADD_MOVIE,
						lucaObject, RaspberrySelection.BOTH);
				break;
			case SCHEDULE:
			case TIMER:
				_serviceController.StartRestService(Constants.SCHEDULE_DOWNLOAD, action,
						Constants.BROADCAST_ADD_SCHEDULE, lucaObject, RaspberrySelection.BOTH);
				break;
			case WIRELESS_SOCKET:
				_serviceController.StartRestService(Constants.SOCKET_DOWNLOAD, action, Constants.BROADCAST_ADD_SOCKET,
						lucaObject, RaspberrySelection.BOTH);
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

			LucaObject lucaObject = (LucaObject) intent.getSerializableExtra(Constants.BUNDLE_LUCA_OBJECT);
			if (lucaObject == null) {
				_logger.Error("_updateReceiver received data with null lucaobject!");
				return;
			}

			String action = intent.getStringExtra(Constants.BUNDLE_ACTION);
			if (action == null) {
				_logger.Error("_updateReceiver received data with null action!");
				return;
			}

			switch (lucaObject) {
			case BIRTHDAY:
				_serviceController.StartRestService(Constants.BIRTHDAY_DOWNLOAD, action,
						Constants.BROADCAST_UPDATE_BIRTHDAY, lucaObject, RaspberrySelection.BOTH);
				break;
			case MOVIE:
				_serviceController.StartRestService(Constants.MOVIE_DOWNLOAD, action, Constants.BROADCAST_UPDATE_MOVIE,
						lucaObject, RaspberrySelection.BOTH);
				break;
			case SCHEDULE:
			case TIMER:
				_serviceController.StartRestService(Constants.SCHEDULE_DOWNLOAD, action,
						Constants.BROADCAST_UPDATE_SCHEDULE, lucaObject, RaspberrySelection.BOTH);
				break;
			case WIRELESS_SOCKET:
				_serviceController.StartRestService(Constants.SOCKET_DOWNLOAD, action,
						Constants.BROADCAST_UPDATE_SOCKET, lucaObject, RaspberrySelection.BOTH);
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

			LucaObject lucaObject = (LucaObject) intent.getSerializableExtra(Constants.BUNDLE_LUCA_OBJECT);
			if (lucaObject == null) {
				_logger.Error("_downloadReceiver received data with null lucaobject!");
				return;
			}

			String action = intent.getStringExtra(Constants.BUNDLE_ACTION);
			if (action != null) {
				_logger.Error("_startDownloadReceiver received data with action! Not performing download!");
				return;
			}

			switch (lucaObject) {
			case BIRTHDAY:
				String[] birthdayAnswerArray = intent.getStringArrayExtra(Constants.BIRTHDAY_DOWNLOAD);
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
					Toast.makeText(_context, "Add of birthday failed!", Toast.LENGTH_LONG).show();
				}
				break;
			case MOVIE:
				String[] movieAnswerArray = intent.getStringArrayExtra(Constants.MOVIE_DOWNLOAD);
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
					Toast.makeText(_context, "Add of movie failed!", Toast.LENGTH_LONG).show();
				}
				break;
			case SCHEDULE:
			case TIMER:
				String[] scheduleAnswerArray = intent.getStringArrayExtra(Constants.SCHEDULE_DOWNLOAD);
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
					Toast.makeText(_context, "Add of schedule failed!", Toast.LENGTH_LONG).show();
				}
				break;
			case WIRELESS_SOCKET:
				String[] socketAnswerArray = intent.getStringArrayExtra(Constants.SOCKET_DOWNLOAD);
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
					Toast.makeText(_context, "Add of socket failed!", Toast.LENGTH_LONG).show();
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

			_birthdayController = new BirthdayController();
			_broadcastController = new BroadcastController(_context);
			_dialogService = new DialogService(_context);
			_networkController = new NetworkController(_context,
					new DialogController(_context, ContextCompat.getColor(_context, R.color.TextIcon),
							ContextCompat.getColor(_context, R.color.Background)));
			_openWeatherController = new OpenWeatherController(_context, Constants.CITY);
			_receiverController = new ReceiverController(_context);
			_scheduleService = new ScheduleService();
			_serviceController = new ServiceController(_context);
			_sharedPrefController = new SharedPrefController(_context, Constants.SHARED_PREF_NAME);

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
			MainServiceAction action = (MainServiceAction) data.getSerializable(Constants.BUNDLE_MAIN_SERVICE_ACTION);
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
		_receiverController.RegisterReceiver(_actionReceiver,
				new String[] { Constants.BROADCAST_MAIN_SERVICE_COMMAND });
		_receiverController.RegisterReceiver(_birthdayDownloadReceiver,
				new String[] { Constants.BROADCAST_DOWNLOAD_BIRTHDAY_FINISHED });
		_receiverController.RegisterReceiver(_changeDownloadReceiver,
				new String[] { Constants.BROADCAST_DOWNLOAD_CHANGE_FINISHED });
		_receiverController.RegisterReceiver(_forecastModelReceiver,
				new String[] { OpenWeatherConstants.BROADCAST_GET_FORECAST_WEATHER_JSON_FINISHED });
		_receiverController.RegisterReceiver(_informationDownloadReceiver,
				new String[] { Constants.BROADCAST_DOWNLOAD_INFORMATION_FINISHED });
		_receiverController.RegisterReceiver(_mapContentDownloadReceiver,
				new String[] { Constants.BROADCAST_DOWNLOAD_MAP_CONTENT_FINISHED });
		_receiverController.RegisterReceiver(_movieDownloadReceiver,
				new String[] { Constants.BROADCAST_DOWNLOAD_MOVIE_FINISHED });
		_receiverController.RegisterReceiver(_scheduleDownloadReceiver,
				new String[] { Constants.BROADCAST_DOWNLOAD_SCHEDULE_FINISHED });
		_receiverController.RegisterReceiver(_socketDownloadReceiver,
				new String[] { Constants.BROADCAST_DOWNLOAD_SOCKET_FINISHED });
		_receiverController.RegisterReceiver(_soundDownloadReceiver,
				new String[] { Constants.BROADCAST_IS_SOUND_PLAYING });
		_receiverController.RegisterReceiver(_temperatureDownloadReceiver,
				new String[] { Constants.BROADCAST_DOWNLOAD_TEMPERATURE_FINISHED });
		_receiverController.RegisterReceiver(_weatherModelReceiver,
				new String[] { OpenWeatherConstants.BROADCAST_GET_CURRENT_WEATHER_JSON_FINISHED });

		_receiverController.RegisterReceiver(_addReceiver, new String[] { Constants.BROADCAST_ADD_BIRTHDAY,
				Constants.BROADCAST_ADD_MOVIE, Constants.BROADCAST_ADD_SCHEDULE, Constants.BROADCAST_ADD_SOCKET });
		_receiverController.RegisterReceiver(_updateReceiver,
				new String[] { Constants.BROADCAST_UPDATE_BIRTHDAY, Constants.BROADCAST_UPDATE_MOVIE,
						Constants.BROADCAST_UPDATE_SCHEDULE, Constants.BROADCAST_UPDATE_SOCKET });
		_receiverController.RegisterReceiver(_startDownloadReceiver,
				new String[] { Constants.BROADCAST_ADD_BIRTHDAY, Constants.BROADCAST_ADD_MOVIE,
						Constants.BROADCAST_ADD_SCHEDULE, Constants.BROADCAST_ADD_SOCKET,
						Constants.BROADCAST_UPDATE_BIRTHDAY, Constants.BROADCAST_UPDATE_MOVIE,
						Constants.BROADCAST_UPDATE_SCHEDULE, Constants.BROADCAST_UPDATE_SOCKET });

		_receiverController.RegisterReceiver(_reloadBirthdayReceiver,
				new String[] { Constants.BROADCAST_RELOAD_BIRTHDAY });
		_receiverController.RegisterReceiver(_reloadMovieReceiver, new String[] { Constants.BROADCAST_RELOAD_MOVIE });
		_receiverController.RegisterReceiver(_reloadScheduleReceiver,
				new String[] { Constants.BROADCAST_RELOAD_SCHEDULE, Constants.BROADCAST_RELOAD_TIMER });
		_receiverController.RegisterReceiver(_reloadSocketReceiver,
				new String[] { Constants.BROADCAST_RELOAD_SOCKETS });

		_receiverController.RegisterReceiver(_reloadChangeReceiver,
				new String[] { Constants.BROADCAST_RELOAD_BIRTHDAY, Constants.BROADCAST_RELOAD_MOVIE,
						Constants.BROADCAST_RELOAD_SCHEDULE, Constants.BROADCAST_RELOAD_TIMER,
						Constants.BROADCAST_RELOAD_SOCKETS });

		_context.registerReceiver(_batteryChangedReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
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

		_context.unregisterReceiver(_batteryChangedReceiver);
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
		if (!_sharedPrefController.LoadBooleanValueFromSharedPreferences(Constants.SHARED_PREF_INSTALLED)) {
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
			Toast.makeText(_context, "No LucaHome network! ...", Toast.LENGTH_LONG).show();
			return;
		}

		if (!_downloadingData) {
			if (!_sharedPrefController.LoadBooleanValueFromSharedPreferences(Constants.USER_DATA_ENTERED)) {
				_dialogService.ShowUserCredentialsDialog(null, _startDownloadRunnable, false);
			} else {
				Calendar now = Calendar.getInstance();
				if (_lastUpdate != null) {
					long difference = Calendar.getInstance().getTimeInMillis() - _lastUpdate.getTimeInMillis();

					if (difference < 60000) {
						_logger.Warn("Just updated the data!");

						_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_COMMAND,
								new String[] { Constants.BUNDLE_COMMAND, Constants.BUNDLE_NAVIGATE_DATA },
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
		_broadcastController.SendSerializableBroadcast(Constants.BROADCAST_COMMAND, Constants.BUNDLE_COMMAND,
				Command.SHOW_USER_LOGIN_DIALOG);

		_sharedPrefController.SaveBooleanValue(Constants.SHARED_PREF_INSTALLED, true);

		_sharedPrefController.SaveBooleanValue(Constants.DISPLAY_SOCKET_NOTIFICATION, true);
		_sharedPrefController.SaveBooleanValue(Constants.DISPLAY_WEATHER_NOTIFICATION, true);
		_sharedPrefController.SaveBooleanValue(Constants.DISPLAY_TEMPERATURE_NOTIFICATION, true);

		_sharedPrefController.SaveBooleanValue(Constants.START_AUDIO_APP, true);
		_sharedPrefController.SaveBooleanValue(Constants.START_OSMC_APP, true);

		_sharedPrefController.SaveIntegerValue(Constants.SOUND_RASPBERRY_SELECTION,
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

		if (_sharedPrefController.LoadBooleanValueFromSharedPreferences(Constants.DISPLAY_WEATHER_NOTIFICATION)) {
			_context.startService(new Intent(_context, OpenWeatherService.class));
		}

		startTimeout();
	}

	private void updateDownloadCount() {
		_progress++;
		_broadcastController.SendIntBroadcast(Constants.BROADCAST_UPDATE_PROGRESSBAR, Constants.BUNDLE_VIEW_PROGRESS,
				_progress);
		if (_progress >= _downloadCount) {
			stopTimeout();
			_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_COMMAND,
					new String[] { Constants.BUNDLE_COMMAND, Constants.BUNDLE_NAVIGATE_DATA },
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
				_sharedPrefController.SaveBooleanValue(key, true);
			}
		}
	}

	private void startDownloadBirthday() {
		_serviceController.StartRestService(Constants.BIRTHDAY_DOWNLOAD, Constants.ACTION_GET_BIRTHDAYS,
				Constants.BROADCAST_DOWNLOAD_BIRTHDAY_FINISHED, LucaObject.BIRTHDAY, RaspberrySelection.BOTH);
	}

	private void startDownloadChange() {
		_serviceController.StartRestService(Constants.CHANGE_DOWNLOAD, Constants.ACTION_GET_CHANGES,
				Constants.BROADCAST_DOWNLOAD_CHANGE_FINISHED, LucaObject.CHANGE, RaspberrySelection.BOTH);
	}

	private void startDownloadCurrentWeather() {
		_openWeatherController.loadCurrentWeather();
	}

	private void startDownloadForecastWeather() {
		_openWeatherController.loadForecastWeather();
	}

	private void startDownloadInformation() {
		_serviceController.StartRestService(Constants.INFORMATION_DOWNLOAD, Constants.ACTION_GET_INFORMATIONS,
				Constants.BROADCAST_DOWNLOAD_INFORMATION_FINISHED, LucaObject.INFORMATION, RaspberrySelection.BOTH);
	}

	private void startDownloadMapContent() {
		_serviceController.StartRestService(Constants.MAP_CONTENT_DOWNLOAD, Constants.ACTION_GET_MAP_CONTENTS,
				Constants.BROADCAST_DOWNLOAD_MAP_CONTENT_FINISHED, LucaObject.MAP_CONTENT, RaspberrySelection.BOTH);
	}

	private void startDownloadMovie() {
		_serviceController.StartRestService(Constants.MOVIE_DOWNLOAD, Constants.ACTION_GET_MOVIES,
				Constants.BROADCAST_DOWNLOAD_MOVIE_FINISHED, LucaObject.MOVIE, RaspberrySelection.BOTH);
	}

	private void startDownloadSchedule() {
		_serviceController.StartRestService(Constants.SCHEDULE_DOWNLOAD, Constants.ACTION_GET_SCHEDULES,
				Constants.BROADCAST_DOWNLOAD_SCHEDULE_FINISHED, LucaObject.SCHEDULE, RaspberrySelection.BOTH);
	}

	private void startDownloadSocket() {
		_serviceController.StartRestService(Constants.SOCKET_DOWNLOAD, Constants.ACTION_GET_SOCKETS,
				Constants.BROADCAST_DOWNLOAD_SOCKET_FINISHED, LucaObject.WIRELESS_SOCKET, RaspberrySelection.BOTH);
	}

	private void startDownloadIsSoundPlaying() {
		_serviceController.StartRestService(TAG, Constants.ACTION_IS_SOUND_PLAYING,
				Constants.BROADCAST_IS_SOUND_PLAYING, LucaObject.SOUND, RaspberrySelection.BOTH);
	}

	private void startDownloadTemperature() {
		_serviceController.StartRestService(Constants.TEMPERATURE_DOWNLOAD, Constants.ACTION_GET_TEMPERATURES,
				Constants.BROADCAST_DOWNLOAD_TEMPERATURE_FINISHED, LucaObject.TEMPERATURE, RaspberrySelection.BOTH);
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