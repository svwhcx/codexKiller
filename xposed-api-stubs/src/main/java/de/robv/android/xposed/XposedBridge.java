package de.robv.android.xposed;

import java.lang.reflect.Member;

public final class XposedBridge {
    private XposedBridge() {
    }

    public static XC_MethodHook.Unhook hookMethod(Member hookMethod, XC_MethodHook callback) {
        throw new UnsupportedOperationException("Stub");
    }

    public static Object invokeOriginalMethod(Member method, Object thisObject, Object[] args) throws Throwable {
        throw new UnsupportedOperationException("Stub");
    }

    public static void log(String text) {
    }

    public static void log(Throwable throwable) {
    }
}
