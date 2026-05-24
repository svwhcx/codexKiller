package com.svwh.noenvhook.core.db;

import com.svwh.noenvhook.conf.HookConfig;
import com.svwh.noenvhook.core.LogInvocation;

/**
 * @description
 * @Author chenxin
 * @Date 2025/5/22 21:30
 */
public class InsertDatabaseHookInvocation extends LogInvocation {
    public InsertDatabaseHookInvocation(HookConfig hookConfig) {
        super(hookConfig);
    }
}
