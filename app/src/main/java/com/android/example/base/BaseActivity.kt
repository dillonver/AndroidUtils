package com.android.example.base

import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import xyz.dcln.androidutils.utils.ActivityUtils.addLifecycleObserver
import xyz.dcln.androidutils.utils.LogUtils

abstract class BaseActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preSetContentView()
        addLifecycleObserver(
            onCreate = { LogUtils.i("onCreate:" + this.javaClass.simpleName) },
            onResume = { LogUtils.i("onResume:" + this.javaClass.simpleName) },
            onPause = { LogUtils.i("onPause:" + this.javaClass.simpleName) },
            onStart = { LogUtils.i("onStart:" + this.javaClass.simpleName) },
            onStop = { LogUtils.i("onStop:" + this.javaClass.simpleName) },
            onDestroy = { LogUtils.i("onDestroy:" + this.javaClass.simpleName) }

        )
    }

    open fun preSetContentView() {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
    }

}
