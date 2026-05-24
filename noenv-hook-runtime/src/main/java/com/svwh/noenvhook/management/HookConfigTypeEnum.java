package com.svwh.noenvhook.management;

/**
 * @description
 * @Author chenxin
 * @Date 2025/5/18 0:23
 */
public interface HookConfigTypeEnum {

    int CUSTOMER = 0;

    int MD5 = 2;

    int AES = 3;
    int HMAC = 4;

    int CLICK = 5;

    int DIALOG = 6;

    int KEY_WORLD_DIALOG = 7;

    /**
     * 控件文本赋值操作
     */
    int TEXT_SET = 8;

    /**
     * Toast显示
     */
    int TOAST_SHOW = 9;

    /**
     * Activity显示
     */
    int ACTIVITY_LOG = 10;

    /**
     * webview 可调试
     */
    int WEB_VIEW_DEBUGGABLE = 11;

    /**
     * webView 加载url
     */
    int WEB_VIEW_LOAD_URL = 12;

    /**
     * Application监听
     */
    int APPLICATION = 13;

    /**
     * 防止应用闪退
     */
    int INTERRUPT_QUIT = 14;

    /**
     * 拦截应用退出
     */
    int QUIT = 15;

    /**
     * 截屏录屏限制
     */
    int SCREEN = 16;

    /**
     * 读取应用签名监听
     */
    int SIGNATURE = 17;

    /**
     * 日志捕获
     */
    int LOG = 18;

    /**
     * 执行SQL
     */
    int INVOKE_SQL = 19;

    /**
     * 从数据库中查询数据
     */
    int QUERY_DATABASE = 20;

    /**
     * 更新数据库
     */
    int UPDATE_DATABASE = 21;

    /**
     * 删除数据库
     */
    int DELETE_DATABASE = 22;

    /**
     * 插入数据库
     */
    int INSERT_DATABASE = 23;

    /**
     * 打开数据库
     */
    int OPEN_DATABASE = 24;

    /**
     * 隐藏wifi
     */
    int WIFI = 25;

    /**
     * 隐藏VPN
     */
    int VPN = 26;

    /**
     * 文件写入
     */
    int FILE_WRITE = 27;

    /**
     * 文件读取
     */
    int FILE_READ = 28;

    /**
     * 文件删除
     */
    int FILE_DELETE = 29;

    /**
     * shell命令执行
     */
    int SHELL = 30;

    /**
     * assets资源加载
     */
    int ASSETS = 31;

    /**
     * shared_preference 读取监听
     */
    int SP_READ = 32;

    /**
     * share_preference 写入监听
     */
    int SP_WRITE = 33;
}
