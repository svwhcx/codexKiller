package com.svwh.noenvhook.core;

import android.content.Intent;
import android.os.Bundle;

import com.svwh.noenvhook.conf.HookConfig;
import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.Log;
import com.svwh.noenvhook.log.StackTraceCollector;
import com.svwh.noenvhook.management.HookConfigTypeEnum;

import java.time.LocalDateTime;

import top.canyie.pine.Pine;

public class StartActivityLogHookInvocation extends LogInvocation {

    public StartActivityLogHookInvocation(
            HookConfig hookConfig,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector
    ) {
        super(hookConfig, logWriter, stackTraceCollector);
    }

    @Override
    public Object replaceMethodHook(Pine.CallFrame callFrame) {
        Log log = new Log();
        log.setType(HookConfigTypeEnum.ACTIVITY_LOG);
        log.setTitle(hookConfig.getConfigName());
        log.setTime(LocalDateTime.now().format(dataTimeFormatter));
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("类名：").append(callFrame.thisObject.getClass().getCanonicalName()).append("\n\n");

        contentBuilder.append("Intent: ").append(callFrame.args[0]).append("\n\n");
        Intent intent = (Intent) callFrame.args[0];
        contentBuilder.append("Intent Extra：").append(intent.getExtras()).append("\n\n");
        contentBuilder.append("requestCode: ").append(callFrame.args[1]).append("\n\n");
        Bundle bundle = (Bundle) callFrame.args[2];
        if (bundle != null) {
            contentBuilder.append("Extra数据：").append(bundle).append("\n");
        }

        Object resVal = null;
        try {
            resVal = callFrame.invokeOriginalMethod();
        } catch (Exception e) {
            android.util.Log.e("Killer_Hook", "出现了错误");
        }
        contentBuilder.append("\n");
        logStackElement(log);
        log.setContent(contentBuilder.toString());
        saveLog(log);
        return resVal;
    }
}
