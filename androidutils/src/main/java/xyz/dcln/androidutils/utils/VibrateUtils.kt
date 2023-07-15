package xyz.dcln.androidutils.utils

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission
import xyz.dcln.androidutils.AndroidUtils


/**
 * Vibration tool class for performing vibration operations.
 *
 * Usage:
 * - Vibrate with duration, interval, and count: VibrateUtils.vibrateWithDuration(duration, interval, count)
 * - Vibrate with a vibration effect: VibrateUtils.vibrate(effect)
 * - Vibrate with a vibration pattern: VibrateUtils.vibrate(pattern, repeat)
 * - Stop the current vibration operation: VibrateUtils.cancel()
 */
object VibrateUtils {

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AndroidUtils.withApplication { application ->
                (application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
            }
        } else {
            AndroidUtils.withApplication { application ->
                application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        }
    }

    /**
     * Executes a vibration operation with the given duration, interval, and count.
     *
     * @param duration The duration of each vibration in milliseconds.
     * @param interval The interval between each vibration in milliseconds.
     * @param count The number of times to vibrate.
     *
     * @throws SecurityException if the calling app does not have the VIBRATE permission.
     */
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun vibrate(duration: Long = 500L, interval: Long = 200L, count: Int = 1) {
        if (duration < 0 || interval < 0 || count < 0) {
            LogUtils.e(
                "VibrateUtils",
                "The duration, interval, and count must all be non-negative."
            )
            return
        }

        vibrator?.let {
            if (it.hasVibrator()) {
                val timings = LongArray(count * 2) { i ->
                    if (i % 2 == 0) {
                        duration
                    } else {
                        interval
                    }
                }

                val effect = VibrationEffect.createWaveform(timings, -1)
                it.vibrate(effect)
            }
        }
    }

    /**
     * Executes a vibration operation with the given vibration effect.
     *
     * @param effect The vibration effect to use.
     *
     * @throws SecurityException if the calling app does not have the VIBRATE permission.
     */
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun vibrate(effect: VibrationEffect) {
       // val effect = VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)

        vibrator?.let {
            if (it.hasVibrator()) {
                it.vibrate(effect)
            }
        }
    }

    /**
     * Executes a vibration operation with the given vibration pattern.
     *
     * @param pattern The vibration pattern to use.
     * @param repeat Whether to repeat the pattern.
     *
     * @throws SecurityException if the calling app does not have the VIBRATE permission.
     */
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun vibrate(pattern: LongArray, repeat: Boolean = false) {
        vibrator?.let {
            if (it.hasVibrator()) {
                val effect = VibrationEffect.createWaveform(pattern, if (repeat) 0 else -1)
                it.vibrate(effect)
            }
        }
    }

    /**
     * Stops the current vibration operation.
     */
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun cancel() {
        vibrator?.cancel()
    }
}
