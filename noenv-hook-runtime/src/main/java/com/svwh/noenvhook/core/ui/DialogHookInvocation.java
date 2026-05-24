package com.svwh.noenvhook.core.ui;

import android.app.Dialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.svwh.noenvhook.conf.HookConfig;
import com.svwh.noenvhook.core.LogInvocation;
import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.Log;
import com.svwh.noenvhook.log.StackTraceCollector;
import com.svwh.noenvhook.management.HookConfigTypeEnum;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import top.canyie.pine.Pine;

public class DialogHookInvocation extends LogInvocation {

    public DialogHookInvocation(
            HookConfig hookConfig,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector
    ) {
        super(hookConfig, logWriter, stackTraceCollector);
    }

    @Override
    public Object replaceMethodHook(Pine.CallFrame callFrame) {
        Object resVal = null;
        try {
            resVal = callFrame.invokeOriginalMethod();
        } catch (Exception e) {
            android.util.Log.e("Killer_Hook", "出现了错误");
        }
        Log log = new Log();
        log.setType(HookConfigTypeEnum.DIALOG);
        log.setTitle(hookConfig.getConfigName());
        log.setTime(LocalDateTime.now().format(dataTimeFormatter));
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("类名：").append(callFrame.thisObject.getClass().getCanonicalName()).append("\n\n");
        Dialog dialog = (Dialog) callFrame.thisObject;
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        boolean interrupt = false;
        if (window != null) {
            View decorView = window.getDecorView();
            List<String> textContents = new ArrayList<>();
            traverseDialogViews(decorView, textContents);
            contentBuilder.append("弹窗内容：\n");
            String expValue = hookConfig.getExp() == null ? "" : hookConfig.getExp();
            String[] split = expValue.split(",");
            for (String text : textContents) {
                for (String exp : split) {
                    if (!exp.isEmpty() && text.contains(exp)) {
                        interrupt = true;
                        break;
                    }
                }
                contentBuilder.append(text).append("\n\n");
            }
        }
        if (interrupt) {
            log.setTitle(hookConfig.getConfigName() + " (已自动拦截)");
            dialog.dismiss();
        }
        contentBuilder.append("\n");
        logStackElement(log);
        log.setContent(contentBuilder.toString());
        saveLog(log);
        return resVal;
    }

    private void traverseDialogViews(View view, List<String> textContents) {
        if (view == null) {
            return;
        }
        if (view instanceof TextView) {
            String text = ((TextView) view).getText().toString().trim();
            if (!text.isEmpty()) {
                textContents.add(text);
            }
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                traverseDialogViews(group.getChildAt(i), textContents);
            }
        }
    }
}
