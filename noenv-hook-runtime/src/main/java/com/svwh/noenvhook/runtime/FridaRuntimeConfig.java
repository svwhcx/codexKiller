package com.svwh.noenvhook.runtime;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public final class FridaRuntimeConfig {

    private FridaRuntimeConfig() {
    }

    public static long delayInjectMillis(Context context) {
        File configFile = RuntimePaths.fridaNoEnvConfigFile(context);
        if (configFile == null || !configFile.exists() || !configFile.isFile()) {
            return 0L;
        }

        try {
            JSONObject jsonObject = new JSONObject(readText(configFile));
            long delayMillis = jsonObject.optLong(RuntimeConstants.FRIDA_DELAY_INJECT_MILLIS_KEY, 0L);
            return Math.max(0L, delayMillis);
        } catch (Throwable e) {
            Log.e(RuntimeConstants.TAG, "读取 Frida noenv 配置失败", e);
            return 0L;
        }
    }

    private static String readText(File file) throws Exception {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8")
        );
        try {
            char[] buffer = new char[2048];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, read);
            }
            return builder.toString();
        } finally {
            reader.close();
        }
    }
}
