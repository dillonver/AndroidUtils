package com.android.example.ui.test

import android.Manifest
import com.android.example.base.BaseBindingActivity
import com.android.example.databinding.ActivityPermissionSampleBinding
import xyz.dcln.androidutils.utils.GsonUtils
import xyz.dcln.androidutils.utils.LogUtils.logI
import xyz.dcln.androidutils.utils.PermissionUtils.requestDrawOverlays
import xyz.dcln.androidutils.utils.PermissionUtils.requestMultiPermissions
import xyz.dcln.androidutils.utils.PermissionUtils.requestNotifyPermission
import xyz.dcln.androidutils.utils.PermissionUtils.requestPackageUsageStatsPermission
import xyz.dcln.androidutils.utils.PermissionUtils.requestSinglePermission
import xyz.dcln.androidutils.utils.ToastUtils.toastLong
import xyz.dcln.androidutils.utils.ToastUtils.toastShort


class PermissionUtilsActivity : BaseBindingActivity<ActivityPermissionSampleBinding>() {
    override fun createBinding(): ActivityPermissionSampleBinding {
        return ActivityPermissionSampleBinding.inflate(layoutInflater)
    }

    override fun initListener() {
        super.initListener()
        viewBinding.tvTest6.setOnClickListener {
            requestNotifyPermission(
                autoReturn = false,
                onGranted = { toastShort("权限同意了") },
                onDenied = { toastLong("权限拒绝了") })

        }
        viewBinding.tvTest5.setOnClickListener {
            requestNotifyPermission(
                autoReturn = true,
                onGranted = { toastShort("权限同意了") },
                onDenied = { toastShort("权限拒绝了") })

        }
        viewBinding.tvTest4.setOnClickListener {
            requestDrawOverlays(
                autoReturn = false,
                onGranted = { toastShort("权限同意了") },
                onDenied = { toastShort("权限拒绝了") })

        }
        viewBinding.tvTest3.setOnClickListener {
            requestDrawOverlays(
                autoReturn = true,
                onGranted = { toastShort("权限同意了") },
                onDenied = { toastShort("权限拒绝了") })

        }
        viewBinding.tvTest2.setOnClickListener {
            requestMultiPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                onAllGranted = {
                    toastShort("全部同意")
                }, onAllDenied = {
                    toastShort("全部拒绝")
                }, onPartialDenied = {
                    toastShort("部分拒绝："+GsonUtils.toJson(it))
                }, onPartialGranted = {
                    toastShort("部分同意："+GsonUtils.toJson(it))
                }, onDeniedPermanently = {
                   // toastShort("至少一个权限永久拒绝")
                }, launchSettingsOnDeniedPermanently = false
            )

        }

        viewBinding.tvTest1.setOnClickListener {
            requestSinglePermission(Manifest.permission.ACCESS_FINE_LOCATION, onGranted = {
                toastShort("权限同意")
            }, onDenied = {
                toastShort("权限拒绝")
            })

        }



        viewBinding.tvCancel.setOnClickListener {
            requestPackageUsageStatsPermission(
                onGranted = {
                    logI("onGranted")
                },
                onDenied = {
                    logI("onDenied")
                },
            )
        }
    }

}

