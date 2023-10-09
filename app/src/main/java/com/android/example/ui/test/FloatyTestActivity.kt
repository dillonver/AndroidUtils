package com.android.example.ui.test

import androidx.core.view.isVisible
import com.android.example.R
import com.android.example.base.BaseBindingActivity
import com.android.example.databinding.ActivityTestBinding
import com.android.example.databinding.DialogTestABinding
import xyz.dcln.androidutils.utils.ActivityUtils.addTopActivityChangeListener
import xyz.dcln.androidutils.utils.AppUtils.addAppStateListener
import xyz.dcln.androidutils.utils.LogUtils
import xyz.dcln.androidutils.utils.LogUtils.logI
import xyz.dcln.androidutils.utils.ToastUtils.toastShort
import xyz.dcln.androidutils.view.Floaty


class FloatyTestActivity : BaseBindingActivity<ActivityTestBinding>() {
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
        val myTag = "dillonTest"

        viewBinding.tvCancel.setOnClickListener {
            Floaty.cancelByTag(myTag)

            // Floaty.cancelAll()
            // finish()

        }
        viewBinding.tvTest3.apply {
            text = "常规弹窗"
            setOnClickListener {
                Floaty.create(this@FloatyTestActivity, tag = myTag, reuse = false) {
                    setLifecycleListener(
                        onShow = {
                            LogUtils.i(getFloatTag(), tag = "Floaty")
                        }, onHide = {
                            LogUtils.i(getFloatTag(), tag = "Floaty")
                        })

                    setContentView(R.layout.dialog_test_a) {
                        val binding = DialogTestABinding.bind(this)
                        binding.tvAgree.apply {
                            text = "我是同意"
                            setOnClickListener { toastShort("点击了同意") }
                        }
                        binding.tvCancel.apply {
                            text = "我是取消"
                            setOnClickListener { hide() }
                        }
                    }
                   // setAnimationStyle()
                    //setDisplayDuration(5*1000L)
                    //setGravity(Gravity.TOP)
                    //setWidth(ScreenUtils.getScreenWidth()*3/4)
                    //setHeight(ScreenUtils.getScreenHeight()*3/4)
                    //setYOffset(100)
                    //setXOffset(100)
                    //setBackgroundDimAmount(0f)
                    setDraggable(true)
                    setTouchThroughEnabled(true)
//                    setDismissOnOutsideClick(false)

                }.show()
            }

        }

        viewBinding.tvTest2.apply {
        }

        viewBinding.tvTest1.apply {
            text = "复用弹窗2"
            setOnClickListener {
                LogUtils.i(Floaty.isShowing(myTag))
                val floaty= Floaty.getFloatyByTag(myTag)
                floaty?.getContentView()?.apply {
                    val binding = DialogTestABinding.bind(this)
                    binding.tvAgree.apply {
                        text = "我不是同意"
                        setOnClickListener { toastShort("我不是同意") }
                    }
                    binding.tvCancel.apply {
                        text = "我不是取消"
                    }
                }
            }
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        //   BusUtils.unsubscribeByTag("TagOnly111")
    }
}

