package com.hhvvg.anytext.utils

import android.app.Application
import android.content.Context
import android.widget.TextView
import de.robv.android.xposed.XposedHelpers

fun dp2px(context: Context, dp: Float): Float {
    val scale = context.resources.displayMetrics.density
    return dp * scale + 0.5f
}

fun px2dp(context: Context, px: Float): Float {
    val scale = context.resources.displayMetrics.density
    return px / scale + 0.5f
}

fun highlightText(textView: TextView) {
    appPropertyInject(textView.context.applicationContext as Application, APP_HIGHLIGHT_FIELD_NAME, true)
    textView.text = textView.text
}

fun clearTextHighlight(textView: TextView) {
    appPropertyInject(textView.context.applicationContext as Application, APP_HIGHLIGHT_FIELD_NAME, false)
    textView.text = textView.text.toString()
}

fun appPropertyInject(app: Application, fieldName: String, value: Any) {
    XposedHelpers.setAdditionalInstanceField(app, fieldName, value)
}

fun <T> getAppInjectedProperty(app: Application, name: String): T {
    return XposedHelpers.getAdditionalInstanceField(app, name) as T
}
