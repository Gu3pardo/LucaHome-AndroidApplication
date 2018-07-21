package guepardoapps.lucahome.common.extensions

import guepardoapps.lucahome.common.extensions.wirelesssocket.getPropertyJsonKey
import guepardoapps.lucahome.common.models.wirelesssocket.WirelessSocket

import org.junit.experimental.theories.DataPoints
import org.junit.experimental.theories.Theories
import org.junit.experimental.theories.Theory
import org.junit.runner.RunWith

import org.junit.Assert.assertEquals

@RunWith(Theories::class)
class WirelessSocketExtensionUnitTest {
/*
    @Test
    @Throws(Exception::class)
    fun getJsonKey_isCorrect() {
        val expectedParent = "Data"
        val expectedKey = "WirelessSocket"
        val actual = WirelessSocket().getJsonKey()

        assertEquals(expectedParent, actual.parent)
        assertEquals(expectedKey, actual.key)
    }
*/
    class PropertyJson internal constructor(
        var property: String,
        var parent: String,
        internal var key: String)

    companion object {
        @DataPoints
        var propertyJsonList = arrayOf(
                PropertyJson(WirelessSocket::uuid.name, "", "Uuid"),
                PropertyJson(WirelessSocket::roomUuid.name, "", "RoomUuid"),
                PropertyJson(WirelessSocket::name.name, "", "Name"),
                PropertyJson(WirelessSocket::code.name, "", "Code"),
                PropertyJson(WirelessSocket::state.name, "", "State"),
                PropertyJson(WirelessSocket::lastTriggerDateTime.name, "LastTrigger", "DateTime"),
                PropertyJson(WirelessSocket::lastTriggerUser.name, "LastTrigger", "User")
        )
    }

    @Theory
    @Throws(Exception::class)
    fun getPropertyJsonKey_isCorrect(propertyJson: PropertyJson) {
        val actual = WirelessSocket().getPropertyJsonKey(propertyJson.property)

        assertEquals(propertyJson.parent, actual.parent)
        assertEquals(propertyJson.key, actual.key)
    }
}