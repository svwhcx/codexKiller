package com.svwh.noenvhook.config;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.svwh.noenvhook.conf.ChangeConfig;
import com.svwh.noenvhook.conf.HookConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 通过 Provider 获取配置信息
 * @description
 * @Author chenxin
 * @Date 2025/10/19 21:28
 */
public class ProviderHookConfigService implements IHookConfigService{


    /**
     * Killer 配置信息提供者
     */
    private static final String URI_TAG = "content://com.svwh.noenvhook.provider/hookconfig";

    @Override
    @SuppressLint("Range")
    public List<HookConfig> queryHookConfig(Context context) {
        List<HookConfig> hookConfigs = new ArrayList<>();
        Uri uri = Uri.parse(URI_TAG);
        Log.i("Killer_Hook", "这是一个日志");
        try {
            Cursor query = context.getContentResolver().query(uri, new String[]{"1", "0", context.getPackageName()}, null, null, null);
            if (query != null) {
                while (query.moveToNext()) {
                    HookConfig hookConfig = new HookConfig();
                    hookConfig.setConfigName(query.getString(query.getColumnIndex("configName")));
                    hookConfig.setClassName(query.getString(query.getColumnIndex("className")));
                    hookConfig.setMethodName(query.getString(query.getColumnIndex("methodName")));
                    hookConfig.setParams(query.getString(query.getColumnIndex("params")));
                    hookConfig.setExp(query.getString(query.getColumnIndex("exp")));
                    hookConfig.setType(query.getInt(query.getColumnIndex("type")));
                    String changeConfigsJson = query.getString(query.getColumnIndex("changeConfigs"));
                    JSONArray changeConfigs = new JSONArray(changeConfigsJson);
                    hookConfig.setChangeConfigs(new ArrayList<>());
                    for (int i = 0; i < changeConfigs.length(); i++) {
                        JSONObject config = changeConfigs.getJSONObject(i);
                        ChangeConfig changeConfig = new ChangeConfig();
                        changeConfig.setCondition(config.getString("rule"));
                        changeConfig.setParamNum(config.getInt("paramNumber"));
                        changeConfig.setTarget(config.getString("targetValue"));
                        changeConfig.setReplaceValue(config.getString("replaceValue"));
                        hookConfig.getChangeConfigs().add(changeConfig);
                    }
                    hookConfig.setInterrupt(query.getInt(query.getColumnIndex("interrupt")) == 1);
                    hookConfig.setLog(query.getInt(query.getColumnIndex("log")) == 1);
                    hookConfigs.add(hookConfig);
                    Log.i("Killer_Hook", "这是一个日志" + hookConfig);

                }
                query.close();
            }
        } catch (Exception e) {
            Log.e("Killer_Hook", "发生了错误");
            throw new RuntimeException(e);
        }
        Log.i("Killer_Hook", "结束了，配置列表：" + hookConfigs);

        return hookConfigs;
    }
}
