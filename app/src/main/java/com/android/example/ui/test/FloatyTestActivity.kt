package com.android.example.ui.test

import android.view.Gravity
import androidx.core.view.isVisible
import com.android.example.R
import com.android.example.base.BaseBindingActivity
import com.android.example.databinding.ActivityTestBinding
import com.android.example.databinding.DialogImEntranceBinding
import com.android.example.databinding.DialogTestABinding
import xyz.dcln.androidutils.utils.ActivityUtils.addTopActivityChangeListener
import xyz.dcln.androidutils.utils.AppUtils.addAppStateListener
import xyz.dcln.androidutils.utils.LogUtils
import xyz.dcln.androidutils.utils.LogUtils.logI
import xyz.dcln.androidutils.utils.ToastUtils.toastShort
import xyz.dcln.androidutils.utils.ext.setCornerRadiusAndBackground
import xyz.dcln.androidutils.view.dialog.DfDialog
import xyz.dcln.androidutils.view.floaty.Floaty


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
                Floaty.cancelByTag(myTag, false)

                Floaty.create(this@FloatyTestActivity, tag = myTag) {

                    this.setContentView(R.layout.dialog_im_entrance) {
                        val binding = DialogImEntranceBinding.bind(this)
                        binding.ivGoConversationList.apply {
                            setOnClickListener { toastShort("ivGoConversationList") }
                        }
//
                    }
                    // setAnimationStyle()
                    //  setDuration(5*1000)
                    // setGravity(Gravity.TOP or Gravity.CENTER)
//                     setWidth(ScreenUtils.getScreenWidth()*3/4)
//                      setHeight(ScreenUtils.getScreenHeight()*3/4)
//                    setBackgroundDimAmount(0.3f)
                    setGravity(Gravity.END or Gravity.BOTTOM)
                    setXOffset(50)
                    setYOffset(110)
                    setDraggable()
                    // setOutsideTouchable(true)
                    setLifecycleListener(
                        onShow = {
                            LogUtils.e("Floaty onShow：" + it.selfTag())
                        }, onHide = {
                            LogUtils.e("Floaty onHide：" + it.selfTag())
                        }
                    )

                }.show()
            }

        }

        viewBinding.tvTest2.apply {
            setOnClickListener {
                Floaty.create(this@FloatyTestActivity, tag = myTag + "888") {

                    this.setContentView(R.layout.dialog_im_entrance) {
                        val binding = DialogImEntranceBinding.bind(this)
                        binding.ivGoConversationList.apply {
                            text = "我是同意"
                            setOnClickListener { toastShort("ivGoConversationList") }
                        }
                    }

                    setGravity(Gravity.END or Gravity.BOTTOM)
                    setXOffset(50)
                    setYOffset(110)
                    setDraggable()
                    setLifecycleListener(
                        onShow = {
                            LogUtils.e("Floaty onShow：" + it.selfTag())
                        }, onHide = {
                            LogUtils.e("Floaty onHide" + it.selfTag())
                        }
                    )

                }.show()

            }
        }

        viewBinding.tvTest1.apply {
            text = "复用弹窗2"
            setOnClickListener {
                LogUtils.i(Floaty.isShowing(myTag))
                val floaty = Floaty.getFloatyByTag(myTag)
                floaty?.getFloatyContentView()?.apply {
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

