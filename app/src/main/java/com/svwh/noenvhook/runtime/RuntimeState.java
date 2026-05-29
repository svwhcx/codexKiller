package com.svwh.noenvhook.runtime;

import android.content.Context;

import java.io.File;

public final class RuntimeState {

    private RuntimeState() {
    }

    public static boolean isHookEnabled(Context context) {
        return isHookEnabled(context, 0);
    }

    public static boolean isHookEnabled(Context context, int envType) {
        File enableFile = envType == 1
                ? RuntimePaths.withEnvEnableFile(context)
                : RuntimePaths.noEnvEnableFile(context);
        return enableFile != null && enableFile.exists();
    }
}
