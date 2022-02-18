package com.hhvvg.anytext.utils

import android.view.View
import de.robv.android.xposed.XposedHelpers

fun hookViewListener(view: View, replacedListenerCallback: (originListener: View.OnClickListener?) -> View.OnClickListener?) {
    val info = XposedHelpers.callMethod(view, "getListenerInfo")
    val originListener = XposedHelpers.getObjectField(info, "mOnClickListener") as View.OnClickListener?
    val replacedListener = replacedListenerCallback.invoke(originListener)
    XposedHelpers.setObjectField(info, "mOnClickListener", replacedListener)
}
