package com.svwh.noenvhook.core.custom;

import com.svwh.noenvhook.conf.ChangeConfig;
import com.svwh.noenvhook.conf.HookConfig;
import com.svwh.noenvhook.core.LogInvocation;
import com.svwh.noenvhook.framework.HookCallFrame;
import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.Log;
import com.svwh.noenvhook.log.StackTraceCollector;

import java.time.LocalDateTime;


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
    public Object replaceMethodHook(HookCallFrame callFrame) {
        Log log = new Log();
        log.setTitle(hookConfig.getConfigName());
        log.setTime(LocalDateTime.now().format(dataTimeFormatter));

        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("类名：").append(callFrame.getThisObject().getClass().getCanonicalName()).append("\n");
        contentBuilder.append("方法名：").append(callFrame.getMethodName()).append("\n");
        if (Boolean.TRUE.equals(hookConfig.getLog())) {
            Object[] args = callFrame.getArgs();
            for (int i = 0; args != null && i < args.length; i++) {
                Object arg = args[i];
                contentBuilder.append("参数").append(i + 1).append("：")
                        .append(arg == null ? "null" : arg.getClass().getCanonicalName()).append("\n");
                contentBuilder.append("参数值：").append(arg).append("\n");
            }
        }
        if (hookConfig.getChangeConfigs() != null && !hookConfig.getChangeConfigs().isEmpty()) {
            for (int i = 0; i < hookConfig.getChangeConfigs().size(); i++) {
                ChangeConfig changeConfig = hookConfig.getChangeConfigs().get(i);
                contentBuilder.append("替换规则").append(i + 1).append("：")
                        .append("paramNum=").append(changeConfig.getParamNum())
                        .append(", target=").append(changeConfig.getTarget())
                        .append(", replaceValue=").append(changeConfig.getReplaceValue())
                        .append(", condition=").append(changeConfig.getCondition())
                        .append("\n");
            }
        }

        Object resVal = null;
        if (!Boolean.TRUE.equals(hookConfig.getInterrupt())) {
            try {
                resVal = callFrame.invokeOriginal();
            } catch (Throwable e) {
                android.util.Log.e("Killer_Hook", "出现了错误");
            }
            if (hookConfig.getChangeConfigs() != null) {
                for (ChangeConfig changeConfig : hookConfig.getChangeConfigs()) {
                    if (changeConfig.getParamNum() < 1 && changeConfig.getTarget() != null && !changeConfig.getTarget().isEmpty()) {
                        if ("void".equals(callFrame.getReturnType().getName())) {
                            resVal = changeConfig.getTarget();
                        }
                    }
                }
            }
        }

        contentBuilder.append("返回值类型：").append(callFrame.getReturnType().getCanonicalName()).append("\n");
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
