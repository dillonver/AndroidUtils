package com.android.example.ui.test

import android.os.Build
import androidx.annotation.RequiresApi
import com.android.example.base.BaseBindingActivity
import com.android.example.databinding.ActivityTestBinding
import xyz.dcln.androidutils.utils.ClipboardUtils
import xyz.dcln.androidutils.utils.LogUtils


class ClipboardUtilsTestAct : BaseBindingActivity<ActivityTestBinding>() {
    override fun createBinding(): ActivityTestBinding {
        return ActivityTestBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun initListener() {
        super.initListener()
        viewBinding.tvTest3.setOnClickListener {

            ClipboardUtils.copyText("我是测试")
            LogUtils.i(ClipboardUtils.getText())
        }
        viewBinding.tvTest2.setOnClickListener {
            ClipboardUtils.copyText("我是测试2") {
                LogUtils.i(it)
                LogUtils.i(ClipboardUtils.getText())
            }

        }
        viewBinding.tvTest1.setOnClickListener {
            LogUtils.i(ClipboardUtils.getText())

        }


        viewBinding.tvCancel.setOnClickListener {

        }
    }

}

