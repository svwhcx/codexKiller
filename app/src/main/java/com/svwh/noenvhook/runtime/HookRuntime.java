package com.svwh.noenvhook.runtime;

import android.content.Context;

import com.svwh.noenvhook.conf.HookConfig;
import com.svwh.noenvhook.config.DBHookConfigService;
import com.svwh.noenvhook.config.IHookConfigService;
import com.svwh.noenvhook.framework.HookFramework;
import com.svwh.noenvhook.log.FilteredStackTraceCollector;
import com.svwh.noenvhook.log.LogService;
import com.svwh.noenvhook.management.HookManager;

import java.util.List;

public final class HookRuntime {

    public static final int ENV_TYPE_NO_ENV = 0;
    public static final int ENV_TYPE_WITH_ENV = 1;

    private final IHookConfigService configService;
    private final HookManager hookManager;
    private final RuntimeLogger runtimeLogger;
    private final int envType;
    private final HookFramework hookFramework;

    public HookRuntime() {
        this(
                new DBHookConfigService(ENV_TYPE_NO_ENV),
                null,
                new AndroidRuntimeLogger(),
                ENV_TYPE_NO_ENV,
                null
        );
    }

    public HookRuntime(HookFramework hookFramework) {
        this(hookFramework, ENV_TYPE_NO_ENV);
    }

    public HookRuntime(HookFramework hookFramework, int envType) {
        this(
                new DBHookConfigService(envType),
                null,
                new AndroidRuntimeLogger(),
                envType,
                hookFramework
        );
    }

    HookRuntime(IHookConfigService configService, HookManager hookManager, RuntimeLogger runtimeLogger) {
        this(configService, hookManager, runtimeLogger, ENV_TYPE_NO_ENV, null);
    }

    HookRuntime(
            IHookConfigService configService,
            HookManager hookManager,
            RuntimeLogger runtimeLogger,
            int envType,
            HookFramework hookFramework
    ) {
        this.configService = configService;
        this.hookManager = hookManager;
        this.runtimeLogger = runtimeLogger;
        this.envType = envType;
        this.hookFramework = hookFramework;
    }

    public boolean start(Context context) {
        try {
            runtimeLogger.info("Start Hook Runtime, envType=" + envType);
            if (!RuntimeState.isHookEnabled(context, envType)) {
                runtimeLogger.info("Hook enable file not found, skip registration, envType=" + envType);
                return false;
            }

            List<HookConfig> hookConfigs = configService.queryHookConfig(context);
            if (hookConfigs == null || hookConfigs.isEmpty()) {
                runtimeLogger.info("No Hook config found, envType=" + envType);
                return true;
            }

            resolveHookManager(context).registerHooks(context.getClassLoader(), hookConfigs);
            runtimeLogger.info("Hook Runtime started, envType=" + envType + ", configCount=" + hookConfigs.size());
            return true;
        } catch (Throwable throwable) {
            runtimeLogger.error("Hook Runtime startup failed, envType=" + envType, throwable);
            return false;
        }
    }

    private HookManager resolveHookManager(Context context) {
        if (hookManager != null) {
            return hookManager;
        }
        return new HookManager(
                new LogService(context, new FilteredStackTraceCollector()),
                new AndroidRuntimeLogger(),
                new FilteredStackTraceCollector(),
                hookFramework
        );
    }
}
