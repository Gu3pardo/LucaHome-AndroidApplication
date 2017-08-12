package guepardoapps.lucahome.common.service.broadcasts.content;

import java.io.Serializable;

public class ObjectChangeFinishedContent implements Serializable {
    public boolean Success;
    public byte[] Response;

    public ObjectChangeFinishedContent(boolean succcess, byte[] response) {
        Success = succcess;
        Response = response;
    }
}
