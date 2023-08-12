package com.android.example.ui

import android.os.Build
import androidx.annotation.RequiresApi
import com.android.example.base.BaseBindingActivity
import com.android.example.databinding.ActivityMainBinding
import com.android.example.ui.test.BarUtilsTestAct
import com.android.example.ui.test.ClipboardUtilsTestAct
import com.android.example.ui.test.DeviceUtilsTestAct
import com.android.example.ui.test.KeyboardUtilsTestAct
import com.android.example.ui.test.LogUtils
import com.android.example.ui.test.NetworkUtilsTestAct
import com.android.example.ui.test.PermissionUtilsActivity
import com.android.example.ui.test.WifiTransferActivity
import xyz.dcln.androidutils.utils.ActivityUtils
import xyz.dcln.androidutils.utils.PermissionUtils


class MainAct : BaseBindingActivity<ActivityMainBinding>() {
    override fun createBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun initListener() {
        super.initListener()
        viewBinding.tvNetworkUtils.setOnClickListener {
            ActivityUtils.startActivity<NetworkUtilsTestAct>()
        }

        viewBinding.tvDeviceUtils.setOnClickListener {
            ActivityUtils.startActivity<DeviceUtilsTestAct>()
        }


        viewBinding.tvBarUtils.setOnClickListener {
            ActivityUtils.startActivity<BarUtilsTestAct>()

        }
        viewBinding.tvClipboardUtils.setOnClickListener {
            ActivityUtils.startActivity<ClipboardUtilsTestAct>()

        }
        viewBinding.tvKeyboardUtils.setOnClickListener {
            ActivityUtils.startActivity<KeyboardUtilsTestAct>()

        }
        viewBinding.tvWifiTransferUtils.setOnClickListener {
            //ActivityUtils.startActivity<WifiTransferActivity>()
            LogUtils.e(LogUtils.readLogsFromLocalCache(),tag = "LocalCache")
        }
        viewBinding.tvPermissionUtils.setOnClickListener {
           // ActivityUtils.startActivity<PermissionUtilsActivity>()
//            LogUtils.i("INFO", "Message 1", null, tag = "INFO", writeToCache = true)
//            LogUtils.d("DEBUG", 3, "Message 4", tag = "DEBUG")
//            LogUtils.v("VERBOSE", "Message 5", 6, tag = "VERBOSE", writeToCache = true)
//            LogUtils.e("ERROR", 7, 8.1, tag = "ERROR")
           LogUtils.deleteLogsFromLocalCache()

        }
    }

}

