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

    public static File withEnvEnableFile(Context context) {
        File stoolDir = stoolDir(context);
        return stoolDir == null ? null : new File(stoolDir, RuntimeConstants.WITH_ENV_ENABLE_FILE);
    }

    public static File configDatabaseDir(Context context) {
        return stoolDir(context);
    }

    public static File fridaNoEnvDir(Context context) {
        File mediaDir = firstExternalMediaDir(context);
        if (mediaDir == null) {
            return null;
        }
        return new File(
                new File(mediaDir, RuntimeConstants.FRIDA_DIR_NAME),
                RuntimeConstants.NO_ENV_DIR_NAME
        );
    }

    public static File fridaNoEnvConfigFile(Context context) {
        File fridaNoEnvDir = fridaNoEnvDir(context);
        return fridaNoEnvDir == null ? null : new File(fridaNoEnvDir, RuntimeConstants.FRIDA_CONFIG_FILE);
    }
}
