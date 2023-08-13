package com.android.example.base

import android.os.Bundle
import androidx.viewbinding.ViewBinding


abstract class BaseBindingActivity<T : ViewBinding> : BaseActivity() {

    private val _viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        createBinding()
    }

    protected val viewBinding: T
        get() = _viewBinding

    protected abstract fun createBinding(): T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        initView()
        initListener()
        initNetData()
    }

    protected open fun initView() {}

    protected open fun initListener() {}

    protected open fun initNetData() {}

}

