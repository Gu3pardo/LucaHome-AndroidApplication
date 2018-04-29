package guepardoapps.lucahome.common.services;

import guepardoapps.lucahome.common.classes.LucaSecurity;

@SuppressWarnings({"unused"})
public interface ILucaSecurityService extends ILucaService<LucaSecurity> {
    String LucaSecurityDownloadFinishedBroadcast = "guepardoapps.lucahome.common.services.lucasecurity.download.finished";
    String LucaSecurityCameraStateFinishedBroadcast = "guepardoapps.lucahome.common.services.lucasecurity.camera.state.finished";
    String LucaSecurityMotionStateFinishedBroadcast = "guepardoapps.lucahome.common.services.lucasecurity.motion.state.finished";

    String LucaSecurityCameraStateFinishedBundle = "LucaSecurityCameraStateFinishedBundle";
    String LucaSecurityDownloadFinishedBundle = "LucaSecurityDownloadFinishedBundle";
    String LucaSecurityMotionStateFinishedBundle = "LucaSecurityMotionStateFinishedBundle";

    int NotificationId = 94802738;

    void SetCameraState(boolean state);

    void SetMotionState(boolean state);
}
