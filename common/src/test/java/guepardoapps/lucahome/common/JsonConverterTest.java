package guepardoapps.lucahome.common;

import android.test.mock.MockContext;

import org.junit.Test;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.classes.SerializablePair;
import guepardoapps.lucahome.common.classes.*;
import guepardoapps.lucahome.common.converter.*;
import guepardoapps.lucahome.common.enums.ShoppingEntryGroup;
import guepardoapps.lucahome.common.enums.Weekday;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class JsonConverterTest {
    @Test
    public void JsonDataToBirthdayConverterTest() throws Exception {
        String response = "{\"Data\":[{\"Birthday\":{\"Id\":0,\"Name\":\"Jonas Schubert\",\"Group\":\"Family\",\"RemindMe\":0,\"SentMail\":0,\"Date\":{\"Day\":2,\"Month\":1,\"Year\":1990}}},{\"Birthday\":{\"Id\":1,\"Name\":\"Artur Rychter\",\"Group\":\"Friends\",\"RemindMe\":1,\"SentMail\":0,\"Date\":{\"Day\":21,\"Month\":3,\"Year\":1990}}}]} ";
        MockContext mockContext = new MockContext();

        SerializableList<LucaBirthday> birthdayList = JsonDataToBirthdayConverter.getInstance().GetList(response, mockContext);

        assertNotNull(birthdayList);
        assertEquals(birthdayList.getSize(), 2);
        assertEquals(birthdayList.getValue(0).GetName(), "Jonas Schubert");
    }

    @Test
    public void JsonDataToChangeConverterTest() throws Exception {
        String response = "{\"Data\":[{\"Change\":{\"Type\":\"Birthdays\",\"UserName\":\"Jonas\",\"Date\":{\"Day\":13,\"Month\":12,\"Year\":2017},\"Time\":{\"Hour\":20,\"Minute\":5}}},{\"Change\":{\"Type\":\"Coins\",\"UserName\":\"Jonas\",\"Date\":{\"Day\":13,\"Month\":12,\"Year\":2017},\"Time\":{\"Hour\":20,\"Minute\":5}}},{\"Change\":{\"Type\":\"MapContent\",\"UserName\":\"Jonas\",\"Date\":{\"Day\":13,\"Month\":12,\"Year\":2017},\"Time\":{\"Hour\":20,\"Minute\":5}}},{\"Change\":{\"Type\":\"Menu\",\"UserName\":\"Jonas\",\"Date\":{\"Day\":13,\"Month\":12,\"Year\":2017},\"Time\":{\"Hour\":20,\"Minute\":5}}},{\"Change\":{\"Type\":\"Settings\",\"UserName\":\"Jonas\",\"Date\":{\"Day\":13,\"Month\":12,\"Year\":2017},\"Time\":{\"Hour\":20,\"Minute\":5}}},{\"Change\":{\"Type\":\"ShoppingList\",\"UserName\":\"Jonas\",\"Date\":{\"Day\":13,\"Month\":12,\"Year\":2017},\"Time\":{\"Hour\":20,\"Minute\":5}}}]} ";

        SerializableList<Change> changeList = JsonDataToChangeConverter.getInstance().GetList(response);

        assertNotNull(changeList);
        assertEquals(changeList.getSize(), 6);
        assertEquals(changeList.getValue(0).GetUser(), "Jonas");
    }

    @Test
    public void JsonDataToCoinConversionConverterTest() throws Exception {
        String response = "{\"BTC\":{\"EUR\":1989.62},\"DASH\":{\"EUR\":144.12},\"ETC\":{\"EUR\":13.12},\"ETH\":{\"EUR\":185.94},\"LTC\":{\"EUR\":35.93},\"XMR\":{\"EUR\":31.17},\"ZEC\":{\"EUR\":174}}";

        SerializableList<SerializablePair<String, Double>> conversionList = JsonDataToCoinConversionConverter.getInstance().GetList(response);

        assertEquals(conversionList.getSize(), 7);
    }

    @Test
    public void JsonDataToCoinConverterTest() throws Exception {
        String response = "{\"Data\":[{\"Coin\":{\"Id\":1,\"User\":\"Jonas\",\"Type\":\"ETC\",\"Amount\":4}},{\"Coin\":{\"Id\":2,\"User\":\"Jonas\",\"Type\":\"ETH\",\"Amount\":4}},{\"Coin\":{\"Id\":3,\"User\":\"Jonas\",\"Type\":\"LTC\",\"Amount\":4}},{\"Coin\":{\"Id\":4,\"User\":\"Jonas\",\"Type\":\"IOTA\",\"Amount\":264}}]} ";

        SerializableList<Coin> coinList = JsonDataToCoinConverter.getInstance().GetList(response, new SerializableList<>());

        assertNotNull(coinList);
        assertEquals(coinList.getSize(), 4);
        assertEquals(coinList.getValue(0).GetType(), "ETC");
    }

    @Test
    public void JsonDataToCoinTrendConverterTest() throws Exception {
        Coin coin = new Coin(1, "Jonas", "BTC", 0.25623, 5717.12, Coin.Trend.NULL, -1, false, ILucaClass.LucaServerDbAction.Null);
        String response = "{\"Response\":\"Success\",\"Type\":100,\"Aggregated\":true,\"Data\":[{\"time\":1509526800,\"close\":5701.01,\"high\":5731.75,\"low\":5567.39,\"open\":5568.18,\"volumefrom\":5069.370000000001,\"volumeto\":28905115.34},{\"time\":1509537600,\"close\":5683.31,\"high\":5706.18,\"low\":5631.76,\"open\":5701.01,\"volumefrom\":3043.34,\"volumeto\":17265365.919999998},{\"time\":1509548400,\"close\":5737.8,\"high\":5759.6,\"low\":5671.74,\"open\":5683.31,\"volumefrom\":2867.68,\"volumeto\":16420272.52},{\"time\":1509559200,\"close\":5727.92,\"high\":5743.72,\"low\":5690.27,\"open\":5743.43,\"volumefrom\":1774.25,\"volumeto\":10127145.39},{\"time\":1509570000,\"close\":5870.89,\"high\":5871.48,\"low\":5717.72,\"open\":5727.23,\"volumefrom\":1840.0499999999997,\"volumeto\":10577935.32},{\"time\":1509580800,\"close\":5893.69,\"high\":5967.44,\"low\":5816.51,\"open\":5870.89,\"volumefrom\":1933.44,\"volumeto\":11355695.91},{\"time\":1509591600,\"close\":5931.23,\"high\":5947.51,\"low\":5885.83,\"open\":5897.27,\"volumefrom\":1056.1100000000001,\"volumeto\":6221662.460000001},{\"time\":1509602400,\"close\":6096.4,\"high\":6098.09,\"low\":5926.82,\"open\":5931.23,\"volumefrom\":3175.85,\"volumeto\":19039197.2},{\"time\":1509613200,\"close\":6039.48,\"high\":6426.61,\"low\":5779.65,\"open\":6093.49,\"volumefrom\":7850.2,\"volumeto\":47803459.3},{\"time\":1509624000,\"close\":6087.96,\"high\":6242.89,\"low\":5831.48,\"open\":6039.14,\"volumefrom\":4806.23,\"volumeto\":29064349.4},{\"time\":1509634800,\"close\":6080.03,\"high\":6165.98,\"low\":5945.54,\"open\":6090.11,\"volumefrom\":3146.4,\"volumeto\":19045486.759999998},{\"time\":1509645600,\"close\":6084.4,\"high\":6123.23,\"low\":5939.34,\"open\":6079.93,\"volumefrom\":2860.61,\"volumeto\":17153807.4},{\"time\":1509656400,\"close\":6077.35,\"high\":6124.34,\"low\":6062.22,\"open\":6084.4,\"volumefrom\":1538.7400000000002,\"volumeto\":9335896.53},{\"time\":1509667200,\"close\":6074.23,\"high\":6101.29,\"low\":6014.6,\"open\":6077.35,\"volumefrom\":1054.1399999999999,\"volumeto\":6332995.869999999},{\"time\":1509678000,\"close\":6227.98,\"high\":6229.75,\"low\":6073.99,\"open\":6074.3,\"volumefrom\":1258.25,\"volumeto\":7727202.23},{\"time\":1509688800,\"close\":6418.95,\"high\":6420.19,\"low\":6218.89,\"open\":6227.98,\"volumefrom\":2811.1400000000003,\"volumeto\":17611338.57},{\"time\":1509699600,\"close\":6335.81,\"high\":6447.05,\"low\":6287.77,\"open\":6418.95,\"volumefrom\":4276.21,\"volumeto\":27126568.7},{\"time\":1509710400,\"close\":6332.87,\"high\":6362.85,\"low\":6246.37,\"open\":6332.68,\"volumefrom\":3022.9300000000003,\"volumeto\":18933861.06},{\"time\":1509721200,\"close\":6360.81,\"high\":6392.17,\"low\":6322.82,\"open\":6332.87,\"volumefrom\":2151.13,\"volumeto\":13640346.049999999},{\"time\":1509732000,\"close\":6302.36,\"high\":6399.26,\"low\":6301.99,\"open\":6360.82,\"volumefrom\":1560.4499999999998,\"volumeto\":9881534.65},{\"time\":1509742800,\"close\":6248.98,\"high\":6311.22,\"low\":6187.69,\"open\":6303.14,\"volumefrom\":2538.9,\"volumeto\":15771730.52},{\"time\":1509753600,\"close\":6219.44,\"high\":6250.01,\"low\":6079.32,\"open\":6248.98,\"volumefrom\":1933.16,\"volumeto\":11848070.45},{\"time\":1509764400,\"close\":6217.52,\"high\":6283.66,\"low\":6202.47,\"open\":6219.44,\"volumefrom\":657.5999999999999,\"volumeto\":4081269.96},{\"time\":1509775200,\"close\":6218.61,\"high\":6275.6,\"low\":6176.74,\"open\":6218.12,\"volumefrom\":834.12,\"volumeto\":5177045.94},{\"time\":1509786000,\"close\":6205.33,\"high\":6243.29,\"low\":6171.01,\"open\":6218.61,\"volumefrom\":875.45,\"volumeto\":5411794.54}],\"TimeTo\":1509793200,\"TimeFrom\":1509526800,\"FirstValueInArray\":true,\"ConversionType\":{\"type\":\"direct\",\"conversionSymbol\":\"\"}}";

        coin = JsonDataToCoinTrendConverter.UpdateTrend(coin, response, "EUR");

        assertNotEquals(coin, Coin.Trend.NULL);
    }

    @Test
    public void JsonDataToListedMenuConverterTest() throws Exception {
        String response = "{\"Data\":[{\"ListedMenu\":{\"Id\":1,\"Title\":\"Bratlinge mit Reis\",\"Description\":\"\",\"UseCounter\":4,\"Rating\":0}},{\"ListedMenu\":{\"Id\":2,\"Title\":\"Pizza\",\"Description\":\"\",\"UseCounter\":5,\"Rating\":0}},{\"ListedMenu\":{\"Id\":3,\"Title\":\"Sojagyros\",\"Description\":\"\",\"UseCounter\":5,\"Rating\":0}}]} ";

        SerializableList<ListedMenu> listedMenuList = JsonDataToListedMenuConverter.getInstance().GetList(response);

        assertNotNull(listedMenuList);
        assertEquals(listedMenuList.getSize(), 3);
        assertEquals(listedMenuList.getValue(0).GetTitle(), "Bratlinge mit Reis");
    }

    @Test
    public void JsonDataToMapContentConverterTest() throws Exception {
        String response = "{\"Data\":[{\"MapContent\":{\"Id\":1,\"Type\":\"WirelessSocket\",\"TypeId\":1,\"Name\":\"Light_Sleeping\",\"ShortName\":\"LiSlSo\",\"Area\":\"Sleeping_Room\",\"Visibility\":1,\"Position\":{\"Point\":{\"X\":4,\"Y\":3}}}},{\"MapContent\":{\"Id\":2,\"Type\":\"WirelessSocket\",\"TypeId\":2,\"Name\":\"MediaServer_Sleeping\",\"ShortName\":\"MsSlSo\",\"Area\":\"Sleeping_Room\",\"Visibility\":1,\"Position\":{\"Point\":{\"X\":30,\"Y\":3}}}}]} ";

        SerializableList<MapContent> mapContentList = JsonDataToMapContentConverter.getInstance().GetList(response,
                new SerializableList<>(), new SerializableList<>(), new SerializableList<>(), new SerializableList<>(), null, new SerializableList<>(), new SerializableList<>(), new SerializableList<>());

        assertNotNull(mapContentList);
        assertEquals(mapContentList.getSize(), 2);
        assertEquals(mapContentList.getValue(0).GetName(), "Light_Sleeping");
    }

    @Test
    public void JsonDataToMenuConverterTest() throws Exception {
        String response = "{\"Data\":[{\"Menu\":{\"Title\":\"-\",\"Description\":\"-\",\"Weekday\":\"monday\",\"Date\":{\"Day\":11,\"Month\":12,\"Year\":2017}}},{\"Menu\":{\"Title\":\"-\",\"Description\":\"-\",\"Weekday\":\"tuesday\",\"Date\":{\"Day\":12,\"Month\":12,\"Year\":2017}}},{\"Menu\":{\"Title\":\"-\",\"Description\":\"-\",\"Weekday\":\"wednesday\",\"Date\":{\"Day\":13,\"Month\":12,\"Year\":2017}}},{\"Menu\":{\"Title\":\"-\",\"Description\":\"-\",\"Weekday\":\"thursday\",\"Date\":{\"Day\":14,\"Month\":12,\"Year\":2017}}},{\"Menu\":{\"Title\":\"-\",\"Description\":\"-\",\"Weekday\":\"friday\",\"Date\":{\"Day\":15,\"Month\":12,\"Year\":2017}}},{\"Menu\":{\"Title\":\"-\",\"Description\":\"-\",\"Weekday\":\"saturday\",\"Date\":{\"Day\":16,\"Month\":12,\"Year\":2017}}},{\"Menu\":{\"Title\":\"-\",\"Description\":\"-\",\"Weekday\":\"sunday\",\"Date\":{\"Day\":17,\"Month\":12,\"Year\":2017}}}]} ";

        SerializableList<LucaMenu> menuList = JsonDataToMenuConverter.getInstance().GetList(response);

        assertNotNull(menuList);
        assertEquals(menuList.getSize(), 7);
        assertEquals(menuList.getValue(0).GetWeekday(), Weekday.MO);
    }

    @Test
    public void JsonDataToMeterDataConverterTest() throws Exception {
        String response = "{\"Data\":[{\"MeterData\":{\"Id\":\"0\",\"Type\":\"WaterCold\",\"TypeId\":\"0\",\"Date\":{\"Day\":1,\"Month\":12,\"Year\":2017},\"Time\":{\"Hour\":9,\"Minute\":32,},\"MeterId\":\"15.736078\",\"Area\":\"Bath\",\"Value\":12.386,\"ImageName\":\"IMG_20171201_093255.jpg\"}},{\"MeterData\":{\"Id\":\"1\",\"Type\":\"WaterWarm\",\"TypeId\":\"1\",\"Date\":{\"Day\":1,\"Month\":12,\"Year\":2017},\"Time\":{\"Hour\":9,\"Minute\":33,},\"MeterId\":\"16.096472\",\"Area\":\"Bath\",\"Value\":3.914,\"ImageName\":\"IMG_20171201_093302.jpg\"}},{\"MeterData\":{\"Id\":\"2\",\"Type\":\"Current\",\"TypeId\":\"2\",\"Date\":{\"Day\":1,\"Month\":12,\"Year\":2017},\"Time\":{\"Hour\":9,\"Minute\":34,},\"MeterId\":\"574480\",\"Area\":\"Hall\",\"Value\":16422.8,\"ImageName\":\"IMG_20171201_093458.jpg\"}},{\"MeterData\":{\"Id\":\"3\",\"Type\":\"Heating\",\"TypeId\":\"3\",\"Date\":{\"Day\":1,\"Month\":12,\"Year\":2017},\"Time\":{\"Hour\":9,\"Minute\":32,},\"MeterId\":\"EA910\",\"Area\":\"SleepingRoom\",\"Value\":12,\"ImageName\":\"IMG_20171201_093211.jpg\"}},{\"MeterData\":{\"Id\":\"4\",\"Type\":\"Heating\",\"TypeId\":\"4\",\"Date\":{\"Day\":1,\"Month\":12,\"Year\":2017},\"Time\":{\"Hour\":9,\"Minute\":32,},\"MeterId\":\"EB323\",\"Area\":\"SleepingRoom\",\"Value\":14,\"ImageName\":\"IMG_20171201_093219.jpg\"}},{\"MeterData\":{\"Id\":\"5\",\"Type\":\"Heating\",\"TypeId\":\"5\",\"Date\":{\"Day\":1,\"Month\":12,\"Year\":2017},\"Time\":{\"Hour\":9,\"Minute\":32,},\"MeterId\":\"CL075\",\"Area\":\"Bath\",\"Value\":9,\"ImageName\":\"IMG_20171201_093235.jpg\"}},{\"MeterData\":{\"Id\":\"6\",\"Type\":\"Heating\",\"TypeId\":\"6\",\"Date\":{\"Day\":1,\"Month\":12,\"Year\":2017},\"Time\":{\"Hour\":9,\"Minute\":31,},\"MeterId\":\"CO796\",\"Area\":\"LivingRoom\",\"Value\":11,\"ImageName\":\"IMG_20171201_093159.jpg\"}},{\"MeterData\":{\"Id\":\"7\",\"Type\":\"Heating\",\"TypeId\":\"7\",\"Date\":{\"Day\":1,\"Month\":12,\"Year\":2017},\"Time\":{\"Hour\":9,\"Minute\":31,},\"MeterId\":\"BT072\",\"Area\":\"Kitchen\",\"Value\":11.5,\"ImageName\":\"IMG_20171201_093150.jpg\"}},{\"MeterData\":{\"Id\":\"8\",\"Type\":\"WaterCold\",\"TypeId\":\"0\",\"Date\":{\"Day\":15,\"Month\":12,\"Year\":2017},\"Time\":{\"Hour\":10,\"Minute\":40,},\"MeterId\":\"15.736078\",\"Area\":\"Bath\",\"Value\":13.282,\"ImageName\":\"20171215_104041.jpg\"}},{\"MeterData\":{\"Id\":\"9\",\"Type\":\"WaterWarm\",\"TypeId\":\"1\",\"Date\":{\"Day\":15,\"Month\":12,\"Year\":2017},\"Time\":{\"Hour\":10,\"Minute\":40,},\"MeterId\":\"16.096472\",\"Area\":\"Bath\",\"Value\":4.159,\"ImageName\":\"20171215_104032.jpg\"}},{\"MeterData\":{\"Id\":\"10\",\"Type\":\"Current\",\"TypeId\":\"2\",\"Date\":{\"Day\":15,\"Month\":12,\"Year\":2017},\"Time\":{\"Hour\":10,\"Minute\":36,},\"MeterId\":\"574480\",\"Area\":\"Hall\",\"Value\":16451.9,\"ImageName\":\"20171215_103601.jpg\"}},{\"MeterData\":{\"Id\":\"11\",\"Type\":\"Heating\",\"TypeId\":\"3\",\"Date\":{\"Day\":15,\"Month\":12,\"Year\":2017},\"Time\":{\"Hour\":10,\"Minute\":41,},\"MeterId\":\"EA910\",\"Area\":\"SleepingRoom\",\"Value\":1,\"ImageName\":\"20171215_104103.jpg\"}},{\"MeterData\":{\"Id\":\"12\",\"Type\":\"Heating\",\"TypeId\":\"4\",\"Date\":{\"Day\":15,\"Month\":12,\"Year\":2017},\"Time\":{\"Hour\":10,\"Minute\":41,},\"MeterId\":\"EB323\",\"Area\":\"SleepingRoom\",\"Value\":1,\"ImageName\":\"20171215_104110.jpg\"}},{\"MeterData\":{\"Id\":\"13\",\"Type\":\"Heating\",\"TypeId\":\"5\",\"Date\":{\"Day\":15,\"Month\":12,\"Year\":2017},\"Time\":{\"Hour\":10,\"Minute\":41,},\"MeterId\":\"CL075\",\"Area\":\"Bath\",\"Value\":1,\"ImageName\":\"20171215_104124.jpg\"}},{\"MeterData\":{\"Id\":\"14\",\"Type\":\"Heating\",\"TypeId\":\"6\",\"Date\":{\"Day\":15,\"Month\":12,\"Year\":2017},\"Time\":{\"Hour\":10,\"Minute\":41,},\"MeterId\":\"CO796\",\"Area\":\"LivingRoom\",\"Value\":1,\"ImageName\":\"20171215_104133.jpg\"}},{\"MeterData\":{\"Id\":\"15\",\"Type\":\"Heating\",\"TypeId\":\"7\",\"Date\":{\"Day\":15,\"Month\":12,\"Year\":2017},\"Time\":{\"Hour\":10,\"Minute\":41,},\"MeterId\":\"BT072\",\"Area\":\"Kitchen\",\"Value\":1,\"ImageName\":\"20171215_104141.jpg\"}}]} ";

        SerializableList<MeterData> meterDataList = JsonDataToMeterDataConverter.getInstance().GetList(response);

        assertNotNull(meterDataList);
        assertEquals(meterDataList.getSize(), 16);
        assertEquals(meterDataList.getValue(0).GetType(), "WaterCold");
    }

    @Test
    public void JsonDataToMoneyMeterDataConverterTest() throws Exception {
        String response = "{\"Data\":[{\"MoneyMeterData\":{\"Id\":\"0\",\"TypeId\":\"0\",\"Bank\":\"Postbank\",\"Plan\":\"Girokonto\",\"Amount\":123.45,\"Unit\":\"EUR\",\"Date\":{\"Day\":18,\"Month\":12,\"Year\":2017},\"User\":\"Jonas\"}},{\"MoneyMeterData\":{\"Id\":\"1\",\"TypeId\":\"1\",\"Bank\":\"HypoVereinsbank\",\"Plan\":\"Tagesgeldkonto\",\"Amount\":43.21,\"Unit\":\"EUR\",\"Date\":{\"Day\":18,\"Month\":12,\"Year\":2017},\"User\":\"Jonas\"}},{\"MoneyMeterData\":{\"Id\":\"2\",\"TypeId\":\"2\",\"Bank\":\"DKB\",\"Plan\":\"Kreditkarte\",\"Amount\":111.11,\"Unit\":\"EUR\",\"Date\":{\"Day\":18,\"Month\":12,\"Year\":2017},\"User\":\"Jonas\"}}]} ";

        SerializableList<MoneyMeterData> moneyMeterDataList = JsonDataToMoneyMeterDataConverter.getInstance().GetList(response);

        assertNotNull(moneyMeterDataList);
        assertEquals(moneyMeterDataList.getSize(), 3);
        assertEquals(moneyMeterDataList.getValue(0).GetBank(), "Postbank");
        assertEquals(moneyMeterDataList.getValue(0).GetAmount(), 43.21);
    }

    @Test
    public void JsonDataToMovieConverterTest() throws Exception {
        String response = "{\"Data\":[{\"Movie\":{\"Id\":0,\"Title\":\"Fluch der Karibik III - Am Ende der Welt\",\"Genre\":\"- \",\"Description\":\"- \",\"Rating\":0,\"Watched\":0}},{\"Movie\":{\"Id\":1,\"Title\":\"Fluch der Karibik IV - Fremde Gezeiten\",\"Genre\":\"- \",\"Description\":\"- \",\"Rating\":0,\"Watched\":0}},{\"Movie\":{\"Id\":2,\"Title\":\"Forrest Gump\",\"Genre\":\"- \",\"Description\":\"- \",\"Rating\":0,\"Watched\":0}}]} ";

        SerializableList<Movie> movieList = JsonDataToMovieConverter.getInstance().GetList(response);

        assertNotNull(movieList);
        assertEquals(movieList.getSize(), 3);
        assertEquals(movieList.getValue(0).GetTitle(), "Fluch der Karibik III - Am Ende der Welt");
    }

    @Test
    public void JsonDataToPuckJsConverterTest() throws Exception {
        String response = "{\"Data\":[{\"PuckJs\":{\"Id\":0,\"Name\":\"Puck.js e4d\",\"Area\":\"Kitchen\",\"Mac\":\"D4:62:F7:46:E4:D3\"}},{\"PuckJs\":{\"Id\":1,\"Name\":\"Puck.js bf7\",\"Area\":\"Living_Room\",\"Mac\":\"E0:06:19:E9:BF:7E\"}},{\"PuckJs\":{\"Id\":2,\"Name\":\"Puck.js 448\",\"Area\":\"Hall\",\"Mac\":\"FB:0A:16:D5:44:88\"}},{\"PuckJs\":{\"Id\":3,\"Name\":\"Puck.js b8e\",\"Area\":\"Sleeping_Room\",\"Mac\":\"F4:E3:C5:E9\"}}]}";

        SerializableList<PuckJs> puckJsList = JsonDataToPuckJsConverter.getInstance().GetList(response);

        assertNotNull(puckJsList);
        assertEquals(puckJsList.getSize(), 4);
        assertEquals(puckJsList.getValue(0).GetArea(), "Kitchen");
    }

    @Test
    public void JsonDataToScheduleConverterTest() throws Exception {
        String response = "{\"Data\":[{\"Schedule\":{\"Id\":0,\"Name\":\"Enable_Sleep_Light\",\"Socket\":\"Light_Sleeping\",\"Gpio\":\"\",\"Switch\":\"\",\"Weekday\":0,\"Hour\":20,\"Minute\":33,\"Action\":1,\"IsTimer\":0,\"IsActive\":1}},{\"Schedule\":{\"Id\":1,\"Name\":\"Disable_Living\",\"Socket\":\"Light_Living\",\"Gpio\":\"\",\"Switch\":\"\",\"Weekday\":2,\"Hour\":11,\"Minute\":15,\"Action\":0,\"IsTimer\":1,\"IsActive\":1}}]} ";

        SerializableList<Schedule> scheduleList = JsonDataToScheduleConverter.getInstance().GetList(response, new SerializableList<>(), new SerializableList<>());

        assertNotNull(scheduleList);
        assertEquals(scheduleList.getSize(), 1);
        assertEquals(scheduleList.getValue(0).GetName(), "Enable_Sleep_Light");
    }

    @Test
    public void JsonDataToSecurityConverterTest() throws Exception {
        String response = "{\"MotionData\":{\"State\":\"OFF\",\"Control\":\"OFF\",\"URL\":\"192.168.178.25:8081\",\"Events\":[]}}";

        SerializableList<Security> securityList = JsonDataToSecurityConverter.getInstance().GetList(response);

        assertNotNull(securityList);
        assertEquals(securityList.getSize(), 1);
        assertTrue(securityList.getValue(0).IsCameraActive());
    }

    @Test
    public void JsonDataToShoppingListConverterTest() throws Exception {
        String response = "{\"Data\":[{\"ShoppingEntry\":{\"Id\":1,\"Name\":\"Mehl\",\"Group\":\"Baking\",\"Quantity\":2,\"Unit\":\"kg\"}},{\"ShoppingEntry\":{\"Id\":1,\"Name\":\"Apfel\",\"Group\":\"Fruit\",\"Quantity\":4,\"Unit\":\"-\"}}]} ";

        SerializableList<ShoppingEntry> shoppingList = JsonDataToShoppingListConverter.getInstance().GetList(response);

        assertNotNull(shoppingList);
        assertEquals(shoppingList.getSize(), 2);
        assertEquals(shoppingList.getValue(0).GetGroup(), ShoppingEntryGroup.BAKING);
    }

    @Test
    public void JsonDataToTemperatureConverterTest() throws Exception {
        String response = "{\"Temperature\":{\"Value\":17.562,\"Area\":\"Workspace_Jonas\",\"SensorPath\":\"/sys/bus/w1/devices/28-000006f437d1/w1_slave\",\"GraphPath\":\"192.168.178.25/cgi-bin/webgui.py\"}}";

        SerializableList<Temperature> temperatureList = JsonDataToTemperatureConverter.getInstance().GetList(response);

        assertNotNull(temperatureList);
        assertEquals(temperatureList.getSize(), 1);
        assertEquals(temperatureList.getValue(0).GetArea(), "Workspace_Jonas");
    }

    @Test
    public void JsonDataToTimerConverterTest() throws Exception {
        String response = "{\"Data\":[{\"Schedule\":{\"Id\":0,\"Name\":\"Enable_Sleep_Light\",\"Socket\":\"Light_Sleeping\",\"Gpio\":\"\",\"Switch\":\"\",\"Weekday\":0,\"Hour\":20,\"Minute\":33,\"Action\":1,\"IsTimer\":0,\"IsActive\":1}},{\"Schedule\":{\"Id\":1,\"Name\":\"Disable_Living\",\"Socket\":\"Light_Living\",\"Gpio\":\"\",\"Switch\":\"\",\"Weekday\":2,\"Hour\":11,\"Minute\":15,\"Action\":0,\"IsTimer\":1,\"IsActive\":1}}]} ";

        SerializableList<LucaTimer> timerList = JsonDataToTimerConverter.getInstance().GetList(response, new SerializableList<>(), new SerializableList<>());

        assertNotNull(timerList);
        assertEquals(timerList.getSize(), 1);
        assertEquals(timerList.getValue(0).GetName(), "Disable_Living");
    }

    @Test
    public void JsonDataToWirelessSocketConverterTest() throws Exception {
        String response = "{\"Data\":[{\"WirelessSocket\":{\"TypeId\":0,\"Name\":\"Light_Sleeping\",\"Area\":\"Sleeping_Room\",\"Code\":\"10101A\",\"State\":0,\"LastTrigger\":{\"Hour\":-1,\"Minute\":-1,\"Day\":-1,\"Month\":-1,\"Year\":-1,\"UserName\":\"N.A.\"}}},{\"WirelessSocket\":{\"TypeId\":1,\"Name\":\"MediaServer_Sleeping\",\"Area\":\"Sleeping_Room\",\"Code\":\"10101B\",\"State\":0,\"LastTrigger\":{\"Hour\":-1,\"Minute\":-1,\"Day\":-1,\"Month\":-1,\"Year\":-1,\"UserName\":\"N.A.\"}}},{\"WirelessSocket\":{\"TypeId\":2,\"Name\":\"MediaServer_Living\",\"Area\":\"Living_Room\",\"Code\":\"10101C\",\"State\":1,\"LastTrigger\":{\"Hour\":-1,\"Minute\":-1,\"Day\":-1,\"Month\":-1,\"Year\":-1,\"UserName\":\"N.A.\"}}}]} ";

        SerializableList<WirelessSocket> wirelessSocketList = JsonDataToWirelessSocketConverter.getInstance().GetList(response);

        assertNotNull(wirelessSocketList);
        assertEquals(wirelessSocketList.getSize(), 3);
        assertEquals(wirelessSocketList.getValue(0).GetName(), "Light_Sleeping");
    }

    @Test
    public void JsonDataToWirelessSwitchConverterTest() throws Exception {
        String response = "{\"Data\":[{\"WirelessSwitch\":{\"TypeId\":0,\"Name\":\"Light_Hall\",\"Area\":\"Hall\",\"RemoteId\":-1,\"KeyCode\":1,\"Action\":0,\"LastTrigger\":{\"Hour\":0,\"Minute\":0,\"Day\":1,\"Month\":1,\"Year\":1970,\"UserName\":\"NULL\"}}}]} ";

        SerializableList<WirelessSwitch> wirelessSwitchList = JsonDataToWirelessSwitchConverter.getInstance().GetList(response);

        assertNotNull(wirelessSwitchList);
        assertEquals(wirelessSwitchList.getSize(), 3);
        assertEquals(wirelessSwitchList.getValue(0).GetName(), "Light_Hall");
    }
}