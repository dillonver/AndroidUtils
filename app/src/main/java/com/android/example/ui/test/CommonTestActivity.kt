package com.android.example.ui.test

import android.widget.Toast
import androidx.core.view.isVisible
import com.android.example.R
import com.android.example.base.BaseBindingActivity
import com.android.example.databinding.ActivityTestBinding
import xyz.dcln.androidutils.utils.ActivityUtils.addTopActivityChangeListener
import xyz.dcln.androidutils.utils.AppUtils
import xyz.dcln.androidutils.utils.AppUtils.addAppStateListener
import xyz.dcln.androidutils.utils.CoroutineUtils
import xyz.dcln.androidutils.utils.LogUtils
import xyz.dcln.androidutils.utils.LogUtils.logI
import xyz.dcln.androidutils.utils.PathUtils
import xyz.dcln.androidutils.utils.ToastUtils
import xyz.dcln.androidutils.utils.ToastUtils.toastLong
import xyz.dcln.androidutils.utils.ToastUtils.toastShort


class CommonTestActivity : BaseBindingActivity<ActivityTestBinding>() {
    override fun createBinding(): ActivityTestBinding {
        return ActivityTestBinding.inflate(layoutInflater)
    }

    override fun initNetData() {
        super.initNetData()
        // receive<User> { logI(it) }
        addAppStateListener { logI("App isForeground:$it") }

        addTopActivityChangeListener { logI("TopActivity is:" + it.javaClass.simpleName) }


    }

    override fun initView() {
        super.initView()
        viewBinding.ivBg.setImageResource(R.drawable.img_act_bg4)
        viewBinding.etTest.isVisible = false
    }


    override fun initListener() {
        super.initListener()

        viewBinding.tvTest3.apply {
            text = "toastShort"
            setOnClickListener {
                toastShort( "12312")
                finish()

            }

        }
        viewBinding.tvTest2.apply {
            text = "toastLong"
            setOnClickListener {
                toastLong("21322131")
                finish()
            }

        }

        viewBinding.tvTest1.apply {
            text = "showToast"
            setOnClickListener {
                //CoroutineUtils.launchOnUI {
                    ToastUtils.showToast(AppUtils.getApp()) {
                        msg = "0"
                        cancelCurrent = true
                    }

               // }
                finish()
            }

        }

        viewBinding.tvCancel.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //   BusUtils.unsubscribeByTag("TagOnly111")
    }
}

