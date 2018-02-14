package guepardoapps.lucahome.common.observer;

import java.io.Serializable;

public interface ISettingsContentObserver {
    enum VolumeChangeState implements Serializable {Null, Increased, Decreased}

    String BroadcastVolumeChange = "guepardoapps.lucahome.common.observer.settingsvontentobserver.volume.change";
    String BundleVolumeChange = "BundleVolumeChange";
}
