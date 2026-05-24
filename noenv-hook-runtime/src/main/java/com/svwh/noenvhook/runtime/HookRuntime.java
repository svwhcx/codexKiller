package com.svwh.noenvhook.runtime;

import android.content.Context;

import com.svwh.noenvhook.conf.HookConfig;
import com.svwh.noenvhook.config.DBHookConfigService;
import com.svwh.noenvhook.config.IHookConfigService;
import com.svwh.noenvhook.log.FilteredStackTraceCollector;
import com.svwh.noenvhook.log.LogService;
import com.svwh.noenvhook.management.HookManager;

import java.util.List;

public final class HookRuntime {

    private final IHookConfigService configService;
    private final HookManager hookManager;
    private final RuntimeLogger runtimeLogger;

    public HookRuntime() {
        this(
                new DBHookConfigService(),
                new HookManager(LogService.getInstance(), new AndroidRuntimeLogger(), new FilteredStackTraceCollector()),
                new AndroidRuntimeLogger()
        );
    }

    HookRuntime(IHookConfigService configService, HookManager hookManager, RuntimeLogger runtimeLogger) {
        this.configService = configService;
        this.hookManager = hookManager;
        this.runtimeLogger = runtimeLogger;
    }

    public boolean start(Context context) {
        runtimeLogger.info("开始启动无环境 Hook Runtime");
        if (!RuntimeState.isHookEnabled(context)) {
            runtimeLogger.info("未检测到无环境启用文件，跳过 Hook 注册");
            return false;
        }

        List<HookConfig> hookConfigs = configService.queryHookConfig(context);
        if (hookConfigs == null || hookConfigs.isEmpty()) {
            runtimeLogger.info("未查询到 Hook 配置，本次仅保持启用状态");
            return true;
        }

        hookManager.registerHooks(context.getClassLoader(), hookConfigs);
        runtimeLogger.info("无环境 Hook Runtime 启动完成，配置数量：" + hookConfigs.size());
        return true;
    }
}
