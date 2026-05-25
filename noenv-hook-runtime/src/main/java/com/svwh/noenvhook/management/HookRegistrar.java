package com.svwh.noenvhook.management;

import com.svwh.noenvhook.conf.HookConfig;
import com.svwh.noenvhook.core.StartActivityLogHookInvocation;
import com.svwh.noenvhook.core.custom.ArrounHookInvocation;
import com.svwh.noenvhook.core.crypto.CipherHookInvocation;
import com.svwh.noenvhook.core.crypto.DigestHookInvocation;
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
import com.svwh.noenvhook.framework.HookFramework;
import com.svwh.noenvhook.framework.pine.PineHookFramework;
import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.StackTraceCollector;
import com.svwh.noenvhook.runtime.RuntimeLogger;

import java.lang.reflect.Member;
import java.util.HashSet;
import java.util.Set;

public class HookRegistrar {

    private final HookLogWriter logWriter;
    private final StackTraceCollector stackTraceCollector;
    private final RuntimeLogger runtimeLogger;
    private final HookFramework hookFramework;
    private final Set<Integer> singletonHooks = new HashSet<>();

    public HookRegistrar(
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector,
            RuntimeLogger runtimeLogger
    ) {
        this(logWriter, stackTraceCollector, runtimeLogger, new PineHookFramework());
    }

    public HookRegistrar(
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector,
            RuntimeLogger runtimeLogger,
            HookFramework hookFramework
    ) {
        this.logWriter = logWriter;
        this.stackTraceCollector = stackTraceCollector;
        this.runtimeLogger = runtimeLogger;
        this.hookFramework = hookFramework;
    }

    public void register(ClassLoader classLoader, Member target, HookConfig hookConfig) throws Exception {
        Integer type = hookConfig.getType();
        if (type == null) {
            runtimeLogger.warn("Hook 类型为空，跳过：" + hookConfig.getConfigName());
            return;
        }

        switch (type) {
            case HookConfigTypeEnum.CUSTOMER:
                hookFramework.hookReplacement(target,
                        new ArrounHookInvocation(hookConfig, logWriter, stackTraceCollector)
                );
                break;
            case HookConfigTypeEnum.CLICK:
                hookFramework.hookReplacement(target,
                        new OnClickLogHookInvocation(hookConfig, logWriter, stackTraceCollector)
                );
                break;
            case HookConfigTypeEnum.DIALOG:
                hookFramework.hookReplacement(target,
                        new DialogHookInvocation(hookConfig, logWriter, stackTraceCollector)
                );
                break;
            case HookConfigTypeEnum.TEXT_SET:
                hookFramework.hookReplacement(target,
                        new TextSetHookInvocation(hookConfig, logWriter, stackTraceCollector)
                );
                break;
            case HookConfigTypeEnum.TOAST_SHOW:
                hookFramework.hookReplacement(target,
                        new ToastShowHookInvocation(hookConfig, logWriter, stackTraceCollector)
                );
                break;
            case HookConfigTypeEnum.ACTIVITY_LOG:
                hookFramework.hookReplacement(target,
                        new StartActivityLogHookInvocation(hookConfig, logWriter, stackTraceCollector)
                );
                break;
            case HookConfigTypeEnum.VPN:
                hookFramework.hookReplacement(target,
                        new VpnHookInvocation(hookConfig, logWriter, stackTraceCollector)
                );
                break;
            case HookConfigTypeEnum.ASSETS:
                registerSingleton(type, () -> AssetsHook.hook(hookFramework, logWriter));
                break;
            case HookConfigTypeEnum.FILE_READ:
                registerSingleton(type, () -> FileReadHookInvocation.hook(hookFramework, logWriter));
                break;
            case HookConfigTypeEnum.FILE_WRITE:
                registerSingleton(type, () -> FileWriteHookInvocation.hook(hookFramework, logWriter));
                break;
            case HookConfigTypeEnum.FILE_DELETE:
                registerSingleton(type, () -> FileDeleteHookInvocation.hook(hookFramework, logWriter));
                break;
            case HookConfigTypeEnum.SIGNATURE:
                registerSingleton(type, () -> SignatureHookInvocation.hook(hookFramework, classLoader, logWriter));
                break;
            case HookConfigTypeEnum.SCREEN:
                registerSingleton(type, () -> ScreenHook.hook(hookFramework));
                break;
            case HookConfigTypeEnum.DIGEST:
                registerSingleton(type, () -> DigestHookInvocation.hook(hookFramework, logWriter, stackTraceCollector));
                break;
            case HookConfigTypeEnum.CIPHER:
                registerSingleton(type, () -> CipherHookInvocation.hook(hookFramework, logWriter, stackTraceCollector));
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
