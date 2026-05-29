package com.svwh.noenvhook.framework;

import com.svwh.noenvhook.framework.pine.PineHookFramework;

public final class HookEnvironment {

    private static volatile HookToolkit toolkit;

    private HookEnvironment() {
    }

    public static void install(HookFramework framework) {
        toolkit = framework.toolkit();
    }

    public static HookToolkit toolkit() {
        HookToolkit current = toolkit;
        if (current == null) {
            synchronized (HookEnvironment.class) {
                current = toolkit;
                if (current == null) {
                    current = new PineHookFramework().toolkit();
                    toolkit = current;
                }
            }
        }
        return current;
    }

    public static HookFramework framework() {
        return toolkit().framework();
    }

    public static HookBridge bridge() {
        return toolkit().bridge();
    }

    public static HookHelpers helpers() {
        return toolkit().helpers();
    }
}
