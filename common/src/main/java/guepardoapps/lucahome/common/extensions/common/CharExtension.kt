package guepardoapps.lucahome.common.extensions.common

fun Char.div(divider: Int): Char {
    return (this.toInt() / divider).toChar()
}