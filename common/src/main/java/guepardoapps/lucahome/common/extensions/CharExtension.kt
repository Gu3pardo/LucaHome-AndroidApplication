package guepardoapps.lucahome.common.extensions

fun Char.div(divider: Int): Char {
    return (this.toInt() / divider).toChar()
}