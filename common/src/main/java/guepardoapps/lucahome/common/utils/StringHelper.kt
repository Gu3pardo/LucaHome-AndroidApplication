package guepardoapps.lucahome.common.utils

import android.support.annotation.NonNull

class StringHelper {
    companion object {
        fun getStringCount(@NonNull stringToTest: String,
                           @NonNull stringToFind: String): Int {
            var lastIndex = 0
            var count = 0

            while (lastIndex != -1) {
                lastIndex = stringToTest.indexOf(stringToFind, lastIndex)
                if (lastIndex != -1) {
                    count++
                    lastIndex += stringToFind.length
                }
            }

            return count
        }

        fun getCharPositions(@NonNull stringToTest: String,
                             charToFind: Char): IntArray {
            val count = getStringCount(stringToTest, charToFind.toString())
            if (count <= 1) {
                return IntArray(0)
            }

            val positions = IntArray(count)
            var foundIndex = 0

            for (index in 0 until stringToTest.length step 1) {
                val charAtIndex = stringToTest[index]
                if (charAtIndex == charToFind) {
                    positions[foundIndex] = index
                    foundIndex++
                }
            }

            return positions
        }

        fun stringsAreEqual(@NonNull stringArray: Array<String>): Boolean {
            val stringCount = stringArray.size

            for (index in 0 until stringCount step 1) {
                for (testIndex in 0 until stringCount step 1) {
                    if (stringArray[index] != stringArray[testIndex]) {
                        return false
                    }
                }
            }

            return true
        }

        fun excludeAndSelectString(@NonNull stringArray: Array<String>,
                                   @NonNull stringToExclude: String,
                                   @NonNull stringToFind: String): String {
            val stringCount = stringArray.size

            for (index in 0 until stringCount step 1) {
                val stringToCheck = stringArray[index]

                if (!stringToCheck.contains(stringToExclude)
                        && stringToCheck.contains(stringToFind)) {
                    return stringToCheck
                }
            }

            return ""
        }
    }
}