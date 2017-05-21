package guepardoapps.lucahome.services;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.view.Display;
import android.view.SurfaceHolder;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.dto.WeatherModelDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.openweather.common.enums.WeatherCondition;

import guepardoapps.library.toolset.common.Tools;
import guepardoapps.library.toolset.common.classes.SerializableTime;
import guepardoapps.library.toolset.controller.DisplayController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.views.MainView;
import guepardoapps.lucahome.views.controller.*;

@SuppressWarnings("deprecation")
public class WatchFaceService extends CanvasWatchFaceService {

    private static final String TAG = WatchFaceService.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {

        private Display _display;
        private Time _time;
        private Paint _backgroundPaint;

        private float _scale = 1;
        private Rect _cardBounds = new Rect();

        private boolean _lowBitAmbient;
        private boolean _burnInProtection;
        private boolean _registeredTimeZoneReceiver = false;
        private boolean _ambient;
        private boolean _isHomeNetwork = true;

        private Bitmap _backgroundBitmap;
        private Bitmap _grayBackgroundBitmap;
        private Bitmap _launcherBitmap;

        private Context _context;
        private DisplayController _displayController;
        private Tools _tools;

        private int _textColor = 0xFFFFFFFF;
        private WeatherModelDto _currentWeather;

        private BatteryPhoneViewController _batteryPhoneViewController;
        private BatteryWearViewController _batteryWearViewController;
        private CurrentWeatherViewController _currentWeatherViewController;
        private DateViewController _dateViewController;
        private ReceiverController _receiverController;
        private StepViewController _stepViewController;
        private TemperatureViewController _temperatureViewController;

        private final Handler _updateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if (R.id.message_update == message.what) {
                    invalidate();
                    if (shouldTimerBeRunning()) {
                        long timeMs = System.currentTimeMillis();
                        long delayMs = INTERACTIVE_UPDATE_RATE_MS - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                        _updateTimeHandler.sendEmptyMessageDelayed(R.id.message_update, delayMs);
                    }
                }
            }
        };

        private BroadcastReceiver _currentWeatherReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                _logger.Debug("_currentWeatherReceiver onReceive");
                WeatherModelDto newWeather = (WeatherModelDto) intent.getSerializableExtra(Bundles.CURRENT_WEATHER);
                if (newWeather != null) {
                    _logger.Debug("New current weather: " + newWeather.toString());
                    _currentWeather = newWeather;
                    setLayout(_currentWeather.GetWeatherCondition());
                }
            }
        };

        private final BroadcastReceiver _timeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                _time.clear(intent.getStringExtra("time-zone"));
                _time.setToNow();
            }
        };

        private final BroadcastReceiver _wifiStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                _logger.Debug("_wifiStateReceiver onReceive");
                String wifiState = intent.getStringExtra(Bundles.WIFI_STATE);
                if (wifiState != null) {
                    if (wifiState.contains("HOME")) {
                        _isHomeNetwork = true;
                    } else if (wifiState.contains("NO")) {
                        _isHomeNetwork = false;
                    } else {
                        _isHomeNetwork = false;
                        _logger.Warn("wifiState not supported: " + wifiState);
                    }
                } else {
                    _logger.Warn("wifiState is null!");
                }
            }
        };

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            _logger = new LucaHomeLogger(TAG);
            _logger.Debug("onCreate");

            _context = WatchFaceService.this;
            _displayController = new DisplayController(_context);
            _tools = new Tools();
            _display = _displayController.GetDisplayDimension();

            startService(new Intent(_context, WearMessageListenerService.class));

            _batteryPhoneViewController = new BatteryPhoneViewController(_context);
            _batteryWearViewController = new BatteryWearViewController(_context);
            _currentWeatherViewController = new CurrentWeatherViewController(_context);
            _dateViewController = new DateViewController(_context);
            _receiverController = new ReceiverController(_context);
            _stepViewController = new StepViewController(_context);
            _temperatureViewController = new TemperatureViewController(_context);

            setWatchFaceStyle(
                    new WatchFaceStyle.Builder((Service) _context).setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                            .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                            .setShowSystemUiTime(false).setAcceptsTapEvents(true).build());

            _backgroundPaint = new Paint();
            _backgroundPaint.setColor(Color.BLACK);

            setLayout(WeatherCondition.NULL);
            _launcherBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.launcher);
            _time = new Time();

            _receiverController.RegisterReceiver(_currentWeatherReceiver,
                    new String[]{Broadcasts.UPDATE_CURRENT_WEATHER});
            _receiverController.RegisterReceiver(_wifiStateReceiver, new String[]{Broadcasts.UPDATE_WIFI_STATE});
        }

        @Override
        public void onDestroy() {
            _logger.Debug("onDestroy");

            _batteryWearViewController.onDestroy();
            _batteryPhoneViewController.onDestroy();
            _currentWeatherViewController.onDestroy();
            _dateViewController.onDestroy();
            _stepViewController.onDestroy();
            _temperatureViewController.onDestroy();

            _receiverController.UnregisterReceiver(_currentWeatherReceiver);
            _receiverController.UnregisterReceiver(_wifiStateReceiver);

            _updateTimeHandler.removeMessages(R.id.message_update);
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            _logger.Debug("onPropertiesChanged");
            super.onPropertiesChanged(properties);
            _lowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            _burnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
        }

        @Override
        public void onTimeTick() {
            _logger.Debug("onTimeTick");
            super.onTimeTick();
            if (_currentWeather == null) {
                setLayout(WeatherCondition.NULL);
            } else {
                setLayout(_currentWeather.GetWeatherCondition());
            }
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            _logger.Debug("onAmbientModeChanged");
            super.onAmbientModeChanged(inAmbientMode);
            if (_ambient != inAmbientMode) {
                _ambient = inAmbientMode;
                invalidate();
            }

            updateTimer();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            _logger.Debug("onSurfaceChanged");
            super.onSurfaceChanged(holder, format, width, height);

            scaleBackground(width);

            if (!_burnInProtection || !_lowBitAmbient) {
                initGrayBackgroundBitmap();
            }
        }

        @Override
        public void onTapCommand(@TapType int tapType, int x, int y, long eventTime) {
            switch (tapType) {
                case WatchFaceService.TAP_TYPE_TAP:
                    if (withinTapRegion(x, y) && _isHomeNetwork) {
                        Intent intent = new Intent(_context, MainView.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    break;
                default:
                    super.onTapCommand(tapType, x, y, eventTime);
                    break;
            }
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            _logger.Debug("onDraw");
            _time.setToNow();

            if (_ambient && (_lowBitAmbient || _burnInProtection)) {
                canvas.drawColor(Color.BLACK);
            } else if (_ambient) {
                canvas.drawBitmap(_grayBackgroundBitmap, 0, 0, _backgroundPaint);
            } else {
                canvas.drawBitmap(_backgroundBitmap, 0, 0, _backgroundPaint);
                if (_isHomeNetwork) {
                    canvas.drawBitmap(_launcherBitmap, (_display.getWidth() / 3) + 25, (_display.getHeight() / 3) + 25,
                            _backgroundPaint);
                }
            }

            if (_ambient) {
                canvas.drawRect(_cardBounds, _backgroundPaint);
            }

            _batteryWearViewController.Draw(canvas, _textColor);
            _batteryPhoneViewController.Draw(canvas, _textColor);
            _currentWeatherViewController.Draw(canvas, _textColor);
            _dateViewController.DrawDate(canvas, _time, _textColor);
            _dateViewController.DrawWeekOfYear(canvas, _time, _textColor);
            _dateViewController.DrawTime(canvas, _time, _textColor);
            _stepViewController.Draw(canvas, _textColor);
            _temperatureViewController.Draw(canvas, _textColor);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            _logger.Debug("onVisibilityChanged");
            super.onVisibilityChanged(visible);

            if (visible) {
                registerTimeReceiver();

                _time.clear(TimeZone.getDefault().getID());
                _time.setToNow();
            } else {
                unregisterTimeReceiver();
            }

            updateTimer();
        }

        @Override
        public void onPeekCardPositionUpdate(Rect rect) {
            _logger.Debug("onPeekCardPositionUpdate");
            super.onPeekCardPositionUpdate(rect);
            _cardBounds.set(rect);
        }

        private void initGrayBackgroundBitmap() {
            _logger.Debug("initGrayBackgroundBitmap");
            _grayBackgroundBitmap = Bitmap.createBitmap(_backgroundBitmap.getWidth(), _backgroundBitmap.getHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(_grayBackgroundBitmap);
            Paint grayPaint = new Paint();
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
            grayPaint.setColorFilter(filter);
            canvas.drawBitmap(_backgroundBitmap, 0, 0, grayPaint);
        }

        private void registerTimeReceiver() {
            _logger.Debug("registerReceiver");
            if (_registeredTimeZoneReceiver) {
                return;
            }
            _receiverController.RegisterReceiver(_timeZoneReceiver, new String[]{Intent.ACTION_TIMEZONE_CHANGED});
            _registeredTimeZoneReceiver = true;
        }

        private void unregisterTimeReceiver() {
            _logger.Debug("unregisterReceiver");
            if (!_registeredTimeZoneReceiver) {
                return;
            }
            _registeredTimeZoneReceiver = false;
            _receiverController.UnregisterReceiver(_timeZoneReceiver);
        }

        private void updateTimer() {
            _logger.Debug("updateTimer");
            _updateTimeHandler.removeMessages(R.id.message_update);
            if (shouldTimerBeRunning()) {
                _updateTimeHandler.sendEmptyMessage(R.id.message_update);
            }
        }

        private boolean shouldTimerBeRunning() {
            _logger.Debug("shouldTimerBeRunning");
            return isVisible() && !isInAmbientMode();
        }

        private boolean withinTapRegion(int x, int y) {
            if (x > ((_display.getWidth() / 3) + 25) && x < ((_display.getWidth() / 3) + 25 + 25)) {
                if (y > ((_display.getHeight() / 3) + 25) && y < ((_display.getHeight() / 3) + 25 + 25)) {
                    return true;
                }
            }
            return false;
        }

        private void setLayout(@NonNull WeatherCondition currentCondition) {
            _logger.Debug(String.format("setBackgroundBitmap with WeatherCondition %s", currentCondition));

            if (_currentWeather != null) {
                _logger.Info(String.format("Time now is %s | Time sunrise is %s | time sunset is %s",
                        new SerializableTime().toMilliSecond(), _currentWeather.GetSunriseTime(),
                        _currentWeather.GetSunsetTime()));

                if (_currentWeather.GetSunriseTime().isAfterNow() || _currentWeather.GetSunsetTime().isBeforeNow()) {
                    _textColor = 0xFFFFFFFF;
                    _backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.wallpaper_night);
                    scaleBackground(_display.getWidth());
                    return;
                } else {
                    _logger.Debug("Current weather is between sunset and sunrise! Checking condition!");
                }
            } else {
                _logger.Warn("Current weather is null!");
            }

            _textColor = 0xFFFFFFFF;
            _backgroundBitmap = BitmapFactory.decodeResource(getResources(), currentCondition.GetWallpaper());

            scaleBackground(_display.getWidth());
        }

        private void scaleBackground(int width) {
            _scale = ((float) width) / (float) _backgroundBitmap.getWidth();

            _backgroundBitmap = Bitmap.createScaledBitmap(_backgroundBitmap,
                    (int) (_backgroundBitmap.getWidth() * _scale), (int) (_backgroundBitmap.getHeight() * _scale),
                    true);
        }
    }
}