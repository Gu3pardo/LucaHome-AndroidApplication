package guepardoapps.lucahome.common.services.UserService;

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import guepardoapps.lucahome.common.services.user.UserService;

import static org.junit.Assert.assertEquals;

@RunWith(Theories.class)
public class IsValidPasswordUnitTest {

    public static class PasswordValidation {
        String password;
        boolean isValid;

        PasswordValidation(String password, boolean isValid) {
            this.password = password;
            this.isValid = isValid;
        }
    }

    @DataPoints
    public static PasswordValidation[] passwords = {
            new PasswordValidation("Pass1", false),
            new PasswordValidation("Pass12", true),
            new PasswordValidation("Pass123", true),
            new PasswordValidation("Password123456789Abc1234567890Ab", true),
            new PasswordValidation("Password123456789Abc1234567890Abc", false),
            new PasswordValidation("Password123456789Abc1234567890Abc1", false),
            new PasswordValidation("Pass123.", false)
    };

    @Theory
    public void isValidUserName_isCorrect(PasswordValidation passwordValidation) throws Exception {
        boolean actualIsValid = UserService.Companion.getInstance().isValidPassword(passwordValidation.password);
        assertEquals(passwordValidation.isValid, actualIsValid);
    }
}