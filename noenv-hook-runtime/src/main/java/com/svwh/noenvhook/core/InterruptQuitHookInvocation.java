package com.svwh.noenvhook.core;

import com.svwh.noenvhook.conf.HookConfig;
import com.svwh.noenvhook.log.Log;
import com.svwh.noenvhook.management.HookConfigTypeEnum;

import java.time.LocalDateTime;

import top.canyie.pine.Pine;

/**
 * @description 拦截应用闪退的hook配置
 * @Author chenxin
 * @Date 2025/5/22 21:23
 */
public class InterruptQuitHookInvocation extends LogInvocation{
    public InterruptQuitHookInvocation(HookConfig hookConfig) {
        super(hookConfig);
    }

    @Override
    public Object replaceMethodHook(Pine.CallFrame callFrame) {
        Log log = new Log();
        log.setType(HookConfigTypeEnum.INTERRUPT_QUIT);
        log.setTitle(hookConfig.getConfigName());
        log.setTime(LocalDateTime.now().format(dataTimeFormatter));
        // 这里就是记录日志的详细信息了。
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("类名：").append(callFrame.thisObject.getClass().getTypeName()).append("\n");
        contentBuilder.append("\n");
        Throwable throwable =(Throwable) callFrame.args[0];
        contentBuilder.append("错误信息：").append(throwable.getMessage()).append("\n");
        contentBuilder.append("错误日志：").append("\n");
        for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            contentBuilder.append(stackTraceElement.toString()).append("\n");
        }
        logStackElement(log);
        // 内容是Json但是这里也要构建好，
        log.setContent(contentBuilder.toString());
        // 记录拦截日志
        saveLog(log);
        return null;
    }
}
