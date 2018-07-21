package guepardoapps.lucahome.common.services.user

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.experimental.theories.DataPoints
import org.junit.experimental.theories.Theories
import org.junit.experimental.theories.Theory
import org.junit.runner.RunWith

import org.junit.Assert.assertEquals
import org.junit.platform.runner.JUnitPlatform

// http://spekframework.github.io/spek/docs/latest/#_ignoring_tests

@RunWith(JUnitPlatform::class)
class UserServiceUnitTest : Spek({

    describe("Unit tests for UserServiceUnitTest") {

        beforeEachTest { }

        afterEachTest { }

        it("") {
            // Arrange

            // Act

            // Assert
        }
    }
})
@RunWith(Theories::class)
class IsValidPasswordUnitTest {

    data class PasswordValidation(var password: String, var isValid: Boolean)

    companion object {
        @DataPoints
        var passwords = arrayOf(
                PasswordValidation("Pass1", false),
                PasswordValidation("Pass12", true),
                PasswordValidation("Pass123", true),
                PasswordValidation("Password123456789Abc1234567890Ab", true),
                PasswordValidation("Password123456789Abc1234567890Abc", false),
                PasswordValidation("Password123456789Abc1234567890Abc1", false),
                PasswordValidation("Pass123.", false)
        )
    }

    @Theory
    @Throws(Exception::class)
    fun isValidUserName_isCorrect(passwordValidation: PasswordValidation) {
        val actualIsValid = UserService.instance.isValidPassword(passwordValidation.password)
        assertEquals(passwordValidation.isValid, actualIsValid)
    }
}