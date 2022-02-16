package com.hhvvg.anytext.hook

import android.content.res.Resources
import android.content.res.XModuleResources
import de.robv.android.xposed.IXposedHookZygoteInit

class AnyHookZygote : IXposedHookZygoteInit {
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
