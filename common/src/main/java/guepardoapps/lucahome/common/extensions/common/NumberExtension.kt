package guepardoapps.lucahome.common.extensions.common

import java.util.*

/**
 * @param digits the numbers to show
 * @return returns a string with specified format and additional zeros
 */
fun Int.integerFormat(digits: Int): String {
    return String.format(Locale.getDefault(), "%0${digits}d", this)
}

/**
 * @return returns a boolean, true if int is not 0
 */
fun Int.toBoolean(): Boolean {
    return this != 0
}

/**
 * @param digits the numbers to show after separator, the decimals
 * @return returns a string with specified format and additional decimal zeros
 */
fun Double.doubleFormat(digits: Int): String {
    return String.format(Locale.getDefault(), "%.${digits}f", this)
}