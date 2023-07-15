package com.android.example.ui.test

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.android.example.base.BaseBindingActivity
import com.android.example.databinding.ActivityTestBinding
import xyz.dcln.androidutils.utils.DeviceUtils
import xyz.dcln.androidutils.utils.GsonUtils
import xyz.dcln.androidutils.utils.LogUtils
import xyz.dcln.androidutils.utils.NetworkUtils
import xyz.dcln.androidutils.utils.PermissionUtils.requestSinglePermission


class DeviceUtilsTestAct : BaseBindingActivity<ActivityTestBinding>() {
    override fun createBinding(): ActivityTestBinding {
        return ActivityTestBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()

    }


    @RequiresApi(Build.VERSION_CODES.R)
    override fun initListener() {
        super.initListener()
        viewBinding.tvTest3.setOnClickListener {
            LogUtils.i(DeviceUtils.getUniqueDeviceId())
            LogUtils.i(DeviceUtils.getAndroidID())
            LogUtils.i(DeviceUtils.getManufacturer())
            LogUtils.i(DeviceUtils.getModel())
            LogUtils.i(DeviceUtils.getSDKVersionCode())
            LogUtils.i(DeviceUtils.getSDKVersionName())
            LogUtils.i(GsonUtils.toJson(DeviceUtils.getABIs()))

            LogUtils.i(DeviceUtils.isDevelopmentSettingsEnabled())
            LogUtils.i(DeviceUtils.isSameDevice(DeviceUtils.getUniqueDeviceId()))

            LogUtils.i(DeviceUtils.isDeviceRooted())
            LogUtils.i(DeviceUtils.isEmulator())
            LogUtils.i(DeviceUtils.isTablet())
        }


    }

}

