package guepardoapps.lucahome.common.utils;

import guepardoapps.lucahome.common.extensions.div

class ByteStringHelper {

    companion object {

        /**
         * @param charArray the charArray  to convert to a string
         * @return returns a converted string from a char array
         */
        fun readStringFromCharArray(charArray: CharArray): String {
            for (index in 0..charArray.size) {
                charArray[index] = charArray[index].div(69)
            }

            return String(charArray)
        }
    }
}
