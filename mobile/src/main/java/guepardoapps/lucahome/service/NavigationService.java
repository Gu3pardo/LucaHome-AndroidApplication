package guepardoapps.lucahome.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import guepardoapps.lucahome.views.*;

public class NavigationService {
    public enum NavigationResult {NULL, PERMITTED, PERMITTED_SAME_ACTIVITIES, POSSIBLE, SUCCESS}

    private static final NavigationService SINGLETON = new NavigationService();

    // private static final String TAG = NavigationService.class.getSimpleName();

    private Class<?> _currentActivity;

    private List<Class<?>> _backEntryList = new ArrayList<>();

    private NavigationService() {
    }

    public static NavigationService getInstance() {
        return SINGLETON;
    }

    public NavigationResult NavigateToActivity(@NonNull Context startActivityContext, @NonNull Class<?> endActivity) {
        NavigationResult canNavigateResult = canNavigate(startActivityContext, endActivity);
        if (canNavigateResult != NavigationResult.POSSIBLE) {
            return canNavigateResult;
        }

        startActivityContext.startActivity(new Intent(startActivityContext, endActivity));

        _backEntryList.add(startActivityContext.getClass());
        _currentActivity = endActivity;

        return NavigationResult.SUCCESS;
    }

    public NavigationResult NavigateToActivityWithData(@NonNull Context startActivityContext, @NonNull Class<?> endActivity, @NonNull Bundle data) {
        NavigationResult canNavigateResult = canNavigate(startActivityContext, endActivity);
        if (canNavigateResult != NavigationResult.POSSIBLE) {
            return canNavigateResult;
        }

        Intent startIntent = new Intent(startActivityContext, endActivity);
        startIntent.putExtras(data);
        startActivityContext.startActivity(startIntent);

        _backEntryList.add(startActivityContext.getClass());
        _currentActivity = endActivity;

        return NavigationResult.SUCCESS;
    }

    public NavigationResult GoBack(@NonNull Context startActivityContext) {
        if (_backEntryList.size() == 0) {
            _backEntryList.add(MainActivity.class);
        }

        Class<?> backActivity = _backEntryList.get(_backEntryList.size() - 1);
        NavigationResult canNavigateResult = canNavigate(startActivityContext, backActivity);
        if (canNavigateResult != NavigationResult.POSSIBLE) {
            return canNavigateResult;
        }

        ((Activity) startActivityContext).finish();

        _backEntryList.remove(_backEntryList.size() - 1);
        _currentActivity = backActivity;

        return NavigationResult.SUCCESS;
    }

    public void ClearCurrentActivity() {
        _currentActivity = null;
    }

    public void ClearGoBackList() {
        _backEntryList.clear();
    }

