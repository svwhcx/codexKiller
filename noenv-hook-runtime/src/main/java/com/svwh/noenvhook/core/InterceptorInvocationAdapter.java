package com.svwh.noenvhook.core;

import com.svwh.noenvhook.conf.HookConfig;

import top.canyie.pine.Pine;
import top.canyie.pine.callback.MethodReplacement;

/**
 * @description
 * @Author chenxin
 * @Date 2025/4/26 14:38
 */
public class InterceptorInvocationAdapter extends MethodReplacement {

    private final MethodHookInvocation methodHookInvocation;


    public InterceptorInvocationAdapter(MethodHookInvocation methodHookInvocation){
        this.methodHookInvocation = methodHookInvocation;
    }

    @Override
    protected Object replaceCall(Pine.CallFrame callFrame)  {
        return this.methodHookInvocation.replaceMethodHook(callFrame);
    }
}
