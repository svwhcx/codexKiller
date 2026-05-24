package com.svwh.noenvhook.runtime;

import android.content.Context;

import java.io.File;

public final class RuntimeState {

    private RuntimeState() {
    }

    public static boolean isHookEnabled(Context context) {
        File enableFile = RuntimePaths.noEnvEnableFile(context);
        return enableFile != null && enableFile.exists();
    }
}
