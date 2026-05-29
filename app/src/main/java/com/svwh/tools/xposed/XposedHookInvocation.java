package com.svwh.tools.xposed;

import android.app.Application;
import android.content.Context;

import com.svwh.noenvhook.runtime.HookRuntime;
import com.svwh.tools.BuildConfig;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class XposedHookInvocation implements IXposedHookLoadPackage {

    private static final String TAG = "STool_LSPosed";
    private static final String MODULE_PACKAGE = BuildConfig.APPLICATION_ID;
    private static final int WITH_ENV_TYPE = HookRuntime.ENV_TYPE_WITH_ENV;

    private final Set<String> initializedProcesses = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam == null || lpparam.packageName == null) {
            return;
        }

        if (MODULE_PACKAGE.equals(lpparam.packageName)) {
            markModuleActive(lpparam.classLoader);
            return;
        }

        hookApplicationAttach(lpparam);
    }

    private void hookApplicationAttach(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(
                Application.class,
                "attach",
                Context.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Context context = (Context) param.args[0];
                        if (context == null || !lpparam.packageName.equals(context.getPackageName())) {
                            return;
                        }
                        startRuntimeOnce(context, lpparam.processName);
                    }
                }
        );
    }

    private void startRuntimeOnce(Context context, String processName) {
        String key = context.getPackageName() + ":" + (processName == null ? "" : processName);
        if (!initializedProcesses.add(key)) {
            return;
        }

        try {
            boolean started = new HookRuntime(new XposedHookFramework(), WITH_ENV_TYPE).start(context);
            XposedBridge.log(TAG + " runtime started=" + started + ", package=" + context.getPackageName());
        } catch (Throwable throwable) {
            XposedBridge.log(TAG + " runtime start failed: " + context.getPackageName());
            XposedBridge.log(throwable);
        }
    }

    private void markModuleActive(ClassLoader classLoader) {
        try {
            Class<?> statusClass = XposedHelpers.findClass(
                    "com.svwh.tools.core.environment.LsposedStatus",
                    classLoader
            );
            Object status = XposedHelpers.getStaticObjectField(statusClass, "INSTANCE");
            XposedHelpers.callMethod(status, "setEnabled", true);
            XposedHelpers.findAndHookMethod(
                    statusClass,
                    "isEnabled",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            param.setResult(true);
                        }
                    }
            );
            XposedBridge.log(TAG + " module active");
        } catch (Throwable throwable) {
            XposedBridge.log(TAG + " active check hook failed");
            XposedBridge.log(throwable);
        }
    }
}
