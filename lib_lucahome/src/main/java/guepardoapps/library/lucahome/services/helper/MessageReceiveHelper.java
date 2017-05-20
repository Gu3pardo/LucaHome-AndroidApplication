package guepardoapps.library.lucahome.services.helper;

import android.content.Context;

import java.util.ArrayList;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.dto.BirthdayDto;
import guepardoapps.library.lucahome.common.dto.MediaMirrorViewDto;
import guepardoapps.library.lucahome.common.dto.MenuDto;
import guepardoapps.library.lucahome.common.dto.ScheduleDto;
import guepardoapps.library.lucahome.common.dto.ShoppingEntryDto;
import guepardoapps.library.lucahome.common.dto.TemperatureDto;
import guepardoapps.library.lucahome.common.dto.TimerDto;
import guepardoapps.library.lucahome.common.dto.WeatherModelDto;
import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.library.lucahome.converter.wear.MessageToBirthdayConverter;
import guepardoapps.library.lucahome.converter.wear.MessageToMediaMirrorConverter;
import guepardoapps.library.lucahome.converter.wear.MessageToMenuConverter;
import guepardoapps.library.lucahome.converter.wear.MessageToRaspberryConverter;
import guepardoapps.library.lucahome.converter.wear.MessageToScheduleConverter;
import guepardoapps.library.lucahome.converter.wear.MessageToShoppingListConverter;
import guepardoapps.library.lucahome.converter.wear.MessageToSocketConverter;
import guepardoapps.library.lucahome.converter.wear.MessageToTimerConverter;
import guepardoapps.library.lucahome.converter.wear.MessageToWeatherConverter;

import guepardoapps.library.toolset.common.Logger;
import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.controller.BroadcastController;

public class MessageReceiveHelper {

    private static final String CURRENT_WEATHER = "CurrentWeather:";
    private static final String PHONE_BATTERY = "PhoneBattery:";
    private static final String RASPBERRY_TEMPERATURE = "RaspberryTemperature:";

    private static final String BIRTHDAYS = "Birthdays:";
    private static final String MEDIA_MIRROR = "MediaMirror:";
    private static final String MENU = "Menu:";
    private static final String SCHEDULES = "Schedules:";
    private static final String SOCKETS = "Sockets:";
    private static final String SHOPPING_LIST = "ShoppingList:";
    private static final String TIMER = "Timer:";

    private static final String WIFI = "Wifi:";

    private static final String TAG = MessageReceiveHelper.class.getSimpleName();
    private Logger _logger;

    private BroadcastController _broadcastController;

    private MessageToBirthdayConverter _messageToBirthdayConverter;
    private MessageToMediaMirrorConverter _messageToMediaMirrorConverter;
    private MessageToMenuConverter _messageToMenuConverter;
    private MessageToRaspberryConverter _messageToRaspberryConverter;
    private MessageToScheduleConverter _messageToScheduleConverter;
    private MessageToShoppingListConverter _messageToShoppingListConverter;
    private MessageToSocketConverter _messageToSocketConverter;
    private MessageToTimerConverter _messageToTimerConverter;
    private MessageToWeatherConverter _messageToWeatherConverter;

    public MessageReceiveHelper(Context context) {
        _logger = new Logger(TAG);

        _broadcastController = new BroadcastController(context);

        _messageToBirthdayConverter = new MessageToBirthdayConverter();
        _messageToMediaMirrorConverter = new MessageToMediaMirrorConverter();
        _messageToMenuConverter = new MessageToMenuConverter();
        _messageToRaspberryConverter = new MessageToRaspberryConverter();
        _messageToScheduleConverter = new MessageToScheduleConverter();
        _messageToShoppingListConverter = new MessageToShoppingListConverter();
        _messageToSocketConverter = new MessageToSocketConverter();
        _messageToTimerConverter = new MessageToTimerConverter();
        _messageToWeatherConverter = new MessageToWeatherConverter();
    }

