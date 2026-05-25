package com.svwh.noenvhook.framework.compat;

import com.svwh.noenvhook.framework.HookCallback;
import com.svwh.noenvhook.framework.HookEnvironment;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class XposedHelpersCompat {

    private XposedHelpersCompat() {
    }

    public static Class<?> findClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
        return HookEnvironment.helpers().findClass(className, classLoader);
    }

    public static Class<?> findClassIfExists(String className, ClassLoader classLoader) {
        return HookEnvironment.helpers().findClassIfExists(className, classLoader);
    }

    public static Method findMethodExact(Class<?> targetClass, String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        return HookEnvironment.helpers().findMethodExact(targetClass, methodName, parameterTypes);
    }

    public static Method findMethodExact(
            String className,
            ClassLoader classLoader,
            String methodName,
            Class<?>... parameterTypes
    ) throws ClassNotFoundException, NoSuchMethodException {
        return HookEnvironment.helpers().findMethodExact(className, classLoader, methodName, parameterTypes);
    }

    public static Constructor<?> findConstructorExact(Class<?> targetClass, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        return HookEnvironment.helpers().findConstructorExact(targetClass, parameterTypes);
    }

    public static Field findField(Class<?> targetClass, String fieldName) throws NoSuchFieldException {
        return HookEnvironment.helpers().findField(targetClass, fieldName);
    }

    public static Field findFieldIfExists(Class<?> targetClass, String fieldName) {
        return HookEnvironment.helpers().findFieldIfExists(targetClass, fieldName);
    }

    public static Object getObjectField(Object instance, String fieldName) throws ReflectiveOperationException {
        return HookEnvironment.helpers().getObjectField(instance, fieldName);
    }

    public static void setObjectField(Object instance, String fieldName, Object value)
            throws ReflectiveOperationException {
        HookEnvironment.helpers().setObjectField(instance, fieldName, value);
    }

    public static Object getStaticObjectField(Class<?> targetClass, String fieldName)
            throws ReflectiveOperationException {
        return HookEnvironment.helpers().getStaticObjectField(targetClass, fieldName);
    }

    public static void setStaticObjectField(Class<?> targetClass, String fieldName, Object value)
            throws ReflectiveOperationException {
        HookEnvironment.helpers().setStaticObjectField(targetClass, fieldName, value);
    }

    public static Object callMethod(Object instance, String methodName, Object... args)
            throws ReflectiveOperationException {
        return HookEnvironment.helpers().callMethod(instance, methodName, args);
    }

    public static Object callStaticMethod(Class<?> targetClass, String methodName, Object... args)
            throws ReflectiveOperationException {
        return HookEnvironment.helpers().callStaticMethod(targetClass, methodName, args);
    }

    public static Object newInstance(Class<?> targetClass, Object... args) throws ReflectiveOperationException {
        return HookEnvironment.helpers().newInstance(targetClass, args);
    }

    public static void findAndHookMethod(
            String className,
            ClassLoader classLoader,
            String methodName,
            HookCallback callback,
            Class<?>... parameterTypes
    ) throws ReflectiveOperationException {
        HookEnvironment.helpers().findAndHookMethod(className, classLoader, methodName, callback, parameterTypes);
    }

    public static void findAndHookConstructor(
            String className,
            ClassLoader classLoader,
            HookCallback callback,
            Class<?>... parameterTypes
    ) throws ReflectiveOperationException {
        HookEnvironment.helpers().findAndHookConstructor(className, classLoader, callback, parameterTypes);
    }
}
