package com.android.example.ui.test

import android.Manifest
import android.annotation.SuppressLint
import com.android.example.base.BaseBindingActivity
import com.android.example.databinding.ActivityTestBinding
import xyz.dcln.androidutils.utils.GsonUtils
import xyz.dcln.androidutils.utils.LogUtils
import xyz.dcln.androidutils.utils.PermissionUtils.requestMultiPermissions
import xyz.dcln.androidutils.utils.ToastUtils.toastShort
import xyz.dcln.androidutils.utils.WifiUtils


class WifiUtilsTestAct : BaseBindingActivity<ActivityTestBinding>() {
    override fun createBinding(): ActivityTestBinding {
        return ActivityTestBinding.inflate(layoutInflater)
    }

    @SuppressLint("MissingPermission")
    override fun initListener() {
        super.initListener()
        viewBinding.tvTest3.setOnClickListener {
            LogUtils.i(WifiUtils.isWifiEnabled(this))
//            "BSSID":"68:77:24:cc:7d:9e","SSID":"HBY-5G"
//            "BSSID":"28:93:7d:36:a9:04","SSID":"HBY"
       //     "a4:39:b3:75:c7:01" home
        }

        viewBinding.tvTest2.setOnClickListener {
            WifiUtils.setWifiEnabled(this, !WifiUtils.isWifiEnabled(this))
        }

        viewBinding.tvTest1.setOnClickListener {
            requestMultiPermissions(
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                onAllGranted = {
                    WifiUtils.requestWifiScan(this@WifiUtilsTestAct,
                        onScanResultsAvailable = {
                            LogUtils.i(GsonUtils.toJson(it))
                        }, onError = {
                            LogUtils.i(it)
                        })
                },
                onPartialDenied = {
                    LogUtils.i("onPartialDenied")
                })


        }

        viewBinding.tvCancel.setOnClickListener {
            WifiUtils.isWithinWifiRange(this, "68:77:24:cc:7d:99", onResult = {
                toastShort(it.toString())
                LogUtils.i(it)
            }, onError = {
                LogUtils.i(it)
            })
            // LogUtils.i(GsonUtils.toJson(WifiUtils.getWifiManager(this)?.scanResults))
        }
    }

}

