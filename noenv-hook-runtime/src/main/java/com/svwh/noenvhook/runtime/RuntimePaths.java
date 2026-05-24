package com.svwh.noenvhook.runtime;

import android.content.Context;

import java.io.File;

public final class RuntimePaths {

    private RuntimePaths() {
    }

    public static File firstExternalMediaDir(Context context) {
        if (context == null) {
            return null;
        }
        File[] mediaDirs = context.getExternalMediaDirs();
        if (mediaDirs == null || mediaDirs.length == 0) {
            return null;
        }
        return mediaDirs[0];
    }

    public static File stoolDir(Context context) {
        File mediaDir = firstExternalMediaDir(context);
        return mediaDir == null ? null : new File(mediaDir, RuntimeConstants.STOOL_DIR_NAME);
    }

    public static File noEnvEnableFile(Context context) {
        File stoolDir = stoolDir(context);
        return stoolDir == null ? null : new File(stoolDir, RuntimeConstants.NO_ENV_ENABLE_FILE);
    }

    public static File configDatabaseDir(Context context) {
        return stoolDir(context);
    }
}
