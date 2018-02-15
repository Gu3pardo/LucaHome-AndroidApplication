package guepardoapps.lucahome.common.server.handler;

import android.content.Context;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.common.controller.mediaserver.interfaces.ICenterViewController;
import guepardoapps.lucahome.common.controller.mediaserver.interfaces.IRssViewController;

public interface IMediaServerDataHandler extends IDataHandler {
    String BroadcastScreenNormal = "guepardoapps.lucahome.common.server.handler.broadcast.screen.normal";
    String BroadcastScreenOn = "guepardoapps.lucahome.common.server.handler.broadcast.screen.on";
    String BroadcastScreenOff = "guepardoapps.lucahome.common.server.handler.broadcast.screen.off";

    String BroadcastVideoPlay = "guepardoapps.lucahome.common.server.handler.broadcast.video.play";
    String BroadcastVideoPause = "guepardoapps.lucahome.common.server.handler.broadcast.video.pause";
    String BroadcastVideoStop = "guepardoapps.lucahome.common.server.handler.broadcast.video.stop";

    String BroadcastVideoPosition = "guepardoapps.lucahome.common.server.handler.broadcast.video.position";
    String BundleVideoPosition = "BundleVideoPosition";

    String BroadcastRadioStreamStop = "guepardoapps.lucahome.common.server.handler.broadcast.radioStream.stop";

    String BroadcastShowCenterModel = "guepardoapps.lucahome.common.server.handler.broadcast.show.centerModel";
    String BundleShowCenterModel = "BundleShowCenterModel";

    String BroadcastRssFeedReset = "guepardoapps.lucahome.common.server.handler.broadcast.rssFeed.reset";

    String BroadcastRssFeedUpdate = "guepardoapps.lucahome.common.server.handler.broadcast.rssFeed.update";
    String BundleRssFeedUpdate = "BundleRssFeedUpdate";

    String BroadcastIpAddressUpdate = "guepardoapps.lucahome.common.server.handler.broadcast.ipAddress.update";

    void Initialize(@NonNull Context context, @NonNull ICenterViewController iCenterViewController, @NonNull IRssViewController iRssViewController);
}
