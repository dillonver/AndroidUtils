package com.android.example.ui

import android.os.Build
import androidx.annotation.RequiresApi
import com.android.example.base.BaseBindingActivity
import com.android.example.databinding.ActivityMainBinding
import com.android.example.ui.test.BarUtilsTestAct
import com.android.example.ui.test.ClipboardUtilsTestAct
import com.android.example.ui.test.CommonTestActivity
import com.android.example.ui.test.DeviceUtilsTestAct
import com.android.example.ui.test.KeyboardUtilsTestAct
import com.android.example.ui.test.NetworkUtilsTestAct
import com.android.example.ui.test.PermissionUtilsActivity
import com.android.example.ui.test.WifiTransferActivity
import xyz.dcln.androidutils.utils.ActivityUtils


class MainAct : BaseBindingActivity<ActivityMainBinding>() {
    override fun createBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun initListener() {
        super.initListener()
        viewBinding.tvCommonTest.setOnClickListener {
            ActivityUtils.startActivity<CommonTestActivity>()
        }

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
            ActivityUtils.startActivity<WifiTransferActivity>()
        }
        viewBinding.tvPermissionUtils.setOnClickListener {
            ActivityUtils.startActivity<PermissionUtilsActivity>()
        }
    }

}

