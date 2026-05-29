package com.svwh.noenvhook.config;

import android.content.Context;

import com.svwh.noenvhook.conf.HookConfig;

import java.util.List;

/**
 * @description
 * @Author chenxin
 * @Date 2025/10/19 21:26
 */
public interface IHookConfigService {


    List<HookConfig> queryHookConfig(Context context);
}
