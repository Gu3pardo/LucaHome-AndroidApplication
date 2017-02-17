package guepardoapps.lucahome.services.helper;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.classes.*;
import guepardoapps.lucahome.common.constants.Broadcasts;
import guepardoapps.lucahome.common.constants.Bundles;
import guepardoapps.lucahome.common.constants.SharedPrefConstants;
import guepardoapps.lucahome.common.controller.ServiceController;
import guepardoapps.lucahome.common.dto.BirthdayDto;
import guepardoapps.lucahome.common.dto.MovieDto;
import guepardoapps.lucahome.common.dto.ScheduleDto;
import guepardoapps.lucahome.common.dto.TimerDto;
import guepardoapps.lucahome.common.dto.UserDto;
import guepardoapps.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.lucahome.common.enums.*;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.lucahome.view.controller.ScheduleController;
import guepardoapps.lucahome.view.controller.SocketController;
import guepardoapps.lucahome.view.controller.SoundController;
import guepardoapps.lucahome.view.controller.TimerController;
import guepardoapps.lucahome.view.customadapter.YoutubeVideoListAdapter;

import guepardoapps.mediamirror.common.dto.YoutubeVideoDto;

import guepardoapps.toolset.common.enums.Weekday;
import guepardoapps.toolset.controller.*;
import guepardoapps.toolset.services.*;

public class DialogService extends DialogController {

	private static final String TAG = DialogService.class.getName();
	private LucaHomeLogger _logger;

	private LucaObject _lucaObject;
	private BirthdayDto _birthday;
	private MovieDto _movie;
	private WirelessSocketDto _socket;
	private ScheduleDto _schedule;
	private TimerDto _timer;

	private boolean _socketListInitialized;
	private SerializableList<WirelessSocketDto> _socketList;

	private String _scheduleName;
	private String _scheduleSocketString;
	private String _scheduleWeekdayString;
	private String _scheduleActionString;
	private Time _scheduleTime;
	private boolean _schedulePlaySound;
	private RaspberrySelection _schedulePlayRaspberry;

	private String _timerName;
	private String _timerSocketString;
	private Time _timerTime;
	private boolean _timerPlaySound = false;
	private RaspberrySelection _timerPlayRaspberry;

	private MailService _mailService;
	private ScheduleController _scheduleController;
	private ServiceController _serviceController;
	private SharedPrefController _sharedPrefController;
	private SocketController _socketController;
	private TimerController _timerController;

	private UserService _userService;

	private Runnable _storedRunnable = null;

	private Runnable _userValidationCallback = new Runnable() {
		@Override
		public void run() {
			if (_userService.GetValidationResult()) {
				_sharedPrefController.SaveBooleanValue(SharedPrefConstants.USER_DATA_ENTERED, true);

				CloseDialogCallback.run();
				if (_storedRunnable != null) {
					_storedRunnable.run();
					_storedRunnable = null;
				}
				resetValues();
			} else {
				final EditText userNameEdit = (EditText) _dialog.findViewById(R.id.dialog_user_input);
				userNameEdit.setText("Invalid user!");
				userNameEdit.selectAll();
			}
		}
	};

	private Runnable _sendMail = new Runnable() {
		@Override
		public void run() {
			_mailService.SendMail("guepardoapps@gmail.com", false);
		}
	};

	private Runnable _updateRunnable = new Runnable() {
		@Override
		public void run() {
			checkOpenDialog();

			switch (_lucaObject) {
			case BIRTHDAY:
				if (_birthday != null) {
					ShowAddBirthdayDialog(_birthday.GetId(), null, _birthday, false);
				} else {
					Toasty.error(_context, "_birthday is null!", Toast.LENGTH_LONG).show();
					_logger.Warn("_birthday is null!");
				}
				break;
			case MOVIE:
				if (_movie != null) {
					ShowAddMovieDialog(null, _movie, false);
				} else {
					Toasty.error(_context, "_movie is null!", Toast.LENGTH_LONG).show();
					_logger.Warn("_movie is null!");
				}
				break;
			case WIRELESS_SOCKET:
				if (_socket != null) {
					ShowAddSocketDialog(null, _socket, false);
				} else {
					Toasty.error(_context, "_socket is null!", Toast.LENGTH_LONG).show();
					_logger.Warn("_socket is null!");
				}
				break;
			case SCHEDULE:
				if (_schedule != null) {
					if (_socketList != null) {
						ShowAddScheduleDialog(null, _socketList, _schedule, false);
					} else {
						Toasty.error(_context, "_socketList is null!", Toast.LENGTH_LONG).show();
						_logger.Warn("_socketList is null!");
					}
				} else {
					Toasty.error(_context, "_schedule is null!", Toast.LENGTH_LONG).show();
					_logger.Warn("_schedule is null!");
				}
				break;
			case TIMER:
				if (_timer != null) {
					if (_socketList != null) {
						ShowAddTimerDialog(null, _socketList, _timer, false);
					} else {
						Toasty.error(_context, "_socketList is null!", Toast.LENGTH_LONG).show();
						_logger.Warn("_socketList is null!");
					}
				} else {
					Toasty.error(_context, "_timer is null!", Toast.LENGTH_LONG).show();
					_logger.Warn("_timer is null!");
				}
				break;
			default:
				_logger.Warn("Not possible to update object " + _lucaObject.toString());
				break;
			}
		}
	};

