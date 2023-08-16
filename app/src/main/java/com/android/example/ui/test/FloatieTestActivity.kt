package com.android.example.ui.test

import androidx.core.view.isVisible
import com.android.example.R
import com.android.example.base.BaseBindingActivity
import com.android.example.databinding.ActivityTestBinding
import com.android.example.databinding.DialogTestABinding
import xyz.dcln.androidutils.utils.ActivityUtils.addTopActivityChangeListener
import xyz.dcln.androidutils.utils.AppUtils
import xyz.dcln.androidutils.utils.AppUtils.addAppStateListener
import xyz.dcln.androidutils.utils.LogUtils
import xyz.dcln.androidutils.utils.LogUtils.logI
import xyz.dcln.androidutils.utils.ToastUtils.toastShort
import xyz.dcln.androidutils.view.Floatie


class FloatieTestActivity : BaseBindingActivity<ActivityTestBinding>() {
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
            Floatie.cancelByTag("dillonTest")

           // Floatie.cancelAll()
           // finish()

        }
        viewBinding.tvTest3.apply {
            text = "常规弹窗"
            setOnClickListener {
                Floatie.create(this@FloatieTestActivity, tag = "dillonTest") {
                    setLifecycleListener(
                        onShow = {
                            LogUtils.i(getFloatTag(), tag = "Floatie")
                        }, onHide = {
                            LogUtils.i(getFloatTag(), tag = "Floatie")
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

                    //setDisplayDuration(5*1000L)
                    //setGravity(Gravity.TOP)
                    //setWidth(ScreenUtils.getScreenWidth()*3/4)
                    //setHeight(ScreenUtils.getScreenHeight()*3/4)
                    //setYOffset(100)
                    //setXOffset(100)
                    setBackgroundDimAmount(0f)
                    setDraggable(true)


                }.show()
            }

        }

        viewBinding.tvTest2.apply {
            text = "复用弹窗"
            val myTag = "tvTest2"
            val floatie = Floatie.create(AppUtils.getApp(), myTag, true) {
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
                setLifecycleListener(
                    onShow = {
                        LogUtils.i("onShow", tag = "Floatie")
                    }, onHide = {
                        LogUtils.i("onHide", tag = "Floatie")
                    })
                setPermissionExceptionCallback {
                    LogUtils.i(it.message)
                }
                setWindowExceptionCallback {
                    LogUtils.i(it.message)

                }
                //setDisplayDuration(5*1000L)
                //setGravity(Gravity.TOP)
                //setWidth(ScreenUtils.getScreenWidth()*3/4)
                //setHeight(ScreenUtils.getScreenHeight()*3/4)
                //setYOffset(100)
                //setXOffset(100)
                setBackgroundDimAmount(0.5f)
                setDraggable(true)
            }

            setOnClickListener {
                floatie.show()
            }
        }

        viewBinding.tvTest1.apply {
            text = "复用弹窗2"
            setOnClickListener {
                val myTag = "dillonTest"
                LogUtils.i(Floatie.isShowing(myTag))
                Floatie.create(this@FloatieTestActivity, myTag, true) {
                    setLifecycleListener(
                        onShow = {
                            LogUtils.i("onShow", tag = "Floatie")
                        }, onHide = {
                            LogUtils.i("onHide", tag = "Floatie")
                        })

                }
            }
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        //   BusUtils.unsubscribeByTag("TagOnly111")
    }
}

