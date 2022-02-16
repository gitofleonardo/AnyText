package com.hhvvg.anytext.wrapper

import android.content.Context
import android.view.View
import android.widget.TextView
import com.hhvvg.anytext.ui.TextEditingDialog

const val IGNORE_HOOK = "IGNORE_HOOK"

class TextViewOnClickWrapper(private val originListener: View.OnClickListener?, private val originView: View) : View.OnClickListener {
    override fun onClick(v: View?) {
        if (v == null) {
            return
        }
        if (v !is TextView || v.tag == IGNORE_HOOK) {
            originListener?.onClick(v)
            return
        }
        openTextEditingDialog(v.context, v, originListener)
    }

    fun performOriginClick() {
        originListener?.onClick(originView)
    }

    private fun openTextEditingDialog(
        context: Context,
        textView: TextView,
        originListener: View.OnClickListener? = null
    ) {
        TextEditingDialog(context, textView, originListener).apply {
            this.show()
        }
    }
}
