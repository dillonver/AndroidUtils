package com.android.example.ui.test

import com.android.example.R
import com.android.example.base.BaseBindingActivity
import com.android.example.databinding.ActivityTestBinding
import xyz.dcln.androidutils.utils.ActivityUtils
import xyz.dcln.androidutils.utils.ActivityUtils.addTopActivityChangeListener
import xyz.dcln.androidutils.utils.AppUtils.addAppStateListener
import xyz.dcln.androidutils.utils.BusUtils.sendEvent
import xyz.dcln.androidutils.utils.BusUtils.sendEventSticky
import xyz.dcln.androidutils.utils.LogUtils.logI


class TestActivityB : BaseBindingActivity<ActivityTestBinding>() {
    override fun createBinding(): ActivityTestBinding {
        return ActivityTestBinding.inflate(layoutInflater)
    }

    override fun initNetData() {
        super.initNetData()
        // receive<User> { logI(it) }
        addAppStateListener { logI("App isForeground:$it") }

        addTopActivityChangeListener { logI("TopActivity is:" + it?.javaClass?.simpleName) }


    }

    override fun initView() {
        super.initView()
        viewBinding.ivBg.setImageResource(R.drawable.img_act_bg4)


    }


    override fun initListener() {
        super.initListener()

        viewBinding.tvCancel.setOnClickListener {
            // ActivityUtils.finishActivity(TestActivityB::class.java)
            finish()

        }



        viewBinding.tvTest3.setOnClickListener {
            //ActivityUtils.finishActivitiesInRange { it is TestActivityB }
//            sendEvent(User("dillon", 18))
//            sendEvent(User("cindy", 15), true)
            sendEvent("tagInt", 8)
            sendEventSticky("tagIntSticky", 888)

        }

        viewBinding.tvTest2.setOnClickListener {

        }

        viewBinding.tvTest1.setOnClickListener {
            ActivityUtils.finishActivities()

        }


    }

    override fun onDestroy() {
        super.onDestroy()
        //   BusUtils.unsubscribeByTag("TagOnly111")
    }
}

