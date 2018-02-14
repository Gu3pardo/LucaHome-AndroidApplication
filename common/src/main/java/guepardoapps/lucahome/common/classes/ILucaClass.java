package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.UUID;

public interface ILucaClass extends Serializable {
    enum LucaServerDbAction {Null, Add, Update, Delete}

    UUID GetUuid();

    UUID GetRoomUuid() throws NoSuchMethodException;

    String GetCommandAdd() throws NoSuchMethodException;

    String GetCommandUpdate() throws NoSuchMethodException;

    String GetCommandDelete() throws NoSuchMethodException;

    void SetIsOnServer(boolean isOnServer) throws NoSuchMethodException;

    boolean GetIsOnServer() throws NoSuchMethodException;

    void SetServerDbAction(@NonNull LucaServerDbAction lucaServerDbAction) throws NoSuchMethodException;

    LucaServerDbAction GetServerDbAction() throws NoSuchMethodException;
}
