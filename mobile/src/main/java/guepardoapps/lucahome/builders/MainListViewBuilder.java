package guepardoapps.lucahome.builders;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import java.util.Locale;

import de.mateware.snacky.Snacky;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.classes.SerializableTime;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.classes.MainListViewItem;
import guepardoapps.lucahome.common.dto.BirthdayDto;
import guepardoapps.lucahome.common.dto.CoinDto;
import guepardoapps.lucahome.common.dto.ScheduleDto;
import guepardoapps.lucahome.common.dto.ShoppingEntryDto;
import guepardoapps.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.lucahome.common.enums.ShoppingEntryGroup;
import guepardoapps.lucahome.common.enums.SocketAction;
import guepardoapps.lucahome.common.service.BirthdayService;
import guepardoapps.lucahome.common.service.CoinService;
import guepardoapps.lucahome.common.service.ScheduleService;
import guepardoapps.lucahome.common.service.ShoppingListService;
import guepardoapps.lucahome.common.service.WirelessSocketService;
import guepardoapps.lucahome.service.NavigationService;
import guepardoapps.lucahome.views.*;

public class MainListViewBuilder {
    private static final String TAG = MainListViewBuilder.class.getSimpleName();
    private Logger _logger;

    private NavigationService _navigationService;

    private Context _context;
    private SerializableList<MainListViewItem> _mainListViewItems = new SerializableList<>();

    public MainListViewBuilder(@NonNull Context context) {
        _logger = new Logger(TAG);

        _navigationService = NavigationService.getInstance();

        _context = context;
        createDefaultList();
    }

    public SerializableList<MainListViewItem> GetList() {
        _logger.Debug("GetList");
        return _mainListViewItems;
    }

    public void UpdateItemDescription(@NonNull MainListViewItem.Type type, @NonNull String description) {
        for (int index = 0; index < _mainListViewItems.getSize(); index++) {
            MainListViewItem mainListViewItem = _mainListViewItems.getValue(index);
            if (mainListViewItem.GetType() == type) {
                mainListViewItem.SetDescription(description);
                break;
            }
        }
    }

    public void UpdateItemImageResource(@NonNull MainListViewItem.Type type, int imageResource) {
        for (int index = 0; index < _mainListViewItems.getSize(); index++) {
            MainListViewItem mainListViewItem = _mainListViewItems.getValue(index);
            if (mainListViewItem.GetType() == type) {
                mainListViewItem.SetImageResource(imageResource);
                break;
            }
        }
    }

