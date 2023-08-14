package com.android.example.ui.test

import android.os.Build
import androidx.annotation.RequiresApi
import com.android.example.base.BaseBindingActivity
import com.android.example.databinding.ActivityTestBinding
import xyz.dcln.androidutils.utils.LogUtils
import xyz.dcln.androidutils.utils.RegexUtils
import xyz.dcln.androidutils.utils.ext.isDigit
import xyz.dcln.androidutils.utils.ext.isSymbol


class RegexUtilsTestAct : BaseBindingActivity<ActivityTestBinding>() {
    override fun createBinding(): ActivityTestBinding {
        return ActivityTestBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun initListener() {
        super.initListener()
        viewBinding.tvTest3.setOnClickListener {
            LogUtils.i(RegexUtils.isValidUrl("http://www.dcln.xyz"))
            LogUtils.i(RegexUtils.isValidUrl("https://www.dcln.xyz"))
            LogUtils.i(RegexUtils.isValidUrl("ftp://www.dcln.xyz"))
            LogUtils.i(RegexUtils.isValidUrl("dcln.xyz"))
            LogUtils.i(RegexUtils.isValidDate("2022-02-22"))
            LogUtils.i(RegexUtils.isValidDate("1111"))
            LogUtils.i(RegexUtils.isValidFloat("1111"))
            LogUtils.i(RegexUtils.isValidFloat("1111.111"))
            LogUtils.i(RegexUtils.isValidEmail("1111.111"))
            LogUtils.i(RegexUtils.isValidEmail("1304947@qq.com"))
            LogUtils.i(RegexUtils.isValidInteger("1304947@qq.com"))
            LogUtils.i(RegexUtils.isValidInteger("6666"))
            LogUtils.i(RegexUtils.isDigit("6666"),"6666".isDigit())
            LogUtils.i(RegexUtils.isSymbol("-;;"),"6666".isSymbol())
            LogUtils.i(RegexUtils.isLetter("-6666.8"))
            LogUtils.i(RegexUtils.isValidPositiveFloat("6666.7"))
            LogUtils.i(RegexUtils.isValidPositiveFloat("-6666.8"))


        }
        viewBinding.tvTest2.setOnClickListener {

        }

        viewBinding.tvTest1.setOnClickListener {

        }

        viewBinding.tvCancel.setOnClickListener {

        }
    }

}

