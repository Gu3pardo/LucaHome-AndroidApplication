package guepardoapps.lucahome.common.extensions

import guepardoapps.lucahome.common.extensions.wirelesssocket.getJsonKey
import guepardoapps.lucahome.common.extensions.wirelesssocket.getPropertyJsonKey
import guepardoapps.lucahome.common.models.wirelesssocket.WirelessSocket

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

import org.junit.Assert.assertEquals
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
class WirelessSocketExtensionUnitTest : Spek({

    describe("Unit tests for WirelessSocketExtensionUnitTest") {

        beforeEachTest { }

        afterEachTest { }

        it("getJsonKey for wirelessSocket should be correct") {
            // Arrange
            val wirelessSocket = WirelessSocket()
            val expectedParent = "Data"
            val expectedKey = "WirelessSocket"

            // Act
            val actualJsonKey = wirelessSocket.getJsonKey()

            // Assert
            assertEquals(expectedParent, actualJsonKey.parent)
            assertEquals(expectedKey, actualJsonKey.key)
        }

        it("getPropertyJsonKey for city  should be correct") {
            // Arrange
            val wirelessSocket = WirelessSocket()
            val expectedParent = "LastTrigger"
            val expectedKey = "DateTime"

            // Act
            val name = wirelessSocket::lastTriggerDateTime.name
            val actualJsonKey = wirelessSocket.getPropertyJsonKey(name)

            // Assert
            assertEquals(expectedParent, actualJsonKey.parent)
            assertEquals(expectedKey, actualJsonKey.key)
        }
    }
})
