package com.svwh.tools.xposed;

import com.svwh.noenvhook.framework.HookCallFrame;

import java.lang.reflect.Member;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

final class XposedCallFrame implements HookCallFrame {

    private final XC_MethodHook.MethodHookParam delegate;

    XposedCallFrame(XC_MethodHook.MethodHookParam delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object getThisObject() {
        return delegate.thisObject;
    }

    @Override
    public Object[] getArgs() {
        return delegate.args;
    }

    @Override
    public Member getMember() {
        return delegate.method;
    }

    @Override
    public Object getResult() {
        return delegate.getResult();
    }

    @Override
    public void setResult(Object result) {
        delegate.setResult(result);
    }

    @Override
    public Object invokeOriginal() throws Throwable {
        return XposedBridge.invokeOriginalMethod(delegate.method, delegate.thisObject, delegate.args);
    }
}
