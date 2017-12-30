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
import guepardoapps.lucahome.common.classes.MeterData;
import guepardoapps.lucahome.common.classes.MoneyMeterData;
import guepardoapps.lucahome.common.dto.BirthdayDto;
import guepardoapps.lucahome.common.dto.CoinDto;
import guepardoapps.lucahome.common.dto.ScheduleDto;
import guepardoapps.lucahome.common.dto.ShoppingEntryDto;
import guepardoapps.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.lucahome.common.dto.WirelessSwitchDto;
import guepardoapps.lucahome.common.enums.ShoppingEntryGroup;
import guepardoapps.lucahome.common.enums.SocketAction;
import guepardoapps.lucahome.common.enums.Weekday;
import guepardoapps.lucahome.common.service.BirthdayService;
import guepardoapps.lucahome.common.service.CoinService;
import guepardoapps.lucahome.common.service.MeterListService;
import guepardoapps.lucahome.common.service.MoneyMeterListService;
import guepardoapps.lucahome.common.service.ScheduleService;
import guepardoapps.lucahome.common.service.ShoppingListService;
import guepardoapps.lucahome.common.service.UserService;
import guepardoapps.lucahome.common.service.WirelessSocketService;
import guepardoapps.lucahome.common.service.WirelessSwitchService;
import guepardoapps.lucahome.service.NavigationService;
import guepardoapps.lucahome.views.*;

public class MainListViewBuilder {
    private static final String TAG = MainListViewBuilder.class.getSimpleName();

    private Context _context;
    private SerializableList<MainListViewItem> _mainListViewItems = new SerializableList<>();

    public MainListViewBuilder(@NonNull Context context) {
        _context = context;
        createDefaultList();
    }

    public SerializableList<MainListViewItem> GetList() {
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
                () -> navigateTo(BirthdayActivity.class),
                () -> {
                    Bundle data = new Bundle();
                    data.putSerializable(BirthdayService.BirthdayIntent, new BirthdayDto(BirthdayService.getInstance().GetDataList().getSize(), "", new SerializableDate(), "", true, BirthdayDto.Action.Add));
                    navigateWithDataTo(BirthdayEditActivity.class, data);
                },
                MainListViewItem.Type.Birthday
        );

        MainListViewItem coinItem = new MainListViewItem(
                "Coins", "Watch your coins", R.drawable.main_image_coins,
                () -> navigateTo(CoinActivity.class),
                () -> {
                    Bundle data = new Bundle();
                    data.putSerializable(CoinService.CoinIntent, new CoinDto(CoinService.getInstance().GetDataList().getSize(), "", "", -1, CoinDto.Action.Add));
                    navigateWithDataTo(CoinEditActivity.class, data);
                },
                MainListViewItem.Type.Coin
        );

        MainListViewItem mediaMirrorItem = new MainListViewItem(
                "MediaMirror", "Control your local media mirror", R.drawable.main_image_mediamirror,
                () -> navigateTo(MediaServerActivity.class),
                MainListViewItem.Type.MediaServer
        );

        MainListViewItem menuItem = new MainListViewItem(
                "Menu", "Plan your meals for the week", R.drawable.main_image_menu,
                () -> navigateTo(MenuActivity.class),
                MainListViewItem.Type.Menu
        );

        MainListViewItem meterDataItem = new MainListViewItem(
                "Meter data", "Log your current and water consumption", R.drawable.main_image_meter,
                () -> navigateTo(MeterDataActivity.class),
                () -> {
                    int newHighestId = MeterListService.getInstance().GetHighestId() + 1;
                    int newHighestTypeId = MeterListService.getInstance().GetHighestTypeId("") + 1;
                    MeterData newMeterData = new MeterData(newHighestId, "", newHighestTypeId, new SerializableDate(), new SerializableTime(), "", "", 0, "");
                    Bundle data = new Bundle();
                    data.putSerializable(MeterListService.MeterDataIntent, newMeterData);
                    navigateWithDataTo(MeterDataEditActivity.class, data);
                },
                MainListViewItem.Type.Meter
        );

        MainListViewItem moneyMeterDataItem = new MainListViewItem(
                "Money meter data", "Log your money", R.drawable.main_image_money_meter,
                () -> navigateTo(MoneyMeterDataActivity.class),
                () -> {
                    int newHighestId = MoneyMeterListService.getInstance().GetHighestId() + 1;
                    int newHighestTypeId = MoneyMeterListService.getInstance().GetHighestTypeId("", "") + 1;
                    MoneyMeterData newMoneyMeterData = new MoneyMeterData(newHighestId, newHighestTypeId, "", "", 0, "", new SerializableDate(), UserService.getInstance().GetUser().GetName());
                    Bundle data = new Bundle();
                    data.putSerializable(MoneyMeterListService.MoneyMeterDataIntent, newMoneyMeterData);
                    navigateWithDataTo(MoneyMeterDataEditActivity.class, data);
                },
                MainListViewItem.Type.MoneyMeter
        );

