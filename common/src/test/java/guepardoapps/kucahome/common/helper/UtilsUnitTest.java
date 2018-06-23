package guepardoapps.kucahome.common.helper;

import org.junit.Test;

import guepardoapps.lucahome.common.utils.ByteStringHelper;

import static org.junit.Assert.assertEquals;

public class UtilsUnitTest {
    @Test
    public void byteStringConversion_isCorrect() throws Exception {
        String expectedValue = "HelloWorld!";
        char[] valueToConvert = {
                4968, 6969, 7452,
                7452, 7659, 6003,
                7659, 7866, 7452,
                6900, 2277
        };

        String actualValue = ByteStringHelper.ReadStringFromCharArray(valueToConvert);

        assertEquals(expectedValue, actualValue);
    }
}