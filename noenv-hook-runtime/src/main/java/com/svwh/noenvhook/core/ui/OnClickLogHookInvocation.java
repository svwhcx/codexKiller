package com.svwh.noenvhook.core.ui;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.svwh.noenvhook.conf.HookConfig;
import com.svwh.noenvhook.core.LogInvocation;
import com.svwh.noenvhook.framework.HookCallFrame;
import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.StackTraceCollector;
import com.svwh.noenvhook.management.HookConfigTypeEnum;

import java.time.LocalDateTime;


public class OnClickLogHookInvocation extends LogInvocation {

    public OnClickLogHookInvocation(
            HookConfig hookConfig,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector
    ) {
        super(hookConfig, logWriter, stackTraceCollector);
    }

    @Override
    public Object replaceMethodHook(HookCallFrame callFrame) {
        View.OnClickListener clickListener = (View.OnClickListener) callFrame.getArg(0);
        callFrame.setArg(0, buildLogClickListener(clickListener));
        try {
            return callFrame.invokeOriginal();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private View.OnClickListener buildLogClickListener(View.OnClickListener originalClickListener) {
        return view -> {
            com.svwh.noenvhook.log.Log log = new com.svwh.noenvhook.log.Log();
            log.setType(HookConfigTypeEnum.CLICK);
            log.setTitle(hookConfig.getConfigName());
            log.setTime(LocalDateTime.now().format(dataTimeFormatter));
            StringBuilder contentBuilder = new StringBuilder();
            Log.i("Killer_Hook", "回调类：" + originalClickListener);
            Log.i("Killer_Hook", "view: " + view);

            contentBuilder.append("\n");
            contentBuilder.append("类名：").append(originalClickListener.getClass().getTypeName()).append("\n");
            contentBuilder.append("控件类型：").append(view.getClass().getTypeName()).append("\n");
            contentBuilder.append("控件id: ").append(Integer.toHexString(view.getId())).append("\n");
            StringBuilder textBuilder = new StringBuilder();
            traverseViewsAndSaveText(view, textBuilder);
            contentBuilder.append("控件内容: ").append(textBuilder).append("\n");
            contentBuilder.append("回调类：").append(originalClickListener.getClass().getTypeName()).append("\n");
            contentBuilder.append("\n");
            contentBuilder.append("返回值类型：void\n");
            contentBuilder.append("返回值：void\n\n");
            logStackElement(log);
            log.setContent(contentBuilder.toString());
            saveLog(log);
            originalClickListener.onClick(view);
        };
    }

    private void traverseViewsAndSaveText(View view, StringBuilder stringBuilder) {
        if (view == null) {
            return;
        }
        if (view instanceof TextView) {
            String text = ((TextView) view).getText().toString();
            if (!text.isEmpty()) {
                stringBuilder.append(text).append(" ");
            }
        } else if (view instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) view;
            for (int i = 0; i < parent.getChildCount(); i++) {
                traverseViewsAndSaveText(parent.getChildAt(i), stringBuilder);
            }
        }
    }
}
