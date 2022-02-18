package com.hhvvg.anytext.hook

import android.app.Activity
import android.app.AndroidAppHelper
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import com.hhvvg.anytext.utils.APP_HIGHLIGHT_FIELD_NAME
import com.hhvvg.anytext.utils.DEFAULT_SHARED_PREFERENCES_FILE_NAME
import com.hhvvg.anytext.utils.KEY_SHOW_TEXT_BORDER
import com.hhvvg.anytext.utils.PACKAGE_NAME
import com.hhvvg.anytext.utils.appPropertyInject
import com.hhvvg.anytext.utils.getAppInjectedProperty
import com.hhvvg.anytext.utils.hookViewListener
import com.hhvvg.anytext.wrapper.IGNORE_HOOK
import com.hhvvg.anytext.wrapper.TextViewOnClickWrapper
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Class for package hook.
 * @author hhvvg
 */
class AnyHookLoaded : IXposedHookLoadPackage {
    override fun handleLoadPackage(p0: XC_LoadPackage.LoadPackageParam?) {
        if (p0 == null) {
            return
        }

        // Don't hook itself
        val packageName = p0.packageName
        if (packageName == PACKAGE_NAME) {
            return
        }

        // Hook onCreate method in Application
        val appClazz: Class<Application> = Application::class.java
        val methodHook = ApplicationOnCreateMethodHook()
        val method = XposedHelpers.findMethodBestMatch(appClazz, "onCreate", arrayOf(), arrayOf())
        XposedBridge.hookMethod(method, methodHook)

        // Hook setOnClickListener in TextView
        val clickMethodHook = TextViewOnClickMethodHook()
        val clickMethod = XposedHelpers.findMethodBestMatch(
            TextView::class.java,
            "setOnClickListener",
            View.OnClickListener::class.java
        )
        XposedBridge.hookMethod(clickMethod, clickMethodHook)

        // Hook setText to make text highlighted
        val setTextHook = TextViewSetTextMethodHook()
        val setTextMethod2Arg = XposedHelpers.findMethodBestMatch(
            TextView::class.java,
            "setText",
            CharSequence::class.java,
            TextView.BufferType::class.java
        )
        XposedBridge.hookMethod(setTextMethod2Arg, setTextHook)
    }

    private class TextViewSetTextMethodHook : XC_MethodHook() {

        override fun beforeHookedMethod(param: MethodHookParam?) {
            if (param == null) {
                return
            }
            val textView = param.thisObject
            if (textView !is TextView || textView.tag == IGNORE_HOOK) {
                return
            }
            val textArg = (param.args[0] ?: return).toString()
            val showHighlight = getAppInjectedProperty<Boolean>(
                textView.context.applicationContext as Application,
                APP_HIGHLIGHT_FIELD_NAME
            )
            if (showHighlight) {
                val spanString = SpannableString(textArg)
                spanString.setSpan(
                    BackgroundColorSpan(Color.RED),
                    0,
                    textArg.length,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
                param.args[0] = spanString
            }
        }
    }

    private class TextViewOnClickMethodHook : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam?) {
            // After calling setOnClickListener, our wrapper listener will be replaced
            // So hook again to ensure the click listener is always our wrapper one
            if (param == null) {
                return
            }
            val tv = param.thisObject
            if (tv !is TextView) {
                return
            }
            tv.isClickable = true
            hookViewListener(tv) { originListener ->
                if (originListener is TextViewOnClickWrapper) {
                    originListener
                } else {
                    TextViewOnClickWrapper(originListener, tv)
                }
            }
        }
    }

    private class ApplicationOnCreateMethodHook : XC_MethodHook() {

        override fun afterHookedMethod(param: MethodHookParam?) {
            if (param == null) {
                return
            }
            val app = AndroidAppHelper.currentApplication()
            val appName = AndroidAppHelper.currentApplication()::class.java.name
            val packageName = AndroidAppHelper.currentApplicationInfo().packageName
            val sp = app.applicationContext.getSharedPreferences(
                DEFAULT_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE
            )
            // Update application class name
            updateApplicationClassName(sp, packageName, appName)
            hookLifecycleCallback(app, ActivityCallback())

            // Add global highlight property
            appPropertyInject(app, APP_HIGHLIGHT_FIELD_NAME, sp.getBoolean(KEY_SHOW_TEXT_BORDER, false))
        }

        private fun hookLifecycleCallback(
            app: Application,
            callback: Application.ActivityLifecycleCallbacks
        ) {
            val clazz = app::class.java
            val callbackField = XposedHelpers.findField(clazz, "mActivityLifecycleCallbacks")
            val callbackArray =
                callbackField.get(app) as ArrayList<Application.ActivityLifecycleCallbacks>
            callbackArray.add(callback)
        }

        private fun updateApplicationClassName(
            sp: SharedPreferences,
            packageNameAsKey: String,
            name: String
        ) {
            val edit = sp.edit()
            edit.putString(packageNameAsKey, name)
            edit.apply()
        }
    }

    private class ActivityCallback : Application.ActivityLifecycleCallbacks {

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        }

        override fun onActivityPostCreated(activity: Activity, savedInstanceState: Bundle?) {
            val contentView = activity.window.decorView as ViewGroup
            dfsHookTextView(contentView)
            contentView.viewTreeObserver.addOnGlobalLayoutListener {
                dfsHookTextView(contentView)
            }
        }

        private fun dfsHookTextView(viewGroup: ViewGroup) {
            val children = viewGroup.children
            for (child in children) {
                if (child is ViewGroup) {
                    dfsHookTextView(child)
                    continue
                }
                if (child !is TextView) {
                    continue
                }
                child.isClickable = true
                hookViewListener(child) { originListener ->
                    if (originListener is TextViewOnClickWrapper) {
                        originListener
                    } else {
                        TextViewOnClickWrapper(originListener, child)
                    }
                }
            }
        }

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
        }

        override fun onActivityPaused(activity: Activity) {
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        }

        override fun onActivityDestroyed(activity: Activity) {
        }
    }
}
