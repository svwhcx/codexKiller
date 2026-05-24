package com.svwh.noenvhook.core;

import android.telecom.Call;

import com.svwh.noenvhook.conf.ChangeConfig;
import com.svwh.noenvhook.conf.HookConfig;

import top.canyie.pine.Pine;

/**
 * @description
 * @Author chenxin
 * @Date 2025/4/26 14:28
 */
public abstract class BaseHookInvocation implements MethodHookInvocation{

    protected HookConfig hookConfig;

    public BaseHookInvocation(HookConfig hookConfig){
        this.hookConfig = hookConfig;
    }

    /**
     * 修改方法的参数值
     * TODO 是否记录修改前的和修改后的参数
     * @param callFrame
     */
    protected void changeParam(Pine.CallFrame callFrame){
        for (ChangeConfig changeConfig : hookConfig.getChangeConfigs()) {
            if (changeConfig.getParamNum() > 0){
                // 强制类型转换
                Object cast = callFrame.args[changeConfig.getParamNum()].getClass().cast(changeConfig.getTarget());
                callFrame.args[changeConfig.getParamNum()] = cast;
            }
        }
    }

    protected void changeResult(Pine.CallFrame callFrame){

    }


    @Override
    public void beforeMethodHook(Pine.CallFrame callFrame) {

    }

    @Override
    public void afterMethodHook(Pine.CallFrame callFrame) {

    }

    @Override
    public void changeStaticField(Pine.CallFrame callFrame) {

    }

    @Override
    public void changeField(Pine.CallFrame callFrame) {

    }

    @Override
    public void build() {

    }
}
