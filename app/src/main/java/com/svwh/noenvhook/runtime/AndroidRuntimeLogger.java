package com.svwh.noenvhook.runtime;

import android.util.Log;

public class AndroidRuntimeLogger implements RuntimeLogger {

    @Override
    public void info(String message) {
        Log.i(RuntimeConstants.TAG, message);
    }

    @Override
    public void warn(String message) {
        Log.w(RuntimeConstants.TAG, message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        Log.e(RuntimeConstants.TAG, message, throwable);
    }
}
