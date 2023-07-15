package com.android.example.ui.test

import android.annotation.SuppressLint
import com.android.example.base.BaseBindingActivity
import com.android.example.databinding.ActivityWifiTransferBinding
import xyz.dcln.androidutils.utils.CoroutineUtils
import xyz.dcln.androidutils.utils.CoroutineUtils.launchOnUI
import xyz.dcln.androidutils.utils.LogUtils
import xyz.dcln.androidutils.utils.NetworkUtils
import xyz.dcln.androidutils.utils.ToastUtils.toastShort
import xyz.dcln.androidutils.utils.WiFiTransferUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class WifiTransferActivity : BaseBindingActivity<ActivityWifiTransferBinding>() {
    override fun createBinding(): ActivityWifiTransferBinding {
        return ActivityWifiTransferBinding.inflate(layoutInflater)
    }

    private val myIp by lazy {
        NetworkUtils.getWifiIpAddress()
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        super.initView()
        viewBinding.tvMyIp.text = "我的IP：$myIp"
    }

    @SuppressLint("SetTextI18n")
    override fun initListener() {
        super.initListener()

        viewBinding.tvStartListen.setOnClickListener {
            val receivePortStr = viewBinding.etListenPort.text.toString()
            if (receivePortStr.isBlank()) {
                toastShort("端口填写错误")
                return@setOnClickListener
            }
            val receivePort = receivePortStr.toIntOrNull()
            if (receivePort == null || receivePort !in 0..65535) {
                toastShort("端口填写错误")
                return@setOnClickListener
            }

            WiFiTransferUtils.startListening(receivePort, {
                CoroutineUtils.launchOnUI {
                    toastShort("监听启动成功")
                    viewBinding.tvResult.text = "--监听启动成功--\n"
                }
            }, { message, ipAddress ->
                CoroutineUtils.launchOnUI {
                    LogUtils.i("接收到来自 $ipAddress 的消息：$message")
                    viewBinding.tvResult.append("\n${timestampToCalendarString(System.currentTimeMillis())}\n$ipAddress: $message\n")
                }
            }, { error ->
                CoroutineUtils.launchOnUI {
                    LogUtils.e("发生错误：$error")
                    viewBinding.tvResult.append("\n${timestampToCalendarString(System.currentTimeMillis())}\n$error")
                }
            })
        }

        viewBinding.tvSend.setOnClickListener {
            val targetPortStr = viewBinding.etTargetPort.text.toString()
            val targetIp = viewBinding.etTargetIp.text.toString()
            val message = viewBinding.etMsg.text.toString()
            if (targetPortStr.isBlank()) {
                toastShort("端口填写错误")
                return@setOnClickListener
            }
            val targetPort = targetPortStr.toIntOrNull()
            if (targetPort == null || targetPort !in 0..65535) {
                toastShort("端口错误")
                return@setOnClickListener
            }
            if (targetIp.isBlank()) {
                toastShort("请填写对方的 IP")
                return@setOnClickListener
            }
            if (message.isBlank()) {
                toastShort("请填写你要发送的信息")
                return@setOnClickListener
            }

            WiFiTransferUtils.sendMessage(targetIp, targetPort, message) { success ->
                CoroutineUtils.launchOnUI {
                    if (success) {
                        LogUtils.i("消息发送成功！")
                        viewBinding.tvResult.append("\n${timestampToCalendarString(System.currentTimeMillis())}\n$myIp: $message\n")
                        viewBinding.etMsg.setText("")
                        viewBinding.etMsg.clearFocus()
                    } else {
                        LogUtils.i("消息发送失败！")
                        toastShort("消息发送失败！")
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        WiFiTransferUtils.stopListening()
    }

    fun timestampToCalendarString(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp

        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return format.format(calendar.time)
    }
}

