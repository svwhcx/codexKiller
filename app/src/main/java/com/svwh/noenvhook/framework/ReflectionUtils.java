package com.svwh.noenvhook.framework;

import java.lang.reflect.Field;

public final class ReflectionUtils {

    private ReflectionUtils() {
    }

    public static Field findField(Class<?> targetClass, String name) throws NoSuchFieldException {
        Class<?> current = targetClass;
        while (current != null) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }
}
