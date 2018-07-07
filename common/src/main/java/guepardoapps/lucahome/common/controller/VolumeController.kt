package guepardoapps.lucahome.common.controller

import android.content.Context
import android.media.AudioManager

class VolumeController(context: Context) : IVolumeController {

    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // volumeType could be e.g. AudioManager.STREAM_MUSIC
    override fun getVolume(volumeType: Int): Int {
        return audioManager.getStreamVolume(volumeType)
    }

    // volumeType could be e.g. AudioManager.STREAM_MUSIC
    override fun getMaxVolume(volumeType: Int): Int {
        return audioManager.getStreamMaxVolume(volumeType)
    }

    // volumeType could be e.g. AudioManager.STREAM_MUSIC
    override fun setVolume(volumeType: Int, volume: Int) {
        when {
            volume < 0 -> audioManager.setStreamVolume(volumeType, 0, 0)
            volume > getMaxVolume(volumeType) -> audioManager.setStreamVolume(volumeType, getMaxVolume(volumeType), 0)
            else -> audioManager.setStreamVolume(volumeType, volume, 0)
        }
    }

    // volumeType could be e.g. AudioManager.STREAM_MUSIC
    override fun increaseVolume(volumeType: Int) {
        val volume = getVolume(volumeType)
        setVolume(volumeType, volume + 1)
    }

    // volumeType could be e.g. AudioManager.STREAM_MUSIC
    override fun decreaseVolume(volumeType: Int) {
        val volume = getVolume(volumeType)
        setVolume(volumeType, volume - 1)
    }

    // volumeType could be e.g. AudioManager.STREAM_MUSIC
    override fun muteVolume(volumeType: Int) {
        audioManager.adjustStreamVolume(volumeType, AudioManager.ADJUST_MUTE, 0)
    }

    // volumeType could be e.g. AudioManager.STREAM_MUSIC
    override fun unMuteVolume(volumeType: Int) {
        audioManager.adjustStreamVolume(volumeType, AudioManager.ADJUST_UNMUTE, 0)
    }
}