package guepardoapps.lucahome.bixby.classes.actions;

public interface IBixbyAction {
    enum ActionType {Null, Application, Network, WirelessSocket, WirelessSwitch}

    enum NetworkType {Null, Mobile, Wifi}

    enum StateType {Null, Off, On}

    String GetDatabaseString() throws NoSuchMethodException;

    String GetInformationString();
}
