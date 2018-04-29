package guepardoapps.lucahome.common.services;

import android.support.annotation.NonNull;

import guepardoapps.lucahome.common.classes.User;

@SuppressWarnings({"unused"})
public interface IUserService extends ILucaService<User> {
    String UserCheckedFinishedBroadcast = "guepardoapps.lucahome.common.services.user.checked.finished";
    String UserCheckedFinishedBundle = "UserCheckedFinishedBundle";

    void SetUser(@NonNull User user);

    User GetUser();

    void ValidateUser();

    boolean IsAnUserSaved();

    boolean IsEnteredUserValid(@NonNull String user);

    boolean IsEnteredPasswordValid(@NonNull String password);
}
