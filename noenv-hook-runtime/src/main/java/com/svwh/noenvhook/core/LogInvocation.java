package com.svwh.noenvhook.core;

import com.svwh.noenvhook.conf.HookConfig;
import com.svwh.noenvhook.framework.HookCallFrame;
import com.svwh.noenvhook.log.FilteredStackTraceCollector;
import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.Log;
import com.svwh.noenvhook.log.LogService;
import com.svwh.noenvhook.log.StackTraceCollector;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class LogInvocation extends BaseHookInvocation {

    protected final DateTimeFormatter dataTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final HookLogWriter logWriter;
    private final StackTraceCollector stackTraceCollector;

    public LogInvocation(
            HookConfig hookConfig,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector
    ) {
        super(hookConfig);
        this.logWriter = logWriter;
        this.stackTraceCollector = stackTraceCollector;
    }

    public LogInvocation(HookConfig hookConfig) {
        this(hookConfig, LogService.getInstance(), new FilteredStackTraceCollector());
    }

    protected void saveLog(Log log) {
        logWriter.save(log);
    }

    @Override
    public Object replaceMethodHook(HookCallFrame callFrame) throws Throwable {
        return null;
    }

    protected void logParams(HookCallFrame callFrame, List<String> logString) {
        Object[] args = callFrame.getArgs();
        for (int i = 0; args != null && i < args.length; i++) {
            Object arg = args[i];
            logString.add("param" + (i + 1) + ": " + (arg == null ? "null" : arg.getClass().getCanonicalName()));
            logString.add("value: " + arg);
        }
    }

    protected void logReturnValue(HookCallFrame callFrame, List<String> logString) {
        logString.add("return type: " + callFrame.getReturnType().getCanonicalName());
    }

    protected void logStackElement(Log log) {
        log.setStackTrace(stackTraceCollector.collect());
    }
}
