package com.svwh.noenvhook.core;

import com.svwh.noenvhook.conf.ChangeConfig;
import com.svwh.noenvhook.conf.HookConfig;
import com.svwh.noenvhook.framework.HookCallFrame;

public abstract class BaseHookInvocation implements MethodHookInvocation {

    protected HookConfig hookConfig;

    public BaseHookInvocation(HookConfig hookConfig) {
        this.hookConfig = hookConfig;
    }

    protected void changeParam(HookCallFrame callFrame) {
        if (hookConfig.getChangeConfigs() == null) {
            return;
        }
        for (ChangeConfig changeConfig : hookConfig.getChangeConfigs()) {
            Integer paramNum = changeConfig.getParamNum();
            if (paramNum != null && paramNum > 0) {
                Object current = callFrame.getArg(paramNum);
                Object value = changeConfig.getTarget();
                callFrame.setArg(paramNum, current == null || value == null ? value : current.getClass().cast(value));
            }
        }
    }

    protected void changeResult(HookCallFrame callFrame) {
    }

    @Override
    public void beforeMethodHook(HookCallFrame callFrame) {
    }

    @Override
    public void afterMethodHook(HookCallFrame callFrame) {
    }

    @Override
    public Object replaceMethodHook(HookCallFrame callFrame) throws Throwable {
        return callFrame.invokeOriginal();
    }

    @Override
    public void changeStaticField(HookCallFrame callFrame) {
    }

    @Override
    public void changeField(HookCallFrame callFrame) {
    }

    @Override
    public void build() {
    }
}
