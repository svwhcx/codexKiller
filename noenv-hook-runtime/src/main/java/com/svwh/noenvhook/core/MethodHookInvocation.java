package com.svwh.noenvhook.core;

import com.svwh.noenvhook.conf.HookConfig;

import top.canyie.pine.Pine;

/**
 * @description
 * @Author chenxin
 * @Date 2025/4/26 11:05
 */
public interface MethodHookInvocation {

    /**
     * 方法前执行的操作
     */
    void beforeMethodHook(Pine.CallFrame callFrame);

    /**
     * 方法后执行的操作
     */
    void afterMethodHook(Pine.CallFrame callFrame);

    /**
     * 直接替换操作
     */
    Object replaceMethodHook(Pine.CallFrame callFrame);

    /**
     * 修改静态字段的值
     */
    void changeStaticField(Pine.CallFrame callFrame);

    /**
     * 修改实例字段的值
     */
    void changeField(Pine.CallFrame callFrame);

    /**
     * 构建一个MethodHook，你懂的。
     */
    void build();
}
