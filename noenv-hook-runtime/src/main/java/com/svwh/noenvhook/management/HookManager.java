package com.svwh.noenvhook.management;

import com.svwh.noenvhook.conf.HookConfig;
import com.svwh.noenvhook.framework.HookFramework;
import com.svwh.noenvhook.log.FilteredStackTraceCollector;
import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.LogService;
import com.svwh.noenvhook.log.StackTraceCollector;
import com.svwh.noenvhook.runtime.AndroidRuntimeLogger;
import com.svwh.noenvhook.runtime.RuntimeLogger;

import java.lang.reflect.Member;
import java.util.List;

public class HookManager {

    private final HookTargetResolver targetResolver;
    private final HookRegistrar hookRegistrar;
    private final HookRegistrationFailureReporter failureReporter;

    public HookManager() {
        this(LogService.getInstance(), new AndroidRuntimeLogger(), new FilteredStackTraceCollector());
    }

    public HookManager(
            HookLogWriter logWriter,
            RuntimeLogger runtimeLogger,
            StackTraceCollector stackTraceCollector
    ) {
        this(logWriter, runtimeLogger, stackTraceCollector, null);
    }

    public HookManager(
            HookLogWriter logWriter,
            RuntimeLogger runtimeLogger,
            StackTraceCollector stackTraceCollector,
            HookFramework hookFramework
    ) {
        this.targetResolver = new HookTargetResolver();
        this.hookRegistrar = hookFramework == null
                ? new HookRegistrar(logWriter, stackTraceCollector, runtimeLogger)
                : new HookRegistrar(logWriter, stackTraceCollector, runtimeLogger, hookFramework);
        this.failureReporter = new HookRegistrationFailureReporter(logWriter, runtimeLogger);
    }

    public void registerHooks(ClassLoader classLoader, List<HookConfig> hookConfigs) {
        if (hookConfigs == null || hookConfigs.isEmpty()) {
            return;
        }
        for (HookConfig hookConfig : hookConfigs) {
            registerHook(classLoader, hookConfig);
        }
    }

    public void registerHook(ClassLoader classLoader, HookConfig hookConfig) {
        if (classLoader == null || hookConfig == null) {
            return;
        }

        try {
            List<Member> targets = targetResolver.resolve(classLoader, hookConfig);
            for (Member target : targets) {
                hookRegistrar.register(classLoader, target, hookConfig);
            }
        } catch (Exception e) {
            failureReporter.report(hookConfig, e);
        }
    }
}
