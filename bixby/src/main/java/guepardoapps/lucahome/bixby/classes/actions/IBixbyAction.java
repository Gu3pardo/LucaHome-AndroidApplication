package guepardoapps.lucahome.bixby.classes.actions;

import java.io.Serializable;

public interface IBixbyAction extends Serializable {
    enum ActionType {Null, Application, Network, WirelessSocket, WirelessSwitch}

    enum NetworkType {Null, Mobile, Wifi}

    enum StateType {Null, Off, On}

    String GetDatabaseString() throws NoSuchMethodException;

    String GetInformationString();
}
