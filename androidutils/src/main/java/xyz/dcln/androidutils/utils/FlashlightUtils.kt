package xyz.dcln.androidutils.utils

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager


/**
 * Description:
 * Author: Dillon
 * Date: 2023/7/9 3:12
 */
object FlashlightUtils {
    private val cameraManager: CameraManager by lazy {
        AppUtils.getApp().getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private val defaultCameraId: String = "0" // 默认相机ID

    fun isFlashlightEnable(): Boolean {
        return AppUtils.getApp().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    fun isFlashlightOn(): Boolean {
        return try {
            cameraManager.getCameraCharacteristics(getCameraId() ?: defaultCameraId).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            false
        }
    }

    fun setFlashlightStatus(status: Boolean) {
        if (isFlashlightEnable()) {
            val cameraId = getCameraId() ?: defaultCameraId
            try {
                cameraManager.setTorchMode(cameraId, status)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }

    private fun getCameraId(): String? {
        return try {
            for (id in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                if (characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true) {
                    return id
                }
            }
            null
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            null
        }
    }
}