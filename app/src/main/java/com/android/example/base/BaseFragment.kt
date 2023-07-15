package com.android.example.base


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import xyz.dcln.androidutils.utils.LogUtils

abstract class BaseFragment<T : ViewBinding> : Fragment() {

    protected abstract fun createBinding(): T

    private val _viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        createBinding()
    }

    protected val viewBinding: T
        get() = _viewBinding

    private var initCompleted = false // 是否完成了初始化
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return _viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!initCompleted) {
            initView()
            initListener()
            initNetData()
            initCompleted = true
        }
    }

    open fun initView() {}

    open fun initListener() {}

    open fun initNetData() {}


    // 当 Fragment 的可见性发生变化时调用
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        LogUtils.i("${this.javaClass.simpleName}: ${if (!hidden) "可见" else "不可见"}")
    }

    // 当 Fragment 可见并处于活动状态时调用
    override fun onResume() {
        super.onResume()
        LogUtils.i("onResume: ${this.javaClass.simpleName}")
    }

    override fun onPause() {
        super.onPause()
        LogUtils.i("onPause: ${this.javaClass.simpleName}")
    }

    // 当 Fragment 不再处于活动状态时调用
    override fun onStop() {
        super.onStop()
        LogUtils.i("onStop: ${this.javaClass.simpleName}")
    }

    // 当 Fragment 销毁时调用
    override fun onDestroy() {
        super.onDestroy()
        LogUtils.i("onDestroy: ${this.javaClass.simpleName}")
    }
}

