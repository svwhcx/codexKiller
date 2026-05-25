package com.svwh.noenvhook.framework;

import android.util.Log;

import com.svwh.noenvhook.core.MethodHookInvocation;
import com.svwh.noenvhook.runtime.RuntimeConstants;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

public class DefaultHookBridge implements HookBridge {

    private final HookFramework framework;

    public DefaultHookBridge(HookFramework framework) {
        this.framework = framework;
    }

    @Override
    public void hookMethod(Member target, HookCallback callback) {
        framework.hook(target, callback);
    }

    @Override
    public void hookReplacement(Member target, MethodHookInvocation invocation) {
        framework.hookReplacement(target, invocation);
    }

    @Override
    public Set<Method> hookAllMethods(Class<?> targetClass, String methodName, HookCallback callback) {
        Set<Method> hookedMethods = new LinkedHashSet<>();
        for (Method method : targetClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                method.setAccessible(true);
                hookMethod(method, callback);
                hookedMethods.add(method);
            }
        }
        return hookedMethods;
    }

    @Override
    public Set<Constructor<?>> hookAllConstructors(Class<?> targetClass, HookCallback callback) {
        Set<Constructor<?>> hookedConstructors = new LinkedHashSet<>();
        for (Constructor<?> constructor : targetClass.getDeclaredConstructors()) {
            constructor.setAccessible(true);
            hookMethod(constructor, callback);
            hookedConstructors.add(constructor);
        }
        return hookedConstructors;
    }

    @Override
    public void log(String message) {
        Log.i(RuntimeConstants.TAG, message);
    }

    @Override
    public void log(Throwable throwable) {
        Log.e(RuntimeConstants.TAG, throwable == null ? "" : throwable.getMessage(), throwable);
    }
}
