package com.svwh.noenvhook.conf;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Objects;

/**
 * @description
 * @Author chenxin
 * @Date 2025/4/26 11:07
 */
public class HookConfig {

    /**
     * hook配置的名称
     */
    private String configName;

    /**
     * hook配置的类名
     */
    private String className;

    private String methodName;

    /**
     * hook配置的参数签名列表（未进行转换的）
     */
    private String params;

    private List<ChangeConfig> changeConfigs;

    /**
     * 是否记录日志
     */
    private Boolean isLog;

    /**
     * 是否拦截执行
     */
    private Boolean isInterrupt;

    /**
     * 扩展配置字段 （需要后续能够解析的那种）
     */
    private String exp;

    /**
     * 类型
     */
    private Integer type;


    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public List<ChangeConfig> getChangeConfigs() {
        return changeConfigs;
    }

    public void setChangeConfigs(List<ChangeConfig> changeConfigs) {
        this.changeConfigs = changeConfigs;
    }

    public Boolean getInterrupt() {
        return isInterrupt;
    }

    public void setInterrupt(Boolean interrupt) {
        isInterrupt = interrupt;
    }

    public Boolean getLog() {
        return isLog;
    }

    public void setLog(Boolean log) {
        isLog = log;
    }

    public String getExp() {
        return exp;
    }

    public void setExp(String exp) {
        this.exp = exp;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HookConfig that = (HookConfig) o;
        return Objects.equals(configName, that.configName) && Objects.equals(className, that.className) && Objects.equals(methodName, that.methodName) && Objects.equals(params, that.params) && Objects.equals(changeConfigs, that.changeConfigs) && Objects.equals(isLog, that.isLog) && Objects.equals(isInterrupt, that.isInterrupt) && Objects.equals(exp, that.exp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configName, className, methodName, params, changeConfigs, isLog, isInterrupt, exp);
    }

    @Override
    public String toString() {
        return "HookConfig{" +
                "configName='" + configName + '\'' +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", params='" + params + '\'' +
                ", changeConfigs=" + changeConfigs +
                ", isLog=" + isLog +
                ", isInterrupt=" + isInterrupt +
                ", exp='" + exp + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
