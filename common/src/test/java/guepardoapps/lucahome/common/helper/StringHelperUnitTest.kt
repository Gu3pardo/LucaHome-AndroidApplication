package guepardoapps.lucahome.common.helper

import guepardoapps.lucahome.common.utils.StringHelper
import org.junit.Test

import org.junit.Assert.*

class StringHelperUnitTest {
    @Test
    fun getStringCount_isCorrect() {
        // Arrange
        val stringToTest = "Hello world says 'Hello world' and in german 'Hallo Welt'"
        val stringToFind = "Hello world"
        val expectedCount = 2

        // Act
        val actualCount = StringHelper.getStringCount(stringToTest, stringToFind)

        // Assert
        assertEquals(expectedCount, actualCount)
    }

    @Test
    fun getCharPositions_isCorrect() {
        // Arrange
        val stringToTest = "Aber Hallo sagte Achim"
        val charToFind: Char = "A".toCharArray()[0]
        val expectedPositions: List<Int> = listOf(0, 17)

        // Act
        val actualPositions = StringHelper.getCharPositions(stringToTest, charToFind)

        // Assert
        assertEquals(expectedPositions, actualPositions)
    }

    @Test
    fun stringsAreEqual_shouldReturnTrue_IfEqual() {
        // Arrange
        val stringArrayToTest = arrayOf("Hello", "Hello", "Hello", "Hello")

        // Act
        val areEqual = StringHelper.stringsAreEqual(stringArrayToTest)

        // Assert
        assertTrue(areEqual)
    }

    @Test
    fun stringsAreEqual_shouldReturnFalse_IfNotEqual() {
        // Arrange
        val stringArrayToTest = arrayOf("Hello", "Hallo", "Hello", "Hello")

        // Act
        val areEqual = StringHelper.stringsAreEqual(stringArrayToTest)

        // Assert
        assertFalse(areEqual)
    }

    @Test
    fun excludeAndSelectString_shouldReturnExpectedString() {
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
