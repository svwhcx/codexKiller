package com.svwh.noenvhook.core.custom;

import com.svwh.noenvhook.conf.ChangeConfig;
import com.svwh.noenvhook.conf.HookConfig;
import com.svwh.noenvhook.core.LogInvocation;
import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.Log;
import com.svwh.noenvhook.log.StackTraceCollector;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import top.canyie.pine.Pine;

public class ArrounHookInvocation extends LogInvocation {

    public ArrounHookInvocation(
            HookConfig hookConfig,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector
    ) {
        super(hookConfig, logWriter, stackTraceCollector);
    }

    public ArrounHookInvocation(HookConfig hookConfig) {
        super(hookConfig);
    }

    @Override
    public Object replaceMethodHook(Pine.CallFrame callFrame) {
        Log log = new Log();
        log.setTitle(hookConfig.getConfigName());
        log.setTime(LocalDateTime.now().format(dataTimeFormatter));

        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("类名：").append(callFrame.thisObject.getClass().getCanonicalName()).append("\n");
        contentBuilder.append("方法名：").append(callFrame.method.getName()).append("\n");
        if (Boolean.TRUE.equals(hookConfig.getLog())) {
            for (int i = 0; i < callFrame.args.length; i++) {
                Object arg = callFrame.args[i];
                contentBuilder.append("参数").append(i + 1).append("：")
                        .append(arg == null ? "null" : arg.getClass().getCanonicalName()).append("\n");
                contentBuilder.append("参数值：").append(arg).append("\n");
            }
        }

        Object resVal = null;
        if (!Boolean.TRUE.equals(hookConfig.getInterrupt())) {
            try {
                resVal = callFrame.invokeOriginalMethod();
            } catch (Exception e) {
                android.util.Log.e("Killer_Hook", "出现了错误");
            }
            if (hookConfig.getChangeConfigs() != null) {
                for (ChangeConfig changeConfig : hookConfig.getChangeConfigs()) {
                    if (changeConfig.getParamNum() < 1 && changeConfig.getTarget() != null && !changeConfig.getTarget().isEmpty()) {
                        if ("void".equals(((Method) callFrame.method).getReturnType().getName())) {
                            resVal = changeConfig.getTarget();
                        }
                    }
                }
            }
        }

        contentBuilder.append("返回值类型：").append(((Method) callFrame.method).getReturnType().getCanonicalName()).append("\n");
        contentBuilder.append("返回值：").append(resVal).append("\n");
        if (Boolean.TRUE.equals(hookConfig.getInterrupt())) {
            log.setTitle(log.getTitle() + " 已被拦截");
        }
        logStackElement(log);
        log.setContent(contentBuilder.toString());
        if (Boolean.TRUE.equals(hookConfig.getLog())) {
            saveLog(log);
        }

        return resVal;
    }
}
