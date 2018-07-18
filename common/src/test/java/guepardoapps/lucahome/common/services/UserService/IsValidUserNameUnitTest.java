package guepardoapps.lucahome.common.services.UserService;

import guepardoapps.lucahome.common.services.user.UserService;

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(Theories.class)
public class IsValidUserNameUnitTest {

    public static class UserNameValidation {
        String userName;
        boolean isValid;

        UserNameValidation(String userName, boolean isValid) {
            this.userName = userName;
            this.isValid = isValid;
        }
    }

    @DataPoints
    public static UserNameValidation[] userNames = {
            new UserNameValidation("Administrator", true),
            new UserNameValidation("Admin1", false),
            new UserNameValidation("Admin.", false),
            new UserNameValidation("Adm", false),
            new UserNameValidation("12345", false),
            new UserNameValidation("..--..", false),
            new UserNameValidation("Test", true),
            new UserNameValidation("Tester", true),
            new UserNameValidation("", false),
            new UserNameValidation("TesterHelloWorld", true),
            new UserNameValidation("TesterHelloWorldI", false)
    };

    @Theory
    public void isValidUserName_isCorrect(UserNameValidation userNameValidation) throws Exception {
        boolean actualIsValid = UserService.Companion.getInstance().isValidUserName(userNameValidation.userName);
        assertEquals(userNameValidation.isValid, actualIsValid);
    }
}