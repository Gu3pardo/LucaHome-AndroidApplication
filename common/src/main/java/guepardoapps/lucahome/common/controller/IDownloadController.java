package guepardoapps.lucahome.common.controller;

import android.support.annotation.NonNull;

import java.io.Serializable;

public interface IDownloadController {
    enum DownloadType implements Serializable {
        AccessAlarm,
        Birthday, BirthdayAdd, BirthdayUpdate, BirthdayDelete,
        Coin, CoinAdd, CoinUpdate, CoinDelete, CoinData,
        LucaSecurity, LucaSecurityCamera, LucaSecurityCameraControl,
        MapContent,
        Meal, MealUpdate, MealClear,
        MediaServerData,
        MeterLogItem, MeterLogItemAdd, MeterLogItemUpdate, MeterLogItemDelete,
        MoneyLogItem, MoneyLogItemAdd, MoneyLogItemUpdate, MoneyLogItemDelete,
        Movie, MovieUpdate,
        PuckJs, PuckJsAdd, PuckJsUpdate, PuckJsDelete,
        Room, RoomAdd, RoomUpdate, RoomDelete,
        ShoppingItem, ShoppingItemAdd, ShoppingItemUpdate, ShoppingItemDelete,
        SuggestedMeal, SuggestedMealAdd, SuggestedMealUpdate, SuggestedMealDelete,
        SuggestedShoppingItem, SuggestedShoppingItemAdd, SuggestedShoppingItemUpdate, SuggestedShoppingItemDelete,
        Temperature,
        User,
        WirelessSchedule, WirelessScheduleSet, WirelessScheduleAdd, WirelessScheduleUpdate, WirelessScheduleDelete,
        WirelessSocket, WirelessSocketSet, WirelessSocketAdd, WirelessSocketUpdate, WirelessSocketDelete,
        WirelessSwitch, WirelessSwitchToggle, WirelessSwitchAdd, WirelessSwitchUpdate, WirelessSwitchDelete,
        WirelessTimer, WirelessTimerAdd, WirelessTimerUpdate, WirelessTimerDelete
    }

    enum DownloadState implements Serializable {
        Canceled, NoNetwork, NoHomeNetwork, InvalidUrl, Success
    }

    String DownloadFinishedBroadcast = "guepardoapps.lucahome.common.controller.download.finished";
    String DownloadFinishedBundle = "DownloadFinishedBundle";

    void SendCommandToWebsiteAsync(@NonNull String requestUrl, @NonNull DownloadType downloadType, boolean needsHomeNetwork, Serializable additional);

    void SendCommandToWebsiteAsync(@NonNull String requestUrl, @NonNull DownloadType downloadType, boolean needsHomeNetwork);
}
