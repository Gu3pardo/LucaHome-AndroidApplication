package guepardoapps.lucahome.common.interfaces.classes;

import android.support.annotation.NonNull;

public interface IMediaServerClass {
    String GetCommunicationString();

    void ParseCommunicationString(@NonNull String communicationString) throws Exception;
}
