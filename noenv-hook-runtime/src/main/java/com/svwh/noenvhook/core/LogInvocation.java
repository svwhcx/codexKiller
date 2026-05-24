package com.svwh.noenvhook.core;

import com.svwh.noenvhook.conf.HookConfig;
import com.svwh.noenvhook.log.FilteredStackTraceCollector;
import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.Log;
import com.svwh.noenvhook.log.LogService;
import com.svwh.noenvhook.log.StackTraceCollector;

import java.lang.reflect.Method;
import java.time.format.DateTimeFormatter;
import java.util.List;

import top.canyie.pine.Pine;

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
    public Object replaceMethodHook(Pine.CallFrame callFrame) {
        return null;
    }

    protected void logParams(Pine.CallFrame callFrame, List<String> logString) {
        for (int i = 0; i < callFrame.args.length; i++) {
            Object arg = callFrame.args[i];
            logString.add("参数" + (i + 1) + "：" + (arg == null ? "null" : arg.getClass().getCanonicalName()));
            logString.add("参数值：" + arg);
        }
    }

    protected void logReturnValue(Pine.CallFrame callFrame, List<String> logString) {
        logString.add("返回值类型：" + ((Method) callFrame.method).getReturnType().getCanonicalName());
    }

    protected void logStackElement(Log log) {
        log.setStackTrace(stackTraceCollector.collect());
    }
}
