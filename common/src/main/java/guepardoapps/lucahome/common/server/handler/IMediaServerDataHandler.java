package guepardoapps.lucahome.common.server.handler;

import android.content.Context;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.common.controller.mediaserver.interfaces.ICenterViewController;
import guepardoapps.lucahome.common.controller.mediaserver.interfaces.IRssViewController;

public interface IMediaServerDataHandler extends IDataHandler {
    void Initialize(@NonNull Context context, @NonNull ICenterViewController iCenterViewController, @NonNull IRssViewController iRssViewController);
}
