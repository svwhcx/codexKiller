package com.svwh.noenvhook.log.query;

import android.database.Cursor;

/**
 * @description
 * @Author chenxin
 * @Date 2025/5/10 23:20
 */
public interface ILogOperation {

    /**
     * 操作，第三方模块调用
     * @param args 参数值列表
     * @return 结果集
     */
    Cursor operation(String[] args);
}
