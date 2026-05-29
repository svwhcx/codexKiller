package com.svwh.noenvhook.framework;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface HookHelpers {

    Class<?> findClass(String className, ClassLoader classLoader) throws ClassNotFoundException;

    Class<?> findClassIfExists(String className, ClassLoader classLoader);

    Method findMethodExact(Class<?> targetClass, String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException;

    Method findMethodExact(String className, ClassLoader classLoader, String methodName, Class<?>... parameterTypes)
            throws ClassNotFoundException, NoSuchMethodException;

    Constructor<?> findConstructorExact(Class<?> targetClass, Class<?>... parameterTypes)
            throws NoSuchMethodException;

    Field findField(Class<?> targetClass, String fieldName) throws NoSuchFieldException;

    Field findFieldIfExists(Class<?> targetClass, String fieldName);

    Object getObjectField(Object instance, String fieldName) throws ReflectiveOperationException;

    void setObjectField(Object instance, String fieldName, Object value) throws ReflectiveOperationException;

    Object getStaticObjectField(Class<?> targetClass, String fieldName) throws ReflectiveOperationException;

    void setStaticObjectField(Class<?> targetClass, String fieldName, Object value) throws ReflectiveOperationException;

    Object callMethod(Object instance, String methodName, Object... args) throws ReflectiveOperationException;

    Object callStaticMethod(Class<?> targetClass, String methodName, Object... args)
            throws ReflectiveOperationException;

    Object newInstance(Class<?> targetClass, Object... args) throws ReflectiveOperationException;

    void findAndHookMethod(
            String className,
            ClassLoader classLoader,
            String methodName,
            HookCallback callback,
            Class<?>... parameterTypes
    ) throws ReflectiveOperationException;

    void findAndHookConstructor(
            String className,
            ClassLoader classLoader,
            HookCallback callback,
            Class<?>... parameterTypes
    ) throws ReflectiveOperationException;
}
