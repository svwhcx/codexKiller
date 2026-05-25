package com.svwh.noenvhook.framework;

import com.svwh.noenvhook.core.MethodHookInvocation;

import java.lang.reflect.Member;

public interface HookFramework {

    void hook(Member target, HookCallback callback);

    void hookReplacement(Member target, MethodHookInvocation invocation);

    default HookBridge bridge() {
        return new DefaultHookBridge(this);
    }

    default HookHelpers helpers() {
        return new DefaultHookHelpers(bridge());
    }

    default HookToolkit toolkit() {
        HookBridge bridge = bridge();
        return new HookToolkit(this, bridge, new DefaultHookHelpers(bridge));
    }
}
