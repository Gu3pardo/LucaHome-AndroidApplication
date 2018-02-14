package guepardoapps.lucahome.common.utils;

import android.support.annotation.NonNull;

import java.util.Objects;

@SuppressWarnings({"unused", "WeakerAccess"})
public class StringHelper {
    public static int GetStringCount(
            @NonNull String stringToTest,
            @NonNull String charToFind) {
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {
            lastIndex = stringToTest.indexOf(charToFind, lastIndex);
            if (lastIndex != -1) {
                count++;
                lastIndex += charToFind.length();
            }
        }

        return count;
    }

    public static int[] GetCharPositions(
            @NonNull String stringToTest,
            char charToFind) {
        int count = GetStringCount(stringToTest, String.valueOf(charToFind));
        if (count <= 0) {
            return new int[]{};
        }

        int[] positions = new int[count];
        int foundIndex = 0;

        for (int charIndex = 0; charIndex < stringToTest.length(); charIndex++) {
            if (stringToTest.charAt(charIndex) == charToFind) {
                positions[foundIndex] = charIndex;
                foundIndex++;
            }
        }

        return positions;
    }

    public static boolean StringsAreEqual(@NonNull String[] stringArray) {
        boolean areEqual = true;

        int stringCount = stringArray.length;

        for (int index = 1; index < stringCount; index++) {
            areEqual &= Objects.equals(stringArray[index], stringArray[index - 1]);
        }

        return areEqual;
    }

    public static String SelectString(
            @NonNull String[] stringArray,
            @NonNull String stringToFound) {
        int stringCount = stringArray.length;

        for (int index = 1; index < stringCount; index++) {
            if (stringArray[index].contains(stringToFound)) {
                if (!stringArray[index].contains("Error")) {
                    return stringArray[index];
                }
            }
        }

        return "";
    }
}
