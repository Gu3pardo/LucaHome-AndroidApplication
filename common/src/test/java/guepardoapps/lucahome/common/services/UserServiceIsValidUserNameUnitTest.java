package guepardoapps.lucahome.common.services;

import guepardoapps.lucahome.common.services.user.UserService;

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(Theories.class)
public class UserServiceIsValidUserNameUnitTest {

    public static class UserNameValidation {
        String userName;
        boolean isValid;

        UserNameValidation(String userName, boolean isValid) {
            this.userName = userName;
            this.isValid = isValid;
        }
    }

    @DataPoints
    public static UserNameValidation[] userNames = {new UserNameValidation("Administrator", true)};

    @Theory
    public void isValidUserName_isCorrect(UserNameValidation userNameValidation) throws Exception {
        boolean actualIsValid = UserService.Companion.getInstance().isValidUserName(userNameValidation.userName);
        assertEquals(userNameValidation.isValid, actualIsValid);
    }
}