package guepardoapps.lucahome.common.enums;

import java.io.Serializable;

public enum NavigationData implements Serializable {

    NULL(-1),

    BOOT(0),
    MAIN(1),
    FINISH(2);

    private int _id;

    NavigationData(int id) {
        _id = id;
    }

    public int GetId() {
        return _id;
    }

    public static NavigationData GetById(int id) {
        for (NavigationData e : values()) {
            if (e._id == id) {
                return e;
            }
        }
        return NULL;
    }
}
