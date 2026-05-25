package com.svwh.noenvhook.management;

import com.svwh.noenvhook.conf.HookConfig;
import com.svwh.noenvhook.core.InterceptorInvocationAdapter;
import com.svwh.noenvhook.core.StartActivityLogHookInvocation;
import com.svwh.noenvhook.core.custom.ArrounHookInvocation;
import com.svwh.noenvhook.core.file.AssetsHook;
import com.svwh.noenvhook.core.file.FileDeleteHookInvocation;
import com.svwh.noenvhook.core.file.FileReadHookInvocation;
import com.svwh.noenvhook.core.file.FileWriteHookInvocation;
import com.svwh.noenvhook.core.other.ScreenHook;
import com.svwh.noenvhook.core.other.SignatureHookInvocation;
import com.svwh.noenvhook.core.ui.DialogHookInvocation;
import com.svwh.noenvhook.core.ui.OnClickLogHookInvocation;
import com.svwh.noenvhook.core.ui.TextSetHookInvocation;
import com.svwh.noenvhook.core.ui.ToastShowHookInvocation;
import com.svwh.noenvhook.core.web.VpnHookInvocation;
import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.StackTraceCollector;
import com.svwh.noenvhook.runtime.RuntimeLogger;

import java.lang.reflect.Member;
import java.util.HashSet;
import java.util.Set;

import top.canyie.pine.Pine;

public class HookRegistrar {

    private final HookLogWriter logWriter;
    private final StackTraceCollector stackTraceCollector;
    private final RuntimeLogger runtimeLogger;
    private final Set<Integer> singletonHooks = new HashSet<>();

    public HookRegistrar(
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector,
            RuntimeLogger runtimeLogger
    ) {
        this.logWriter = logWriter;
        this.stackTraceCollector = stackTraceCollector;
        this.runtimeLogger = runtimeLogger;
    }

    public void register(ClassLoader classLoader, Member target, HookConfig hookConfig) throws Exception {
        Integer type = hookConfig.getType();
        if (type == null) {
            runtimeLogger.warn("Hook 类型为空，跳过：" + hookConfig.getConfigName());
            return;
        }

        switch (type) {
            case HookConfigTypeEnum.CUSTOMER:
                Pine.hook(target, new InterceptorInvocationAdapter(
                        new ArrounHookInvocation(hookConfig, logWriter, stackTraceCollector)
                ));
                break;
            case HookConfigTypeEnum.CLICK:
                Pine.hook(target, new InterceptorInvocationAdapter(
                        new OnClickLogHookInvocation(hookConfig, logWriter, stackTraceCollector)
                ));
                break;
            case HookConfigTypeEnum.DIALOG:
                Pine.hook(target, new InterceptorInvocationAdapter(
                        new DialogHookInvocation(hookConfig, logWriter, stackTraceCollector)
                ));
                break;
            case HookConfigTypeEnum.TEXT_SET:
                Pine.hook(target, new InterceptorInvocationAdapter(
                        new TextSetHookInvocation(hookConfig, logWriter, stackTraceCollector)
                ));
                break;
            case HookConfigTypeEnum.TOAST_SHOW:
                Pine.hook(target, new InterceptorInvocationAdapter(
                        new ToastShowHookInvocation(hookConfig, logWriter, stackTraceCollector)
                ));
                break;
            case HookConfigTypeEnum.ACTIVITY_LOG:
                Pine.hook(target, new InterceptorInvocationAdapter(
                        new StartActivityLogHookInvocation(hookConfig, logWriter, stackTraceCollector)
                ));
                break;
            case HookConfigTypeEnum.VPN:
                Pine.hook(target, new InterceptorInvocationAdapter(
                        new VpnHookInvocation(hookConfig, logWriter, stackTraceCollector)
                ));
                break;
            case HookConfigTypeEnum.ASSETS:
                registerSingleton(type, () -> AssetsHook.hook(logWriter));
                break;
            case HookConfigTypeEnum.FILE_READ:
                registerSingleton(type, () -> FileReadHookInvocation.hook(logWriter));
                break;
            case HookConfigTypeEnum.FILE_WRITE:
                registerSingleton(type, () -> FileWriteHookInvocation.hook(logWriter));
                break;
            case HookConfigTypeEnum.FILE_DELETE:
                registerSingleton(type, () -> FileDeleteHookInvocation.hook(logWriter));
                break;
            case HookConfigTypeEnum.SIGNATURE:
                registerSingleton(type, () -> SignatureHookInvocation.hook(classLoader, logWriter));
                break;
            case HookConfigTypeEnum.SCREEN:
                registerSingleton(type, ScreenHook::hook);
                break;
            default:
                runtimeLogger.warn("未知 Hook 类型，已跳过：" + type);
                break;
        }
    }

    private void registerSingleton(int type, ThrowingRunnable runnable) throws Exception {
        if (singletonHooks.contains(type)) {
            return;
        }
        runnable.run();
        singletonHooks.add(type);
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
