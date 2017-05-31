package guepardoapps.library.lucahome.common.enums;

import java.io.Serializable;
import java.util.Arrays;

import org.altbeacon.beacon.Beacon;

public enum ServerBeacons implements Serializable {

    NULL(0, null, MediaServerSelection.NULL),
    BEACON_1(1,
            new Beacon.Builder()
                    .setId1("47756570-6172-646f-4170-707362794a53")
                    .setId2("1")
                    .setId3("1")
                    .setManufacturer(0x0118)
                    .setTxPower(-59)
                    .setDataFields(Arrays.asList(new Long[]{0l}))
                    .build(),
            MediaServerSelection.MEDIAMIRROR_1),
    BEACON_2(2,
            new Beacon.Builder()
                    .setId1("47756570-6172-646f-4170-707362794a53")
                    .setId2("2")
                    .setId3("2")
                    .setManufacturer(0x0118)
                    .setTxPower(-59)
                    .setDataFields(Arrays.asList(new Long[]{0l}))
                    .build(),
            MediaServerSelection.MEDIAMIRROR_2),
    BEACON_3(3,
            new Beacon.Builder()
                    .setId1("47756570-6172-646f-4170-707362794a53")
                    .setId2("3")
                    .setId3("3")
                    .setManufacturer(0x0118)
                    .setTxPower(-59)
                    .setDataFields(Arrays.asList(new Long[]{0l}))
                    .build(),
            MediaServerSelection.MEDIAMIRROR_3);

    private int _id;
    private Beacon _beacon;
    private MediaServerSelection _mediaServerSelection;

    ServerBeacons(int id, Beacon beacon, MediaServerSelection mediaServerSelection) {
        _id = id;
        _beacon = beacon;
        _mediaServerSelection = mediaServerSelection;
    }

    public int GetId() {
        return _id;
    }

    public Beacon GetBeacon() {
        return _beacon;
    }

    public MediaServerSelection GetMediaServerSelection() {
        return _mediaServerSelection;
    }

    @Override
    public String toString() {
        return _beacon.toString();
    }

    public static ServerBeacons GetById(int id) {
        for (ServerBeacons e : values()) {
            if (e._id == id) {
                return e;
            }
        }
        return NULL;
    }

    public static ServerBeacons GetByBeacon(Beacon beacon) {
        for (ServerBeacons e : values()) {
            if (e._beacon == beacon) {
                return e;
            }
        }
        return NULL;
    }

    public static ServerBeacons GetByMediaServerSelectionId(int mediaServerSelectionId) {
        for (ServerBeacons e : values()) {
            if (e._mediaServerSelection.GetId() == mediaServerSelectionId) {
                return e;
            }
        }
        return NULL;
    }
}
