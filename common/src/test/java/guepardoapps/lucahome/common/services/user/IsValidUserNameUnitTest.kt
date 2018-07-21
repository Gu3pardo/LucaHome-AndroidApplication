package guepardoapps.lucahome.common.services.user

import org.junit.experimental.theories.DataPoints
import org.junit.experimental.theories.Theories
import org.junit.experimental.theories.Theory
import org.junit.runner.RunWith

import org.junit.Assert.assertEquals

@RunWith(Theories::class)
class IsValidUserNameUnitTest {

    class UserNameValidation internal constructor(internal var userName: String, internal var isValid: Boolean)

    companion object {

        @DataPoints
        var userNames = arrayOf(
                UserNameValidation("Administrator", true),
                UserNameValidation("Admin1", false),
                UserNameValidation("Admin.", false),
                UserNameValidation("Adm", false),
                UserNameValidation("12345", false),
                UserNameValidation("..--..", false),
                UserNameValidation("Test", true),
                UserNameValidation("Tester", true),
                UserNameValidation("", false),
                UserNameValidation("TesterHelloWorld", true),
                UserNameValidation("TesterHelloWorldI", false)
        )
    }

    @Theory
    @Throws(Exception::class)
    fun isValidUserName_isCorrect(userNameValidation: UserNameValidation) {
        val actualIsValid = UserService.instance.isValidUserName(userNameValidation.userName)
        assertEquals(userNameValidation.isValid, actualIsValid)
    }
}