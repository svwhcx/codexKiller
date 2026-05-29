package de.robv.android.xposed;

public final class XposedHelpers {
    private XposedHelpers() {
    }

    public static Class<?> findClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
        return Class.forName(className, false, classLoader);
    }

    public static XC_MethodHook.Unhook findAndHookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
        throw new UnsupportedOperationException("Stub");
    }

    public static XC_MethodHook.Unhook findAndHookMethod(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback) {
        throw new UnsupportedOperationException("Stub");
    }

    public static Object callStaticMethod(Class<?> clazz, String methodName, Object... args) {
        throw new UnsupportedOperationException("Stub");
    }

    public static Object callMethod(Object obj, String methodName, Object... args) {
        throw new UnsupportedOperationException("Stub");
    }

    public static Object getStaticObjectField(Class<?> clazz, String fieldName) {
        throw new UnsupportedOperationException("Stub");
    }
}
