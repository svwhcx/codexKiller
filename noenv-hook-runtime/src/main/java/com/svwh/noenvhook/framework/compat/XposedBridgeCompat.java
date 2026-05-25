package com.svwh.noenvhook.framework.compat;

import com.svwh.noenvhook.core.MethodHookInvocation;
import com.svwh.noenvhook.framework.HookCallback;
import com.svwh.noenvhook.framework.HookEnvironment;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Set;

public final class XposedBridgeCompat {

    private XposedBridgeCompat() {
    }

    public static void hookMethod(Member target, HookCallback callback) {
        HookEnvironment.bridge().hookMethod(target, callback);
    }

    public static void hookReplacement(Member target, MethodHookInvocation invocation) {
        HookEnvironment.bridge().hookReplacement(target, invocation);
    }

    public static Set<Method> hookAllMethods(Class<?> targetClass, String methodName, HookCallback callback) {
        return HookEnvironment.bridge().hookAllMethods(targetClass, methodName, callback);
    }

    public static Set<Constructor<?>> hookAllConstructors(Class<?> targetClass, HookCallback callback) {
        return HookEnvironment.bridge().hookAllConstructors(targetClass, callback);
    }

    public static void log(String message) {
        HookEnvironment.bridge().log(message);
    }

    public static void log(Throwable throwable) {
        HookEnvironment.bridge().log(throwable);
    }
}
