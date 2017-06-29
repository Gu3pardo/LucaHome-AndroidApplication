package guepardoapps.lucahome.views.controller.home;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.Locale;

import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.services.helper.NavigationService;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.views.BirthdayView;
import guepardoapps.lucahome.views.GameView;
import guepardoapps.lucahome.views.MediaMirrorView;
import guepardoapps.lucahome.views.MenuView;
import guepardoapps.lucahome.views.MovieView;
import guepardoapps.lucahome.views.ScheduleView;
import guepardoapps.lucahome.views.SecurityView;
import guepardoapps.lucahome.views.SensorAirPressureView;
import guepardoapps.lucahome.views.SensorHumidityView;
import guepardoapps.lucahome.views.SensorTemperatureView;
import guepardoapps.lucahome.views.ShoppingListView;
import guepardoapps.lucahome.views.SocketView;
import guepardoapps.lucahome.views.TimerView;

public class ListButtonController {

    private static final String TAG = ListButtonController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final int LINEAR_LAYOUT_COUNT = 5;

    private static final int _controlIndex = 0;
    private static final int _mediaIndex = 1;
    private static final int _sensorIndex = 2;
    private static final int _livingIndex = 3;
    private static final int _socialIndex = 4;

    private Context _context;
    private NavigationService _navigationService;

    private ScrollView _buttonScrollView;

    private LinearLayout[] _linearLayout = new LinearLayout[LINEAR_LAYOUT_COUNT];

    private LinearLayout _linearLayoutButtonControl;
    private LinearLayout _linearLayoutButtonMedia;
    private LinearLayout _linearLayoutButtonSensors;
    private LinearLayout _linearLayoutButtonLiving;
    private LinearLayout _linearLayoutButtonSocial;

    public ListButtonController(@NonNull Context context) {
        _logger = new LucaHomeLogger(TAG);
        _context = context;
        _navigationService = new NavigationService(_context);
    }

    public void onCreate() {
        _logger.Debug("onCreate");
        _buttonScrollView = ((Activity) _context).findViewById(R.id.buttonScrollView);

        initializeLinearLayout();

        initializeButtonMain();

        initializeButtonControl();
        initializeButtonMedia();
        initializeButtonLiving();
        initializeButtonSocial();
        initializeButtonSensor();
    }

    public void onResume() {
        _logger.Debug("onResume");
    }

    public void onPause() {
        _logger.Debug("onPause");
    }

    public void onDestroy() {
        _logger.Debug("onDestroy");
    }

    private void initializeLinearLayout() {
        _logger.Debug("initializeLinearLayout");

        _linearLayoutButtonControl = ((Activity) _context).findViewById(R.id.linearLayoutButtonControl);
        _linearLayout[_controlIndex] = _linearLayoutButtonControl;
        _linearLayoutButtonMedia = ((Activity) _context).findViewById(R.id.linearLayoutButtonMedia);
        _linearLayout[_mediaIndex] = _linearLayoutButtonMedia;
        _linearLayoutButtonSensors = ((Activity) _context).findViewById(R.id.linearLayoutButtonSensors);
        _linearLayout[_sensorIndex] = _linearLayoutButtonSensors;
        _linearLayoutButtonLiving = ((Activity) _context).findViewById(R.id.linearLayoutLiving);
        _linearLayout[_livingIndex] = _linearLayoutButtonLiving;
        _linearLayoutButtonSocial = ((Activity) _context).findViewById(R.id.linearLayoutButtonSocial);
        _linearLayout[_socialIndex] = _linearLayoutButtonSocial;

        for (int index = 0; index < LINEAR_LAYOUT_COUNT; index++) {
            _linearLayout[index].setVisibility(View.GONE);
        }
    }

