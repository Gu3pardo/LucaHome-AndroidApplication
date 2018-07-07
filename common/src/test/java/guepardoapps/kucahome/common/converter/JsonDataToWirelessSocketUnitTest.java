package guepardoapps.kucahome.common.converter;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import guepardoapps.lucahome.common.converter.wirelesssocket.JsonDataToWirelessSocketConverter;
import guepardoapps.lucahome.common.enums.common.ServerDatabaseAction;
import guepardoapps.lucahome.common.models.wirelesssocket.WirelessSocket;

import static org.junit.Assert.assertEquals;

public class JsonDataToWirelessSocketUnitTest {
    @Test
    public void conversion_isCorrect() throws Exception {
        // Arrange
        JsonDataToWirelessSocketConverter converter = new JsonDataToWirelessSocketConverter();

        String jsonResponse = "{\"Data\":[{\"WirelessSocket\":{\"Uuid\":\"UUID\",\"RoomUuid\":\"RoomUUID\",\"Name\":\"Name\",\"Code\":\"Code\",\"State\":1,\"LastTrigger\":{\"UserName\":\"LastTriggerUser\",\"Year\":2018,\"Month\":1,\"Day\":1,\"Hour\":6,\"Minute\":25}}}]}";

        WirelessSocket wirelessSocket = new WirelessSocket();
        wirelessSocket.uuid = UUID.fromString("UUID");
        wirelessSocket.roomUuid = UUID.fromString("RoomUUID");
        wirelessSocket.name = "Name";
        wirelessSocket.code = "Code";
        wirelessSocket.setState(true);
        wirelessSocket.lastTriggerDateTime = Calendar.getInstance();
        wirelessSocket.lastTriggerUser = "LastTriggerUser";
        wirelessSocket.setOnServer(true);
        wirelessSocket.setServerDatabaseAction(ServerDatabaseAction.Null);
        wirelessSocket.setChangeCount(0);
        wirelessSocket.setShowInNotification(true);

        ArrayList<WirelessSocket> expected = new ArrayList<>();
        expected.add(wirelessSocket);

        // Act
        ArrayList<WirelessSocket> actual = converter.parse(jsonResponse);

        // Assert
        assertEquals(expected.size(), actual.size());
        assertEquals(expected.get(0).getUuid(), actual.get(0).getUuid());
        assertEquals(expected.get(0).getRoomUuid(), actual.get(0).getRoomUuid());
        assertEquals(expected.get(0).getName(), actual.get(0).getName());
        assertEquals(expected.get(0).getCode(), actual.get(0).getCode());
        assertEquals(expected.get(0).getState(), actual.get(0).getState());
    }
}