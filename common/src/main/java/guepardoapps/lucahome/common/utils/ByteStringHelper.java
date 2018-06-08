package guepardoapps.lucahome.common.utils;

public class ByteStringHelper {
    /**
     * @param charArray the charArray  to convert to a string
     * @return returns a converted string from a char array
     */
    public static String ReadStringFromCharArray(char[] charArray) {
        for (int index = 0; index < charArray.length; index++) {
            charArray[index] /= 69;
        }

        return new String(charArray);
    }
}
