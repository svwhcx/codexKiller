package com.svwh.noenvhook.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.svwh.noenvhook.runtime.HookRuntime;
import com.svwh.noenvhook.runtime.RuntimeConstants;

public class KillerBaseApplication extends Application {

    @SuppressLint("StaticFieldLeak")
    public static Context context;

    public static Application application;

    private static boolean hookEnable = false;

    static {
        try {
            System.loadLibrary("killer-inject");
        } catch (Throwable e) {
            Log.e(RuntimeConstants.TAG, "加载 native hook 库失败", e);
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
    }
}
