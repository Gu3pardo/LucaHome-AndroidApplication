package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;

@SuppressWarnings({"unused"})
public class PuckJs implements Serializable, ILucaClass {
    private static final long serialVersionUID = 3784902949363954860L;
    private static final String TAG = PuckJs.class.getSimpleName();

    private int _id;

    private String _name;
    private String _area;
    private String _mac;

    private boolean _isOnServer;
    private LucaServerDbAction _serverDbAction;

    public PuckJs(
            int id,
            @NonNull String name,
            @NonNull String area,
            @NonNull String mac,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        _id = id;

        _name = name;
        _area = area;
        _mac = mac;

        _isOnServer = isOnServer;
        _serverDbAction = serverDbAction;
    }

    @Override
    public int GetId() {
        return _id;
    }

    public String GetName() {
        return _name;
    }

    public void SetName(@NonNull String name) {
        _name = name;
    }

    public String GetArea() {
        return _area;
    }

    public void SetArea(@NonNull String area) {
        _area = area;
    }

    public String GetMac() {
        return _mac;
    }

    public void SetMac(@NonNull String mac) {
        _mac = mac;
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
    public String CommandAdd() {
        return String.format(Locale.getDefault(), LucaServerAction.ADD_PUCKJS_F.toString(), _id, _name, _area, _mac);
    }

    @Override
    public String CommandUpdate() {
        return String.format(Locale.getDefault(), LucaServerAction.UPDATE_PUCKJS_F.toString(), _id, _name, _area, _mac);
    }

    @Override
    public String CommandDelete() {
        return String.format(Locale.getDefault(), LucaServerAction.DELETE_PUCKJS_F.toString(), _id);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "{%s: {Id: %d};{Name: %s};{Area: %s};{Mac: %s}}", TAG, _id, _name, _area, _mac);
    }
}