    private NavigationResult canNavigate(@NonNull Context startActivityContext, @NonNull Class<?> endActivity) {
        if (_currentActivity == endActivity) {
            return NavigationResult.PERMITTED_SAME_ACTIVITIES;
        }

        if (startActivityContext.getClass() == BirthdayActivity.class) {
            if (endActivity.getClass().isInstance(BirthdayEditActivity.class)
                    || endActivity.getClass().isInstance(CoinActivity.class)
                    || endActivity.getClass().isInstance(ForecastWeatherActivity.class)
                    || endActivity.getClass().isInstance(LoginActivity.class)
                    || endActivity.getClass().isInstance(MainActivity.class)
                    || endActivity.getClass().isInstance(MediaServerActivity.class)
                    || endActivity.getClass().isInstance(MenuActivity.class)
                    || endActivity.getClass().isInstance(MovieActivity.class)
                    || endActivity.getClass().isInstance(ScheduleActivity.class)
                    || endActivity.getClass().isInstance(SecurityActivity.class)
                    || endActivity.getClass().isInstance(SettingsActivity.class)
                    || endActivity.getClass().isInstance(ShoppingListActivity.class)
                    || endActivity.getClass().isInstance(TimerActivity.class)
                    || endActivity.getClass().isInstance(WirelessSocketActivity.class)
                    || endActivity.getClass().isInstance(WirelessSwitchActivity.class)) {
                return NavigationResult.POSSIBLE;
            }

            return NavigationResult.PERMITTED;

        } else if (startActivityContext.getClass() == BirthdayEditActivity.class) {
            if (endActivity.getClass().isInstance(BirthdayActivity.class)) {
                return NavigationResult.POSSIBLE;
            }

            return NavigationResult.PERMITTED;

        } else if (startActivityContext.getClass() == BootActivity.class) {
            if (endActivity.getClass().isInstance(LoginActivity.class)) {
                return NavigationResult.POSSIBLE;
            }

            return NavigationResult.PERMITTED;

        } else if (startActivityContext.getClass() == CoinActivity.class) {
            if (endActivity.getClass().isInstance(BirthdayActivity.class)
                    || endActivity.getClass().isInstance(CoinEditActivity.class)
                    || endActivity.getClass().isInstance(ForecastWeatherActivity.class)
                    || endActivity.getClass().isInstance(LoginActivity.class)
                    || endActivity.getClass().isInstance(MainActivity.class)
                    || endActivity.getClass().isInstance(MediaServerActivity.class)
                    || endActivity.getClass().isInstance(MenuActivity.class)
                    || endActivity.getClass().isInstance(MovieActivity.class)
                    || endActivity.getClass().isInstance(ScheduleActivity.class)
                    || endActivity.getClass().isInstance(SecurityActivity.class)
                    || endActivity.getClass().isInstance(SettingsActivity.class)
                    || endActivity.getClass().isInstance(ShoppingListActivity.class)
                    || endActivity.getClass().isInstance(TimerActivity.class)
                    || endActivity.getClass().isInstance(WirelessSocketActivity.class)
                    || endActivity.getClass().isInstance(WirelessSwitchActivity.class)) {
                return NavigationResult.POSSIBLE;
            }

            return NavigationResult.PERMITTED;

        } else if (startActivityContext.getClass() == CoinEditActivity.class) {
            if (endActivity.getClass().isInstance(CoinActivity.class)) {
                return NavigationResult.POSSIBLE;
            }

            return NavigationResult.PERMITTED;

        } else if (startActivityContext.getClass() == ForecastWeatherActivity.class) {
            if (endActivity.getClass().isInstance(BirthdayActivity.class)
                    || endActivity.getClass().isInstance(CoinActivity.class)
                    || endActivity.getClass().isInstance(LoginActivity.class)
                    || endActivity.getClass().isInstance(MainActivity.class)
                    || endActivity.getClass().isInstance(MediaServerActivity.class)
                    || endActivity.getClass().isInstance(MenuActivity.class)
                    || endActivity.getClass().isInstance(MovieActivity.class)
                    || endActivity.getClass().isInstance(ScheduleActivity.class)
                    || endActivity.getClass().isInstance(SecurityActivity.class)
                    || endActivity.getClass().isInstance(SettingsActivity.class)
                    || endActivity.getClass().isInstance(ShoppingListActivity.class)
                    || endActivity.getClass().isInstance(TimerActivity.class)
                    || endActivity.getClass().isInstance(WirelessSocketActivity.class)
                    || endActivity.getClass().isInstance(WirelessSwitchActivity.class)) {
                return NavigationResult.POSSIBLE;
            }

            return NavigationResult.PERMITTED;

        } else if (startActivityContext.getClass() == LoginActivity.class) {
            if (endActivity.getClass().isInstance(BootActivity.class)) {
                return NavigationResult.POSSIBLE;
            }

            return NavigationResult.PERMITTED;

        } else if (startActivityContext.getClass() == MainActivity.class) {
            if (endActivity.getClass().isInstance(BirthdayActivity.class)
                    || endActivity.getClass().isInstance(CoinActivity.class)
                    || endActivity.getClass().isInstance(ForecastWeatherActivity.class)
                    || endActivity.getClass().isInstance(LoginActivity.class)
                    || endActivity.getClass().isInstance(MediaServerActivity.class)
                    || endActivity.getClass().isInstance(MenuActivity.class)
                    || endActivity.getClass().isInstance(MovieActivity.class)
                    || endActivity.getClass().isInstance(ScheduleActivity.class)
                    || endActivity.getClass().isInstance(SecurityActivity.class)
                    || endActivity.getClass().isInstance(SettingsActivity.class)
                    || endActivity.getClass().isInstance(ShoppingListActivity.class)
                    || endActivity.getClass().isInstance(TimerActivity.class)
                    || endActivity.getClass().isInstance(WirelessSocketActivity.class)
                    || endActivity.getClass().isInstance(WirelessSwitchActivity.class)) {
                return NavigationResult.POSSIBLE;
            }

            return NavigationResult.PERMITTED;

        } else if (startActivityContext.getClass() == MenuActivity.class) {
            if (endActivity.getClass().isInstance(BirthdayActivity.class)
                    || endActivity.getClass().isInstance(CoinActivity.class)
                    || endActivity.getClass().isInstance(ForecastWeatherActivity.class)
                    || endActivity.getClass().isInstance(LoginActivity.class)
                    || endActivity.getClass().isInstance(MainActivity.class)
                    || endActivity.getClass().isInstance(MediaServerActivity.class)
                    || endActivity.getClass().isInstance(MenuEditActivity.class)
                    || endActivity.getClass().isInstance(MovieActivity.class)
                    || endActivity.getClass().isInstance(ScheduleActivity.class)
                    || endActivity.getClass().isInstance(SecurityActivity.class)
                    || endActivity.getClass().isInstance(SettingsActivity.class)
                    || endActivity.getClass().isInstance(ShoppingListActivity.class)
                    || endActivity.getClass().isInstance(TimerActivity.class)
                    || endActivity.getClass().isInstance(WirelessSocketActivity.class)
                    || endActivity.getClass().isInstance(WirelessSwitchActivity.class)) {
                return NavigationResult.POSSIBLE;
            }

            return NavigationResult.PERMITTED;

        } else if (startActivityContext.getClass() == MenuEditActivity.class) {
            if (endActivity.getClass().isInstance(MenuActivity.class)) {
                return NavigationResult.POSSIBLE;
            }

            return NavigationResult.PERMITTED;

        } else if (startActivityContext.getClass() == MediaServerActivity.class) {
            if (endActivity.getClass().isInstance(BirthdayActivity.class)
                    || endActivity.getClass().isInstance(CoinActivity.class)
                    || endActivity.getClass().isInstance(ForecastWeatherActivity.class)
                    || endActivity.getClass().isInstance(LoginActivity.class)
                    || endActivity.getClass().isInstance(MainActivity.class)
                    || endActivity.getClass().isInstance(MenuActivity.class)
                    || endActivity.getClass().isInstance(MovieActivity.class)
                    || endActivity.getClass().isInstance(ScheduleActivity.class)
                    || endActivity.getClass().isInstance(SecurityActivity.class)
                    || endActivity.getClass().isInstance(SettingsActivity.class)
                    || endActivity.getClass().isInstance(ShoppingListActivity.class)
                    || endActivity.getClass().isInstance(TimerActivity.class)
                    || endActivity.getClass().isInstance(WirelessSocketActivity.class)
                    || endActivity.getClass().isInstance(WirelessSwitchActivity.class)) {
                return NavigationResult.POSSIBLE;
            }

            return NavigationResult.PERMITTED;

        } else if (startActivityContext.getClass() == MovieActivity.class) {
            if (endActivity.getClass().isInstance(BirthdayActivity.class)
                    || endActivity.getClass().isInstance(CoinActivity.class)
                    || endActivity.getClass().isInstance(ForecastWeatherActivity.class)
                    || endActivity.getClass().isInstance(LoginActivity.class)
                    || endActivity.getClass().isInstance(MainActivity.class)
                    || endActivity.getClass().isInstance(MediaServerActivity.class)
                    || endActivity.getClass().isInstance(MenuActivity.class)
                    || endActivity.getClass().isInstance(MovieEditActivity.class)
                    || endActivity.getClass().isInstance(ScheduleActivity.class)
                    || endActivity.getClass().isInstance(SecurityActivity.class)
                    || endActivity.getClass().isInstance(SettingsActivity.class)
                    || endActivity.getClass().isInstance(ShoppingListActivity.class)
                    || endActivity.getClass().isInstance(TimerActivity.class)
                    || endActivity.getClass().isInstance(WirelessSocketActivity.class)
                    || endActivity.getClass().isInstance(WirelessSwitchActivity.class)) {
                return NavigationResult.POSSIBLE;
            }

            return NavigationResult.PERMITTED;

        } else if (startActivityContext.getClass() == MovieEditActivity.class) {
            if (endActivity.getClass().isInstance(MovieActivity.class)) {
                return NavigationResult.POSSIBLE;
            }

            return NavigationResult.PERMITTED;

        } else if (startActivityContext.getClass() == ScheduleActivity.class) {
            if (endActivity.getClass().isInstance(BirthdayActivity.class)
                    || endActivity.getClass().isInstance(CoinActivity.class)
                    || endActivity.getClass().isInstance(ForecastWeatherActivity.class)
                    || endActivity.getClass().isInstance(LoginActivity.class)
                    || endActivity.getClass().isInstance(MainActivity.class)
                    || endActivity.getClass().isInstance(MediaServerActivity.class)
                    || endActivity.getClass().isInstance(MenuActivity.class)
                    || endActivity.getClass().isInstance(MovieActivity.class)
                    || endActivity.getClass().isInstance(ScheduleEditActivity.class)
                    || endActivity.getClass().isInstance(SecurityActivity.class)
                    || endActivity.getClass().isInstance(SettingsActivity.class)
                    || endActivity.getClass().isInstance(ShoppingListActivity.class)
                    || endActivity.getClass().isInstance(TimerActivity.class)
                    || endActivity.getClass().isInstance(WirelessSocketActivity.class)
                    || endActivity.getClass().isInstance(WirelessSwitchActivity.class)) {
                return NavigationResult.POSSIBLE;
            }

            return NavigationResult.PERMITTED;

        } else if (startActivityContext.getClass() == ScheduleEditActivity.class) {
            if (endActivity.getClass().isInstance(ScheduleActivity.class)) {
                return NavigationResult.POSSIBLE;
            }

            return NavigationResult.PERMITTED;

        } else if (startActivityContext.getClass() == SecurityActivity.class) {
            if (endActivity.getClass().isInstance(BirthdayActivity.class)
                    || endActivity.getClass().isInstance(CoinActivity.class)
                    || endActivity.getClass().isInstance(ForecastWeatherActivity.class)
                    || endActivity.getClass().isInstance(LoginActivity.class)
                    || endActivity.getClass().isInstance(MainActivity.class)
                    || endActivity.getClass().isInstance(MediaServerActivity.class)
                    || endActivity.getClass().isInstance(MenuActivity.class)
                    || endActivity.getClass().isInstance(MovieActivity.class)
                    || endActivity.getClass().isInstance(ScheduleActivity.class)
                    || endActivity.getClass().isInstance(SettingsActivity.class)
                    || endActivity.getClass().isInstance(ShoppingListActivity.class)
                    || endActivity.getClass().isInstance(TimerActivity.class)
                    || endActivity.getClass().isInstance(WirelessSocketActivity.class)
                    || endActivity.getClass().isInstance(WirelessSwitchActivity.class)) {
                return NavigationResult.POSSIBLE;
            }

            return NavigationResult.PERMITTED;

        } else if (startActivityContext.getClass() == SettingsActivity.class) {
            if (endActivity.getClass().isInstance(BirthdayActivity.class)
                    || endActivity.getClass().isInstance(CoinActivity.class)
                    || endActivity.getClass().isInstance(ForecastWeatherActivity.class)
                    || endActivity.getClass().isInstance(LoginActivity.class)
                    || endActivity.getClass().isInstance(MainActivity.class)
                    || endActivity.getClass().isInstance(MediaServerActivity.class)
                    || endActivity.getClass().isInstance(MenuActivity.class)
                    || endActivity.getClass().isInstance(MovieActivity.class)
                    || endActivity.getClass().isInstance(ScheduleActivity.class)
                    || endActivity.getClass().isInstance(SecurityActivity.class)
                    || endActivity.getClass().isInstance(SettingsActivity.class)
                    || endActivity.getClass().isInstance(ShoppingListActivity.class)
                    || endActivity.getClass().isInstance(TimerActivity.class)
                    || endActivity.getClass().isInstance(WirelessSocketActivity.class)
                    || endActivity.getClass().isInstance(WirelessSwitchActivity.class)) {
                return NavigationResult.POSSIBLE;
            }

            return NavigationResult.PERMITTED;

        } else if (startActivityContext.getClass() == ShoppingListActivity.class) {
            if (endActivity.getClass().isInstance(BirthdayActivity.class)
                    || endActivity.getClass().isInstance(CoinActivity.class)
                    || endActivity.getClass().isInstance(ForecastWeatherActivity.class)
                    || endActivity.getClass().isInstance(LoginActivity.class)
                    || endActivity.getClass().isInstance(MainActivity.class)
                    || endActivity.getClass().isInstance(MediaServerActivity.class)
                    || endActivity.getClass().isInstance(MenuActivity.class)
                    || endActivity.getClass().isInstance(MovieActivity.class)
                    || endActivity.getClass().isInstance(ScheduleActivity.class)
                    || endActivity.getClass().isInstance(SecurityActivity.class)
                    || endActivity.getClass().isInstance(SettingsActivity.class)
                    || endActivity.getClass().isInstance(ShoppingListEditActivity.class)
                    || endActivity.getClass().isInstance(TimerActivity.class)
                    || endActivity.getClass().isInstance(WirelessSocketActivity.class)
                    || endActivity.getClass().isInstance(WirelessSwitchActivity.class)) {
                return NavigationResult.POSSIBLE;
            }

            return NavigationResult.PERMITTED;

        } else if (startActivityContext.getClass() == ShoppingListEditActivity.class) {
            if (endActivity.getClass().isInstance(ShoppingListActivity.class)) {
                return NavigationResult.POSSIBLE;
            }

            return NavigationResult.PERMITTED;

        } else if (startActivityContext.getClass() == TimerActivity.class) {
            if (endActivity.getClass().isInstance(BirthdayActivity.class)
                    || endActivity.getClass().isInstance(CoinActivity.class)
                    || endActivity.getClass().isInstance(ForecastWeatherActivity.class)
                    || endActivity.getClass().isInstance(LoginActivity.class)
                    || endActivity.getClass().isInstance(MainActivity.class)
                    || endActivity.getClass().isInstance(MediaServerActivity.class)
                    || endActivity.getClass().isInstance(MenuActivity.class)
                    || endActivity.getClass().isInstance(MovieActivity.class)
                    || endActivity.getClass().isInstance(ScheduleActivity.class)
                    || endActivity.getClass().isInstance(SecurityActivity.class)
                    || endActivity.getClass().isInstance(SettingsActivity.class)
                    || endActivity.getClass().isInstance(ShoppingListActivity.class)
                    || endActivity.getClass().isInstance(TimerEditActivity.class)
                    || endActivity.getClass().isInstance(WirelessSocketActivity.class)
                    || endActivity.getClass().isInstance(WirelessSwitchActivity.class)) {
                return NavigationResult.POSSIBLE;
            }

            return NavigationResult.PERMITTED;

        } else if (startActivityContext.getClass() == TimerEditActivity.class) {
            if (endActivity.getClass().isInstance(TimerActivity.class)) {
                return NavigationResult.POSSIBLE;
            }

            return NavigationResult.PERMITTED;

        } else if (startActivityContext.getClass() == WirelessSocketActivity.class) {
            if (endActivity.getClass().isInstance(BirthdayActivity.class)
                    || endActivity.getClass().isInstance(CoinActivity.class)
                    || endActivity.getClass().isInstance(ForecastWeatherActivity.class)
                    || endActivity.getClass().isInstance(LoginActivity.class)
                    || endActivity.getClass().isInstance(MainActivity.class)
                    || endActivity.getClass().isInstance(MediaServerActivity.class)
                    || endActivity.getClass().isInstance(MenuActivity.class)
                    || endActivity.getClass().isInstance(MovieActivity.class)
                    || endActivity.getClass().isInstance(ScheduleActivity.class)
                    || endActivity.getClass().isInstance(SecurityActivity.class)
                    || endActivity.getClass().isInstance(SettingsActivity.class)
                    || endActivity.getClass().isInstance(ShoppingListActivity.class)
                    || endActivity.getClass().isInstance(TimerActivity.class)
                    || endActivity.getClass().isInstance(WirelessSocketEditActivity.class)
                    || endActivity.getClass().isInstance(WirelessSwitchActivity.class)) {
                return NavigationResult.POSSIBLE;
            }

            return NavigationResult.PERMITTED;

        } else if (startActivityContext.getClass() == WirelessSocketEditActivity.class) {
            if (endActivity.getClass().isInstance(WirelessSocketActivity.class)) {
                return NavigationResult.POSSIBLE;
            }

            return NavigationResult.PERMITTED;

        } else if (startActivityContext.getClass() == WirelessSwitchActivity.class) {
            if (endActivity.getClass().isInstance(BirthdayActivity.class)
                    || endActivity.getClass().isInstance(CoinActivity.class)
                    || endActivity.getClass().isInstance(ForecastWeatherActivity.class)
                    || endActivity.getClass().isInstance(LoginActivity.class)
                    || endActivity.getClass().isInstance(MainActivity.class)
                    || endActivity.getClass().isInstance(MediaServerActivity.class)
                    || endActivity.getClass().isInstance(MenuActivity.class)
                    || endActivity.getClass().isInstance(MovieActivity.class)
                    || endActivity.getClass().isInstance(ScheduleActivity.class)
                    || endActivity.getClass().isInstance(SecurityActivity.class)
                    || endActivity.getClass().isInstance(SettingsActivity.class)
                    || endActivity.getClass().isInstance(ShoppingListActivity.class)
                    || endActivity.getClass().isInstance(TimerActivity.class)
                    || endActivity.getClass().isInstance(WirelessSocketActivity.class)
                    || endActivity.getClass().isInstance(WirelessSwitchEditActivity.class)) {
                return NavigationResult.POSSIBLE;
            }

            return NavigationResult.PERMITTED;

        } else if (startActivityContext.getClass() == WirelessSwitchEditActivity.class) {
            if (endActivity.getClass().isInstance(WirelessSwitchActivity.class)) {
                return NavigationResult.POSSIBLE;
            }

            return NavigationResult.PERMITTED;

        }

        return NavigationResult.NULL;
    }
}
