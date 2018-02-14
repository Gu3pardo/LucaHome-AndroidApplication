package guepardoapps.lucahome.common.classes;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.utils.MapContentHelper;

@SuppressWarnings({"WeakerAccess"})
public class MapContent implements ILucaClass {
    private static final String Tag = MapContent.class.getSimpleName();

    public enum DrawingType {Null, Camera, LAN, Meal, MediaServer, Meter, NAS, PuckJS, RaspberryPi, ShoppingList, SuggestedMeal, Temperature, WirelessSocket, WirelessSwitch}

    private UUID _uuid;
    private UUID _drawingTypeUuid;
    private DrawingType _drawingType;
    private int[] _position;
    private String _name;
    private String _shortName;
    private boolean _visibility;

    private boolean _isOnServer;
    private LucaServerDbAction _serverDbAction;

    public MapContent(
            @NonNull UUID uuid,
            @NonNull UUID drawingTypeUuid,
            @NonNull DrawingType drawingType,
            int[] position,
            @NonNull String name,
            @NonNull String shortName,
            boolean visibility,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        _uuid = uuid;
        _drawingTypeUuid = drawingTypeUuid;
        _drawingType = drawingType;
        _position = position;
        _name = name;
        _shortName = shortName;
        _visibility = visibility;
        _isOnServer = isOnServer;
        _serverDbAction = serverDbAction;
    }

    @Override
    public UUID GetUuid() {
        return _uuid;
    }

    @Override
    public UUID GetRoomUuid() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetRoomUuid not implemented for " + Tag);
    }

    public void SetDrawingTypeId(UUID drawingTypeUuid) {
        _drawingTypeUuid = drawingTypeUuid;
    }

    public UUID GetDrawingTypeUuid() {
        return _drawingTypeUuid;
    }

    public void SetDrawingType(DrawingType drawingType) {
        _drawingType = drawingType;
    }

    public DrawingType GetDrawingType() {
        return _drawingType;
    }

    public void SetPosition(int[] position) {
        _position = position;
    }

    public int[] GetPosition() {
        return _position;
    }

    public String GetPositionString() {
        return String.format(Locale.getDefault(), "%d|%d", _position[0], _position[1]);
    }

    public void SetName(@NonNull String name) {
        _name = name;
    }

    public String GetName() {
        return _name;
    }

    public void SetShortName(@NonNull String shortName) {
        _shortName = shortName;
    }

    public String GetShortName() {
        return _shortName;
    }

    public void SetVisible(boolean visibility) {
        _visibility = visibility;
    }

    public boolean IsVisible() {
        return _visibility;
    }

    public Runnable GetButtonClick(@NonNull Context context) {
        return MapContentHelper.GetRunnable(this, context);
    }

    public int GetTextColor() {
        return MapContentHelper.GetTextColor(this);
    }

    public Drawable GetDrawable() throws Exception {
        return MapContentHelper.GetDrawable(this);
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
    public String GetCommandAdd() throws NoSuchMethodException {
        throw new NoSuchMethodException("No method CommandAdd for MapContent");
    }

    @Override
    public String GetCommandUpdate() throws NoSuchMethodException {
        throw new NoSuchMethodException("No method CommandUpdate for MapContent");
    }

    @Override
    public String GetCommandDelete() throws NoSuchMethodException {
        throw new NoSuchMethodException("No method CommandDelete for MapContent");
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"TypeId\":\"%s\",\"Type\":\"%s\",\"Position\":\"%s\",\"Name\":\"%s\",\"ShortName\":\"%s\",\"ButtonVisibility\":\"%s\"}",
                Tag, _uuid, _drawingTypeUuid, _drawingType, GetPositionString(), _name, _shortName, _visibility);
    }
}
