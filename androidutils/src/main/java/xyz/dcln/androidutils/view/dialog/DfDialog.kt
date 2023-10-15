package xyz.dcln.androidutils.view.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

//DialogFragment实现，仅支持Activity级弹窗操作，未实现拖动
class DfDialog(private val context: FragmentActivity, private val tag: String) : DialogFragment() {
    private var contentView: View? = null
    private var dimAmount = 0.3f
    private var canceledOnTouchOutside = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setDimAmount(dimAmount) // 0-1之间，值越大表示越暗
        contentView?.let { dialog.setContentView(it) }
        dialog.setCanceledOnTouchOutside(canceledOnTouchOutside)
        return dialog
    }

    fun setContentView(view: View, initView: View.() -> Unit = {}) =
        apply { contentView = view.apply(initView) }

    fun setContentView(id: Int, initView: View.() -> Unit = {}): DfDialog {
        val view = LayoutInflater.from(context).inflate(id, null, false)
        return setContentView(view, initView)
    }

    fun show() {
        show(context.supportFragmentManager, tag)
    }

    fun hide() {
        dismiss()
    }


    fun setCanceledOnTouchOutside(canceledOnTouchOutside: Boolean): DfDialog {
        this.canceledOnTouchOutside = canceledOnTouchOutside
        return this
    }

    /**
     * 设置悬浮窗背景阴影强度
     *
     * @param amount        阴影强度值，填写 0 到 1 之间的值
     */
    fun setBackgroundDimAmount(amount: Float): DfDialog {
        require(amount in 0f..1f) { "amount must be a value between 0 and 1" }
        this.dimAmount = amount
        return this
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        instances.remove(tag)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
    }

    companion object {
        private val instances: ConcurrentHashMap<String, WeakReference<DfDialog>> =
            ConcurrentHashMap()

        fun create(
            context: FragmentActivity,
            tag: String? = null,
            init: DfDialog.() -> Unit
        ): DfDialog {
            val floatyTag = tag?.takeUnless { instances.containsKey(it) }
                ?: generateUniqueTag()
            return DfDialog(context, floatyTag).apply(init).also {
                instances[floatyTag] = WeakReference(it)
            }
        }

        private fun generateUniqueTag(): String {
            var newTag: String
            do {
                newTag = UUID.randomUUID().toString()
            } while (instances.containsKey(newTag))
            return newTag
        }

        fun getDfDialogByTag(tag: String): DfDialog? = instances[tag]?.get()


        fun cancelByTag(tag: String) {
            getDfDialogByTag(tag)?.hide()
        }


        fun cancelAll() {
            for (weakRef in instances.values) {
                weakRef.get()?.hide()
            }
            instances.clear()
        }

        fun getContentView(tag: String): View? {
            return getDfDialogByTag(tag)?.contentView
        }

        fun isShowing(tag: String): Boolean {
            return getDfDialogByTag(tag)?.isVisible == true
        }
    }

}


