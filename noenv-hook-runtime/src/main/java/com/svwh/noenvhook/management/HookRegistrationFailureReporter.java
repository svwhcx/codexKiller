package com.svwh.noenvhook.management;

import com.svwh.noenvhook.conf.HookConfig;
import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.Log;
import com.svwh.noenvhook.runtime.RuntimeLogger;

import java.time.LocalDateTime;

public class HookRegistrationFailureReporter {

    private final HookLogWriter logWriter;
    private final RuntimeLogger runtimeLogger;

    public HookRegistrationFailureReporter(HookLogWriter logWriter, RuntimeLogger runtimeLogger) {
        this.logWriter = logWriter;
        this.runtimeLogger = runtimeLogger;
    }

    public void report(HookConfig hookConfig, Exception e) {
        runtimeLogger.error(
                "注册 Hook 失败：" + hookConfig.getClassName() + "#" + hookConfig.getMethodName(),
                e
        );

        Log log = new Log();
        log.setTitle("Hook 注册失败");
        log.setTime(LocalDateTime.now().toString());
        log.setContent(
                "目标：" + hookConfig.getClassName() + "#" + hookConfig.getMethodName()
                        + "，原因：" + safeMessage(e)
        );
        logWriter.save(log);
    }

    private String safeMessage(Exception e) {
        String message = e.getMessage();
        return message == null || message.isEmpty() ? e.getClass().getSimpleName() : message;
    }
}
