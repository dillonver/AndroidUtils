package com.android.example.ui.test

import android.view.Gravity
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
import xyz.dcln.androidutils.view.window.Floaty


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

            // FloatyUtils.cancelAll()
            // finish()

        }
        viewBinding.tvTest3.apply {
            text = "常规弹窗"
            setOnClickListener {
//                Floaty.with(this@FloatyTestActivity).apply {
//                    setContentView(R.layout.dialog_test_a)
//                    setText( R.id.tvAgree, "我是同意")
//                    setText( R.id.tvCancel, "我是取消")
//                    setGravity(Gravity.TOP or Gravity.CENTER)
//                    // 设置成可拖拽的
//                     setDraggable()
//                    // 设置显示时长
//                   // setDuration(1000)
//                    // 设置动画样式
//                    //setAnimStyle(android.R.style.Animation_Translucent)
//                    // 设置外层是否能被触摸
//                     setOutsideTouchable(true)
//                    // 设置窗口背景阴影强度
//                    //setBackgroundDimAmount(0.5f)
//
//
//                }.show()
//
//                return@setOnClickListener
                Floaty.create(AppUtils.getApp(), tag = myTag) {

                    this.setContentView(R.layout.dialog_test_a) {
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
                   //  setDuration(5*1000)
                    // setGravity(Gravity.TOP or Gravity.CENTER)
                    //setWidth(ScreenUtils.getScreenWidth()*3/4)
                    //setHeight(ScreenUtils.getScreenHeight()*3/4)
                    //setBackgroundDimAmount(0f)
                    setGravity(Gravity.START or Gravity.TOP)
                      setDraggable()
                    setOutsideTouchable(true)
                    setLifecycleListener(onShow = {
                        toastShort("onShow")
                    }, onHide = { toastShort("onHide") })

                }.showAsDropDown( viewBinding.layTest)
            }

        }

        viewBinding.tvTest2.apply {
            setOnClickListener {
                toastShort("12321321")
            }
        }

        viewBinding.tvTest1.apply {
            text = "复用弹窗2"
            setOnClickListener {
                LogUtils.i(Floaty.isShowing(myTag))
                val floaty = Floaty.getFloatyByTag(myTag)
                floaty?.contentView?.apply {
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

