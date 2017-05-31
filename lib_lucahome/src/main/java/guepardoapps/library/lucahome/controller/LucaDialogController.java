package guepardoapps.library.lucahome.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import de.mateware.snacky.Snacky;
import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.R;
import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.constants.Constants;
import guepardoapps.library.lucahome.common.constants.Keys;
import guepardoapps.library.lucahome.common.constants.SharedPrefConstants;
import guepardoapps.library.lucahome.common.dto.BirthdayDto;
import guepardoapps.library.lucahome.common.dto.ListedMenuDto;
import guepardoapps.library.lucahome.common.dto.MenuDto;
import guepardoapps.library.lucahome.common.dto.MovieDto;
import guepardoapps.library.lucahome.common.dto.PlayedYoutubeVideoDto;
import guepardoapps.library.lucahome.common.dto.ScheduleDto;
import guepardoapps.library.lucahome.common.dto.ShoppingEntryDto;
import guepardoapps.library.lucahome.common.dto.TimerDto;
import guepardoapps.library.lucahome.common.dto.UserDto;
import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.library.lucahome.common.dto.YoutubeVideoDto;
import guepardoapps.library.lucahome.common.enums.HomeAutomationAction;
import guepardoapps.library.lucahome.common.enums.MediaServerAction;
import guepardoapps.library.lucahome.common.enums.MediaServerSelection;
import guepardoapps.library.lucahome.common.enums.ShoppingEntryGroup;
import guepardoapps.library.lucahome.common.enums.YoutubeId;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.customadapter.MenuListAdapter;
import guepardoapps.library.lucahome.customadapter.ShoppingListAdapter;
import guepardoapps.library.lucahome.customadapter.YoutubeVideoListAdapter;
import guepardoapps.library.lucahome.services.helper.UserService;
import guepardoapps.library.lucahome.tasks.DownloadYoutubeVideoTask;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.common.classes.SerializableTime;
import guepardoapps.library.toolset.common.enums.Weekday;
import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.DialogController;
import guepardoapps.library.toolset.controller.MailController;
import guepardoapps.library.toolset.controller.ReceiverController;
import guepardoapps.library.toolset.controller.SharedPrefController;
import guepardoapps.library.toolset.runnable.EditDialogRunnable;

public class LucaDialogController extends DialogController {

    public enum LucaObject {NULL, BIRTHDAY, MENU, MOVIE, SCHEDULE, SOCKET, TIMER}

    private static final String TAG = LucaDialogController.class.getSimpleName();

    private LucaObject _lucaObject = LucaObject.NULL;
    private HomeAutomationAction _homeAutomationAction = HomeAutomationAction.NULL;

    private BirthdayDto _birthday;
    private MovieDto _movie;
    private WirelessSocketDto _socket;
    private ScheduleDto _schedule;
    private TimerDto _timer;

    private boolean _socketListInitialized;
    private SerializableList<WirelessSocketDto> _socketList;

    private ListedMenuDto _randomMenu = null;

    private String _scheduleName;
    private String _scheduleSocketString;
    private String _scheduleWeekdayString;
    private String _scheduleActionString;
    private SerializableTime _scheduleTime;
    private boolean _schedulePlaySound;

    private String _timerName;
    private String _timerSocketString;
    private SerializableTime _timerTime;

    private ScheduleDto _selectedSchedule;
    private TimerDto _selectedTimer;

    protected BroadcastController _broadcastController;
    private MailController _mailController;
    private MediaMirrorController _mediaMirrorController;
    private MenuController _menuController;
    private ReceiverController _receiverController;
    private ScheduleController _scheduleController;
    private ServiceController _serviceController;
    private SharedPrefController _sharedPrefController;
    private SocketController _socketController;
    private TimerController _timerController;

    private UserService _userService;

    private Runnable _storedRunnable = null;
    private BroadcastReceiver _menuReceiver = null;
    private BroadcastReceiver _shoppingListReceiver = null;

    private EditDialogRunnable _searchYoutubeRunnable = new EditDialogRunnable() {

        private int _searchIndex = 0;
        private int _ipIndex = 1;

        @Override
        public void SetData(@NonNull String[] data) {
            for (String entry : data) {
                _logger.Debug(String.format(Locale.getDefault(), "SetData with entry %s in data", entry));
            }
            _data = data;
        }

        @Override
        public void run() {
            String search = _data[_searchIndex];
            search = search.replace(" ", "+");

            String ip = _data[_ipIndex];
            String url = String.format(Locale.getDefault(), Constants.YOUTUBE_SEARCH, Constants.YOUTUBE_MAX_RESULTS, search, Keys.YOUTUBE_API_KEY);

            ProgressDialog loadingVideosDialog = ProgressDialog.show(_context, "Loading Videos...", "");
            loadingVideosDialog.setCancelable(false);

            DownloadYoutubeVideoTask task = new DownloadYoutubeVideoTask(
                    _context,
                    _broadcastController,
                    LucaDialogController.this,
                    loadingVideosDialog,
                    ip);
            task.execute(url);
        }
    };

    private Runnable _deletePromptRunnable = new Runnable() {
        @Override
        public void run() {
            checkOpenDialog();

            if (_lucaObject == null || _lucaObject == LucaObject.NULL) {
                _logger.Error("_dialogType is null!");
                CloseDialogCallback.run();
                resetValues();
                return;
            }

            switch (_lucaObject) {
                case BIRTHDAY:
                    ShowDialogDouble(
                            "Delete Birthday",
                            "Do you really want to delete the birthday?",
                            "Yes", _deleteRunnable,
                            "Cancel", CloseDialogCallback,
                            false);
                    _isDialogOpen = true;
                    break;

                case MOVIE:
                    ShowDialogDouble(
                            "Delete Movie",
                            "Do you really want to delete the movie?",
                            "Yes", _deleteRunnable,
                            "Cancel", CloseDialogCallback,
                            false);
                    _isDialogOpen = true;
                    break;

                case SCHEDULE:
                    ShowDialogDouble(
                            "Delete Schedule",
                            "Do you really want to delete the schedule?",
                            "Yes", _deleteRunnable,
                            "Cancel", CloseDialogCallback,
                            false);
                    _isDialogOpen = true;
                    break;

                case SOCKET:
                    ShowDialogDouble(
                            "Delete Socket",
                            "Do you really want to delete the socket?",
                            "Yes", _deleteRunnable,
                            "Cancel", CloseDialogCallback,
                            false);
                    _isDialogOpen = true;
                    break;

                case TIMER:
                    ShowDialogDouble(
                            "Delete Timer",
                            "Do you really want to delete the timer?",
                            "Yes", _deleteRunnable,
                            "Cancel", CloseDialogCallback,
                            false);
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
                _logger.Error("_dialogType is null");
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

                    _serviceController.StartRestService(
                            _birthday.GetName(),
                            _birthday.GetCommandDelete(),
                            Broadcasts.RELOAD_BIRTHDAY);
                    break;

                case MOVIE:
                    if (_movie == null) {
                        _logger.Error("_movie is null!");
                        return;
                    }

                    _serviceController.StartRestService(
                            _movie.GetTitle(),
                            _movie.GetCommandDelete(),
                            Broadcasts.RELOAD_MOVIE);
                    break;

                case SCHEDULE:
                    if (_schedule == null) {
                        _logger.Error("_schedule is null!");
                        return;
                    }

                    _serviceController.StartRestService(
                            _schedule.GetName(),
                            _schedule.GetCommandDelete(),
                            Broadcasts.RELOAD_SCHEDULE);
                    break;

                case SOCKET:
                    if (_socket == null) {
                        _logger.Error("_socket is null!");
                        return;
                    }

                    _socketController.DeleteSocket(_socket);
                    break;

                case TIMER:
                    if (_timer == null) {
                        _logger.Error("_timer is null!");
                        return;
                    }

                    _serviceController.StartRestService(
                            _timer.GetName(),
                            _timer.GetCommandDelete(),
                            Broadcasts.RELOAD_TIMER);
                    break;

                default:
                    _logger.Warn("Still not possible to delete object " + _lucaObject.toString());
                    break;
            }

            CloseDialogCallback.run();
            resetValues();
        }
    };

