package guepardoapps.lucahome.common.controller

interface IVolumeController {
    fun getVolume(volumeType: Int): Int
    fun getMaxVolume(volumeType: Int): Int

    fun setVolume(volumeType: Int, volume: Int)
    fun increaseVolume(volumeType: Int)
    fun decreaseVolume(volumeType: Int)

    fun muteVolume(volumeType: Int)
    fun unMuteVolume(volumeType: Int)
}