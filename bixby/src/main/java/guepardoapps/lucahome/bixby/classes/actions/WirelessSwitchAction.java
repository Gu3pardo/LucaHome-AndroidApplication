package guepardoapps.lucahome.bixby.classes.actions;

import android.support.annotation.NonNull;

import java.util.Locale;

@SuppressWarnings("WeakerAccess")
public class WirelessSwitchAction implements IBixbyAction {
    private static final String Tag = WirelessSwitchAction.class.getSimpleName();

    private String _wirelessSwitchName;

    public WirelessSwitchAction(@NonNull String wirelessSwitchName) {
        _wirelessSwitchName = wirelessSwitchName;
    }

    public WirelessSwitchAction() {
        this("");
    }

    public String GetWirelessSwitchName() {
        return _wirelessSwitchName;
    }

    @Override
    public String GetDatabaseString() {
        return String.format(Locale.getDefault(), "%s", _wirelessSwitchName);
    }

    @Override
    public String GetInformationString() {
        return String.format(Locale.getDefault(), "%s \nfor %s -> Toggle", Tag, _wirelessSwitchName);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"WirelessSocketName\":\"%s\"}",
                Tag, _wirelessSwitchName);
    }
}
