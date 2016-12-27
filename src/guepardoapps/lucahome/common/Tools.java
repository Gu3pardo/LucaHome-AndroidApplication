package guepardoapps.lucahome.common;

public class Tools {
	public static int GetStringCount(String searchString, String findString) {
		int lastIndex = 0;
		int count = 0;

		while (lastIndex != -1) {
			lastIndex = searchString.indexOf(findString, lastIndex);
			if (lastIndex != -1) {
				count++;
				lastIndex += findString.length();
			}
		}

		return count;
	}

	public static boolean StringsAreEqual(String[] stringArray) {
		boolean areEqual = true;

		int stringCount = stringArray.length;

		for (int index = 1; index < stringCount; index++) {
			areEqual &= stringArray[index] == stringArray[index - 1];
		}

		return areEqual;
	}

	public static String SelectString(String[] stringArray, String stringToFound) {
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
