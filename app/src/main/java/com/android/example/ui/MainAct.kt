package com.android.example.ui

import android.os.Build
import androidx.annotation.RequiresApi
import com.android.example.base.BaseBindingActivity
import com.android.example.databinding.ActivityMainBinding
import com.android.example.ui.test.BarUtilsTestAct
import com.android.example.ui.test.ClipboardUtilsTestAct
import com.android.example.ui.test.CommonTestActivity
import com.android.example.ui.test.DeviceUtilsTestAct
import com.android.example.ui.test.FloatyTestActivity
import com.android.example.ui.test.KeyboardUtilsTestAct
import com.android.example.ui.test.NetworkUtilsTestAct
import com.android.example.ui.test.PermissionUtilsActivity
import com.android.example.ui.test.RegexUtilsTestAct
import com.android.example.ui.test.WifiTransferActivity
import com.android.example.ui.test.WifiUtilsTestAct
import xyz.dcln.androidutils.utils.ActivityUtils
import xyz.dcln.androidutils.utils.DeviceUtils
import xyz.dcln.androidutils.utils.LogUtils
import xyz.dcln.androidutils.utils.VibrateUtils


class MainAct : BaseBindingActivity<ActivityMainBinding>() {
    override fun createBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun initListener() {
        super.initListener()
        LogUtils.e("isHarmonyOS：" + DeviceUtils.isHarmonyOS())
        viewBinding.tvFloatieTest.setOnClickListener {
            ActivityUtils.startActivity<FloatyTestActivity>()
        }
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

        viewBinding.tvRegexUtils.setOnClickListener {
            ActivityUtils.startActivity<RegexUtilsTestAct>()

        }
        viewBinding.tvWifiTransferUtils.setOnClickListener {
            ActivityUtils.startActivity<WifiTransferActivity>()
        }
        viewBinding.tvPermissionUtils.setOnClickListener {
            ActivityUtils.startActivity<PermissionUtilsActivity>()
        }

        viewBinding.tvWifiUtilsTest.setOnClickListener {
            ActivityUtils.startActivity<WifiUtilsTestAct>()
        }

        viewBinding.tvVibrateUtilsTest.setOnClickListener {
            VibrateUtils.vibrate(duration = 300, interval = 300, count = 999)
        }
    }

}

