package com.hhvvg.anytext.hook

import android.app.Activity
import android.app.AndroidAppHelper
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import com.hhvvg.anytext.utils.DEFAULT_SHARED_PREFERENCES_FILE_NAME
import com.hhvvg.anytext.utils.KEY_SHOW_TEXT_BORDER
import com.hhvvg.anytext.utils.PACKAGE_NAME
import com.hhvvg.anytext.wrapper.TextViewOnClickWrapper
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
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
        val packageName = p0.packageName
        // Don't hook itself
        if (packageName == PACKAGE_NAME) {
            return
        }
        val spInstance = XSharedPreferences(packageName, DEFAULT_SHARED_PREFERENCES_FILE_NAME)
        val appName = spInstance.getString(packageName, null)
        val appClazz: Class<Application> = if (appName != null) {
            try {
                XposedHelpers.findClass(appName, p0.classLoader) as Class<Application>
            } catch (e: Exception) {
                Application::class.java
            }
        } else {
            Application::class.java
        }
        val methodHook = ApplicationOnCreateMethodHook()
        val method = XposedHelpers.findMethodBestMatch(appClazz, "onCreate", arrayOf(), arrayOf())
        XposedBridge.hookMethod(method, methodHook)
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
            val spInstance = activity.getSharedPreferences(DEFAULT_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
            val showTextHighlight = spInstance.getBoolean(KEY_SHOW_TEXT_BORDER, false)
            val contentView = activity.window.decorView as ViewGroup
            iterateHook(contentView, showTextHighlight)
            contentView.viewTreeObserver.addOnGlobalLayoutListener {
                iterateHook(contentView, showTextHighlight)
            }
        }

        private fun iterateHook(viewGroup: ViewGroup, showHighlight: Boolean) {
            val children = viewGroup.children
            for (child in children) {
                if (child is ViewGroup) {
                    iterateHook(child, showHighlight)
                    continue
                }
                if (child !is TextView) {
                    continue
                }
                val info = XposedHelpers.callMethod(child, "getListenerInfo")
                val originListener =
                    XposedHelpers.getObjectField(info, "mOnClickListener")
                if (originListener == null) {
                    child.setOnClickListener(TextViewOnClickWrapper(null, child))
                } else if (originListener !is TextViewOnClickWrapper) {
                    // Not hooked
                    child.setOnClickListener(
                        TextViewOnClickWrapper(
                            originListener as View.OnClickListener,
                            child
                        )
                    )
                }
                // Set text highlight or not
                if (showHighlight) {
                    child.setTextColor(Color.RED)
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
