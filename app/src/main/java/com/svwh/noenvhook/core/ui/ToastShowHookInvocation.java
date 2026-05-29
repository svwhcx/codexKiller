package com.svwh.noenvhook.core.ui;

import com.svwh.noenvhook.conf.HookConfig;
import com.svwh.noenvhook.core.LogInvocation;
import com.svwh.noenvhook.framework.HookCallFrame;
import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.Log;
import com.svwh.noenvhook.log.StackTraceCollector;
import com.svwh.noenvhook.management.HookConfigTypeEnum;

import java.time.LocalDateTime;


public class ToastShowHookInvocation extends LogInvocation {

    public ToastShowHookInvocation(
            HookConfig hookConfig,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector
    ) {
        super(hookConfig, logWriter, stackTraceCollector);
    }

    @Override
    public Object replaceMethodHook(HookCallFrame callFrame) {
        Log log = new Log();
        log.setType(HookConfigTypeEnum.DIALOG);
        log.setTitle(hookConfig.getConfigName());
        log.setTime(LocalDateTime.now().format(dataTimeFormatter));
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("类名：").append(callFrame.getMember().getClass().getCanonicalName()).append("\n\n");
        contentBuilder.append("提示内容：").append(callFrame.getArg(1)).append("\n");

        Object resVal = null;
        try {
            resVal = callFrame.invokeOriginal();
        } catch (Throwable e) {
            android.util.Log.e("Killer_Hook", "出现了错误");
        }
        contentBuilder.append("返回值类型：void\n");
        contentBuilder.append("返回值：void\n\n");
        logStackElement(log);
        log.setContent(contentBuilder.toString());
        saveLog(log);
        return resVal;
    }
}
