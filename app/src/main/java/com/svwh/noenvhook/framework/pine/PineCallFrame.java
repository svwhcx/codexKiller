package com.svwh.noenvhook.framework.pine;

import com.svwh.noenvhook.framework.HookCallFrame;

import java.lang.reflect.Member;

import top.canyie.pine.Pine;

public class PineCallFrame implements HookCallFrame {

    private final Pine.CallFrame delegate;

    public PineCallFrame(Pine.CallFrame delegate) {
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
        return delegate.invokeOriginalMethod();
    }
}
