package guepardoapps.lucahome.common.utils;

public class ByteStringHelper {
    public static String ReadStringFromCharArray(char[] charArray) {
        for (int index = 0; index < charArray.length; index++) {
            charArray[index] /= 69;
        }

        return new String(charArray);
    }
}