    private void createDefaultList() {
        _mainListViewItems.clear();

        MainListViewItem birthdayItem = new MainListViewItem(
                "Birthdays", "Who's next for birthday", R.drawable.main_image_birthday,
                new Runnable() {
                    @Override
                    public void run() {
                        navigateTo(BirthdayActivity.class);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        Bundle data = new Bundle();
                        data.putSerializable(BirthdayService.BirthdayIntent, new BirthdayDto(-1, "", new SerializableDate(), BirthdayDto.Action.Add));
                        navigateWithDataTo(BirthdayEditActivity.class, data);
                    }
                },
                MainListViewItem.Type.Birthday
        );

        MainListViewItem coinItem = new MainListViewItem(
                "Coins", "Watch your coins", R.drawable.main_image_coins,
                new Runnable() {
                    @Override
                    public void run() {
                        navigateTo(CoinActivity.class);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        Bundle data = new Bundle();
                        data.putSerializable(CoinService.CoinIntent, new CoinDto(-1, "", "", -1, CoinDto.Action.Add));
                        navigateWithDataTo(CoinEditActivity.class, data);
                    }
                },
                MainListViewItem.Type.Coin
        );

        MainListViewItem mediaMirrorItem = new MainListViewItem(
                "MediaMirror", "Control your local media mirror", R.drawable.main_image_mediamirror,
                new Runnable() {
                    @Override
                    public void run() {
                        navigateTo(MediaMirrorActivity.class);
                    }
                },
                MainListViewItem.Type.MediaMirror
        );

        MainListViewItem menuItem = new MainListViewItem(
                "Menu", "Plan your meals for the week", R.drawable.main_image_menu,
                new Runnable() {
                    @Override
                    public void run() {
                        navigateTo(MenuActivity.class);
                    }
                },
                MainListViewItem.Type.Menu
        );

        MainListViewItem movieItem = new MainListViewItem(
                "Movies", "Want to watch some blockbuster?", R.drawable.main_image_movies,
                new Runnable() {
                    @Override
                    public void run() {
                        navigateTo(MovieActivity.class);
                    }
                },
                MainListViewItem.Type.Movie
        );

        MainListViewItem scheduleItem = new MainListViewItem(
                "Schedules", "Manage sockets using schedules", R.drawable.main_image_schedule,
                new Runnable() {
                    @Override
                    public void run() {
                        navigateTo(ScheduleActivity.class);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        Bundle data = new Bundle();
                        data.putSerializable(ScheduleService.ScheduleIntent, new ScheduleDto(-1, "", null, null, new SerializableTime(), SocketAction.Activate, ScheduleDto.Action.Add));
                        navigateWithDataTo(ScheduleEditActivity.class, data);
                    }
                },
                MainListViewItem.Type.Schedule
        );

        MainListViewItem securityItem = new MainListViewItem(
                "Security", "Secure your living room", R.drawable.main_image_security,
                new Runnable() {
                    @Override
                    public void run() {
                        navigateTo(SecurityActivity.class);
                    }
                },
                MainListViewItem.Type.Security
        );

        MainListViewItem shoppingItem = new MainListViewItem(
                "Shopping List", "What do we need to buy?", R.drawable.main_image_shopping,
                new Runnable() {
                    @Override
                    public void run() {
                        navigateTo(ShoppingListActivity.class);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        Bundle data = new Bundle();
                        data.putSerializable(ShoppingListService.ShoppingIntent, new ShoppingEntryDto(-1, "", ShoppingEntryGroup.OTHER, 1));
                        navigateWithDataTo(ShoppingListEditActivity.class, data);
                    }
                },
                MainListViewItem.Type.Shopping
        );

        MainListViewItem timerItem = new MainListViewItem(
                "Timer", "Manage sockets using timer", R.drawable.main_image_timer,
                new Runnable() {
                    @Override
                    public void run() {
                        navigateTo(TimerActivity.class);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        navigateTo(TimerEditActivity.class);
                    }
                },
                MainListViewItem.Type.Timer
        );

        MainListViewItem weatherItem = new MainListViewItem(
                "Weather", "Get your weather for the next days", R.drawable.weather_wallpaper_dummy,
                new Runnable() {
                    @Override
                    public void run() {
                        navigateTo(ForecastWeatherActivity.class);
                    }
                },
                MainListViewItem.Type.Weather
        );

        MainListViewItem wirelessSocketItem = new MainListViewItem(
                "Sockets", "Control your sockets", R.drawable.main_image_sockets,
                new Runnable() {
                    @Override
                    public void run() {
                        navigateTo(WirelessSocketActivity.class);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        Bundle data = new Bundle();
                        data.putSerializable(WirelessSocketService.WirelessSocketIntent, new WirelessSocketDto(-1, "", "", "", false, WirelessSocketDto.Action.Add));
                        navigateWithDataTo(WirelessSocketEditActivity.class, data);
                    }
                },
                true, MainListViewItem.Type.WirelessSocket
        );

        _mainListViewItems.addValue(wirelessSocketItem);
        _mainListViewItems.addValue(scheduleItem);
        _mainListViewItems.addValue(timerItem);
        _mainListViewItems.addValue(weatherItem);
        _mainListViewItems.addValue(coinItem);
        _mainListViewItems.addValue(menuItem);
        _mainListViewItems.addValue(shoppingItem);
        _mainListViewItems.addValue(birthdayItem);
        _mainListViewItems.addValue(movieItem);
        _mainListViewItems.addValue(mediaMirrorItem);
        _mainListViewItems.addValue(securityItem);
    }

    private void navigateTo(@NonNull Class<?> targetActivity) {
        _logger.Debug(String.format(Locale.getDefault(), "Navigating to activity %s.", targetActivity.getClass().getSimpleName()));

        NavigationService.NavigationResult navigationResult = _navigationService.NavigateToActivity(_context, targetActivity);

        if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
            _logger.Error(String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s", navigationResult));
            Snacky.builder()
                    .setActivty((AppCompatActivity) _context)
                    .setText(String.format(Locale.getDefault(), "Failed to navigate to %s! Please contact LucaHome support!", targetActivity.getClass().getSimpleName()))
                    .setDuration(Snacky.LENGTH_INDEFINITE)
                    .setActionText(android.R.string.ok)
                    .error()
                    .show();
        }
    }

    private void navigateWithDataTo(@NonNull Class<?> targetActivity, @NonNull Bundle data) {
        _logger.Debug(String.format(Locale.getDefault(), "Navigating to activity %s with data %s", targetActivity.getClass().getSimpleName(), data));

        NavigationService.NavigationResult navigationResult = _navigationService.NavigateToActivityWithData(_context, targetActivity, data);

        if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
            _logger.Error(String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s", navigationResult));
            Snacky.builder()
                    .setActivty((AppCompatActivity) _context)
                    .setText(String.format(Locale.getDefault(), "Failed to navigate to %s! Please contact LucaHome support!", targetActivity.getClass().getSimpleName()))
                    .setDuration(Snacky.LENGTH_INDEFINITE)
                    .setActionText(android.R.string.ok)
                    .error()
                    .show();
        }
    }
}
