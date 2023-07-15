package com.android.example.ui.test

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.android.example.base.BaseBindingActivity
import com.android.example.databinding.ActivityTestBinding
import xyz.dcln.androidutils.utils.GsonUtils
import xyz.dcln.androidutils.utils.LogUtils
import xyz.dcln.androidutils.utils.NetworkUtils
import xyz.dcln.androidutils.utils.PermissionUtils.requestSinglePermission


class NetworkUtilsTestAct : BaseBindingActivity<ActivityTestBinding>() {
    override fun createBinding(): ActivityTestBinding {
        return ActivityTestBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()

    }


    @RequiresApi(Build.VERSION_CODES.R)
    override fun initListener() {
        super.initListener()
        viewBinding.tvTest3.setOnClickListener {
            LogUtils.i(NetworkUtils.isConnected())
            LogUtils.i(NetworkUtils.getNetworkType())
            LogUtils.i(NetworkUtils.isConnectedAndAvailable())
        }

        viewBinding.tvTest2.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestSinglePermission(  Manifest.permission.ACCESS_FINE_LOCATION, {
                    LogUtils.i(NetworkUtils.getNearbyWifiList()?.let { GsonUtils.toJson(it) })
                })
                return@setOnClickListener
            }
            LogUtils.i(NetworkUtils.getNearbyWifiList()?.let { GsonUtils.toJson(it) })


        }

    }

}

