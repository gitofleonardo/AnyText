package com.hhvvg.anytext.hook

import android.app.Activity
import android.app.AndroidAppHelper
import android.app.Application
import android.content.res.Resources
import android.content.res.XModuleResources
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import com.hhvvg.anytext.wrapper.TextViewOnClickWrapper
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage

class AnyHookLoaded : IXposedHookLoadPackage, IXposedHookZygoteInit {
    private val methodHook = object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam?) {
            if (param == null) {
                return
            }
            val app = AndroidAppHelper.currentApplication()
            app.registerActivityLifecycleCallbacks(ActivityCallback())
            Toast.makeText(app.applicationContext, "App Hooked", Toast.LENGTH_SHORT).show()
        }
    }

    override fun handleLoadPackage(p0: XC_LoadPackage.LoadPackageParam?) {
        if (p0 == null) {
            return
        }
        val appClazz = Application::class.java
        XposedHelpers.findAndHookMethod(
            appClazz,
            "onCreate",
            methodHook
        )
        XposedBridge.log("Hook package: ${p0.packageName}")
    }

    private class ActivityCallback : Application.ActivityLifecycleCallbacks {

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        }

        override fun onActivityPostCreated(activity: Activity, savedInstanceState: Bundle?) {
            XposedBridge.log("Activity created: ${activity::class.java.name}")
            val contentView = activity.window.decorView as ViewGroup
            iterateHook(contentView)
            contentView.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
                override fun onChildViewAdded(parent: View?, child: View?) {
                    if (child == null) {
                        return
                    }
                    if (child is ViewGroup) {
                        iterateHook(child)
                        return
                    }
                    if (child is TextView) {
                        child.setBackgroundColor(Color.BLUE)
                        child.setTextColor(Color.RED)
                        child.setOnClickListener(TextViewOnClickWrapper(null))
                    }
                }

                override fun onChildViewRemoved(parent: View?, child: View?) {
                }
            })
        }

        private fun iterateHook(viewGroup: ViewGroup) {
            val children = viewGroup.children
            XposedBridge.log("ViewGroup detected, child count: ${children.count()}")
            for (child in children) {
                XposedBridge.log("View class: ${child.javaClass.name}")
                if (child is ViewGroup) {
                    iterateHook(child)
                    continue
                }
                if (child !is TextView) {
                    continue
                }
                XposedBridge.log("Hooked one text view")
                child.setBackgroundColor(Color.BLUE)
                child.setTextColor(Color.RED)
                child.setOnClickListener(TextViewOnClickWrapper(null))
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
