package com.svwh.noenvhook.core.ui;

import android.view.View;
import android.widget.TextView;

import com.svwh.noenvhook.conf.HookConfig;
import com.svwh.noenvhook.core.LogInvocation;
import com.svwh.noenvhook.framework.HookCallFrame;
import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.Log;
import com.svwh.noenvhook.log.StackTraceCollector;
import com.svwh.noenvhook.management.HookConfigTypeEnum;

import java.time.LocalDateTime;


public class TextSetHookInvocation extends LogInvocation {

    public TextSetHookInvocation(
            HookConfig hookConfig,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector
    ) {
        super(hookConfig, logWriter, stackTraceCollector);
    }

    @Override
    public Object replaceMethodHook(HookCallFrame callFrame) {
        Log log = new Log();
        log.setType(HookConfigTypeEnum.TEXT_SET);
        log.setTitle(hookConfig.getConfigName());
        log.setTime(LocalDateTime.now().format(dataTimeFormatter));
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("类名：").append(callFrame.getThisObject().getClass().getCanonicalName()).append("\n\n");
        contentBuilder.append("控件类型：").append(callFrame.getThisObject().getClass().getCanonicalName()).append("\n");
        View view = (View) callFrame.getThisObject();
        if (view instanceof TextView) {
            contentBuilder.append("控件id：").append(Integer.toHexString(view.getId())).append("\n");
            android.util.Log.i("Killer_Hook", "控件文本：" + callFrame.getArg(0));
            contentBuilder.append("控件文本：").append(callFrame.getArg(0)).append("\n");
        }
        Object resVal = null;
        try {
            resVal = callFrame.invokeOriginal();
        } catch (Throwable e) {
            android.util.Log.e("Killer_Hook", "出现了错误");
        }
        contentBuilder.append("\n");
        contentBuilder.append("返回值类型：").append(callFrame.getReturnType().getCanonicalName()).append("\n");
        contentBuilder.append("返回值：").append(resVal).append("\n\n");
        logStackElement(log);
        log.setContent(contentBuilder.toString());
        saveLog(log);
        return resVal;
    }
}
