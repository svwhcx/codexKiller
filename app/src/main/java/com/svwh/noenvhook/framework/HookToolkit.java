package com.svwh.noenvhook.framework;

public final class HookToolkit {

    private final HookFramework framework;
    private final HookBridge bridge;
    private final HookHelpers helpers;

    public HookToolkit(HookFramework framework, HookBridge bridge, HookHelpers helpers) {
        this.framework = framework;
        this.bridge = bridge;
        this.helpers = helpers;
    }

    public HookFramework framework() {
        return framework;
    }

    public HookBridge bridge() {
        return bridge;
    }

    public HookHelpers helpers() {
        return helpers;
    }
}
