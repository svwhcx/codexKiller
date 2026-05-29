package com.svwh.noenvhook.log;

/**
 * @description
 * @Author chenxin
 * @Date 2025/5/10 23:09
 */
public class LogQueryType {

    /**
     * 日志列表的查询方式
     */
    public static final String LOG_LIST = "0";

    /**
     * 查看日志详情的方式
     */
    public static final String LOG_DETAIL = "1";

    /**
     * 删除日志列表的方式
     */
    public static final String LOG_DELETE = "2";

    /**
     * 搜索列表的方式
     */
    public static final String LOG_SEARCH = "3";

    /**
     * 阅读一个日志
     */
    public static final String READ_LOG = "4";

    /**
     * 日志列表的偏移查询方式
     */
    public static final String LOG_LIST_OFFSET = "5";

    /**
     * 删除全部
     */
    public static final String DELETE_ALL = "6";

    /**
     * 删除已经勾选的类型
     */
    public static final String DELETE_TYPE_ALL = "7";
}
