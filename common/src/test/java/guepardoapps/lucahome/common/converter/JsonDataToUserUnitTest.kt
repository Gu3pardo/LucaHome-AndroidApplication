package guepardoapps.lucahome.common.converter

import org.junit.Test

import java.util.UUID

import guepardoapps.lucahome.common.converter.user.JsonDataToUserConverter
import guepardoapps.lucahome.common.enums.user.UserRole
import guepardoapps.lucahome.common.models.user.User

import org.junit.Assert.assertEquals

class JsonDataToUserUnitTest {
    @Test
    @Throws(Exception::class)
    fun conversion_isCorrect() {
        // Arrange
        val converter = JsonDataToUserConverter()

        val jsonResponse = "{\"Data\":[{\"User\":{\"Uuid\":\"09dec9a7-1652-44f9-bbf6-b2b8bc5b2c7c\",\"Name\":\"Name\",\"Password\":\"Password\",\"Role\":1}}]}"

        val expected = User()
        expected.uuid = UUID.fromString("09dec9a7-1652-44f9-bbf6-b2b8bc5b2c7c")
        expected.name = "Name"
        expected.password = "Password"
        expected.role = UserRole.Guest

        // Act
        val actual = converter.parse(jsonResponse)

        // Assert
        assertEquals(expected.uuid, actual?.uuid)
        assertEquals(expected.name, actual?.name)
        assertEquals(expected.password, actual?.password)
        assertEquals(expected.role, actual?.role)
    }
}