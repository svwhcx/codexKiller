package com.svwh.tools.xposed;

import com.svwh.noenvhook.framework.DefaultHookBridge;
import com.svwh.noenvhook.framework.HookFramework;

import de.robv.android.xposed.XposedBridge;

final class XposedRuntimeBridge extends DefaultHookBridge {

    XposedRuntimeBridge(HookFramework framework) {
        super(framework);
    }

    @Override
    public void log(String message) {
        XposedBridge.log(message);
    }

    @Override
    public void log(Throwable throwable) {
        XposedBridge.log(throwable);
    }
}
