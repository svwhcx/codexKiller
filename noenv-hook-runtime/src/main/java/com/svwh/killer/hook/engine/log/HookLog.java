package com.svwh.killer.hook.engine.log;

import androidx.annotation.NonNull;

/**
 * @description
 * @Author chenxin
 * @Date 2025/10/24 23:01
 */
public class HookLog {

    /**
     * 日志的id
     */
    private Integer id;

    /**
     * 记录日志时候的时间
     */
    private String time;

    /**
     * 日志的类型,现在目前就默认是0
     */
    private Integer type = 0;

    private String title;

    /**
     * 日志的内容，目前先是JSON格式的吧.
     */
    private String content;

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    /**
     * 调用堆栈的字串
     */
    private String stackTrace = "";

    /**
     * 预留的扩展字段
     */
    private String exp;

    public Boolean getRead() {
        return isRead;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }

    /**
     * 当前日志是否已经被阅读
     */
    private Boolean isRead;

    private Integer status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getExp() {
        return exp;
    }

    public void setExp(String exp) {
        this.exp = exp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @NonNull
    @Override
    public String toString() {
        return "Log{" +
                "id=" + id +
                ", time='" + time + '\'' +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", stackTrace='" + stackTrace + '\'' +
                ", exp='" + exp + '\'' +
                ", isRead=" + isRead +
                ", status=" + status +
                '}';
    }
}
