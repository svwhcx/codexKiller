package com.svwh.noenvhook.framework;

public interface HookCallback {

    default void beforeCall(HookCallFrame callFrame) throws Throwable {
    }

    default void afterCall(HookCallFrame callFrame) throws Throwable {
    }
}
