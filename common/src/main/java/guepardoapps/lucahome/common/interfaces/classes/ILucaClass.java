package guepardoapps.lucahome.common.interfaces.classes;

import android.support.annotation.NonNull;

public interface ILucaClass {
    enum LucaServerDbAction {Null, Add, Update, Delete}

    int GetId();

    void SetIsOnServer(boolean isOnServer);

    boolean GetIsOnServer();

    void SetServerDbAction(@NonNull LucaServerDbAction serverDbAction);

    LucaServerDbAction GetServerDbAction();

    String CommandAdd() throws NoSuchMethodException;

    String CommandUpdate() throws NoSuchMethodException;

    String CommandDelete() throws NoSuchMethodException;
}
