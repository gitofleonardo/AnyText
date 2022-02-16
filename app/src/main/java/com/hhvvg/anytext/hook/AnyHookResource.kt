package com.hhvvg.anytext.hook

import de.robv.android.xposed.IXposedHookInitPackageResources
import de.robv.android.xposed.callbacks.XC_InitPackageResources

/**
 * @author hhvvg
 *
 * Usage remaining.
 */
class AnyHookResource : IXposedHookInitPackageResources {
    override fun handleInitPackageResources(resParam: XC_InitPackageResources.InitPackageResourcesParam?) {
        if (resParam == null) {
            return
        }
    }
}
