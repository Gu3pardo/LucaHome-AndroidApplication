package guepardoapps.lucahome.common.classes;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.common.builder.MapContentBuilder;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;

public class MapContent implements Serializable, ILucaClass {
    private static final long serialVersionUID = 8796770534384442492L;
    private static final String TAG = MapContent.class.getSimpleName();

    public enum DrawingType {Null, Socket, LAN, MediaServer, RaspberryPi, NAS, LightSwitch, Temperature, PuckJS, Menu, ShoppingList, Camera, Meter }

    private int _id;
    private DrawingType _drawingType;
    private int _drawingTypeId;
    private int[] _position;
    private String _name;
    private String _shortName;
    private String _area;
    private boolean _visibility;

    private SerializableList<ListedMenu> _listedMenuList;
    private SerializableList<LucaMenu> _menuList;
    private SerializableList<ShoppingEntry> _shoppingList;

    private MediaServerData _mediaServer;
    private Security _security;
    private Temperature _temperature;
    private WirelessSocket _wirelessSocket;
    private WirelessSwitch _wirelessSwitch;

    private boolean _isOnServer;
    private LucaServerDbAction _serverDbAction;

    public MapContent(
            int id,
            DrawingType drawingType,
            int drawingTypeId,
            int[] position,
            @NonNull String name,
            @NonNull String shortName,
            @NonNull String area,
            boolean visibility,

            SerializableList<ListedMenu> listedMenuList,
            SerializableList<LucaMenu> menuList,
            SerializableList<ShoppingEntry> shoppingList,

            MediaServerData mediaServer,
            Security security,
            Temperature temperature,
            WirelessSocket wirelessSocket,
            WirelessSwitch wirelessSwitch,

            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        _id = id;
        _drawingType = drawingType;
        _drawingTypeId = drawingTypeId;

        _position = position;

        _name = name;
        _shortName = shortName;
        _area = area;

        _visibility = visibility;

        _listedMenuList = listedMenuList;
        _menuList = menuList;
        _shoppingList = shoppingList;

        _mediaServer = mediaServer;
        _security = security;
        _temperature = temperature;
        _wirelessSocket = wirelessSocket;
        _wirelessSwitch = wirelessSwitch;

        _isOnServer = isOnServer;
        _serverDbAction = serverDbAction;
    }

    @Override
    public int GetId() {
        return _id;
    }

    public DrawingType GetDrawingType() {
        return _drawingType;
    }

    public void SetDrawingType(DrawingType drawingType) {
        _drawingType = drawingType;
    }

    public int GetDrawingTypeId() {
        return _drawingTypeId;
    }

    public void SetDrawingTypeId(int drawingTypeId) {
        _drawingTypeId = drawingTypeId;
    }

    public int[] GetPosition() {
        return _position;
    }

    public String GetPositionString() {
        return String.format(Locale.getDefault(), "%d|%d", _position[0], _position[1]);
    }

    public void SetPosition(int[] position) {
        _position = position;
    }

    public String GetName() {
        return _name;
    }

    public void SetName(@NonNull String name) {
        _name = name;
    }

    public String GetShortName() {
        return _shortName;
    }

    public void SetShortName(@NonNull String shortName) {
        _shortName = shortName;
    }

    public String GetArea() {
        return _area;
    }

    public void SetArea(@NonNull String area) {
        _area = area;
    }

    public boolean IsVisible() {
        return _visibility;
    }

    public void SetVisible(boolean visibility) {
        _visibility = visibility;
    }

    public SerializableList<ListedMenu> GetListedMenuList() {
        return _listedMenuList;
    }

    public SerializableList<LucaMenu> GetMenuList() {
        return _menuList;
    }

    public SerializableList<ShoppingEntry> GetShoppingList() {
        return _shoppingList;
    }

    public MediaServerData GetMediaServer() {
        return _mediaServer;
    }

    public Security GetSecurity() {
        return _security;
    }

    public Temperature GetTemperature() {
        return _temperature;
    }

    public WirelessSocket GetWirelessSocket() {
        return _wirelessSocket;
    }

    public WirelessSwitch GetWirelessSwitch() {
        return _wirelessSwitch;
    }

    public Runnable GetButtonClick(@NonNull Context context) {
        return MapContentBuilder.GetRunnable(this, context);
    }

    public int GetDrawable() {
        return MapContentBuilder.GetDrawable(this);
    }

    @Override
    public void SetIsOnServer(boolean isOnServer) {
        _isOnServer = isOnServer;
    }

    @Override
    public boolean GetIsOnServer() {
        return _isOnServer;
    }

    @Override
    public void SetServerDbAction(@NonNull LucaServerDbAction serverDbAction) {
        _serverDbAction = serverDbAction;
    }

    @Override
    public LucaServerDbAction GetServerDbAction() {
        return _serverDbAction;
    }

    @Override
    public String CommandAdd() throws NoSuchMethodException {
        throw new NoSuchMethodException("No method CommandAdd for MapContent");
    }

    @Override
    public String CommandUpdate() throws NoSuchMethodException {
        throw new NoSuchMethodException("No method CommandUpdate for MapContent");
    }

    @Override
    public String CommandDelete() throws NoSuchMethodException {
        throw new NoSuchMethodException("No method CommandDelete for MapContent");
    }

    @Override
    public String toString() {
        return String.format(
                Locale.getDefault(),
                "( %s: (Id: %d );(Type: %s );(TypeId: %d );(Position: %s );(Name: %s );(ShortName: %s );(Area: %s );(ButtonVisibility: %s ))",
                TAG, _id, _drawingType, _drawingTypeId, GetPositionString(), _name, _shortName, _area, _visibility);
    }
}