    public void HandleMessage(String message) {
        if (message == null) {
            _logger.Warn("message is null!");
            return;
        }

        _logger.Debug("HandleMessage: " + message);
        if (message.startsWith(CURRENT_WEATHER)) {
            message = message.replace(CURRENT_WEATHER, "");
            WeatherModelDto currentWeather = _messageToWeatherConverter.ConvertMessageToWeatherModel(message);
            if (currentWeather != null) {
                _broadcastController.SendSerializableBroadcast(Broadcasts.UPDATE_CURRENT_WEATHER,
                        Bundles.CURRENT_WEATHER, currentWeather);
            } else {
                _logger.Warn("CurrentWeather is null!");
            }
        } else if (message.startsWith(PHONE_BATTERY)) {
            message = message.replace(PHONE_BATTERY, "");
            _broadcastController.SendStringBroadcast(Broadcasts.UPDATE_PHONE_BATTERY, Bundles.PHONE_BATTERY, message);
        } else if (message.startsWith(RASPBERRY_TEMPERATURE)) {
            message = message.replace(RASPBERRY_TEMPERATURE, "");
            TemperatureDto raspberryTemperature = _messageToRaspberryConverter.ConvertMessageToRaspberryModel(message);
            if (raspberryTemperature != null) {
                _broadcastController.SendSerializableArrayBroadcast(Broadcasts.UPDATE_RASPBERRY_TEMPERATURE,
                        new String[]{Bundles.RASPBERRY_TEMPERATURE}, new Object[]{raspberryTemperature});
            } else {
                _logger.Warn("raspberryTemperature1 or raspberryTemperature2 or both are null!");
            }
        } else if (message.startsWith(BIRTHDAYS)) {
            message = message.replace(BIRTHDAYS, "");
            SerializableList<BirthdayDto> itemList = _messageToBirthdayConverter.ConvertMessageToBirthdayList(message);
            if (itemList != null) {
                _broadcastController.SendSerializableBroadcast(Broadcasts.UPDATE_BIRTHDAY_LIST, Bundles.BIRTHDAY_LIST,
                        itemList);
            } else {
                _logger.Warn("BirthdayList is null!");
                _broadcastController.SendSerializableBroadcast(Broadcasts.UPDATE_BIRTHDAY_LIST, Bundles.BIRTHDAY_LIST,
                        new SerializableList<BirthdayDto>());
            }
        } else if (message.startsWith(SOCKETS)) {
            message = message.replace(SOCKETS, "");
            SerializableList<WirelessSocketDto> itemList = _messageToSocketConverter
                    .ConvertMessageToSocketList(message);
            if (itemList != null) {
                _broadcastController.SendSerializableBroadcast(Broadcasts.UPDATE_SOCKET_LIST, Bundles.SOCKET_LIST,
                        itemList);
            } else {
                _logger.Warn("SocketList is null!");
                _broadcastController.SendSerializableBroadcast(Broadcasts.UPDATE_SOCKET_LIST, Bundles.SOCKET_LIST,
                        new SerializableList<WirelessSocketDto>());
            }
        } else if (message.startsWith(SCHEDULES)) {
            message = message.replace(SCHEDULES, "");
            SerializableList<ScheduleDto> itemList = _messageToScheduleConverter.ConvertMessageToScheduleList(message);
            if (itemList != null) {
                _broadcastController.SendSerializableBroadcast(Broadcasts.UPDATE_SCHEDULE_LIST, Bundles.SCHEDULE_LIST,
                        itemList);
            } else {
                _logger.Warn("ScheduleList is null!");
                _broadcastController.SendSerializableBroadcast(Broadcasts.UPDATE_SCHEDULE_LIST, Bundles.SCHEDULE_LIST,
                        new SerializableList<ScheduleDto>());
            }
        } else if (message.startsWith(SHOPPING_LIST)) {
            message = message.replace(SHOPPING_LIST, "");
            SerializableList<ShoppingEntryDto> itemList = _messageToShoppingListConverter
                    .ConvertMessageToShoppingList(message);
            if (itemList != null) {
                _broadcastController.SendSerializableBroadcast(Broadcasts.UPDATE_SHOPPING_LIST, Bundles.SHOPPING_LIST,
                        itemList);
            } else {
                _logger.Warn("ShoppingList is null!");
                _broadcastController.SendSerializableBroadcast(Broadcasts.UPDATE_SHOPPING_LIST, Bundles.SHOPPING_LIST,
                        new SerializableList<ShoppingEntryDto>());
            }
        } else if (message.startsWith(TIMER)) {
            message = message.replace(TIMER, "");
            SerializableList<TimerDto> itemList = _messageToTimerConverter.ConvertMessageToTimerList(message);
            if (itemList != null) {
                _broadcastController.SendSerializableBroadcast(Broadcasts.UPDATE_TIMER_LIST, Bundles.TIMER_LIST,
                        itemList);
            } else {
                _logger.Warn("TimerList is null!");
                _broadcastController.SendSerializableBroadcast(Broadcasts.UPDATE_TIMER_LIST, Bundles.TIMER_LIST,
                        new SerializableList<TimerDto>());
            }
        } else if (message.startsWith(MEDIA_MIRROR)) {
            message = message.replace(MEDIA_MIRROR, "");
            ArrayList<MediaMirrorViewDto> itemList = _messageToMediaMirrorConverter.ConvertMessageToList(message);
            if (itemList != null) {
                _broadcastController.SendSerializableBroadcast(Broadcasts.UPDATE_MEDIAMIRROR, Bundles.MEDIAMIRROR,
                        itemList);
            } else {
                _logger.Warn("MediaMirrorList is null!");
            }
        } else if (message.startsWith(MENU)) {
            message = message.replace(MENU, "");
            SerializableList<MenuDto> itemList = _messageToMenuConverter.ConvertMessageToList(message);
            if (itemList != null) {
                _broadcastController.SendSerializableBroadcast(Broadcasts.UPDATE_MENU, Bundles.MENU, itemList);
            } else {
                _logger.Warn("Menu is null!");
            }
        } else if (message.startsWith(WIFI)) {
            message = message.replace(WIFI, "");
            _broadcastController.SendStringBroadcast(Broadcasts.UPDATE_WIFI_STATE, Bundles.WIFI_STATE, message);
        } else {
            _logger.Warn("Cannot handle message: " + message);
        }
    }
}
