package guepardoapps.lucahome.bixby.classes.requirements;

public interface IBixbyRequirement {
    enum RequirementType {Null, Position, Light, Network, WirelessSocket}

    enum LightCompareType {Null, Below, Near, Above}

    enum NetworkType {Null, Mobile, Wifi}

    enum StateType {Null, Off, On}

    String GetDatabaseString() throws NoSuchMethodException;

    String GetInformationString();
}