	private Runnable _deletePromptRunnable = new Runnable() {
		@Override
		public void run() {
			checkOpenDialog();

			if (_lucaObject == null) {
				_logger.Error("_lucaObject is null!");
				CloseDialogCallback.run();
				resetValues();
				return;
			}

			switch (_lucaObject) {
			case BIRTHDAY:
				ShowDialogDouble("Delete Birthday", "Do you really want to delete the birthday?", "Yes",
						_deleteRunnable, "Cancel", CloseDialogCallback, false);
				_isDialogOpen = true;
				break;
			case MOVIE:
				ShowDialogDouble("Delete Movie", "Do you really want to delete the movie?", "Yes", _deleteRunnable,
						"Cancel", CloseDialogCallback, false);
				_isDialogOpen = true;
				break;
			case WIRELESS_SOCKET:
				ShowDialogDouble("Delete Socket", "Do you really want to delete the socket?", "Yes", _deleteRunnable,
						"Cancel", CloseDialogCallback, false);
				_isDialogOpen = true;
				break;
			case SCHEDULE:
				ShowDialogDouble("Delete Schedule", "Do you really want to delete the schedule?", "Yes",
						_deleteRunnable, "Cancel", CloseDialogCallback, false);
				_isDialogOpen = true;
				break;
			case TIMER:
				ShowDialogDouble("Delete Timer", "Do you really want to delete the timer?", "Yes", _deleteRunnable,
						"Cancel", CloseDialogCallback, false);
				_isDialogOpen = true;
				break;
			default:
				_logger.Warn("Not possible to delete object " + _lucaObject.toString());
				break;
			}
		}
	};

	private Runnable _deleteRunnable = new Runnable() {
		@Override
		public void run() {
			if (_lucaObject == null) {
				_logger.Error("_lucaObject is null");
				CloseDialogCallback.run();
				resetValues();
				return;
			}

			switch (_lucaObject) {
			case BIRTHDAY:
				if (_birthday == null) {
					_logger.Error("_birthday is null!");
					return;
				}
				_serviceController.StartRestService(_birthday.GetName(), _birthday.GetCommandDelete(),
						Broadcasts.RELOAD_BIRTHDAY, _lucaObject, RaspberrySelection.BOTH);
				break;
			case MOVIE:
				if (_movie == null) {
					_logger.Error("_movie is null!");
					return;
				}
				_serviceController.StartRestService(_movie.GetTitle(), _movie.GetCommandDelete(),
						Broadcasts.RELOAD_MOVIE, _lucaObject, RaspberrySelection.BOTH);
				break;
			case WIRELESS_SOCKET:
				if (_socket == null) {
					_logger.Error("_socket is null!");
					return;
				}
				_socketController.DeleteSocket(_socket);
				break;
			case SCHEDULE:
				if (_schedule == null) {
					_logger.Error("_schedule is null!");
					return;
				}
				_serviceController.StartRestService(_schedule.GetName(), _schedule.GetCommandDelete(),
						Broadcasts.RELOAD_SCHEDULE, _lucaObject, RaspberrySelection.BOTH);
				break;
			case TIMER:
				if (_timer == null) {
					_logger.Error("_timer is null!");
					return;
				}
				_serviceController.StartRestService(_timer.GetName(), _timer.GetCommandDelete(),
						Broadcasts.RELOAD_TIMER, _lucaObject, RaspberrySelection.BOTH);
				break;
			default:
				_logger.Warn("Still not possible to delete object " + _lucaObject.toString());
				break;
			}

			CloseDialogCallback.run();
			resetValues();
		}
	};

	public DialogService(Context context) {
		super(context, ContextCompat.getColor(context, R.color.TextIcon),
				ContextCompat.getColor(context, R.color.Background));
		_logger = new LucaHomeLogger(TAG);

		_context = context;

		_isDialogOpen = false;

		_socketList = null;
		_socketListInitialized = false;
		_schedulePlaySound = false;

		_mailService = new MailService(_context);
		_scheduleController = new ScheduleController(_context);
		_serviceController = new ServiceController(_context);
		_sharedPrefController = new SharedPrefController(_context, SharedPrefConstants.SHARED_PREF_NAME);
		_socketController = new SocketController(_context);
		_timerController = new TimerController(_context);

		_userService = new UserService(_context);
	}

	public void InitializeSocketList(SerializableList<WirelessSocketDto> socketList) {
		_logger.Debug("InitializeSocketList");
		if (socketList == null) {
			_logger.Warn("socketList is null!");
			return;
		}

		if (!_socketListInitialized) {
			_socketListInitialized = true;
			_logger.Info("_socketListInitialized: " + String.valueOf(_socketListInitialized));
			_socketList = socketList;
			_logger.Info("_socketList: " + _socketList.toString());
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	public void ShowTemperatureGraphDialog(String graphPath) {
		checkOpenDialog();

		createDialog("ShowTemperatureGraphDialog: " + graphPath, R.layout.dialog_temperature_graph);

		final ProgressBar progressBar = (ProgressBar) _dialog.findViewById(R.id.temperature_dialog_progressbar);

		final WebView webView = (WebView) _dialog.findViewById(R.id.temperature_dialog_webview);
		webView.getSettings().setBuiltInZoomControls(true);
		webView.getSettings().setSupportZoom(true);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setLoadWithOverviewMode(true);
		webView.setWebViewClient(new WebViewClient());
		webView.setWebChromeClient(new WebChromeClient());
		webView.setInitialScale(100);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(false);
		webView.loadUrl("http://" + graphPath);
		webView.setWebViewClient(new WebViewClient() {
			public void onPageFinished(WebView view, String url) {
				progressBar.setVisibility(View.GONE);
				webView.setVisibility(View.VISIBLE);
			}
		});

		Button btnOk = (Button) _dialog.findViewById(R.id.temperature_dialog_button);
		btnOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CloseDialogCallback.run();
				resetValues();
			}
		});

