package guepardoapps.lucahome.common.helper

import guepardoapps.lucahome.common.utils.StringHelper
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

import org.junit.Assert.*
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
class StringHelperUnitTest : Spek({

    describe("Unit tests for StringHelperUnitTest") {

        beforeEachTest { }

        afterEachTest { }

        it("getStringCount should be correct") {
            // Arrange
            val stringToTest = "Hello world says 'Hello world' and in german 'Hallo Welt'"
            val stringToFind = "Hello world"
            val expectedCount = 2

            // Act
            val actualCount = StringHelper.getStringCount(stringToTest, stringToFind)

            // Assert
            assertEquals(expectedCount, actualCount)
        }

        it("getCharPositions should be correct") {
            // Arrange
            val stringToTest = "Aber Hallo sagte Achim"
            val charToFind: Char = "A".toCharArray()[0]
            val expectedPositions: List<Int> = listOf(0, 17)

            // Act
            val actualPositions = StringHelper.getCharPositions(stringToTest, charToFind)

            // Assert
            assertEquals(expectedPositions, actualPositions)
        }

        it("stringsAreEqual should be correct") {
            // Arrange
            val stringArrayToTest = arrayOf("Hello", "Hello", "Hello", "Hello")

            // Act
            val areEqual = StringHelper.stringsAreEqual(stringArrayToTest)

            // Assert
            assertTrue(areEqual)
        }

        it("stringsAreEqual should return false if not equal") {
            // Arrange
            val stringArrayToTest = arrayOf("Hello", "Hallo", "Hello", "Hello")

            // Act
            val areEqual = StringHelper.stringsAreEqual(stringArrayToTest)

            // Assert
            assertFalse(areEqual)
        }

        it("excludeAndSelectString should return expected string") {
            // Arrange
            val stringArrayToTest = arrayOf("Hello World", "Hallo Welt", "Hola bonita")
            val stringToExclude = "World"
            val stringToFind = "bonita"
            val expectedString = "Hola bonita"

            // Act
            val actualString = StringHelper.excludeAndSelectString(stringArrayToTest, stringToExclude, stringToFind)

            // Assert
            assertEquals(expectedString, actualString)
        }
    }
})