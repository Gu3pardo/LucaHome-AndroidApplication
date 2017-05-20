package guepardoapps.library.lucahome.common.dto;

import java.io.Serializable;

public class ActionDto implements Serializable {

    private static final long serialVersionUID = -4577368865681987282L;

    private int _id;

    private String _name;
    private String _action;
    private String _broadcast;

    private static final String TAG = ActionDto.class.getSimpleName();

    public ActionDto(int id, String name, String action, String broadcast) {
        _id = id;

        _name = name;
        _action = action;
        _broadcast = broadcast;
    }

    public int GetId() {
        return _id;
    }

    public String GetName() {
        return _name;
    }

    public String GetAction() {
        return _action;
    }

    public String GetBroadcast() {
        return _broadcast;
    }

    @Override
    public String toString() {
        return "{" + TAG + ":{Id:" + String.valueOf(_id) + "};{Name:" + _name + "};{Action:" + _action + "};{Broadcast:"
                + _broadcast + "};}";
    }
}
