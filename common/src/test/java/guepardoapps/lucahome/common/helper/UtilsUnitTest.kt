package guepardoapps.lucahome.common.helper

import guepardoapps.lucahome.common.utils.ByteStringHelper
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

import org.junit.Assert.assertEquals
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
class UtilsUnitTest : Spek({

    describe("Unit tests for ByteStringHelper") {

        beforeEachTest { }

        afterEachTest { }

        it("conversion should be valid") {
            // Arrange
            val expectedValue = "HelloWorld!"
            val valueToConvert = charArrayOf(
                    4968.toChar(),
                    6969.toChar(),
                    7452.toChar(),
                    7452.toChar(),
                    7659.toChar(),
                    6003.toChar(),
                    7659.toChar(),
                    7866.toChar(),
                    7452.toChar(),
                    6900.toChar(),
                    2277.toChar()
            )

            // Act
            val actualValue = ByteStringHelper.readStringFromCharArray(valueToConvert)

            // Assert
            assertEquals(expectedValue, actualValue)
        }
    }
})