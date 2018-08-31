package guepardoapps.lucahome.common.extensions

import guepardoapps.lucahome.common.extensions.common.doubleFormat
import guepardoapps.lucahome.common.extensions.common.integerFormat
import guepardoapps.lucahome.common.extensions.common.toBoolean
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

import org.junit.Assert.*
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
class NumberExtensionUnitTest : Spek({

    describe("Unit tests for NumberExtensionUnitTest") {

        beforeEachTest { }

        afterEachTest { }

        it("integerFormat should be correct") {
            // Arrange
            val integerToTest = 5
            val charCount = 2
            val expectedString = "05"

            // Act
            val actualString = integerToTest.integerFormat(charCount)

            // Assert
            assertEquals(expectedString, actualString)
        }

        it("toBoolean should be correct false") {
            // Arrange
            val intToTest = 0

            // Act
            val actual = intToTest.toBoolean()

            // Assert
            assertFalse(actual)
        }

        it("toBoolean should be correct true") {
            // Arrange
            val intToTest = 2

            // Act
            val actual = intToTest.toBoolean()

            // Assert
            assertTrue(actual)
        }

        it("doubleFormat should be correct") {
            // Arrange
            val doubleToTest = 5.4
            val decimalCount = 2
            val expectedString = "5.40"

            // Act
            val actualString = doubleToTest.doubleFormat(decimalCount)

            // Assert
            assertEquals(expectedString, actualString)
        }
    }
})
