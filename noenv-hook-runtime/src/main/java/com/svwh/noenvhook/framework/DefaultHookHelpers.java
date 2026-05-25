package com.svwh.noenvhook.framework;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DefaultHookHelpers implements HookHelpers {

    private final HookBridge bridge;

    public DefaultHookHelpers(HookBridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public Class<?> findClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
        return Class.forName(className, false, classLoader);
    }

    @Override
    public Class<?> findClassIfExists(String className, ClassLoader classLoader) {
        try {
            return findClass(className, classLoader);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    @Override
    public Method findMethodExact(Class<?> targetClass, String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = targetClass.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method;
    }

    @Override
    public Method findMethodExact(String className, ClassLoader classLoader, String methodName, Class<?>... parameterTypes)
            throws ClassNotFoundException, NoSuchMethodException {
        return findMethodExact(findClass(className, classLoader), methodName, parameterTypes);
    }

    @Override
    public Constructor<?> findConstructorExact(Class<?> targetClass, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Constructor<?> constructor = targetClass.getDeclaredConstructor(parameterTypes);
        constructor.setAccessible(true);
        return constructor;
    }

    @Override
    public Field findField(Class<?> targetClass, String fieldName) throws NoSuchFieldException {
        return ReflectionUtils.findField(targetClass, fieldName);
    }

    @Override
    public Field findFieldIfExists(Class<?> targetClass, String fieldName) {
        try {
            return findField(targetClass, fieldName);
        } catch (NoSuchFieldException ignored) {
            return null;
        }
    }

    @Override
    public Object getObjectField(Object instance, String fieldName) throws ReflectiveOperationException {
        return findField(instance.getClass(), fieldName).get(instance);
    }

    @Override
    public void setObjectField(Object instance, String fieldName, Object value) throws ReflectiveOperationException {
        findField(instance.getClass(), fieldName).set(instance, value);
    }

    @Override
    public Object getStaticObjectField(Class<?> targetClass, String fieldName) throws ReflectiveOperationException {
        return findField(targetClass, fieldName).get(null);
    }

    @Override
    public void setStaticObjectField(Class<?> targetClass, String fieldName, Object value)
            throws ReflectiveOperationException {
        findField(targetClass, fieldName).set(null, value);
    }

    @Override
    public Object callMethod(Object instance, String methodName, Object... args) throws ReflectiveOperationException {
        Method method = findBestMethod(instance.getClass(), methodName, args);
        return method.invoke(instance, args);
    }

    @Override
    public Object callStaticMethod(Class<?> targetClass, String methodName, Object... args)
            throws ReflectiveOperationException {
        Method method = findBestMethod(targetClass, methodName, args);
        return method.invoke(null, args);
    }

    @Override
    public Object newInstance(Class<?> targetClass, Object... args) throws ReflectiveOperationException {
        Constructor<?> constructor = findBestConstructor(targetClass, args);
        return constructor.newInstance(args);
    }

    @Override
    public void findAndHookMethod(
            String className,
            ClassLoader classLoader,
            String methodName,
            HookCallback callback,
            Class<?>... parameterTypes
    ) throws ReflectiveOperationException {
        bridge.hookMethod(findMethodExact(className, classLoader, methodName, parameterTypes), callback);
    }

    @Override
    public void findAndHookConstructor(
            String className,
            ClassLoader classLoader,
            HookCallback callback,
            Class<?>... parameterTypes
    ) throws ReflectiveOperationException {
        bridge.hookMethod(findConstructorExact(findClass(className, classLoader), parameterTypes), callback);
    }

    private Method findBestMethod(Class<?> targetClass, String methodName, Object[] args) throws NoSuchMethodException {
        Method bestMatch = null;
        for (Class<?> current = targetClass; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && isParameterMatch(method.getParameterTypes(), args)) {
                    bestMatch = method;
                    break;
                }
            }
            if (bestMatch != null) {
                bestMatch.setAccessible(true);
                return bestMatch;
            }
        }
        throw new NoSuchMethodException(methodName);
    }

    private Constructor<?> findBestConstructor(Class<?> targetClass, Object[] args) throws NoSuchMethodException {
        for (Constructor<?> constructor : targetClass.getDeclaredConstructors()) {
            if (isParameterMatch(constructor.getParameterTypes(), args)) {
                constructor.setAccessible(true);
                return constructor;
            }
        }
        throw new NoSuchMethodException(targetClass.getName());
    }

    private boolean isParameterMatch(Class<?>[] parameterTypes, Object[] args) {
        if (parameterTypes.length != args.length) {
            return false;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            Object arg = args[i];
            if (arg != null && !wrapPrimitive(parameterTypes[i]).isAssignableFrom(arg.getClass())) {
                return false;
            }
        }
        return true;
    }

    private Class<?> wrapPrimitive(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == char.class) return Character.class;
        if (type == short.class) return Short.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        if (type == void.class) return Void.class;
        return type;
    }
}
