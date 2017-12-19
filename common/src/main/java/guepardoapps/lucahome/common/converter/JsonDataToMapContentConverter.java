package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.classes.SerializableTime;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.StringHelper;
import guepardoapps.lucahome.common.classes.*;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;

public final class JsonDataToMapContentConverter {
    private static final String TAG = JsonDataToMapContentConverter.class.getSimpleName();
    private static Logger _logger;

    private static String _searchParameter = "{\"Data\":";

    public JsonDataToMapContentConverter() {
        _logger = new Logger(TAG);
    }

    public SerializableList<MapContent> GetList(
            @NonNull String[] stringArray,
            @NonNull SerializableList<ListedMenu> listedMenuList, @NonNull SerializableList<LucaMenu> menuList, @NonNull SerializableList<ShoppingEntry> shoppingList,
            @NonNull SerializableList<MediaServerData> mediaServerList, @NonNull Security security, @NonNull SerializableList<Temperature> temperatureList,
            @NonNull SerializableList<WirelessSocket> wirelessSocketList, @NonNull SerializableList<WirelessSwitch> wirelessSwitchList) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(
                    stringArray[0],
                    listedMenuList, menuList, shoppingList,
                    mediaServerList, security, temperatureList,
                    wirelessSocketList, wirelessSwitchList);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
            return parseStringToList(
                    usedEntry,
                    listedMenuList, menuList, shoppingList,
                    mediaServerList, security, temperatureList,
                    wirelessSocketList, wirelessSwitchList);
        }
    }

    public SerializableList<MapContent> GetList(
            @NonNull String jsonString,
            @NonNull SerializableList<ListedMenu> listedMenuList, @NonNull SerializableList<LucaMenu> menuList, @NonNull SerializableList<ShoppingEntry> shoppingList,
            @NonNull SerializableList<MediaServerData> mediaServerList, @NonNull Security security, @NonNull SerializableList<Temperature> temperatureList,
            @NonNull SerializableList<WirelessSocket> wirelessSocketList, @NonNull SerializableList<WirelessSwitch> wirelessSwitchList) {
        return parseStringToList(
                jsonString,
                listedMenuList, menuList, shoppingList,
                mediaServerList, security, temperatureList,
                wirelessSocketList, wirelessSwitchList);
    }

    private SerializableList<MapContent> parseStringToList(
            @NonNull String value,
            @NonNull SerializableList<ListedMenu> listedMenuList, @NonNull SerializableList<LucaMenu> menuList, @NonNull SerializableList<ShoppingEntry> shoppingList,
            @NonNull SerializableList<MediaServerData> mediaServerList, @NonNull Security security, @NonNull SerializableList<Temperature> temperatureList,
            @NonNull SerializableList<WirelessSocket> wirelessSocketList, @NonNull SerializableList<WirelessSwitch> wirelessSwitchList) {
        try {
            if (!value.contains("Error")) {
                SerializableList<MapContent> list = new SerializableList<>();

                JSONObject jsonObject = new JSONObject(value);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int dataIndex = 0; dataIndex < dataArray.length(); dataIndex++) {
                    JSONObject child = dataArray.getJSONObject(dataIndex).getJSONObject("MapContent");

                    int id = child.getInt("ID");

                    String typeString = child.getString("Type");
                    MapContent.DrawingType drawingType = getDrawingType(typeString);
                    int typeId = child.getInt("TypeId");

                    String name = child.getString("Name");
                    String shortName = child.getString("ShortName");
                    String area = child.getString("Area");

                    boolean visibility = child.getString("Visibility").contains("1");

                    JSONObject positionJson = child.getJSONObject("Position");
                    JSONObject pointJson = positionJson.getJSONObject("Point");

                    int positionX = pointJson.getInt("X");
                    int positionY = pointJson.getInt("Y");

                    int[] position = new int[]{positionX, positionY};

                    SerializableList<ListedMenu> _listedMenuList = ((name.equals("ListedMenu") && drawingType == MapContent.DrawingType.Menu) ? listedMenuList : null);
                    SerializableList<LucaMenu> _menuList = ((name.equals("Menu") && drawingType == MapContent.DrawingType.Menu) ? menuList : null);
                    SerializableList<ShoppingEntry> _shoppingList = ((name.equals("ShoppingList") && drawingType == MapContent.DrawingType.ShoppingList) ? shoppingList : null);

                    MediaServerData _mediaServer = drawingType == MapContent.DrawingType.MediaServer ? (mediaServerList.getSize() > 0 ? mediaServerList.getValue(typeId - 1) : null) : null;
                    Security _security = drawingType == MapContent.DrawingType.Camera ? security : null;

                    // TODO Fix temperature selection
                    // Temperature _temperature = drawingType == MapContent.DrawingType.Temperature ? (temperatureList.getSize() > 0 ? temperatureList.getValue(typeId - 1) : null) : null;
                    Temperature _temperature = new Temperature(-273.15, "SPACE", new SerializableDate(), new SerializableTime(), "", Temperature.TemperatureType.DUMMY, "");
                    WirelessSocket _wirelessSocket = drawingType == MapContent.DrawingType.Socket ? (wirelessSocketList.getSize() > 0 ? wirelessSocketList.getValue(typeId - 1) : null) : null;
                    WirelessSwitch _wirelessSwitch = drawingType == MapContent.DrawingType.LightSwitch ? (wirelessSwitchList.getSize() > 0 ? wirelessSwitchList.getValue(typeId - 1) : null) : null;

                    MapContent newMapContent = new MapContent(id, drawingType, typeId, position, name, shortName, area, visibility,
                            _listedMenuList, _menuList, _shoppingList, _mediaServer, _security, _temperature, _wirelessSocket, _wirelessSwitch,
                            true, ILucaClass.LucaServerDbAction.Null);

                    list.addValue(newMapContent);
                }

                return list;
            }
        } catch (JSONException jsonException) {
            _logger.Error(jsonException.getMessage());
        }

        _logger.Error(value + " has an error!");

        return new SerializableList<>();
    }

    private MapContent.DrawingType getDrawingType(@NonNull String typeString) {
        switch (typeString) {
            case "WirelessSocket":
                return MapContent.DrawingType.Socket;
            case "LAN":
                return MapContent.DrawingType.LAN;
            case "MediaServer":
                return MapContent.DrawingType.MediaServer;
            case "RaspberryPi":
                return MapContent.DrawingType.RaspberryPi;
            case "NAS":
                return MapContent.DrawingType.NAS;
            case "LightSwitch":
                return MapContent.DrawingType.LightSwitch;
            case "Temperature":
                return MapContent.DrawingType.Temperature;
            case "PuckJS":
                return MapContent.DrawingType.PuckJS;
            case "Menu":
                return MapContent.DrawingType.Menu;
            case "ShoppingList":
                return MapContent.DrawingType.ShoppingList;
            case "Camera":
                return MapContent.DrawingType.Camera;
            default:
                return MapContent.DrawingType.Null;
        }
    }
}