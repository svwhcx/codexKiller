package com.svwh.noenvhook.core;

import com.svwh.noenvhook.conf.HookConfig;
import com.svwh.noenvhook.log.Log;

import java.util.Date;

import top.canyie.pine.Pine;

/**
 * @description
 * @Author chenxin
 * @Date 2025/4/26 14:37
 */
public class   InterceptInvocation extends LogInvocation {


    public InterceptInvocation(HookConfig hookConfig) {
        super(hookConfig);
    }

    @Override
    public Object replaceMethodHook(Pine.CallFrame callFrame) {
        if (!hookConfig.getInterrupt()) {
            try {
                return callFrame.invokeOriginalMethod();
            } catch (Exception e) {
                return null;
            }
        }
        Log log = new Log();
        log.setTitle(hookConfig.getConfigName() + " 已被拦截");
        // TODO 日期格式化操作
        log.setTime(new Date().toString());
        // 内容是Json但是这里也要构建好，
        log.setContent("拦截执行");
        // 记录拦截日志
        saveLog(log);
        return null;
    }
}
