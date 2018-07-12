package guepardoapps.lucahome.common.utils

import android.support.annotation.NonNull

class StringHelper {
    companion object {
        fun getStringCount(@NonNull test: String, @NonNull find: String): Int {
            var lastIndex = 0
            var count = 0

            while (lastIndex != -1) {
                lastIndex = test.indexOf(find, lastIndex)
                if (lastIndex != -1) {
                    count++
                    lastIndex += find.length
                }
            }

            return count
        }

        fun getCharPositions(@NonNull test: String, char: Char): List<Int> {
            return test
                    .mapIndexed { index, c -> if (c == char) index else -1 }
                    .filter { x -> x != -1 }
        }

        fun stringsAreEqual(@NonNull array: Array<String>): Boolean {
            return array.all { x -> array.all { y -> x == y } }
        }

        fun excludeAndSelectString(@NonNull array: Array<String>, @NonNull exclude: String, @NonNull find: String): String? {
            return array.find { x -> !x.contains(exclude) && x.contains(find) }
        }
    }
}