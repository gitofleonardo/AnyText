package com.hhvvg.anytext.hook

import android.app.Activity
import android.app.AndroidAppHelper
import android.app.Application
import android.content.SharedPreferences
import android.content.res.Resources
import android.content.res.XModuleResources
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import com.hhvvg.anytext.utils.DEFAULT_SHARED_PREFERENCES_FILE_NAME
import com.hhvvg.anytext.utils.KEY_SHOW_TEXT_BORDER
import com.hhvvg.anytext.utils.PACKAGE_NAME
import com.hhvvg.anytext.wrapper.TextViewOnClickWrapper
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage

class AnyHookLoaded : IXposedHookLoadPackage, IXposedHookZygoteInit {
    private val methodHook = PackageMethodHook()

    override fun handleLoadPackage(p0: XC_LoadPackage.LoadPackageParam?) {
        if (p0 == null) {
            return
        }
        // Don't hook itself
        if (p0.packageName == PACKAGE_NAME) {
            return
        }
        val appClazz = Application::class.java
        XposedHelpers.findAndHookMethod(
            appClazz,
            "onCreate",
            methodHook
        )
        XposedBridge.log("Hook package: ${p0.packageName}, application: ${appClazz.name}")
    }

    private class PackageMethodHook : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam?) {
            if (param == null) {
                return
            }
            val app = AndroidAppHelper.currentApplication()
            app.registerActivityLifecycleCallbacks(ActivityCallback())
            Toast.makeText(
                app.applicationContext,
                "App Hooked, app: ${AndroidAppHelper.currentApplication()::class.java.name}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private class ActivityCallback : Application.ActivityLifecycleCallbacks {
        private val spInstance: SharedPreferences =
            XSharedPreferences(PACKAGE_NAME, DEFAULT_SHARED_PREFERENCES_FILE_NAME)
        private val showBorder: Boolean
            get() = spInstance.getBoolean(KEY_SHOW_TEXT_BORDER, false)

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        }

        override fun onActivityPostCreated(activity: Activity, savedInstanceState: Bundle?) {
            val contentView = activity.window.decorView as ViewGroup
            iterateHook(contentView)
            contentView.viewTreeObserver.addOnGlobalLayoutListener {
                iterateHook(contentView)
            }
        }

        private fun iterateHook(viewGroup: ViewGroup) {
            val children = viewGroup.children
            for (child in children) {
                XposedBridge.log("View class: ${child.javaClass.name}")
                if (child is ViewGroup) {
                    iterateHook(child)
                    continue
                }
                if (child !is TextView) {
                    continue
                }
                val info = XposedHelpers.callMethod(child, "getListenerInfo")
                val originListener =
                    XposedHelpers.getObjectField(info, "mOnClickListener")
                if (showBorder) {
                    child.setBackgroundColor(Color.BLUE)
                    child.setTextColor(Color.RED)
                }
                if (originListener == null) {
                    child.setOnClickListener(TextViewOnClickWrapper(null))
                } else if (originListener !is TextViewOnClickWrapper) {
                    // Not hooked
                    child.setOnClickListener(TextViewOnClickWrapper(originListener as View.OnClickListener))
                }
                XposedBridge.log("Hooked one text view: ${child.javaClass.name}")
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

    override fun initZygote(p0: IXposedHookZygoteInit.StartupParam?) {
        if (p0 == null) {
            return
        }
        modulePath = p0.modulePath
        moduleRes = getModuleRes(modulePath)
    }

    companion object {
        lateinit var moduleRes: Resources
        lateinit var modulePath: String

        @JvmStatic
        fun getModuleRes(path: String): Resources {
            return XModuleResources.createInstance(path, null)
        }
    }
}
