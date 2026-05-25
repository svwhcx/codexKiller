package com.svwh.noenvhook.framework;

import com.svwh.noenvhook.core.MethodHookInvocation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Set;

public interface HookBridge {

    void hookMethod(Member target, HookCallback callback);

    void hookReplacement(Member target, MethodHookInvocation invocation);

    Set<Method> hookAllMethods(Class<?> targetClass, String methodName, HookCallback callback);

    Set<Constructor<?>> hookAllConstructors(Class<?> targetClass, HookCallback callback);

    void log(String message);

    void log(Throwable throwable);
}
