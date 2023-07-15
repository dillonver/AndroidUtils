package xyz.dcln.androidutils.utils

import android.content.Context
import android.media.AudioManager
import android.os.Build
import xyz.dcln.androidutils.utils.AppUtils.getApp

object VolumeUtils {

    //重复获取系统服务可能会导致额外的性能损耗
    private val audioManager: AudioManager by lazy {
        getApp().getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    /**
     * 获取音量值。
     *
     * @param streamType 音频流类型。
     *   - [AudioManager.STREAM_VOICE_CALL]
     *   - [AudioManager.STREAM_SYSTEM]
     *   - [AudioManager.STREAM_RING]
     *   - [AudioManager.STREAM_MUSIC]
     *   - [AudioManager.STREAM_ALARM]
     *   - [AudioManager.STREAM_NOTIFICATION]
     *   - [AudioManager.STREAM_DTMF]
     *   - [AudioManager.STREAM_ACCESSIBILITY]
     * @return 音量值。
     */
    fun getVolume(streamType: Int): Int {
        return audioManager.getStreamVolume(streamType)
    }

    /**
     * 设置音量值。
     * 当参数 'volume' 的值大于媒体音量的最大值时，不会报错或抛出异常，而是将媒体音量设置为最大值。
     * 将参数 'volume' 的值设置为小于 0 的值将最小化媒体音量。
     *
     * @param streamType 音频流类型。
     *   - [AudioManager.STREAM_VOICE_CALL]
     *   - [AudioManager.STREAM_SYSTEM]
     *   - [AudioManager.STREAM_RING]
     *   - [AudioManager.STREAM_MUSIC]
     *   - [AudioManager.STREAM_ALARM]
     *   - [AudioManager.STREAM_NOTIFICATION]
     *   - [AudioManager.STREAM_DTMF]
     *   - [AudioManager.STREAM_ACCESSIBILITY]
     * @param volume 音量值。
     * @param flags 标志位。
     *   - [AudioManager.FLAG_SHOW_UI]
     *   - [AudioManager.FLAG_ALLOW_RINGER_MODES]
     *   - [AudioManager.FLAG_PLAY_SOUND]
     *   - [AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE]
     *   - [AudioManager.FLAG_VIBRATE]
     */
    fun setVolume(streamType: Int, volume: Int, flags: Int) {
        try {
            audioManager.setStreamVolume(streamType, volume, flags)
        } catch (ignore: SecurityException) {
            // 忽略异常
        }
    }

    /**
     * 获取最大音量值。
     *
     * @param streamType 音频流类型。
     *   - [AudioManager.STREAM_VOICE_CALL]
     *   - [AudioManager.STREAM_SYSTEM]
     *   - [AudioManager.STREAM_RING]
     *   - [AudioManager.STREAM_MUSIC]
     *   - [AudioManager.STREAM_ALARM]
     *   - [AudioManager.STREAM_NOTIFICATION]
     *   - [AudioManager.STREAM_DTMF]
     *   - [AudioManager.STREAM_ACCESSIBILITY]
     * @return 最大音量值。
     */
    fun getMaxVolume(streamType: Int): Int {
        return audioManager.getStreamMaxVolume(streamType)
    }

    /**
     * 获取最小音量值。
     *
     * @param streamType 音频流类型。
     *   - [AudioManager.STREAM_VOICE_CALL]
     *   - [AudioManager.STREAM_SYSTEM]
     *   - [AudioManager.STREAM_RING]
     *   - [AudioManager.STREAM_MUSIC]
     *   - [AudioManager.STREAM_ALARM]
     *   - [AudioManager.STREAM_NOTIFICATION]
     *   - [AudioManager.STREAM_DTMF]
     *   - [AudioManager.STREAM_ACCESSIBILITY]
     * @return 最小音量值。
     */
    fun getMinVolume(streamType: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            audioManager.getStreamMinVolume(streamType)
        } else 0
    }
}
