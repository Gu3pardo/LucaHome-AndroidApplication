package guepardoapps.lucahome.common.converter

import org.junit.Test

import java.util.ArrayList
import java.util.Calendar
import java.util.UUID

import guepardoapps.lucahome.common.converter.wirelesssocket.JsonDataToWirelessSocketConverter
import guepardoapps.lucahome.common.enums.common.ServerDatabaseAction
import guepardoapps.lucahome.common.models.wirelesssocket.WirelessSocket

import org.junit.Assert.assertEquals

class JsonDataToWirelessSocketUnitTest {
    @Test
    @Throws(Exception::class)
    fun conversion_isCorrect() {
        // Arrange
        val converter = JsonDataToWirelessSocketConverter()

        val jsonResponse =
                "{" +
                        "\"Data\":" +
                        "[" +
                        "{\"WirelessSocket\":" +
                        "{" +
                        "\"Uuid\":\"c111d375-e729-4cf1-95c8-e5a0253586e1\"," +
                        "\"RoomUuid\":\"6a7acd45-f863-40ed-a02e-d2ef04609f00\"," +
                        "\"Name\":\"Name\"," +
                        "\"Code\":\"Code\"," +
                        "\"State\":1," +
                        "\"LastTrigger\":" +
                        "{" +
                        "\"UserName\":\"LastTriggerUser\"," +
                        "\"Year\":2018," +
                        "\"Month\":1," +
                        "\"Day\":1," +
                        "\"Hour\":6," +
                        "\"Minute\":25" +
                        "}" +
                        "}" +
                        "}" +
                        "]" +
                        "}"

        val wirelessSocket = WirelessSocket()
        wirelessSocket.uuid = UUID.fromString("c111d375-e729-4cf1-95c8-e5a0253586e1")
        wirelessSocket.roomUuid = UUID.fromString("6a7acd45-f863-40ed-a02e-d2ef04609f00")
        wirelessSocket.name = "Name"
        wirelessSocket.code = "Code"
        wirelessSocket.state = true
        wirelessSocket.lastTriggerDateTime = Calendar.getInstance()
        wirelessSocket.lastTriggerUser = "LastTriggerUser"
        wirelessSocket.isOnServer = true
        wirelessSocket.serverDatabaseAction = ServerDatabaseAction.Null
        wirelessSocket.changeCount = 0
        wirelessSocket.showInNotification = true

        val expected = ArrayList<WirelessSocket>()
        expected.add(wirelessSocket)

        // Act
        val actual = converter.parse(jsonResponse)

        // Assert
        assertEquals(expected.size.toLong(), actual.size.toLong())
        assertEquals(expected[0].uuid, actual[0].uuid)
        assertEquals(expected[0].roomUuid, actual[0].roomUuid)
        assertEquals(expected[0].name, actual[0].name)
        assertEquals(expected[0].code, actual[0].code)
        assertEquals(expected[0].state, actual[0].state)
    }
}