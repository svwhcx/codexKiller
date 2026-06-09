package com.svwh.noenvhook.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

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

    private static void scheduleNativeHookLibraryLoad(Context context) {
        if (!nativeHookLoadScheduled.compareAndSet(false, true)) {
            return;
        }

        final Context appContext = context.getApplicationContext() != null
                ? context.getApplicationContext()
                : context;
        new Thread(() -> {
            final long delayMillis = FridaRuntimeConfig.delayInjectMillis(appContext);
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(KillerBaseApplication::loadNativeHookLibrary, delayMillis);
        }, "STool-FridaInjectDelay").start();
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

    @Override
    public void onCreate() {
        super.onCreate();
        hookEnable = new HookRuntime().start(this);
        if (hookEnable) {
            Toast.makeText(this, "开始 Hook", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        context = base;
        application = this;
        scheduleNativeHookLibraryLoad(base);
    }
}