    private Runnable _sendMail = new Runnable() {
        @Override
        public void run() {
            _mailController.SendMail("guepardoapps@gmail.com", false);
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
                case SCHEDULE:
                    if (_schedule != null) {
                        if (_socketList != null) {
                            ShowAddScheduleDialog(null, null, _socketList, _schedule, false);
                        } else {
                            Toasty.error(_context, "_socketList is null!", Toast.LENGTH_LONG).show();
                            _logger.Warn("_socketList is null!");
                        }
                    } else {
                        Toasty.error(_context, "_schedule is null!", Toast.LENGTH_LONG).show();
                        _logger.Warn("_schedule is null!");
                    }
                    break;
                case SOCKET:
                    if (_socket != null) {
                        ShowAddSocketDialog(-1, null, null, _socket, false);
                    } else {
                        Toasty.error(_context, "_socket is null!", Toast.LENGTH_LONG).show();
                        _logger.Warn("_socket is null!");
                    }
                    break;
                case TIMER:
                    if (_timer != null) {
                        if (_socketList != null) {
                            ShowAddTimerDialog(null, null, _socketList, _timer, false);
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
                userNameEdit.setText(_context.getResources().getString(R.string.invalidUser));
                userNameEdit.selectAll();
            }
        }
    };

    public LucaDialogController(
            @NonNull Context context,
            int textColor,
            int backgroundColor) {
        super(context, textColor, backgroundColor);
        _logger = new LucaHomeLogger(TAG);

        _context = context;

        _isDialogOpen = false;

        _socketList = null;
        _socketListInitialized = false;
        _schedulePlaySound = false;

        _broadcastController = new BroadcastController(_context);
        _mailController = new MailController(_context);
        _mediaMirrorController = new MediaMirrorController(_context);
        _mediaMirrorController.Initialize();
        _menuController = new MenuController(_context);
        _receiverController = new ReceiverController(_context);
        _scheduleController = new ScheduleController(_context);
        _serviceController = new ServiceController(_context);
        _sharedPrefController = new SharedPrefController(_context, SharedPrefConstants.SHARED_PREF_NAME);
        _socketController = new SocketController(_context);
        _timerController = new TimerController(_context);

        _userService = new UserService(_context);
    }

    public LucaDialogController(@NonNull Context context) {
        this(context, ContextCompat.getColor(context, R.color.TextIcon),
                ContextCompat.getColor(context, R.color.Background));
    }

    public void InitializeSocketList(@NonNull SerializableList<WirelessSocketDto> socketList) {
        _logger.Debug("InitializeSocketList");
        if (!_socketListInitialized) {
            _socketListInitialized = true;
            _socketList = socketList;
            _logger.Info(String.format(Locale.getDefault(), "_socketList: %s", _socketList));
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void ShowTemperatureGraphDialog(@NonNull String graphPath) {
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
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CloseDialogCallback.run();
            }
        });

        showDialog(false);
    }

    public void ShowUserCredentialsDialog(
            UserDto user,
            final Runnable runnable,
            boolean isCancelable) {
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
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = userNameEdit.getText().toString();
                if (userName.length() < 3
                        || userName.contains(_context.getResources().getString(R.string.enterValidUser))
                        || userName.contains(_context.getResources().getString(R.string.invalidUser))) {
                    userNameEdit.setText(_context.getResources().getString(R.string.enterValidUser));
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

    public void ShowUserDetailsDialog(final UserDto user) {
        checkOpenDialog();

        createDialog("ShowUserDetailsDialog", R.layout.dialog_user_data);

        TextView userNameTextView = (TextView) _dialog.findViewById(R.id.dialog_user_name);
        userNameTextView.setText(user.GetUserName());

        TextView passwordTextView = (TextView) _dialog.findViewById(R.id.dialog_user_password);
        passwordTextView.setText(user.GetPassword());

        Button btnClose = (Button) _dialog.findViewById(R.id.dialog_user_close_button);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CloseDialogCallback.run();
                resetValues();
            }
        });

        Button btnUpdate = (Button) _dialog.findViewById(R.id.dialog_user_update_button);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CloseDialogCallback.run();
                resetValues();

