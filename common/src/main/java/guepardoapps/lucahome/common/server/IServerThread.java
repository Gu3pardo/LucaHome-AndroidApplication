package guepardoapps.lucahome.common.server;

import android.support.annotation.NonNull;

import guepardoapps.lucahome.common.server.handler.IDataHandler;

public interface IServerThread {
    void Start(int port, @NonNull IDataHandler dataHandler);

    void Dispose();
}
