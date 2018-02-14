package guepardoapps.lucahome.common.classes.mediaserver;

import android.support.annotation.NonNull;

import java.io.Serializable;

public interface IMediaServerClass extends Serializable {
    String GetCommunicationString();

    void ParseCommunicationString(@NonNull String communicationString) throws Exception;
}