                // ShowUserCredentialsDialog(user, updateUserRunnable, true);
                Toasty.warning(_context, "Not yet implemented!", Toast.LENGTH_SHORT).show();
                _logger.Warn("Update user not yet implemented!");
            }
        });

        showDialog(false);
    }

    public void ShowAddBirthdayDialog(
            final int id,
            final Runnable runnable,
            BirthdayDto birthday,
            final boolean add) {
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
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    sendBroadCast(Broadcasts.ADD_BIRTHDAY, newBirthday.GetCommandAdd());
                } else {
                    sendBroadCast(Broadcasts.UPDATE_BIRTHDAY, newBirthday.GetCommandUpdate());
                }

                CloseDialogCallback.run();
            }
        });

        showDialog(true);
    }

    public void ShowAddMovieDialog(
            final Runnable runnable,
            MovieDto movie,
            final boolean add) {
        checkOpenDialog();

        createDialog("ShowAddMovieDialog", R.layout.dialog_add_movie);

        final EditText movieTitleEdit = (EditText) _dialog.findViewById(R.id.dialog_movie_title_input);
        final EditText movieGenreEdit = (EditText) _dialog.findViewById(R.id.dialog_movie_genre_input);
        final EditText movieDescriptionEdit = (EditText) _dialog.findViewById(R.id.dialog_movie_description_input);

        final RatingBar movieRatingBar = (RatingBar) _dialog.findViewById(R.id.dialog_movie_description_ratingbar);
        movieRatingBar.setEnabled(true);

        if (movie != null) {
            movieTitleEdit.setText(movie.GetTitle());
            movieGenreEdit.setText(movie.GetGenre());
            movieDescriptionEdit.setText(movie.GetDescription());
            movieRatingBar.setRating(movie.GetRating());
        }

        Button btnSave = (Button) _dialog.findViewById(R.id.dialog_movie_save_button);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = movieTitleEdit.getText().toString();
                if (title.length() < 3) {
                    Toasty.error(_context, "Title too short!", Toast.LENGTH_LONG).show();
                    return;
                }

                String genre = movieGenreEdit.getText().toString();
                String description = movieDescriptionEdit.getText().toString();

                int rating = Math.round(movieRatingBar.getRating());

                MovieDto newMovie = new MovieDto(title, genre, description, rating, 0, null);
                _logger.Debug("new Movie: " + newMovie.toString());

                if (runnable != null) {
                    runnable.run();
                }

                if (add) {
                    sendBroadCast(Broadcasts.ADD_MOVIE, newMovie.GetCommandAdd());
                } else {
                    sendBroadCast(Broadcasts.UPDATE_MOVIE, newMovie.GetCommandUpdate());
                }

                CloseDialogCallback.run();
                resetValues();
            }
        });

        showDialog(true);
    }

    public void ShowAddSocketDialog(
            final int maxId,
            final Activity activity,
            final Runnable runnable,
            final WirelessSocketDto socket,
            final boolean add) {
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
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    isActivated = socket.IsActivated();
                }

                WirelessSocketDto newSocket = new WirelessSocketDto(maxId, name, area, code, isActivated);

                if (runnable != null) {
                    runnable.run();
                }

                String message;
                if (add) {
                    sendBroadCast(Broadcasts.ADD_SOCKET, newSocket.GetCommandAdd());
                    message = String.format(Locale.getDefault(), "trying to add socket %s", newSocket.GetName());
                } else {
                    sendBroadCast(Broadcasts.UPDATE_SOCKET_LIST, newSocket.GetCommandUpdate());
                    message = String.format(Locale.getDefault(), "trying to update socket %s", newSocket.GetName());
                }

                _logger.Debug(message);

                if (activity != null) {
                    Snacky.builder()
                            .setActivty(activity)
                            .setText(message)
                            .setDuration(Snacky.LENGTH_LONG)
                            .setActionText(_context.getResources().getString(android.R.string.ok))
                            .info()
                            .show();
                }

                CloseDialogCallback.run();
                resetValues();
            }
        });

        showDialog(true);
    }

    @SuppressWarnings("deprecation")
    public void ShowAddScheduleDialog(
            final Activity activity,
            final Runnable runnable,
            final SerializableList<WirelessSocketDto> socketList,
            final ScheduleDto schedule,
            final boolean add) {
        checkOpenDialog();

        createDialog("ShowAddScheduleDialog", R.layout.dialog_add_schedule);

        final EditText scheduleNameEdit = (EditText) _dialog.findViewById(R.id.dialog_schedule_name_input);

        final Spinner scheduleSocketSelect = (Spinner) _dialog.findViewById(R.id.dialog_schedule_socket_select);
        List<String> sockets = new ArrayList<>();
        for (int socketIndex = 0; socketIndex < socketList.getSize(); socketIndex++) {
            sockets.add(socketList.getValue(socketIndex).GetName());
        }
        ArrayAdapter<String> socketDataAdapter = new ArrayAdapter<>(_context,
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
        List<String> weekdays = new ArrayList<>();
        for (Weekday weekday : Weekday.values()) {
            weekdays.add(weekday.GetEnglishDay());
        }
        ArrayAdapter<String> weekdayDataAdapter = new ArrayAdapter<>(_context,
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
        List<String> actions = new ArrayList<>();
        actions.add("ON");
        actions.add("OFF");
        ArrayAdapter<String> actionDataAdapter = new ArrayAdapter<>(_context,
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
            scheduleTimePicker.setCurrentHour(schedule.GetTime().Hour());
            scheduleTimePicker.setCurrentMinute(schedule.GetTime().Minute());
        }

        Button btnSave = (Button) _dialog.findViewById(R.id.dialog_schedule_save_button);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _scheduleName = scheduleNameEdit.getText().toString();
                if (_scheduleName.length() < 3) {
                    Toasty.error(_context, "Name too short!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (_scheduleSocketString == null || _scheduleSocketString.length() == 0) {
                    Toasty.error(_context, "Please select a socket!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (_scheduleWeekdayString == null || _scheduleWeekdayString.length() == 0) {
                    Toasty.error(_context, "Please select a weekday!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (_scheduleActionString == null || _scheduleActionString.length() == 0) {
                    Toasty.error(_context, "Please select an action!", Toast.LENGTH_LONG).show();
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
                _scheduleTime = new SerializableTime(scheduleHour, scheduleMinute, 0, 0);

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
                    isActive = schedule.IsActive();
                }

                ScheduleDto newSchedule = new ScheduleDto(
                        _scheduleName,
                        socket,
                        weekday,
                        _scheduleTime,
                        action,
                        _schedulePlaySound,
                        isActive);
                _logger.Debug("new Schedule: " + newSchedule.toString());

                if (runnable != null) {
                    runnable.run();
                }

                String message;
                if (add) {
                    sendBroadCast(Broadcasts.ADD_SCHEDULE, newSchedule.GetCommandAdd());
                    message = String.format(Locale.getDefault(), "trying to add schedule %s", newSchedule.GetName());
                } else {
                    sendBroadCast(Broadcasts.UPDATE_SCHEDULE, newSchedule.GetCommandUpdate());
                    message = String.format(Locale.getDefault(), "trying to update schedule %s", newSchedule.GetName());
                }

                _logger.Debug(message);

                if (activity != null) {
                    Snacky.builder()
                            .setActivty(activity)
                            .setText(message)
                            .setDuration(Snacky.LENGTH_LONG)
                            .setActionText(_context.getResources().getString(android.R.string.ok))
                            .info()
                            .show();
                }

                CloseDialogCallback.run();
                resetValues();
            }
        });

        showDialog(true);
    }

    @SuppressWarnings("deprecation")
    public void ShowAddTimerDialog(
            final Activity activity,
            final Runnable runnable,
            @NonNull final SerializableList<WirelessSocketDto> socketList,
            TimerDto timer,
            final boolean add) {
        checkOpenDialog();

        createDialog("ShowAddTimerDialog", R.layout.dialog_add_timer);

        final EditText timerNameEdit = (EditText) _dialog.findViewById(R.id.dialog_timer_name_input);

        final Spinner timerSocketSelect = (Spinner) _dialog.findViewById(R.id.dialog_timer_socket_select);
        List<String> sockets = new ArrayList<>();
        for (int socketIndex = 0; socketIndex < socketList.getSize(); socketIndex++) {
            sockets.add(socketList.getValue(socketIndex).GetName());
        }
        ArrayAdapter<String> socketDataAdapter = new ArrayAdapter<>(_context,
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

        if (timer != null) {
            timerNameEdit.setText(timer.GetName());
            for (int socketIndex = 0; socketIndex < socketList.getSize(); socketIndex++) {
                if (socketList.getValue(socketIndex).GetName().contains(timer.GetSocket().GetName())) {
                    timerSocketSelect.setSelection(socketIndex, true);
                    break;
                }
            }
            timerTimePicker.setCurrentHour(timer.GetTime().Hour());
            timerTimePicker.setCurrentMinute(timer.GetTime().Minute());
        }

        Button btnSave = (Button) _dialog.findViewById(R.id.dialog_timer_save_button);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _timerName = timerNameEdit.getText().toString();

                if (_timerName.length() < 3) {
                    Toasty.error(_context, "Name too short!", Toast.LENGTH_LONG).show();
                    return;
                }

                if (_timerSocketString == null || _timerSocketString.length() == 0) {
                    Toasty.error(_context, "Please select a socket!", Toast.LENGTH_LONG).show();
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
                _timerTime = new SerializableTime(timerHour, timerMinute, 0, 0);

                while (timerDay > 7) {
                    timerDay -= 7;
                }
                Weekday weekday = Weekday.GetById(timerDay);

                _serviceController.StartRestService(
                        socket.GetName(),
                        socket.GetCommandSet(true),
                        Broadcasts.RELOAD_SOCKETS);

                TimerDto newTimer = new TimerDto(
                        _timerName,
                        socket,
                        weekday,
                        _timerTime,
                        false,
                        false,
                        true);

                _logger.Debug(String.format(Locale.getDefault(), "new Timer: %s", _timerName));

                if (runnable != null) {
                    runnable.run();
                }

                String message;
                if (add) {
                    sendBroadCast(Broadcasts.ADD_SCHEDULE, newTimer.GetCommandAdd());
                    message = String.format(Locale.getDefault(), "trying to add timer %s", newTimer.GetName());
                } else {
                    sendBroadCast(Broadcasts.UPDATE_SCHEDULE, newTimer.GetCommandUpdate());
                    message = String.format(Locale.getDefault(), "trying to update timer %s", newTimer.GetName());
                }

                _logger.Debug(message);

                if (activity != null) {
                    Snacky.builder()
                            .setActivty(activity)
                            .setText(message)
                            .setDuration(Snacky.LENGTH_LONG)
                            .setActionText(_context.getResources().getString(android.R.string.ok))
                            .info()
                            .show();
                }

                CloseDialogCallback.run();
                resetValues();
            }
        });

        showDialog(true);
    }

    public void ShowAddShoppingEntryDialog(
            final Activity activity,
            final Runnable runnable,
            final ShoppingEntryDto entry,
            final boolean add,
            final boolean onlyChangeQuantity,
            final int shoppingListSize) {
        checkOpenDialog();

        createDialog("ShowAddShoppingEntryDialog", R.layout.dialog_add_shopping_entry);

        final EditText entryName = (EditText) _dialog.findViewById(R.id.dialog_shopping_entry_name_input);
        entryName.setTextColor(0xFF000000);

        if (!add) {
            if (entry != null) {
                entryName.setText(entry.GetName());
            }
        }

        if (onlyChangeQuantity) {
            entryName.setEnabled(false);
        }

        final Spinner entryGroupSelect = (Spinner) _dialog.findViewById(R.id.dialog_shopping_entry_group_select);
        List<ShoppingEntryGroup> groups = Arrays.asList(ShoppingEntryGroup.values());
        ArrayAdapter<ShoppingEntryGroup> groupDataAdapter = new ArrayAdapter<>(_context,
                android.R.layout.simple_spinner_item, groups);
        groupDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        entryGroupSelect.setAdapter(groupDataAdapter);
        entryGroupSelect.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                _logger.Debug(
                        String.format("Parent: %s | view: %s | position: %s | id: %s", parent, view, position, id));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        if (!add) {
            if (entry != null) {
                entryGroupSelect.setSelection(entry.GetGroup().GetInt());
            }
        }

        if (onlyChangeQuantity) {
            entryGroupSelect.setEnabled(false);
        }

        final TextView entryCount = (TextView) _dialog.findViewById(R.id.dialog_shopping_entry_count);
        entryCount.setText("1");

        Button btnCountIncrease = (Button) _dialog.findViewById(R.id.dialog_shopping_entry_button_Count_Increase);
        btnCountIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String countString = entryCount.getText().toString();
                int count = Integer.parseInt(countString);
                count++;
                entryCount.setText(String.valueOf(count));
            }
        });

        Button btnCountDecrease = (Button) _dialog.findViewById(R.id.dialog_shopping_entry_button_Count_Decrease);
        btnCountDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String countString = entryCount.getText().toString();
                int count = Integer.parseInt(countString);
                count--;
                if (count < 1) {
                    count = 1;
                }
                entryCount.setText(String.valueOf(count));
            }
        });

        Button btnSave = (Button) _dialog.findViewById(R.id.dialog_shopping_entry_save_button);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = entryName.getText().toString();
                if (name.length() < 3) {
                    Toasty.error(_context, "Name too short!", Toast.LENGTH_LONG).show();
                    return;
                }
                name = name.replace(" ", "");

                int groupSelection = entryGroupSelect.getSelectedItemPosition();
                ShoppingEntryGroup group = ShoppingEntryGroup.GetById(groupSelection);

                String countString = entryCount.getText().toString();
                int count = Integer.parseInt(countString);

                ShoppingEntryDto newEntry;
                if (add) {
                    newEntry = new ShoppingEntryDto(shoppingListSize, name, group, count, false);
                } else {
                    if (entry != null) {
                        newEntry = new ShoppingEntryDto(entry.GetId(), name, group, count, entry.IsBought());
                    } else {
                        newEntry = new ShoppingEntryDto(shoppingListSize, name, group, count, false);
                    }
                }

                _logger.Debug("new entry: " + newEntry.toString());

                if (runnable != null) {
                    runnable.run();
                }

                String message;
                if (add) {
                    _serviceController.StartRestService(
                            newEntry.GetName(),
                            newEntry.GetCommandAdd(),
                            Broadcasts.RELOAD_SHOPPING_LIST);
                    message = String.format(Locale.getDefault(), "trying to add entry %s", newEntry.GetName());
                } else {
                    _serviceController.StartRestService(
                            newEntry.GetName(),
                            newEntry.GetCommandUpdate(),
                            Broadcasts.RELOAD_SHOPPING_LIST);
                    message = String.format(Locale.getDefault(), "trying to update entry %s", newEntry.GetName());
                }

                _logger.Debug(message);

                if (activity != null) {
                    Snacky.builder()
                            .setActivty(activity)
                            .setText(message)
                            .setDuration(Snacky.LENGTH_LONG)
                            .setActionText(_context.getResources().getString(android.R.string.ok))
                            .info()
                            .show();
                }

                CloseDialogCallback.run();
            }
        });

        showDialog(true);
    }

    public void ShowSendInformationMailDialog() {
        ShowDialogSingle("Send Mail", "", "Send", _sendMail, true);
    }

    public void ShowUpdateBirthdayDialog(@NonNull BirthdayDto value) {
        _lucaObject = LucaObject.BIRTHDAY;
        _birthday = value;

        ShowDialogTriple(
                "Birthday", _birthday.GetName(),
                "Update", _updateRunnable,
                "Delete", _deletePromptRunnable,
                "Cancel", CloseDialogCallback,
                false);

        _isDialogOpen = true;
    }

    public void ShowUpdateMovieDialog(@NonNull MovieDto value) {
        _lucaObject = LucaObject.MOVIE;
        _movie = value;

        ShowDialogTriple(
                "Movie", _movie.GetTitle(),
                "Update", _updateRunnable,
                "Delete", _deletePromptRunnable,
                "Cancel", CloseDialogCallback,
                false);

        _isDialogOpen = true;
    }

    public void ShowUpdateScheduleDialog(@NonNull ScheduleDto value) {
        if (!_socketListInitialized) {
            Toasty.warning(_context, "SocketList not initialized!", Toast.LENGTH_SHORT).show();
            _logger.Warn("SocketList not initialized!");
            return;
        }

        _lucaObject = LucaObject.SCHEDULE;
        _schedule = value;

        ShowDialogTriple(
                "Schedule", _schedule.GetName(),
                "Update", _updateRunnable,
                "Delete", _deletePromptRunnable,
                "Cancel", CloseDialogCallback,
                false);

        _isDialogOpen = true;
    }

    public void ShowUpdateSocketDialog(@NonNull WirelessSocketDto value) {
        _lucaObject = LucaObject.SOCKET;
        _socket = value;

        ShowDialogTriple(
                "Socket", _socket.GetName(),
                "Update", _updateRunnable,
                "Delete", _deletePromptRunnable,
                "Cancel", CloseDialogCallback,
                false);

        _isDialogOpen = true;
    }

    public void ShowUpdateTimerDialog(@NonNull TimerDto value) {
        if (!_socketListInitialized) {
            Toasty.warning(_context, "SocketList not initialized!", Toast.LENGTH_SHORT).show();
            _logger.Warn("SocketList not initialized!");
            return;
        }

        _lucaObject = LucaObject.TIMER;
        _timer = value;

        ShowDialogTriple(
                "Timer", _timer.GetName(),
                "Update", _updateRunnable,
                "Delete", _deletePromptRunnable,
                "Cancel", CloseDialogCallback,
                false);

        _isDialogOpen = true;
    }

    public void ShowUpdateMenuDialog(
            @NonNull final MenuDto checkMenu,
            @NonNull SerializableList<ListedMenuDto> predefinedMenus) {
        checkOpenDialog();

        final MenuDto menu = _menuController.UpdateDate(checkMenu);
        if (menu == null) {
            _logger.Error("Menu is null!");
            Toasty.error(_context, "Menu is null!", Toast.LENGTH_SHORT).show();
            return;
        }

        createDialog("ShowUpdateMenuDialog", R.layout.dialog_udpate_menu);
        _logger.Debug("For menu: " + menu.toString());

        TextView dateView = (TextView) _dialog.findViewById(R.id.dialog_menu_date);
        dateView.setText(_menuController.GetDateString(menu));

        final EditText titleInput = (EditText) _dialog.findViewById(R.id.dialog_menu_title_input);
        titleInput.setText(checkMenu.GetTitle());

        final EditText descriptionInput = (EditText) _dialog.findViewById(R.id.dialog_menu_description_input);
        descriptionInput.setText(checkMenu.GetDescription());

        final Spinner predefinedMenuSelection = (Spinner) _dialog.findViewById(R.id.dialog_menu_predfined_menu);
        if (predefinedMenus.getSize() <= 0) {
            _logger.Warn("Size of predefinedMenus is 0!");
            predefinedMenuSelection.setVisibility(View.GONE);
        } else {
            List<String> predefinedMenuStrings = new ArrayList<>();
            for (int index = 0; index < predefinedMenus.getSize(); index++) {
                predefinedMenuStrings.add(predefinedMenus.getValue(index).GetDescription());
            }
            ArrayAdapter<String> predefinedMenusDataAdapter = new ArrayAdapter<>(_context,
                    android.R.layout.simple_spinner_item, predefinedMenuStrings);
            predefinedMenusDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            predefinedMenuSelection.setAdapter(predefinedMenusDataAdapter);
            predefinedMenuSelection.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selection = parent.getItemAtPosition(position).toString();
                    titleInput.setText(selection);
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });
        }

        Button update = (Button) _dialog.findViewById(R.id.dialog_menu_save_button);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titleInput.getText().toString();

                if (title.length() == 0) {
                    _logger.Error("Title is null!");
                    Toasty.error(_context, "Title is null!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String description = descriptionInput.getText().toString();

                menu.SetTitle(title);
                menu.SetDescription(description);

                _serviceController.StartRestService(
                        Bundles.MENU,
                        menu.GetCommandUpdate(),
                        Broadcasts.RELOAD_MENU);

                CloseDialogCallback.run();
            }
        });

        showDialog(true);
    }

    public void ShowRandomMenuDialog(
            @NonNull final MenuDto checkMenu,
            @NonNull final SerializableList<ListedMenuDto> listedMenu) {
        checkOpenDialog();

        createDialog("ShowRandomMenuDialog", R.layout.dialog_random_menu);
        _logger.Debug("For listedMenu: " + listedMenu.toString());

        final MenuDto menu = _menuController.UpdateDate(checkMenu);
        if (menu == null) {
            _logger.Error("Menu is null!");
            Toasty.error(_context, "Menu is null!", Toast.LENGTH_SHORT).show();
            return;
        }

        final CheckBox useLastSuggestionCheckBox = (CheckBox) _dialog.findViewById(R.id.checkBoxLastSuggestion);

        final TextView randomMenuTextView = (TextView) _dialog.findViewById(R.id.dialog_random_menu_selection);
        randomMenuTextView.setText(String.format("Random menu: %s", ""));

        final RatingBar menuRatingBar = (RatingBar) _dialog.findViewById(R.id.randomMenuRatingBar);
        menuRatingBar.setProgress(0);
        menuRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                _logger.Debug(String.format(Locale.getDefault(), "menuRatingBar onRatingChanged %f", rating));

                if (!fromUser) {
                    _logger.Warn(String.format(Locale.getDefault(), "Selection %f was not performed by user! Abort!", rating));
                }
            }
        });

        Button randomize = (Button) _dialog.findViewById(R.id.dialog_random_menu_random_button);
        randomize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int minRating = menuRatingBar.getProgress();
                boolean useLastSuggestion = useLastSuggestionCheckBox.isChecked();

                _logger.Debug(String.format(Locale.GERMAN, "MinRating is %d and useLastSuggestion is %s", minRating,
                        useLastSuggestion ? "on" : "off"));

                SerializableList<ListedMenuDto> randomMenus = new SerializableList<>();
                for (int index = 0; index < listedMenu.getSize(); index++) {
                    ListedMenuDto entry = listedMenu.getValue(index);
                    _logger.Debug(entry.toString());
                    if (entry.GetRating() >= minRating) {
                        if (!useLastSuggestion && entry.IsLastSuggestion()) {
                            continue;
                        }

                        randomMenus.addValue(entry);
                    }
                }

                int menuCount = randomMenus.getSize();
                Random random = new Random();
                int randomMenuId = random.nextInt(menuCount);

                _randomMenu = randomMenus.getValue(randomMenuId);
                randomMenuTextView.setText(String.format("Random menu: %s", _randomMenu.GetDescription()));
            }
        });

        Button ok = (Button) _dialog.findViewById(R.id.dialog_random_menu_ok_button);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (_randomMenu == null) {
                    _logger.Error("RandomMenu is null!");
                    Toasty.error(_context, "RandomMenu is null!", Toast.LENGTH_SHORT).show();
                    return;
                }

                menu.SetTitle(_randomMenu.GetDescription());
                menu.SetDescription("-");

                _serviceController.StartRestService(
                        Bundles.MENU,
                        menu.GetCommandUpdate(),
                        Broadcasts.RELOAD_MENU);

                _randomMenu = null;

                CloseDialogCallback.run();
            }
        });

        showDialog(true);
    }

    public void ShowMapSocketDialog(
            final Activity activity,
            @NonNull final WirelessSocketDto socket,
            @NonNull final SerializableList<ScheduleDto> scheduleList,
            @NonNull final SerializableList<TimerDto> timerList) {
        checkOpenDialog();

        createDialog("ShowMapSocketDialog", R.layout.dialog_map_socket);
        _logger.Debug("For socket: " + socket.GetName());

        _homeAutomationAction = HomeAutomationAction.NULL;

        TextView socketNameTextView = (TextView) _dialog.findViewById(R.id.dialog_map_socket_name);
        socketNameTextView.setText(socket.GetName());
        TextView socketAreaTextView = (TextView) _dialog.findViewById(R.id.dialog_map_socket_area);
        socketAreaTextView.setText(socket.GetArea());
        TextView socketCodeTextView = (TextView) _dialog.findViewById(R.id.dialog_map_socket_code);
        socketCodeTextView.setText(socket.GetCode());

        Switch socketState = (Switch) _dialog.findViewById(R.id.dialog_map_socket_switch);
        socketState.setChecked(socket.IsActivated());
        socketState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                _socketController.SetSocket(socket, isChecked);
                _socketController.CheckMedia(socket);

                if (activity != null) {
                    Snacky.builder()
                            .setActivty(activity)
                            .setText(String.format(Locale.getDefault(), "Trying to set socket %s to %s", socket.GetName(), (isChecked ? "on" : "off")))
                            .setDuration(Snacky.LENGTH_LONG)
                            .setActionText(_context.getResources().getString(android.R.string.ok))
                            .info()
                            .show();
                }
            }
        });

        Spinner scheduleSpinner = (Spinner) _dialog.findViewById(R.id.dialog_map_socket_schedules_spinner);
        List<String> scheduleNames = new ArrayList<>();
        for (int index = 0; index < scheduleList.getSize(); index++) {
            scheduleNames.add(scheduleList.getValue(index).GetName());
        }
        ArrayAdapter<String> scheduleDataAdapter = new ArrayAdapter<>(_context,
                android.R.layout.simple_spinner_item, scheduleNames);
        scheduleDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scheduleSpinner.setAdapter(scheduleDataAdapter);
        scheduleSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String stringSchedule = parent.getItemAtPosition(position).toString();
                _selectedSchedule = null;
                for (int index = 0; index < scheduleList.getSize(); index++) {
                    if (scheduleList.getValue(index).GetName().contains(stringSchedule)) {
                        _selectedSchedule = scheduleList.getValue(index);
                        _logger.Info(String.format("Found schedule %s", _selectedSchedule.GetName()));
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        Button btnScheduleSelectedGo = (Button) _dialog.findViewById(R.id.dialog_map_schedule_SelectButton);
        btnScheduleSelectedGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("btnScheduleSelectedGo onClick");

                if (_selectedSchedule != null) {
                    _logger.Debug(_selectedSchedule.toString());
                    CloseDialogCallback.run();
                    ShowMapScheduleDialog(_selectedSchedule);
                } else {
                    _logger.Warn("_selectedSchedule is null!");
                }
            }
        });

        Spinner timerSpinner = (Spinner) _dialog.findViewById(R.id.dialog_map_socket_timer_spinner);
        List<String> timerNames = new ArrayList<>();
        for (int index = 0; index < timerList.getSize(); index++) {
            timerNames.add(timerList.getValue(index).GetName());
        }
        ArrayAdapter<String> timerDataAdapter = new ArrayAdapter<>(_context, android.R.layout.simple_spinner_item,
                timerNames);
        timerDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timerSpinner.setAdapter(timerDataAdapter);
        timerSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String stringTimer = parent.getItemAtPosition(position).toString();
                _selectedTimer = null;
                for (int index = 0; index < timerList.getSize(); index++) {
                    if (timerList.getValue(index).GetName().contains(stringTimer)) {
                        _selectedTimer = timerList.getValue(index);
                        _logger.Info(String.format("Found timer %s", _selectedTimer.GetName()));
                        break;
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        Button btnTimerSelectedGo = (Button) _dialog.findViewById(R.id.dialog_map_timer_SelectButton);
        btnTimerSelectedGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("btnTimerSelectedGo onClick");

                if (_selectedTimer != null) {
                    _logger.Debug(_selectedTimer.toString());
                    CloseDialogCallback.run();
                    ShowMapTimerDialog(_selectedTimer);
                } else {
                    _logger.Warn("_selectedTimer is null!");
                }
            }
        });

        Spinner specialTaskSpinner = (Spinner) _dialog.findViewById(R.id.dialog_map_socket_specialTask_spinner);
        List<String> specialTasks = new ArrayList<>();
        specialTasks.add("Warm sleep timer");
        specialTasks.add("Sweet dream timer");
        specialTasks.add("Warm sleep and sweet dream timer");
        ArrayAdapter<String> specialTaskDataAdapter = new ArrayAdapter<>(_context,
                android.R.layout.simple_spinner_item, specialTasks);
        specialTaskDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        specialTaskSpinner.setAdapter(specialTaskDataAdapter);
        specialTaskSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                _logger.Info(String.format("Item at position %s selected!", position));

                if (position == 0) {
                    _homeAutomationAction = HomeAutomationAction.ENABLE_HEATING;
                } else if (position == 1) {
                    _homeAutomationAction = HomeAutomationAction.ENABLE_SEA_SOUND;
                } else if (position == 2) {
                    _homeAutomationAction = HomeAutomationAction.ENABLE_HEATING_AND_SOUND;
                } else {
                    _logger.Error("Position contains errors: " + position);
                    Toasty.error(_context, "Position contains errors: " + position, Toast.LENGTH_LONG).show();
                    return;
                }
                _logger.Info(String.format("_homeAutomationAction is %s!", _homeAutomationAction));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        Button btnSpecialTaskGo = (Button) _dialog.findViewById(R.id.dialog_map_socket_specialTask_StartButton);
        btnSpecialTaskGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("btnSpecialTaskGo onClick");

                if (_homeAutomationAction != HomeAutomationAction.NULL) {
                    _logger.Info(String.format("Running mainServiceAction %s", _homeAutomationAction));
                    _broadcastController.SendSerializableArrayBroadcast(
                            Broadcasts.HOME_AUTOMATION_COMMAND,
                            new String[]{Bundles.HOME_AUTOMATION_ACTION},
                            new Object[]{_homeAutomationAction});
                }
            }
        });

        if (!socket.GetName().contains("Heating")) {
            specialTaskSpinner.setVisibility(View.GONE);
            btnSpecialTaskGo.setVisibility(View.GONE);
            TextView specialTaskTitle = (TextView) _dialog.findViewById(R.id.dialog_map_socket_specialTask);
            specialTaskTitle.setVisibility(View.GONE);
            TextView specialTaskDivider = (TextView) _dialog.findViewById(R.id.dialog_map_socket_specialTask_divider);
            specialTaskDivider.setVisibility(View.GONE);
        }

        Button btnClose = (Button) _dialog.findViewById(R.id.dialog_map_socket_button_close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CloseDialogCallback.run();
            }
        });

        showDialog(true);
    }

    private void ShowMapScheduleDialog(@NonNull final ScheduleDto selectedSchedule) {
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
        buttonState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _scheduleController.SetSchedule(selectedSchedule, !selectedSchedule.IsActive());
                CloseDialogCallback.run();
            }
        });

        Button buttonDelete = (Button) _dialog.findViewById(R.id.dialog_map_schedule_delete);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _scheduleController.DeleteSchedule(selectedSchedule);
                CloseDialogCallback.run();
            }
        });

        Button buttonClose = (Button) _dialog.findViewById(R.id.dialog_map_schedule_button_close);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CloseDialogCallback.run();
            }
        });

        showDialog(true);
    }

    private void ShowMapTimerDialog(@NonNull final TimerDto selectedTimer) {
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
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _timerController.Delete(selectedTimer);
                CloseDialogCallback.run();
            }
        });

        Button buttonClose = (Button) _dialog.findViewById(R.id.dialog_map_timer_button_close);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CloseDialogCallback.run();
            }
        });

        showDialog(true);
    }

    public void ShowMapMediaMirrorDialog(
            final Activity activity,
            @NonNull final MediaServerSelection selection,
            final WirelessSocketDto socket) {
        checkOpenDialog();

        createDialog("ShowMapMediaMirrorDialog", R.layout.dialog_map_mediamirror);
        _logger.Debug("For mediaMirror with ip: " + selection.GetIp());

        TextView socketNameTextView = (TextView) _dialog.findViewById(R.id.dialog_map_mediamirror_name);
        socketNameTextView.setText(selection.GetSocket());
        TextView socketAreaTextView = (TextView) _dialog.findViewById(R.id.dialog_map_mediamirror_area);
        socketAreaTextView.setText(selection.GetLocation());
        TextView socketCodeTextView = (TextView) _dialog.findViewById(R.id.dialog_map_mediamirror_code);
        if (socket != null) {
            socketCodeTextView.setText(socket.GetCode());
        } else {
            socketCodeTextView.setVisibility(View.INVISIBLE);
        }

        TextView mediaMirrorIpTextView = (TextView) _dialog.findViewById(R.id.dialog_map_mediamirror_ip);
        mediaMirrorIpTextView.setText(selection.GetIp());

        Switch socketState = (Switch) _dialog.findViewById(R.id.dialog_map_mediamirror_switch);
        if (socket != null) {
            socketState.setChecked(socket.IsActivated());
            socketState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    _socketController.SetSocket(socket, isChecked);

                    if (activity != null) {
                        Snacky.builder()
                                .setActivty(activity)
                                .setText(String.format(Locale.getDefault(), "Trying to set socket %s to %s", socket.GetName(), (isChecked ? "on" : "off")))
                                .setDuration(Snacky.LENGTH_LONG)
                                .setActionText(_context.getResources().getString(android.R.string.ok))
                                .info()
                                .show();
                    }
                }
            });
        } else {
            socketState.setVisibility(View.INVISIBLE);
        }

        Button btnPlay = (Button) _dialog.findViewById(R.id.dialog_map_mediamirror_play);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _broadcastController.SendStringBroadcast(
                        Broadcasts.MEDIAMIRROR_COMMAND,
                        Bundles.MEDIAMIRROR_COMMAND,
                        String.format(Locale.getDefault(), "IP:%s&CMD:%s", selection.GetIp(), "PLAY"));
            }
        });

        Button btnPause = (Button) _dialog.findViewById(R.id.dialog_map_mediamirror_pause);
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _broadcastController.SendStringBroadcast(
                        Broadcasts.MEDIAMIRROR_COMMAND,
                        Bundles.MEDIAMIRROR_COMMAND,
                        String.format(Locale.getDefault(), "IP:%s&CMD:%s", selection.GetIp(), "PAUSE"));
            }
        });

        Button btnStop = (Button) _dialog.findViewById(R.id.dialog_map_mediamirror_stop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _broadcastController.SendStringBroadcast(
                        Broadcasts.MEDIAMIRROR_COMMAND,
                        Bundles.MEDIAMIRROR_COMMAND,
                        String.format(Locale.getDefault(), "IP:%s&CMD:%s", selection.GetIp(), "STOP"));
            }
        });

        Button btnVolumeIncrease = (Button) _dialog.findViewById(R.id.dialog_map_mediamirror_vol_increase);
        btnVolumeIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _broadcastController.SendStringBroadcast(
                        Broadcasts.MEDIAMIRROR_COMMAND,
                        Bundles.MEDIAMIRROR_COMMAND,
                        String.format(Locale.getDefault(), "IP:%s&CMD:%s", selection.GetIp(), "VOL_INCREASE"));
            }
        });

        Button btnVolumeDecrease = (Button) _dialog.findViewById(R.id.dialog_map_mediamirror_vol_decrease);
        btnVolumeDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _broadcastController.SendStringBroadcast(
                        Broadcasts.MEDIAMIRROR_COMMAND,
                        Bundles.MEDIAMIRROR_COMMAND,
                        String.format(Locale.getDefault(), "IP:%s&CMD:%s", selection.GetIp(), "VOL_DECREASE"));
            }
        });

        Button btnSleepStart = (Button) _dialog.findViewById(R.id.dialog_map_mediamirror_sleep_start);
        btnSleepStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Info(String.format(Locale.getDefault(), "Running homeAutomationAction %s", HomeAutomationAction.ENABLE_SEA_SOUND));
                _broadcastController.SendSerializableArrayBroadcast(
                        Broadcasts.HOME_AUTOMATION_COMMAND,
                        new String[]{Bundles.HOME_AUTOMATION_ACTION},
                        new Object[]{HomeAutomationAction.ENABLE_SEA_SOUND});
            }
        });

        Button btnSleepStop = (Button) _dialog.findViewById(R.id.dialog_map_mediamirror_sleep_stop);
        btnSleepStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Info(String.format(Locale.getDefault(), "Running homeAutomationAction %s", HomeAutomationAction.DISABLE_SEA_SOUND));
                _broadcastController.SendSerializableArrayBroadcast(
                        Broadcasts.HOME_AUTOMATION_COMMAND,
                        new String[]{Bundles.HOME_AUTOMATION_ACTION},
                        new Object[]{HomeAutomationAction.DISABLE_SEA_SOUND});
            }
        });

        Button btnClose = (Button) _dialog.findViewById(R.id.dialog_map_mediamirror_button_close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CloseDialogCallback.run();
            }
        });

        if (!selection.IsSleepingMirror()) {
            LinearLayout sleepButtonLinearLayout = (LinearLayout) _dialog
                    .findViewById(R.id.dialog_map_mediamirror_sleep_linearlayout);
            sleepButtonLinearLayout.setVisibility(View.GONE);
        }

        showDialog(true);
    }

    public void ShowShoppingListDialog(@NonNull final SerializableList<ShoppingEntryDto> shoppingList) {
        checkOpenDialog();

        createDialog("ShowShoppingListDialog", R.layout.dialog_skeleton_list);

        TextView titleTextView = (TextView) _dialog.findViewById(R.id.dialog_list_title);
        titleTextView.setText(_context.getResources().getString(R.string.shoppingList));
        final ListView listView = (ListView) _dialog.findViewById(R.id.dialog_list_view);

        ShoppingListAdapter listAdapter = new ShoppingListAdapter(_context, shoppingList, false, null);
        listView.setAdapter(listAdapter);

        if (_shoppingListReceiver != null) {
            _receiverController.UnregisterReceiver(_shoppingListReceiver);
        }

        _shoppingListReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                @SuppressWarnings("unchecked")
                SerializableList<ShoppingEntryDto> receivedShoppingList = (SerializableList<ShoppingEntryDto>) intent
                        .getSerializableExtra(Bundles.SHOPPING_LIST);
                if (receivedShoppingList != null) {
                    ShoppingListAdapter listAdapter = new ShoppingListAdapter(_context, receivedShoppingList, false, null);
                    listView.setAdapter(listAdapter);
                }
            }
        };

        _receiverController.RegisterReceiver(_shoppingListReceiver, new String[]{Broadcasts.UPDATE_SHOPPING_LIST});

        showDialog(true);
    }

    public void ShowMenuDialog(
            @NonNull final SerializableList<MenuDto> menu,
            @NonNull final SerializableList<ListedMenuDto> listedMenu) {
        checkOpenDialog();

        createDialog("ShowShoppingListDialog", R.layout.dialog_skeleton_list);

        TextView titleTextView = (TextView) _dialog.findViewById(R.id.dialog_list_title);
        titleTextView.setText(_context.getResources().getString(R.string.menu));
        final ListView listView = (ListView) _dialog.findViewById(R.id.dialog_list_view);

        MenuListAdapter listAdapter = new MenuListAdapter(_context, menu, listedMenu, false);
        listView.setAdapter(listAdapter);

        if (_menuReceiver != null) {
            _receiverController.UnregisterReceiver(_menuReceiver);
        }

        _menuReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                @SuppressWarnings("unchecked")
                SerializableList<MenuDto> receivedMenu = (SerializableList<MenuDto>) intent
                        .getSerializableExtra(Bundles.MENU);
                if (receivedMenu != null) {
                    MenuListAdapter listAdapter = new MenuListAdapter(_context, menu, listedMenu, false);
                    listView.setAdapter(listAdapter);
                }
            }
        };

        _receiverController.RegisterReceiver(_menuReceiver, new String[]{Broadcasts.UPDATE_MENU});

        showDialog(true);
    }

    public void ShowSelectYoutubeIdDialog(
            @NonNull final String ip,
            @NonNull ArrayList<YoutubeVideoDto> youtubeVideoList) {
        checkOpenDialog();

        createDialog("ShowSelectYoutubeIdDialog", R.layout.dialog_skeleton_list);

        TextView title = (TextView) _dialog.findViewById(R.id.dialog_list_title);
        title.setText(_context.getResources().getString(R.string.selectVideo));

        final YoutubeVideoListAdapter listAdapter = new YoutubeVideoListAdapter(_context, youtubeVideoList,
                LucaDialogController.this, _mediaMirrorController, ip);
        ListView listView = (ListView) _dialog.findViewById(R.id.dialog_list_view);
        listView.setAdapter(listAdapter);
        listView.setVisibility(View.VISIBLE);

        final CheckBox playOnAllMirror = (CheckBox) _dialog.findViewById(R.id.dialog_list_checkbox);
        playOnAllMirror.setText(_context.getResources().getString(R.string.playOnAllMirror));
        playOnAllMirror.setVisibility(View.VISIBLE);
        playOnAllMirror.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                listAdapter.SetPlayOnAllMirror(isChecked);
            }
        });

        showDialog(true);
    }

    public void ShowYoutubeIdSelectionDialog(
            @NonNull final String ip,
            @NonNull final ArrayList<PlayedYoutubeVideoDto> playedYoutubeIds) {
        checkOpenDialog();

        createDialog("ShowYoutubeIdSelectionDialog", R.layout.dialog_select_youtube_id);

        final CheckBox playOnAllMirror = (CheckBox) _dialog.findViewById(R.id.dialog_select_youtube_play_on_all_mirror);

        ImageButton youtubeSearchButton = (ImageButton) _dialog.findViewById(R.id.dialog_enter_youtube_search_button);
        youtubeSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CloseDialogCallback.run();
                _searchYoutubeRunnable.SetData(new String[]{"", ip});
                ShowDialogEditText(
                        "Search youtube",
                        "Enter your search below",
                        "Search", _searchYoutubeRunnable,
                        true);
            }
        });

        final EditText youtubeIdEditText = (EditText) _dialog.findViewById(R.id.dialog_enter_youtube_id_editText);

        final Spinner predefinedYoutubeIdsSpinner = (Spinner) _dialog
                .findViewById(R.id.dialog_select_youtube_predefined_spinner);
        List<YoutubeId> youtubeIds = Arrays.asList(YoutubeId.values());
        ArrayAdapter<YoutubeId> youtubeIdDataAdapter = new ArrayAdapter<>(_context,
                android.R.layout.simple_spinner_item, youtubeIds);
        youtubeIdDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        predefinedYoutubeIdsSpinner.setAdapter(youtubeIdDataAdapter);
        predefinedYoutubeIdsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                _logger.Debug(
                        String.format("Parent: %s | view: %s | position: %s | id: %s", parent, view, position, id));
                YoutubeId selectedYoutubeId = YoutubeId.GetById(position);
                _logger.Debug(String.format("selectedYoutubeId is %s", selectedYoutubeId));
                youtubeIdEditText.setText(selectedYoutubeId.GetYoutubeId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        final ArrayList<String> playedYoutubeIdStrings = new ArrayList<>();
        for (PlayedYoutubeVideoDto entry : playedYoutubeIds) {
            playedYoutubeIdStrings.add(entry.GetYoutubeId());
        }
        final Spinner alreadyPlayedYoutubeIdsSpinner = (Spinner) _dialog
                .findViewById(R.id.dialog_select_youtube_played_spinner);
        ArrayAdapter<String> playedYoutubeIdDataAdapter = new ArrayAdapter<>(_context,
                android.R.layout.simple_spinner_item, playedYoutubeIdStrings);
        playedYoutubeIdDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        alreadyPlayedYoutubeIdsSpinner.setAdapter(playedYoutubeIdDataAdapter);
        alreadyPlayedYoutubeIdsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                _logger.Debug(
                        String.format("Parent: %s | view: %s | position: %s | id: %s", parent, view, position, id));
                String selectedYoutubeId = playedYoutubeIdStrings.get(position);
                _logger.Debug(String.format("selectedYoutubeId is %s", selectedYoutubeId));

                youtubeIdEditText.setText(selectedYoutubeId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        Button btnPlay = (Button) _dialog.findViewById(R.id.dialog_select_youtube_play);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String youtubeId = youtubeIdEditText.getText().toString();
                if (youtubeId.length() < 6) {
                    Toasty.error(_context, "YoutubeId invalid!", Toast.LENGTH_LONG).show();
                    return;
                }
                youtubeId = youtubeId.replace(" ", "");

                if (playOnAllMirror.isChecked()) {
                    for (MediaServerSelection entry : MediaServerSelection.values()) {
                        if (entry.GetId() > 0) {
                            _mediaMirrorController.SendCommand(entry.GetIp(),
                                    MediaServerAction.PLAY_YOUTUBE_VIDEO.toString(), youtubeId);
                        }
                    }
                } else {
                    _mediaMirrorController.SendCommand(ip, MediaServerAction.PLAY_YOUTUBE_VIDEO.toString(), youtubeId);
                }

                _mediaMirrorController.SendCommand(ip, MediaServerAction.GET_MEDIAMIRROR_DTO.toString(), "");

                CloseDialogCallback.run();
            }
        });

        showDialog(true);
    }

    public void Dispose() {
        CloseDialogCallback.run();
        _receiverController.Dispose();
        _mediaMirrorController.Dispose();
    }

    protected void checkOpenDialog() {
        _logger.Debug("checkOpenDialog");

        if (_isDialogOpen || _dialog != null) {
            _logger.Warn("Closing other Dialog...");
            CloseDialogCallback.run();
        }
    }

    protected void createDialog(
            @NonNull String dialogType,
            int layout) {
        _logger.Debug(String.format(Locale.getDefault(), "createDialog %s with layout %d", dialogType, layout));

        _dialog = new Dialog(_context);

        _dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        _dialog.setContentView(layout);
    }

    @SuppressWarnings("deprecation")
    protected void showDialog(boolean isCancelable) {
        _logger.Debug(String.format(Locale.getDefault(), "showDialog and isCancelable: %s", isCancelable));

        _dialog.setCancelable(isCancelable);
        _dialog.show();

        Window window = _dialog.getWindow();
        if (window != null) {
            window.setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        } else {
            _logger.Warn("Window is null!");
        }

        _isDialogOpen = true;
    }

    private void resetValues() {
        _birthday = null;
        _movie = null;
        _socket = null;
        _schedule = null;
        _timer = null;
    }

    private void sendBroadCast(
            @NonNull String broadcast,
            @NonNull String action) {
        Intent broadcastIntent = new Intent(broadcast);

        Bundle broadcastData = new Bundle();
        broadcastData.putString(Bundles.ACTION, action);
        broadcastIntent.putExtras(broadcastData);

        _context.sendBroadcast(broadcastIntent);
    }
}
