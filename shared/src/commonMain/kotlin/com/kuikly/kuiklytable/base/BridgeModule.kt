package com.kuikly.kuiklytable.base

import com.tencent.kuikly.core.module.CallbackFn
import com.tencent.kuikly.core.module.Module
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

internal class BridgeModule : Module() {

    override fun moduleName(): String = MODULE_NAME

    fun closePage() {
        callNativeMethod(CLOSE_PAGE, null, null)
    }

    fun openPage(url: String, callbackFn: CallbackFn? = null) {
        val methodArgs = JSONObject()
        methodArgs.put("url", url)
        callNativeMethod(OPEN_PAGE, methodArgs, callbackFn)
    }

    private fun callNativeMethod(methodName: String, data: JSONObject?, callbackFn: CallbackFn?) {
        toNative(false, methodName, data?.toString(), callbackFn, false)
    }

    companion object {
        const val MODULE_NAME = "HRBridgeModule"
        const val OPEN_PAGE = "openPage"
        const val CLOSE_PAGE = "closePage"
    }
}

internal val com.tencent.kuikly.core.pager.Pager.bridgeModule: BridgeModule
    get() = getExternalModule(BridgeModule.MODULE_NAME) as BridgeModule
