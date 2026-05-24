package com.svwh.noenvhook.core;


import com.svwh.noenvhook.conf.HookConfig;
import com.svwh.noenvhook.log.Log;

import java.time.LocalDateTime;

import top.canyie.pine.Pine;

/**
 * @description
 * @Author chenxin
 * @Date 2025/5/22 21:23
 */
public class QuitHookInvocation extends LogInvocation{
    public QuitHookInvocation(HookConfig hookConfig) {
        super(hookConfig);
    }

    @Override
    public Object replaceMethodHook(Pine.CallFrame callFrame) {
        Log log = new Log();
        log.setTitle(hookConfig.getConfigName());
        log.setTime(LocalDateTime.now().format(dataTimeFormatter));
        // 这里就是记录日志的详细信息了。
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("类名：").append(callFrame.thisObject.getClass().getTypeName()).append("\n");
        contentBuilder.append("\n");
        logStackElement(log);
        // 内容是Json但是这里也要构建好，
        log.setContent(contentBuilder.toString());
        // 记录拦截日志
        saveLog(log);
        return null;
    }
}