    private void initializeButtonMain() {
        _logger.Debug("initializeButtonMain");

        Button buttonControl = ((Activity) _context).findViewById(R.id.buttonControl);
        buttonControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleLinearLayout(_linearLayoutButtonControl);
            }
        });

        Button buttonMedia = ((Activity) _context).findViewById(R.id.buttonMedia);
        buttonMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleLinearLayout(_linearLayoutButtonMedia);
            }
        });

        Button buttonLiving = ((Activity) _context).findViewById(R.id.buttonLiving);
        buttonLiving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleLinearLayout(_linearLayoutButtonLiving);
            }
        });

        Button buttonSocial = ((Activity) _context).findViewById(R.id.buttonSocial);
        buttonSocial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleLinearLayout(_linearLayoutButtonSocial);
            }
        });

        Button buttonSecurity = ((Activity) _context).findViewById(R.id.buttonSecurity);
        buttonSecurity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _navigationService.NavigateTo(SecurityView.class, true);
            }
        });

        Button buttonSensors = ((Activity) _context).findViewById(R.id.buttonSensors);
        buttonSensors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleLinearLayout(_linearLayoutButtonSensors);
            }
        });
    }

    private void initializeButtonControl() {
        _logger.Debug("initializeButtonControl");

        Button buttonSockets = ((Activity) _context).findViewById(R.id.buttonSockets);
        buttonSockets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _navigationService.NavigateTo(SocketView.class, true);
            }
        });

        Button buttonSchedules = ((Activity) _context).findViewById(R.id.buttonSchedules);
        buttonSchedules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _navigationService.NavigateTo(ScheduleView.class, true);
            }
        });

        Button buttonTimer = ((Activity) _context).findViewById(R.id.buttonTimer);
        buttonTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _navigationService.NavigateTo(TimerView.class, true);
            }
        });
    }

    private void initializeButtonMedia() {
        _logger.Debug("initializeButtonMedia");

        Button buttonMovies = ((Activity) _context).findViewById(R.id.buttonMovies);
        buttonMovies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _navigationService.NavigateTo(MovieView.class, true);
            }
        });

        Button buttonMediaServer = ((Activity) _context).findViewById(R.id.buttonMediaServer);
        buttonMediaServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _navigationService.NavigateTo(MediaMirrorView.class, true);
            }
        });
    }

    private void initializeButtonSensor() {
        _logger.Debug("initializeButtonSensor");

        Button buttonTemperature = ((Activity) _context).findViewById(R.id.buttonTemperature);
        buttonTemperature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _navigationService.NavigateTo(SensorTemperatureView.class, true);
            }
        });

        Button buttonHumidity = ((Activity) _context).findViewById(R.id.buttonHumidity);
        buttonHumidity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _navigationService.NavigateTo(SensorHumidityView.class, true);
            }
        });

        Button buttonAirPressure = ((Activity) _context).findViewById(R.id.buttonAirPressure);
        buttonAirPressure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _navigationService.NavigateTo(SensorAirPressureView.class, true);
            }
        });
    }

    private void initializeButtonLiving() {
        _logger.Debug("initializeButtonLiving");

        Button buttonMenu = ((Activity) _context).findViewById(R.id.buttonMenu);
        buttonMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _navigationService.NavigateTo(MenuView.class, true);
            }
        });

        Button buttonShoppingList = ((Activity) _context).findViewById(R.id.buttonShoppingList);
        buttonShoppingList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _navigationService.NavigateTo(ShoppingListView.class, true);
            }
        });
    }

    private void initializeButtonSocial() {
        _logger.Debug("initializeButtonSocial");

        Button buttonBirthdays = ((Activity) _context).findViewById(R.id.buttonBirthdays);
        buttonBirthdays.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _navigationService.NavigateTo(BirthdayView.class, true);
            }
        });

        Button buttonGames = ((Activity) _context).findViewById(R.id.buttonGames);
        buttonGames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _navigationService.NavigateTo(GameView.class, true);
            }
        });
    }

    private void handleLinearLayout(@NonNull LinearLayout linearLayout) {
        _logger.Debug(String.format(Locale.getDefault(), "handleLinearLayout with linearLayout %s", linearLayout));

        for (int index = 0; index < LINEAR_LAYOUT_COUNT; index++) {
            LinearLayout currentLinearLayout = _linearLayout[index];
            if (currentLinearLayout == linearLayout) {
                int visibility = currentLinearLayout.getVisibility();
                if (visibility == View.GONE) {
                    currentLinearLayout.setVisibility(View.VISIBLE);
                } else {
                    currentLinearLayout.setVisibility(View.GONE);
                }
                scrollButtonView(index);
            } else {
                currentLinearLayout.setVisibility(View.GONE);
            }
        }
    }

    private void scrollButtonView(final int index) {
        _logger.Debug(String.format(Locale.getDefault(), "scrollButtonView with index %d", index));

        _buttonScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                int x = _buttonScrollView.getWidth();
                int y = (index * _buttonScrollView.getHeight()) / 5;
                _buttonScrollView.scrollTo(x, y);
            }
        }, 150);
    }
}
