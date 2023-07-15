package com.android.example.ui.test

import android.graphics.Color
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.android.example.base.BaseBindingActivity
import com.android.example.databinding.ActivityTestBinding
import xyz.dcln.androidutils.utils.BarUtils
import xyz.dcln.androidutils.utils.ColorUtils
import xyz.dcln.androidutils.utils.LogUtils


class BarUtilsTestAct : BaseBindingActivity<ActivityTestBinding>() {
    override fun createBinding(): ActivityTestBinding {
        return ActivityTestBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun initListener() {
        super.initListener()
        viewBinding.tvTest3.setOnClickListener {
            LogUtils.i(BarUtils.getStatusBarHeight())
            LogUtils.i(BarUtils.getActionBarHeight(this))
            LogUtils.i(BarUtils.getNavBarHeight())

            LogUtils.i(BarUtils.isStatusBarVisible(this))
            LogUtils.i(BarUtils.isStatusBarLightMode(this))

            LogUtils.i(BarUtils.isNavBarVisible(this))
            LogUtils.i(BarUtils.isSupportNavBar())
            LogUtils.i(BarUtils.isNavBarLightMode(this))

        }
        viewBinding.tvTest2.setOnClickListener {
            BarUtils.setNavBarLightMode(
                this,
                !BarUtils.isNavBarLightMode(this)
            )
        }
        viewBinding.tvTest1.setOnClickListener {
            BarUtils.setNavBarVisibility(this,true)
        }


        viewBinding.tvCancel.setOnClickListener {
            BarUtils.setNavBarVisibility(this,false)

        }
    }

}

