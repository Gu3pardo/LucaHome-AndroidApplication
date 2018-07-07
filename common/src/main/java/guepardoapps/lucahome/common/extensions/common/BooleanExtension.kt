package guepardoapps.lucahome.common.extensions.common

fun Boolean.toInteger(): Int {
    if (this) {
        return 1
    }
    return 0
}