        MainListViewItem movieItem = new MainListViewItem(
                "Movies", "Want to watch some blockbuster?", R.drawable.main_image_movies,
                () -> navigateTo(MovieActivity.class),
                MainListViewItem.Type.Movie
        );

        MainListViewItem scheduleItem = new MainListViewItem(
                "Schedules", "Manage sockets using schedules", R.drawable.main_image_schedule,
                () -> navigateTo(ScheduleActivity.class),
                () -> {
                    Bundle data = new Bundle();
                    data.putSerializable(ScheduleService.ScheduleIntent, new ScheduleDto(ScheduleService.getInstance().GetDataList().getSize(), "", null, null, Weekday.NULL, new SerializableTime(), SocketAction.Activate, ScheduleDto.Action.Add));
                    navigateWithDataTo(ScheduleEditActivity.class, data);
                },
                MainListViewItem.Type.Schedule
        );

        MainListViewItem securityItem = new MainListViewItem(
                "Security", "Secure your living room", R.drawable.main_image_security,
                () -> navigateTo(SecurityActivity.class),
                MainListViewItem.Type.Security
        );

        MainListViewItem shoppingItem = new MainListViewItem(
                "Shopping List", "What do we need to buy?", R.drawable.main_image_shopping,
                () -> navigateTo(ShoppingListActivity.class),
                () -> {
                    Bundle data = new Bundle();
                    data.putSerializable(ShoppingListService.ShoppingIntent, new ShoppingEntryDto(ShoppingListService.getInstance().GetDataList().getSize(), "", ShoppingEntryGroup.OTHER, 1, ""));
                    navigateWithDataTo(ShoppingListEditActivity.class, data);
                },
                MainListViewItem.Type.Shopping
        );

        MainListViewItem timerItem = new MainListViewItem(
                "Timer", "Manage sockets using timer", R.drawable.main_image_timer,
                () -> navigateTo(TimerActivity.class),
                () -> navigateTo(TimerEditActivity.class),
                MainListViewItem.Type.Timer
        );

        MainListViewItem weatherItem = new MainListViewItem(
                "Weather", "Get your weather for the next days", R.drawable.weather_wallpaper_dummy,
                () -> navigateTo(ForecastWeatherActivity.class),
                MainListViewItem.Type.Weather
        );

        MainListViewItem wirelessSocketItem = new MainListViewItem(
                "Sockets", "Control your sockets", R.drawable.main_image_sockets,
                () -> navigateTo(WirelessSocketActivity.class),
                () -> {
                    Bundle data = new Bundle();
                    data.putSerializable(WirelessSocketService.WirelessSocketIntent, new WirelessSocketDto(WirelessSocketService.getInstance().GetDataList().getSize(), "", "", "", false, WirelessSocketDto.Action.Add));
                    navigateWithDataTo(WirelessSocketEditActivity.class, data);
                },
                true, MainListViewItem.Type.WirelessSocket
        );

        MainListViewItem wirelessSwitchItem = new MainListViewItem(
                "Switches", "Control your switches", R.drawable.main_image_switches,
                () -> navigateTo(WirelessSwitchActivity.class),
                () -> {
                    Bundle data = new Bundle();
                    data.putSerializable(WirelessSwitchService.WirelessSwitchIntent, new WirelessSwitchDto(WirelessSwitchService.getInstance().GetDataList().getSize(), "", "", -1, '1', WirelessSwitchDto.Action.Add));
                    navigateWithDataTo(WirelessSwitchEditActivity.class, data);
                },
                true, MainListViewItem.Type.WirelessSwitch
        );

        _mainListViewItems.addValue(wirelessSocketItem);
        _mainListViewItems.addValue(wirelessSwitchItem);
        _mainListViewItems.addValue(weatherItem);
        _mainListViewItems.addValue(coinItem);
        _mainListViewItems.addValue(moneyMeterDataItem);
        _mainListViewItems.addValue(shoppingItem);
        _mainListViewItems.addValue(menuItem);
        _mainListViewItems.addValue(mediaMirrorItem);
        _mainListViewItems.addValue(movieItem);
        _mainListViewItems.addValue(birthdayItem);
        _mainListViewItems.addValue(meterDataItem);
        _mainListViewItems.addValue(securityItem);
        _mainListViewItems.addValue(scheduleItem);
        _mainListViewItems.addValue(timerItem);
    }

    private void navigateTo(@NonNull Class<?> targetActivity) {
        NavigationService.NavigationResult navigationResult = NavigationService.getInstance().NavigateToActivity(_context, targetActivity);

        if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
            Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s", navigationResult));
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
        NavigationService.NavigationResult navigationResult = NavigationService.getInstance().NavigateToActivityWithData(_context, targetActivity, data);

        if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
            Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s", navigationResult));
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
