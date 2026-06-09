package com.svwh.noenvhook.runtime;

public final class RuntimeConstants {

    public static final String TAG = "STool_NoEnvHook";
    public static final String STOOL_DIR_NAME = "stool";
    public static final String FRIDA_DIR_NAME = "frida";
    public static final String NO_ENV_DIR_NAME = "noenv";
    public static final String FRIDA_CONFIG_FILE = "fridaConfig.json";
    public static final String FRIDA_DELAY_INJECT_MILLIS_KEY = "delayInjectMillis";
    public static final String NO_ENV_ENABLE_FILE = "no_env_enable.s";
    public static final String WITH_ENV_ENABLE_FILE = "with_env_enable.s";

    private RuntimeConstants() {
    }
}
