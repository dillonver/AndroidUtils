package com.android.example.ui.test

import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import com.android.example.R
import com.android.example.base.BaseBindingActivity
import com.android.example.databinding.ActivityTestBinding
import xyz.dcln.androidutils.utils.ActivityUtils
import xyz.dcln.androidutils.utils.ActivityUtils.addTopActivityChangeListener
import xyz.dcln.androidutils.utils.AppUtils.addAppStateListener
import xyz.dcln.androidutils.utils.LogUtils.logI
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
        viewBinding.etTest.isVisible = true
    }


    override fun initListener() {
        super.initListener()

        viewBinding.tvCancel.setOnClickListener {
          //  viewBinding.etTest.allowAllInput()
            return@setOnClickListener
            finish()

        }
        viewBinding.tvTest3.apply {
            text = "限制数字"
            setOnClickListener {
                viewBinding.etTest.text.clear()
//                viewBinding.etTest.limitNumbers(
//                    isMasked = false,
//                    allowDecimal = true,
//                    onValidInput = { valid, position ->
//                        toastShort("有效输入：$valid ,P：$position")
//
//                    },
//                    onInvalidInput = { invalid, position ->
//                        toastShort("无效输入：$invalid ,P：$position")
//                    })
            }
        }

        viewBinding.tvTest2.apply {
            text = "限制字母"
            setOnClickListener {
                viewBinding.etTest.text.clear()
//                viewBinding.etTest.limitLetters(
//                    isMasked = false,
//                    allowSymbols = true,
//                    onValidInput = { valid, position ->
//                        toastShort("有效输入：$valid ,P：$position")
//
//                    },
//                    onInvalidInput = { invalid, position ->
//                        toastShort("无效输入：$invalid ,P：$position")
//                    })
            }
        }

        viewBinding.tvTest1.apply {
            text = "限制字母和数字"
            setOnClickListener {
                viewBinding.etTest.text.clear()
//                viewBinding.etTest.limitNumbersAndLetters(onValidInput = { valid, position ->
//                    toastShort("有效输入：$valid ,P：$position")
//
//                },
//                    onInvalidInput = { invalid, position ->
//                        toastShort("无效输入：$invalid ,P：$position")
//                    })

            }
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        //   BusUtils.unsubscribeByTag("TagOnly111")
    }
}

