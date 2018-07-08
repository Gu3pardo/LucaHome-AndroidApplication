package guepardoapps.lucahome.common.helper;

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

        String actualValue = ByteStringHelper.Companion.readStringFromCharArray(valueToConvert);

        assertEquals(expectedValue, actualValue);
    }
}