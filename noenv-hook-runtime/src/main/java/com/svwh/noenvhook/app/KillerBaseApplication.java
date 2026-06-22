package com.svwh.noenvhook.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.svwh.noenvhook.runtime.FridaRuntimeConfig;
import com.svwh.noenvhook.runtime.HookRuntime;
import com.svwh.noenvhook.runtime.RuntimeConstants;

import java.util.concurrent.atomic.AtomicBoolean;

public class KillerBaseApplication extends Application {

    @SuppressLint("StaticFieldLeak")
    public static Context context;

    public static Application application;

    private static boolean hookEnable = false;

    private static final AtomicBoolean nativeHookLoadScheduled = new AtomicBoolean(false);

    private static final AtomicBoolean nativeHookLoaded = new AtomicBoolean(false);

    private static final AtomicBoolean hookRuntimeStarted = new AtomicBoolean(false);

    private static void prepareNativeHookLibraryLoad(Context context) {
        long delayMillis = FridaRuntimeConfig.delayInjectMillis(context);
        if (delayMillis > 0L) {
            scheduleNativeHookLibraryLoad(delayMillis);
            return;
        }
        loadNativeHookLibrary();
    }

    private static void scheduleNativeHookLibraryLoad(long delayMillis) {
        if (nativeHookLoaded.get()) {
            return;
        }
        if (!nativeHookLoadScheduled.compareAndSet(false, true)) {
            return;
        }

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(KillerBaseApplication::loadNativeHookLibrary, delayMillis);
    }

    private static void loadNativeHookLibrary() {
        if (!nativeHookLoaded.compareAndSet(false, true)) {
            return;
        }
        try {
            System.loadLibrary("killer-inject");
        } catch (Throwable e) {
            nativeHookLoaded.set(false);
            Log.e(RuntimeConstants.TAG, "load native hook library failed", e);
        }
    }

    private static void startHookRuntime(Context context) {
        if (!hookRuntimeStarted.compareAndSet(false, true)) {
            return;
        }
        hookEnable = new HookRuntime().start(context);
    }

    public static void beforeAttachBaseContext(Context base) {
        context = base;
        prepareNativeHookLibraryLoad(base);
        startHookRuntime(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        context = base;
        application = this;
        beforeAttachBaseContext(base);
        super.attachBaseContext(base);
    }
}
