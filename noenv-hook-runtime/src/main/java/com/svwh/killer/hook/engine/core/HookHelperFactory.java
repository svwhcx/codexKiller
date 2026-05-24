package com.svwh.killer.hook.engine.core;

/**
 * @description
 * @Author chenxin
 * @Date 2025/10/24 22:51
 */
public class HookHelperFactory {


    public static HookHelper getHelper(){
        return new PineHookHelper();
    }
}
