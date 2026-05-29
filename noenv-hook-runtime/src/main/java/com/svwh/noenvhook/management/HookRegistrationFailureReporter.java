package com.svwh.noenvhook.management;

import com.svwh.noenvhook.conf.HookConfig;
import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.Log;
import com.svwh.noenvhook.runtime.RuntimeLogger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

public class HookRegistrationFailureReporter {

    private final HookLogWriter logWriter;
    private final RuntimeLogger runtimeLogger;

    public HookRegistrationFailureReporter(HookLogWriter logWriter, RuntimeLogger runtimeLogger) {
        this.logWriter = logWriter;
        this.runtimeLogger = runtimeLogger;
    }

    public void report(HookConfig hookConfig, Throwable e) {
        try {
            runtimeLogger.error(
                    "Hook registration failed: " + safeValue(hookConfig.getClassName()) + "#" + safeValue(hookConfig.getMethodName()),
                    e
            );

            Log log = new Log();
            log.setTitle("Hook registration failed");
            log.setType(HookConfigTypeEnum.HOOK_FAILURE);
            log.setStatus(1);
            log.setTime(LocalDateTime.now().toString());
            log.setContent(
                    "className: " + safeValue(hookConfig.getClassName()) + "\n"
                            + "methodName: " + safeValue(hookConfig.getMethodName()) + "\n"
                            + "params: " + safeValue(hookConfig.getParams()) + "\n"
                            + "methodSignature: " + methodSignatureOf(hookConfig) + "\n"
                            + "configName: " + safeValue(hookConfig.getConfigName()) + "\n"
                            + "hookType: " + hookConfig.getType() + "\n"
                            + "errorType: " + e.getClass().getName() + "\n"
                            + "errorMessage: " + safeMessage(e) + "\n"
            );
            log.setExp(stackTraceOf(e));
            logWriter.save(log);
        } catch (Throwable reportError) {
            runtimeLogger.error("Hook registration failure report failed", reportError);
        }
    }

    private String safeMessage(Throwable e) {
        String message = e.getMessage();
        return message == null || message.isEmpty() ? e.getClass().getSimpleName() : message;
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }

    private String methodSignatureOf(HookConfig hookConfig) {
        return safeValue(hookConfig.getClassName())
                + "#"
                + safeValue(hookConfig.getMethodName())
                + "("
                + safeValue(hookConfig.getParams())
                + ")";
    }

    private String stackTraceOf(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        printWriter.flush();
        return stringWriter.toString();
    }
}
