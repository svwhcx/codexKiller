package com.svwh.noenvhook.conf;

import java.util.Objects;

/**
 * @description
 * @Author chenxin
 * @Date 2025/4/26 11:16
 */
public class ChangeConfig {

    /**
     * 第几个参数（0代表返回值）
     */
    private Integer paramNum;

    /**
     * 匹配值
     */
    private String target;

    /**
     * 替换值
     */
    private String replaceValue;

    /**
     * 替换的前置条件（后续可能会增加条件的语法解析）
     */
    private String condition;


    public Integer getParamNum() {
        return paramNum;
    }

    public void setParamNum(Integer paramNum) {
        this.paramNum = paramNum;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getReplaceValue() {
        return replaceValue;
    }

    public void setReplaceValue(String replaceValue) {
        this.replaceValue = replaceValue;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeConfig that = (ChangeConfig) o;
        return Objects.equals(paramNum, that.paramNum) && Objects.equals(target, that.target) && Objects.equals(replaceValue, that.replaceValue) && Objects.equals(condition, that.condition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paramNum, target, replaceValue, condition);
    }
}
