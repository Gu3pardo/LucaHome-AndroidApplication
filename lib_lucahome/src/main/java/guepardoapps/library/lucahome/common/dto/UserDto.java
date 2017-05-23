package guepardoapps.library.lucahome.common.dto;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class UserDto implements Serializable {

    private static final long serialVersionUID = 1533208191654743604L;

    private static final String TAG = UserDto.class.getSimpleName();

    private String _userName;
    private String _password;

    public UserDto(
            @NonNull String userName,
            @NonNull String password) {
        _userName = userName;
        _password = password;
    }

    public String GetUserName() {
        return _userName;
    }

    public String GetPassword() {
        return _password;
    }

    @Override
    public String toString() {
        return "{" + TAG
                + ": {UserName: " + ""
                + "};{Password: " + "" + "}}";
    }
}
