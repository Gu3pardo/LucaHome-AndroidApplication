package guepardoapps.lucahome.common.server.handler;

import android.content.Context;
import android.support.annotation.NonNull;

public interface IDataHandler {
    String CommandSplitChar = "&&";
    String ResponseSplitChar = "###";

    void Initialize(@NonNull Context context);

    String PerformAction(String action) throws Exception;

    void Dispose();
}