		showDialog(false);
	}

	public void ShowUserCredentialsDialog(UserDto user, final Runnable runnable, boolean isCancelable) {
		checkOpenDialog();

		createDialog("ShowUserCredentialsDialog", R.layout.dialog_user_data_update);

		final EditText userNameEdit = (EditText) _dialog.findViewById(R.id.dialog_user_input);
		final EditText passwordEdit = (EditText) _dialog.findViewById(R.id.dialog_password_input);

		if (user != null) {
			userNameEdit.setText(user.GetUserName());
			userNameEdit.selectAll();
			passwordEdit.setText(user.GetPassword());
		}

		Button btnSave = (Button) _dialog.findViewById(R.id.dialog_user_save_button);
		btnSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String userName = userNameEdit.getText().toString();
				if (userName.length() < 3 || userName.contains("Enter valid username!")
						|| userName.contains("Invalid user!")) {
					userNameEdit.setText("Enter valid username!");
					userNameEdit.selectAll();
					return;
				}

				String password = passwordEdit.getText().toString();
				if (password.length() < 4) {
					passwordEdit.selectAll();
					return;
				}

				_sharedPrefController.SaveStringValue(SharedPrefConstants.USER_NAME, userName);
				_sharedPrefController.SaveStringValue(SharedPrefConstants.USER_PASSPHRASE, password);

				if (runnable != null) {
					_storedRunnable = runnable;
				}

				_userService.ValidateUser(new UserDto(userName, password), _userValidationCallback);
			}
		});

		showDialog(isCancelable);
	}

	public void ShowUserDetailsDialog(final UserDto user, final Runnable runnable) {
		checkOpenDialog();

		createDialog("ShowUserDetailsDialog", R.layout.dialog_user_data);

		TextView userNameTextView = (TextView) _dialog.findViewById(R.id.dialog_user_name);
		userNameTextView.setText(user.GetUserName());

		TextView passwordTextView = (TextView) _dialog.findViewById(R.id.dialog_user_password);
		passwordTextView.setText(user.GetPassword());

		Button btnClose = (Button) _dialog.findViewById(R.id.dialog_user_close_button);
		btnClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CloseDialogCallback.run();
				resetValues();
			}
		});

		Button btnUpdate = (Button) _dialog.findViewById(R.id.dialog_user_update_button);
		btnUpdate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CloseDialogCallback.run();
				resetValues();

				// ShowUserCredentialsDialog(user, updateUserRunnable, true);
				Toasty.error(_context, "Not yet implemented!", Toast.LENGTH_SHORT).show();
				_logger.Warn("Update user not yet implemented!");
			}
		});

		showDialog(false);
	}

	public void ShowAddBirthdayDialog(final int id, final Runnable runnable, BirthdayDto birthday, final boolean add) {
		checkOpenDialog();

		createDialog("ShowAddBirthdayDialog", R.layout.dialog_add_birthday);

		final EditText birthdayNameEdit = (EditText) _dialog.findViewById(R.id.dialog_birthday_name_input);

		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);

		final DatePicker birthdayDatePicker = (DatePicker) _dialog.findViewById(R.id.dialog_birthday_datepicker);
		if (birthday != null) {
			birthdayNameEdit.setText(birthday.GetName());
			birthdayDatePicker.init(birthday.GetBirthday().get(Calendar.YEAR),
					birthday.GetBirthday().get(Calendar.MONTH), birthday.GetBirthday().get(Calendar.DAY_OF_MONTH),
					null);
		} else {
			birthdayDatePicker.init(year, month, day, null);
		}

		Button btnSave = (Button) _dialog.findViewById(R.id.dialog_birthday_save_button);
		btnSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String name = birthdayNameEdit.getText().toString();
				if (name.length() < 3) {
					Toasty.warning(_context, "Name too short!", Toast.LENGTH_LONG).show();
					return;
				}

				int birthdayDay = birthdayDatePicker.getDayOfMonth();
				int birthdayMonth = birthdayDatePicker.getMonth();
				int birthdayYear = birthdayDatePicker.getYear();
				Calendar birthdayDate = Calendar.getInstance();
				birthdayDate.set(Calendar.DAY_OF_MONTH, birthdayDay);
				birthdayDate.set(Calendar.MONTH, birthdayMonth);
				birthdayDate.set(Calendar.YEAR, birthdayYear);

				BirthdayDto newBirthday = new BirthdayDto(name, birthdayDate, id);
				_logger.Debug("new Birthday: " + newBirthday.toString());

				if (runnable != null) {
					runnable.run();
				}

				if (add) {
					sendBroadCast(Broadcasts.ADD_BIRTHDAY, LucaObject.BIRTHDAY, newBirthday.GetCommandAdd());
				} else {
					sendBroadCast(Broadcasts.UPDATE_BIRTHDAY, LucaObject.BIRTHDAY, newBirthday.GetCommandUpdate());
				}

				CloseDialogCallback.run();
			}
		});

		showDialog(true);
	}

	public void ShowAddMovieDialog(final Runnable runnable, MovieDto movie, final boolean add) {
		checkOpenDialog();

		createDialog("ShowAddMovieDialog", R.layout.dialog_add_movie);

		final EditText movieTitleEdit = (EditText) _dialog.findViewById(R.id.dialog_movie_title_input);
		final EditText movieGenreEdit = (EditText) _dialog.findViewById(R.id.dialog_movie_genre_input);
		final EditText movieDescriptionEdit = (EditText) _dialog.findViewById(R.id.dialog_movie_description_input);

		final RatingBar movieRatingbar = (RatingBar) _dialog.findViewById(R.id.dialog_movie_description_ratingbar);
		movieRatingbar.setEnabled(true);

		if (movie != null) {
			movieTitleEdit.setText(movie.GetTitle());
			movieGenreEdit.setText(movie.GetGenre());
			movieDescriptionEdit.setText(movie.GetDescription());
			movieRatingbar.setRating(movie.GetRating());
		}

		Button btnSave = (Button) _dialog.findViewById(R.id.dialog_movie_save_button);
		btnSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String title = movieTitleEdit.getText().toString();
				if (title.length() < 3) {
					Toasty.error(_context, "Title too short!", Toast.LENGTH_LONG).show();
					return;
				}

				String genre = movieGenreEdit.getText().toString();
				String description = movieDescriptionEdit.getText().toString();

				int rating = Math.round(movieRatingbar.getRating());

				MovieDto newMovie = new MovieDto(title, genre, description, rating, 0, null);
				_logger.Debug("new Movie: " + newMovie.toString());

				if (runnable != null) {
					runnable.run();
				}

				if (add) {
					sendBroadCast(Broadcasts.ADD_MOVIE, LucaObject.MOVIE, newMovie.GetCommandAdd());
				} else {
					sendBroadCast(Broadcasts.UPDATE_MOVIE, LucaObject.MOVIE, newMovie.GetCommandUpdate());
				}

				CloseDialogCallback.run();
				resetValues();
			}
		});

		showDialog(true);
	}

	public void ShowAddSocketDialog(final Runnable runnable, final WirelessSocketDto socket, final boolean add) {
		checkOpenDialog();

		createDialog("ShowAddSocketDialog", R.layout.dialog_add_socket);

		final EditText socketNameEdit = (EditText) _dialog.findViewById(R.id.dialog_socket_name_input);
		final EditText socketAreaEdit = (EditText) _dialog.findViewById(R.id.dialog_socket_area_input);
		final EditText socketCodeEdit = (EditText) _dialog.findViewById(R.id.dialog_socket_code_input);

		if (socket != null) {
			socketNameEdit.setText(socket.GetName());
			socketAreaEdit.setText(socket.GetArea());
			socketCodeEdit.setText(socket.GetCode());
		}

		Button btnSave = (Button) _dialog.findViewById(R.id.dialog_socket_save_button);
		btnSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String name = socketNameEdit.getText().toString();
				if (name.length() < 3) {
					Toasty.error(_context, "Name too short!", Toast.LENGTH_LONG).show();
					return;
				}
				String area = socketAreaEdit.getText().toString();
				if (area.length() < 3) {
					Toasty.error(_context, "Area too short!", Toast.LENGTH_LONG).show();
					return;
				}
				String code = socketCodeEdit.getText().toString();
				if (!_socketController.ValidateSocketCode(code)) {
					Toasty.error(_context, "Code invalid!", Toast.LENGTH_LONG).show();
					return;
				}

				boolean isActivated = false;
				if (socket != null) {
					isActivated = socket.GetIsActivated();
				}

				WirelessSocketDto newSocket = new WirelessSocketDto(name, area, code, isActivated);
				_logger.Debug("new Socket: " + newSocket.toString());

				if (runnable != null) {
					runnable.run();
				}

				if (add) {
					sendBroadCast(Broadcasts.ADD_SOCKET, LucaObject.WIRELESS_SOCKET, newSocket.GetCommandAdd());
				} else {
					sendBroadCast(Broadcasts.UPDATE_SOCKET, LucaObject.WIRELESS_SOCKET, newSocket.GetCommandUpdate());
				}

				CloseDialogCallback.run();
				resetValues();
			}
		});

		showDialog(true);
	}

	@SuppressWarnings("deprecation")
	public void ShowAddScheduleDialog(final Runnable runnable, final SerializableList<WirelessSocketDto> socketList,
			final ScheduleDto schedule, final boolean add) {
		checkOpenDialog();

		createDialog("ShowAddScheduleDialog", R.layout.dialog_add_schedule);

		final EditText scheduleNameEdit = (EditText) _dialog.findViewById(R.id.dialog_schedule_name_input);

		final Spinner scheduleSocketSelect = (Spinner) _dialog.findViewById(R.id.dialog_schedule_socket_select);
		List<String> sockets = new ArrayList<String>();
		for (int socketIndex = 0; socketIndex < socketList.getSize(); socketIndex++) {
			sockets.add(socketList.getValue(socketIndex).GetName());
		}
		ArrayAdapter<String> socketDataAdapter = new ArrayAdapter<String>(_context,
				android.R.layout.simple_spinner_item, sockets);
		socketDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		scheduleSocketSelect.setAdapter(socketDataAdapter);
		scheduleSocketSelect.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				_scheduleSocketString = parent.getItemAtPosition(position).toString();
				_logger.Debug(_scheduleSocketString);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		final Spinner scheduleWeekdaySelect = (Spinner) _dialog.findViewById(R.id.dialog_schedule_weekday_select);
		List<String> weekdays = new ArrayList<String>();
		for (Weekday weekday : Weekday.values()) {
			weekdays.add(weekday.GetEnglishDay());
		}
		ArrayAdapter<String> weekdayDataAdapter = new ArrayAdapter<String>(_context,
				android.R.layout.simple_spinner_item, weekdays);
		weekdayDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		scheduleWeekdaySelect.setAdapter(weekdayDataAdapter);
		scheduleWeekdaySelect.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				_scheduleWeekdayString = parent.getItemAtPosition(position).toString();
				_logger.Debug(_scheduleWeekdayString);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		final Spinner scheduleActionSelect = (Spinner) _dialog.findViewById(R.id.dialog_schedule_action_select);
		List<String> actions = new ArrayList<String>();
		actions.add("ON");
		actions.add("OFF");
		ArrayAdapter<String> actionDataAdapter = new ArrayAdapter<String>(_context,
				android.R.layout.simple_spinner_item, actions);
		actionDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		scheduleActionSelect.setAdapter(actionDataAdapter);
		scheduleActionSelect.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				_scheduleActionString = parent.getItemAtPosition(position).toString();
				_logger.Debug(_scheduleActionString);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		final TimePicker scheduleTimePicker = (TimePicker) _dialog.findViewById(R.id.dialog_schedule_time_picker);
		final Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		scheduleTimePicker.setIs24HourView(true);
		scheduleTimePicker.setCurrentHour(hour);
		scheduleTimePicker.setCurrentMinute(minute);

		CheckBox playSoundCheckbox = (CheckBox) _dialog.findViewById(R.id.dialog_schedule_playsound_select);
		playSoundCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				_schedulePlaySound = isChecked;
			}
		});

		final Spinner schedulePlayRaspberrySelect = (Spinner) _dialog
				.findViewById(R.id.dialog_schedule_playraspberry_select);
		List<String> raspberrys = new ArrayList<String>();
		raspberrys.add("Living Room");
		raspberrys.add("Sleeping Room");
		ArrayAdapter<String> raspberrysDataAdapter = new ArrayAdapter<String>(_context,
				android.R.layout.simple_spinner_item, raspberrys);
		raspberrysDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		schedulePlayRaspberrySelect.setAdapter(raspberrysDataAdapter);
		schedulePlayRaspberrySelect.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (parent.getItemAtPosition(position).toString().contains("Living Room")) {
					_schedulePlayRaspberry = RaspberrySelection.RASPBERRY_1;
				} else if (parent.getItemAtPosition(position).toString().contains("Sleeping Room")) {
					_schedulePlayRaspberry = RaspberrySelection.RASPBERRY_2;
				} else {
					_schedulePlayRaspberry = RaspberrySelection.DUMMY;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		if (schedule != null) {
			scheduleNameEdit.setText(schedule.GetName());
			for (int socketIndex = 0; socketIndex < socketList.getSize(); socketIndex++) {
				if (socketList.getValue(socketIndex).GetName().contains(schedule.GetSocket().GetName())) {
					scheduleSocketSelect.setSelection(socketIndex, true);
					break;
				}
			}
			scheduleWeekdaySelect.setSelection(schedule.GetWeekday().GetInt(), true);
			if (schedule.GetAction()) {
				scheduleActionSelect.setSelection(0, true);
			} else {
				scheduleActionSelect.setSelection(1, true);
			}
			scheduleTimePicker.setCurrentHour(schedule.GetTime().getHours());
			scheduleTimePicker.setCurrentMinute(schedule.GetTime().getMinutes());
			if (schedule.GetPlaySound()) {
				playSoundCheckbox.setChecked(true);
			}
			schedulePlayRaspberrySelect.setSelection(schedule.GetPlayRaspberry().GetInt() - 1, true);
		}

		Button btnSave = (Button) _dialog.findViewById(R.id.dialog_schedule_save_button);
		btnSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				_scheduleName = scheduleNameEdit.getText().toString();
				if (_scheduleName.length() < 3) {
					Toasty.error(_context, "Name too short!", Toast.LENGTH_LONG).show();
					return;
				}
				if (_scheduleSocketString == null || _scheduleSocketString == "") {
					Toasty.error(_context, "Please select a socket!", Toast.LENGTH_LONG).show();
					return;
				}
				if (_scheduleWeekdayString == null || _scheduleWeekdayString == "") {
					Toasty.error(_context, "Please select a weekday!", Toast.LENGTH_LONG).show();
					return;
				}
				if (_scheduleActionString == null || _scheduleActionString == "") {
					Toasty.error(_context, "Please select an action!", Toast.LENGTH_LONG).show();
					return;
				}
				if (_schedulePlayRaspberry == null || _schedulePlayRaspberry == RaspberrySelection.DUMMY) {
					Toasty.error(_context, "Please select a valid raspberry!", Toast.LENGTH_LONG).show();
					return;
				}

				WirelessSocketDto socket = null;
				for (int socketIndex = 0; socketIndex < socketList.getSize(); socketIndex++) {
					if (socketList.getValue(socketIndex).GetName().contains(_scheduleSocketString)) {
						socket = socketList.getValue(socketIndex);
						break;
					}
				}
				if (socket == null) {
					Toasty.error(_context, "Please select a valid socket!", Toast.LENGTH_LONG).show();
					return;
				}

				Weekday weekday = Weekday.GetByEnglishString(_scheduleWeekdayString);
				if (weekday == Weekday.NULL || weekday == null) {
					Toasty.error(_context, "Please select a valid weekday!", Toast.LENGTH_LONG).show();
					return;
				}

				int scheduleHour = scheduleTimePicker.getCurrentHour();
				int scheduleMinute = scheduleTimePicker.getCurrentMinute();
				_scheduleTime = new Time(scheduleHour, scheduleMinute, 0);

				boolean action;
				if (_scheduleActionString.contains("ON")) {
					action = true;
				} else if (_scheduleActionString.contains("OFF")) {
					action = false;
				} else {
					Toast.makeText(_context, "Please select a valid action!", Toast.LENGTH_LONG).show();
					return;
				}

				boolean isActive = true;
				if (schedule != null) {
					isActive = schedule.GetIsActive();
				}

				ScheduleDto newSchedule = new ScheduleDto(_scheduleName, socket, weekday, _scheduleTime, action, false,
						_schedulePlaySound, _schedulePlayRaspberry, isActive);
				_logger.Debug("new Schedule: " + newSchedule.toString());

				if (runnable != null) {
					runnable.run();
				}

				if (add) {
					sendBroadCast(Broadcasts.ADD_SCHEDULE, LucaObject.SCHEDULE, newSchedule.GetCommandAdd());
				} else {
					sendBroadCast(Broadcasts.UPDATE_SCHEDULE, LucaObject.SCHEDULE, newSchedule.GetCommandUpdate());
				}

				CloseDialogCallback.run();
				resetValues();
			}
		});

		showDialog(true);
	}

	@SuppressWarnings("deprecation")
	public void ShowAddTimerDialog(final Runnable runnable, final SerializableList<WirelessSocketDto> socketList,
			TimerDto timer, final boolean add) {
		checkOpenDialog();

		createDialog("ShowAddTimerDialog", R.layout.dialog_add_timer);

		final EditText timerNameEdit = (EditText) _dialog.findViewById(R.id.dialog_timer_name_input);

		final Spinner timerSocketSelect = (Spinner) _dialog.findViewById(R.id.dialog_timer_socket_select);
		List<String> sockets = new ArrayList<String>();
		for (int socketIndex = 0; socketIndex < socketList.getSize(); socketIndex++) {
			sockets.add(socketList.getValue(socketIndex).GetName());
		}
		ArrayAdapter<String> socketDataAdapter = new ArrayAdapter<String>(_context,
				android.R.layout.simple_spinner_item, sockets);
		socketDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		timerSocketSelect.setAdapter(socketDataAdapter);
		timerSocketSelect.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				_timerSocketString = parent.getItemAtPosition(position).toString();
				_logger.Debug(_timerSocketString);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		final TimePicker timerTimePicker = (TimePicker) _dialog.findViewById(R.id.dialog_timer_time_picker);
		final Calendar c = Calendar.getInstance();
		final int currentDay = c.get(Calendar.DAY_OF_WEEK);
		final int currentHour = c.get(Calendar.HOUR_OF_DAY);
		final int currentMinute = c.get(Calendar.MINUTE);
		timerTimePicker.setIs24HourView(true);
		timerTimePicker.setCurrentHour(currentHour);
		timerTimePicker.setCurrentMinute(currentMinute);

		CheckBox timerPlaySoundCheckbox = (CheckBox) _dialog.findViewById(R.id.dialog_timer_playsound_select);
		timerPlaySoundCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				_timerPlaySound = isChecked;
			}
		});

		final Spinner timerPlayRaspberrySelect = (Spinner) _dialog.findViewById(R.id.dialog_timer_playraspberry_select);
		List<String> raspberrys = new ArrayList<String>();
		raspberrys.add("Living Room");
		raspberrys.add("Sleeping Room");
		ArrayAdapter<String> raspberrysDataAdapter = new ArrayAdapter<String>(_context,
				android.R.layout.simple_spinner_item, raspberrys);
		raspberrysDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		timerPlayRaspberrySelect.setAdapter(raspberrysDataAdapter);
		timerPlayRaspberrySelect.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (parent.getItemAtPosition(position).toString().contains("Living Room")) {
					_timerPlayRaspberry = RaspberrySelection.RASPBERRY_1;
				} else if (parent.getItemAtPosition(position).toString().contains("Sleeping Room")) {
					_timerPlayRaspberry = RaspberrySelection.RASPBERRY_2;
				} else {
					_timerPlayRaspberry = RaspberrySelection.DUMMY;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		if (timer != null) {
			timerNameEdit.setText(timer.GetName());
			for (int socketIndex = 0; socketIndex < socketList.getSize(); socketIndex++) {
				if (socketList.getValue(socketIndex).GetName().contains(timer.GetSocket().GetName())) {
					timerSocketSelect.setSelection(socketIndex, true);
					break;
				}
			}
			timerTimePicker.setCurrentHour(timer.GetTime().getHours());
			timerTimePicker.setCurrentMinute(timer.GetTime().getMinutes());
			if (timer.GetPlaySound()) {
				timerPlaySoundCheckbox.setChecked(true);
			}
			timerPlayRaspberrySelect.setSelection(timer.GetPlayRaspberry().GetInt() - 1, true);
		}

		Button btnSave = (Button) _dialog.findViewById(R.id.dialog_timer_save_button);
		btnSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				_timerName = timerNameEdit.getText().toString();
				if (_timerName.length() < 3) {
					Toasty.error(_context, "Name too short!", Toast.LENGTH_LONG).show();
					return;
				}
				if (_timerSocketString == null || _timerSocketString == "") {
					Toasty.error(_context, "Please select a socket!", Toast.LENGTH_LONG).show();
					return;
				}
				if (_timerPlayRaspberry == null || _timerPlayRaspberry == RaspberrySelection.DUMMY) {
					Toasty.error(_context, "Please select a valid raspberry!", Toast.LENGTH_LONG).show();
					return;
				}

				WirelessSocketDto socket = null;
				for (int socketIndex = 0; socketIndex < socketList.getSize(); socketIndex++) {
					if (socketList.getValue(socketIndex).GetName().contains(_timerSocketString)) {
						socket = socketList.getValue(socketIndex);
						break;
					}
				}
				if (socket == null) {
					Toasty.error(_context, "Please select a valid socket!", Toast.LENGTH_LONG).show();
					return;
				}

				int timerDay = currentDay;
				int timerHour = currentHour + timerTimePicker.getCurrentHour();
				int timerMinute = currentMinute + timerTimePicker.getCurrentMinute();

				while (timerMinute > 60) {
					timerMinute -= 60;
					timerHour++;
				}
				while (timerHour > 24) {
					timerHour -= 24;
					timerDay++;
				}
				_timerTime = new Time(timerHour, timerMinute, 0);

				while (timerDay > 7) {
					timerDay -= 7;
				}
				Weekday weekday = Weekday.GetById(timerDay);

				_serviceController.StartRestService(socket.GetName(), socket.GetCommandSet(true),
						Broadcasts.RELOAD_SOCKETS, LucaObject.WIRELESS_SOCKET, RaspberrySelection.BOTH);

				if (_timerPlaySound && socket.GetName().contains("Sound")) {
					SoundController soundController = new SoundController(_context);
					soundController.StartSound("ALARM", _timerPlayRaspberry);
				}

				TimerDto newTimer = new TimerDto(_timerName, socket, weekday, _timerTime, false, _timerPlaySound,
						_timerPlayRaspberry, true);
				_logger.Debug("new Timer: " + _timerName.toString());

				if (runnable != null) {
					runnable.run();
				}

				if (add) {
					sendBroadCast(Broadcasts.ADD_SCHEDULE, LucaObject.TIMER, newTimer.GetCommandAdd());
				} else {
					sendBroadCast(Broadcasts.UPDATE_SCHEDULE, LucaObject.TIMER, newTimer.GetCommandUpdate());
				}

				CloseDialogCallback.run();
				resetValues();
			}
		});

		showDialog(true);
	}

	public void ShowSendInformationMailDialog() {
		ShowDialogSingle("Send Mail", "", "Send", _sendMail, true);
	}

	public void ShowUpdateBirthdayDialog(BirthdayDto value) {
		_lucaObject = LucaObject.BIRTHDAY;
		_birthday = value;
		ShowDialogTriple("Birthday", _birthday.GetName(), "Update", _updateRunnable, "Delete", _deletePromptRunnable,
				"Cancel", CloseDialogCallback, false);
		_isDialogOpen = true;
	}

	public void ShowUpdateMovieDialog(MovieDto value) {
		_lucaObject = LucaObject.MOVIE;
		_movie = value;
		ShowDialogTriple("Movie", _movie.GetTitle(), "Update", _updateRunnable, "Delete", _deletePromptRunnable,
				"Cancel", CloseDialogCallback, false);
	}

	public void ShowUpdateSocketDialog(WirelessSocketDto value) {
		_lucaObject = LucaObject.WIRELESS_SOCKET;
		_socket = value;
		ShowDialogTriple("Socket", _socket.GetName(), "Update", _updateRunnable, "Delete", _deletePromptRunnable,
				"Cancel", CloseDialogCallback, false);
	}

	public void ShowUpdateScheduleDialog(ScheduleDto value) {
		if (!_socketListInitialized) {
			Toasty.warning(_context, "SocketList not initialized!", Toast.LENGTH_SHORT).show();
			_logger.Warn("SocketList not initialized!");
			return;
		}
		_lucaObject = LucaObject.SCHEDULE;
		_schedule = value;
		ShowDialogTriple("Schedule", _schedule.GetName(), "Update", _updateRunnable, "Delete", _deletePromptRunnable,
				"Cancel", CloseDialogCallback, false);
	}

	public void ShowUpdateTimerDialog(TimerDto value) {
		if (!_socketListInitialized) {
			Toasty.warning(_context, "SocketList not initialized!", Toast.LENGTH_SHORT).show();
			_logger.Warn("SocketList not initialized!");
			return;
		}
		_lucaObject = LucaObject.TIMER;
		_timer = value;
		ShowDialogTriple("Timer", _timer.GetName(), "Update", _updateRunnable, "Delete", _deletePromptRunnable,
				"Cancel", CloseDialogCallback, false);
	}

	public void ShowSelectYoutubeIdDialog(ArrayList<YoutubeVideoDto> youtubeVideoList) {
		checkOpenDialog();

		createDialog("ShowSelectYoutubeIdDialog", R.layout.dialog_skeleton_list);

		TextView title = (TextView) _dialog.findViewById(R.id.dialog_list_title);
		title.setText("Select a video");

		YoutubeVideoListAdapter listAdapter = new YoutubeVideoListAdapter(_context, youtubeVideoList);
		ListView listView = (ListView) _dialog.findViewById(R.id.dialog_list_view);
		listView.setAdapter(listAdapter);
		listView.setVisibility(View.VISIBLE);

		showDialog(true);
	}

	public void ShowMapSocketDialog(final WirelessSocketDto socket, final SerializableList<ScheduleDto> scheduleList,
			final SerializableList<TimerDto> timerList) {
		checkOpenDialog();

		createDialog("ShowMapSocketDialog", R.layout.dialog_map_socket);
		_logger.Debug("For socket: " + socket.GetName());

		TextView socketNameTextView = (TextView) _dialog.findViewById(R.id.dialog_map_socket_name);
		socketNameTextView.setText(socket.GetName());
		TextView socketAreaTextView = (TextView) _dialog.findViewById(R.id.dialog_map_socket_area);
		socketAreaTextView.setText(socket.GetArea());
		TextView socketCodeTextView = (TextView) _dialog.findViewById(R.id.dialog_map_socket_code);
		socketCodeTextView.setText(socket.GetCode());

		Switch socketState = (Switch) _dialog.findViewById(R.id.dialog_map_socket_switch);
		socketState.setChecked(socket.GetIsActivated());
		socketState.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				_socketController.SetSocket(socket, isChecked);
				_socketController.CheckMedia(socket);
			}
		});

		Spinner scheduleSpinner = (Spinner) _dialog.findViewById(R.id.dialog_map_socket_schedules_spinner);
		List<String> scheduleNames = new ArrayList<String>();
		for (int index = 0; index < scheduleList.getSize(); index++) {
			scheduleNames.add(scheduleList.getValue(index).GetName());
		}
		ArrayAdapter<String> scheduleDataAdapter = new ArrayAdapter<String>(_context,
				android.R.layout.simple_spinner_item, scheduleNames);
		scheduleDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		scheduleSpinner.setAdapter(scheduleDataAdapter);
		scheduleSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			private boolean initialized = false;

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (!initialized) {
					initialized = true;
					_logger.Warn("initializing scheduleDataAdapter!");
					return;
				}

				String stringSchedule = parent.getItemAtPosition(position).toString();
				ScheduleDto selectedSchedule = null;
				for (int index = 0; index < scheduleList.getSize(); index++) {
					if (scheduleList.getValue(index).GetName().contains(stringSchedule)) {
						selectedSchedule = scheduleList.getValue(index);
						break;
					}
				}
				if (selectedSchedule != null) {
					_logger.Debug(selectedSchedule.toString());
					CloseDialogCallback.run();
					ShowMapScheduleDialog(selectedSchedule);
				} else {
					_logger.Warn("No schedule found for: " + stringSchedule);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		Spinner timerSpinner = (Spinner) _dialog.findViewById(R.id.dialog_map_socket_timer_spinner);
		List<String> timerNames = new ArrayList<String>();
		for (int index = 0; index < timerList.getSize(); index++) {
			timerNames.add(timerList.getValue(index).GetName());
		}
		ArrayAdapter<String> timerDataAdapter = new ArrayAdapter<String>(_context, android.R.layout.simple_spinner_item,
				timerNames);
		timerDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		timerSpinner.setAdapter(timerDataAdapter);
		timerSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			private boolean initialized = false;

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (!initialized) {
					initialized = true;
					_logger.Warn("initializing scheduleDataAdapter!");
					return;
				}

				String stringTimer = parent.getItemAtPosition(position).toString();
				TimerDto selectedTimer = null;
				for (int index = 0; index < timerList.getSize(); index++) {
					if (timerList.getValue(index).GetName().contains(stringTimer)) {
						selectedTimer = timerList.getValue(index);
						break;
					}
				}
				if (selectedTimer != null) {
					_logger.Debug(selectedTimer.toString());
					CloseDialogCallback.run();
					ShowMapTimerDialog(selectedTimer);
				} else {
					_logger.Warn("No timer found for: " + stringTimer);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		Button btnClose = (Button) _dialog.findViewById(R.id.dialog_map_socket_button_close);
		btnClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CloseDialogCallback.run();
			}
		});

		showDialog(true);
	}

	private void ShowMapScheduleDialog(final ScheduleDto selectedSchedule) {
		checkOpenDialog();

		createDialog("ShowMapScheduleDialog", R.layout.dialog_map_schedule);
		_logger.Debug("For schedule: " + selectedSchedule.GetName());

		TextView nameTextView = (TextView) _dialog.findViewById(R.id.dialog_map_schedule_name);
		nameTextView.setText(selectedSchedule.GetName());

		TextView timeTextView = (TextView) _dialog.findViewById(R.id.dialog_map_schedule_time);
		timeTextView.setText(selectedSchedule.GetTime().toString());

		TextView weekdayTextView = (TextView) _dialog.findViewById(R.id.dialog_map_schedule_weekday);
		weekdayTextView.setText(selectedSchedule.GetWeekday().toString());

		TextView socketTextView = (TextView) _dialog.findViewById(R.id.dialog_map_schedule_socket);
		socketTextView.setText(selectedSchedule.GetSocket().GetName());

		TextView actionTextView = (TextView) _dialog.findViewById(R.id.dialog_map_schedule_action);
		if (selectedSchedule.GetAction()) {
			actionTextView.setText(String.valueOf("Activate"));
		} else {
			actionTextView.setText(String.valueOf("Deactivate"));
		}

		TextView playSoundTextView = (TextView) _dialog.findViewById(R.id.dialog_map_schedule_playsound);
		if (selectedSchedule.GetPlaySound()) {
			playSoundTextView.setText(String.valueOf("Sound"));
		} else {
			playSoundTextView.setText(String.valueOf("-/-"));
		}

		Button buttonState = (Button) _dialog.findViewById(R.id.dialog_map_schedule_state);
		buttonState.setText(selectedSchedule.GetIsActiveString());
		buttonState.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				_scheduleController.SetSchedule(selectedSchedule, !selectedSchedule.GetIsActive());
				CloseDialogCallback.run();
			}
		});

		Button buttonDelete = (Button) _dialog.findViewById(R.id.dialog_map_schedule_delete);
		buttonDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				_scheduleController.DeleteSchedule(selectedSchedule);
				CloseDialogCallback.run();
			}
		});

		Button buttonClose = (Button) _dialog.findViewById(R.id.dialog_map_schedule_button_close);
		buttonClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CloseDialogCallback.run();
			}
		});

		showDialog(true);
	}

	private void ShowMapTimerDialog(final TimerDto selectedTimer) {
		checkOpenDialog();

		createDialog("ShowMapTimerDialog", R.layout.dialog_map_timer);
		_logger.Debug("For timer: " + selectedTimer.GetName());

		TextView nameTextView = (TextView) _dialog.findViewById(R.id.dialog_map_timer_name);
		nameTextView.setText(selectedTimer.GetName());

		TextView timeTextView = (TextView) _dialog.findViewById(R.id.dialog_map_timer_time);
		timeTextView.setText(selectedTimer.GetTime().toString());

		TextView weekdayTextView = (TextView) _dialog.findViewById(R.id.dialog_map_timer_weekday);
		weekdayTextView.setText(selectedTimer.GetWeekday().toString());

		TextView socketTextView = (TextView) _dialog.findViewById(R.id.dialog_map_timer_socket);
		socketTextView.setText(selectedTimer.GetSocket().GetName());

		TextView actionTextView = (TextView) _dialog.findViewById(R.id.dialog_map_timer_action);
		if (selectedTimer.GetAction()) {
			actionTextView.setText(String.valueOf("Activate"));
		} else {
			actionTextView.setText(String.valueOf("Deactivate"));
		}

		TextView playSoundTextView = (TextView) _dialog.findViewById(R.id.dialog_map_timer_playsound);
		if (selectedTimer.GetPlaySound()) {
			playSoundTextView.setText(String.valueOf("Sound"));
		} else {
			playSoundTextView.setText(String.valueOf("-/-"));
		}

		Button buttonDelete = (Button) _dialog.findViewById(R.id.dialog_map_timer_delete);
		buttonDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				_timerController.Delete(selectedTimer);
				CloseDialogCallback.run();
			}
		});

		Button buttonClose = (Button) _dialog.findViewById(R.id.dialog_map_timer_button_close);
		buttonClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CloseDialogCallback.run();
			}
		});

		showDialog(true);
	}

	private void createDialog(String dialogType, int layout) {
		_logger.Debug(dialogType);

		_dialog = new Dialog(_context);

		_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		_dialog.setContentView(layout);
	}

	@SuppressWarnings("deprecation")
	private void showDialog(boolean isCancelable) {
		_logger.Debug("showDialog, isCancelable: " + String.valueOf(isCancelable));

		_dialog.setCancelable(isCancelable);
		_dialog.show();

		Window window = _dialog.getWindow();
		window.setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

		_isDialogOpen = true;
	}

	private void checkOpenDialog() {
		if (_isDialogOpen) {
			_logger.Warn("Closing other Dialog...");
			CloseDialogCallback.run();
		}
	}

	private void sendBroadCast(String broadcast, LucaObject lucaObject, String action) {
		Intent broadcastIntent = new Intent(broadcast);

		Bundle broadcastData = new Bundle();
		broadcastData.putSerializable(Bundles.LUCA_OBJECT, lucaObject);
		broadcastData.putString(Bundles.ACTION, action);
		broadcastIntent.putExtras(broadcastData);

		_context.sendBroadcast(broadcastIntent);
	}

	private void resetValues() {
		_lucaObject = null;
		_birthday = null;
		_movie = null;
		_socket = null;
		_schedule = null;
		_timer = null;
	}
}
