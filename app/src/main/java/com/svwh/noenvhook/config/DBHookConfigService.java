package com.svwh.noenvhook.config;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.svwh.noenvhook.conf.ChangeConfig;
import com.svwh.noenvhook.conf.HookConfig;
import com.svwh.noenvhook.runtime.RuntimeConstants;
import com.svwh.noenvhook.runtime.RuntimePaths;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DBHookConfigService implements IHookConfigService {

    private final int envType;

    public DBHookConfigService() {
        this(0);
    }

    public DBHookConfigService(int envType) {
        this.envType = envType;
    }

    @Override
    public List<HookConfig> queryHookConfig(Context context) {
        DataBaseHelper dbHelper = createDatabaseHelper(context);
        if (dbHelper == null) {
            return Collections.emptyList();
        }

        List<HookConfig> configList = new ArrayList<>();
        try {
            SQLiteDatabase database = dbHelper.getReadableDatabase();
            try (Cursor cursor = database.rawQuery(
                    "SELECT * FROM app_hook_config WHERE envType = ? AND packageName = ?",
                    new String[]{String.valueOf(envType), context.getPackageName()}
            )) {
                while (cursor.moveToNext()) {
                    int configId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    HookConfig config = new HookConfig();

                    config.setConfigName(cursor.getString(cursor.getColumnIndexOrThrow("configName")));
                    config.setClassName(cursor.getString(cursor.getColumnIndexOrThrow("className")));
                    config.setMethodName(cursor.getString(cursor.getColumnIndexOrThrow("methodName")));
                    config.setParams(cursor.getString(cursor.getColumnIndexOrThrow("params")));
                    config.setLog(cursor.getInt(cursor.getColumnIndexOrThrow("isLog")) == 1);
                    config.setInterrupt(cursor.getInt(cursor.getColumnIndexOrThrow("isInterrupted")) == 1);

                    String exp = cursor.getString(cursor.getColumnIndexOrThrow("exp"));
                    config.setExp(exp != null ? exp : "");

                    config.setType(cursor.getInt(cursor.getColumnIndexOrThrow("type")));
                    config.setChangeConfigs(queryChangeConfigs(database, configId));
                    configList.add(config);
                }
            }
        } catch (Exception e) {
            Log.e(RuntimeConstants.TAG, "Query Hook config failed", e);
        } finally {
            dbHelper.close();
        }
        return configList;
    }

    private DataBaseHelper createDatabaseHelper(Context context) {
        File configDir = RuntimePaths.configDatabaseDir(context);
        if (configDir == null) {
            Log.w(RuntimeConstants.TAG, "External media directory is unavailable, cannot read Hook config database");
            return null;
        }
        return new DataBaseHelper(configDir.getPath(), context);
    }

    private List<ChangeConfig> queryChangeConfigs(SQLiteDatabase database, int hookConfigId) {
        List<ChangeConfig> changeConfigs = new ArrayList<>();
        try (Cursor cursor = database.rawQuery(
                "SELECT * FROM change_value_config WHERE hookConfigId = ?",
                new String[]{String.valueOf(hookConfigId)}
        )) {
            while (cursor.moveToNext()) {
                ChangeConfig changeConfig = new ChangeConfig();
                changeConfig.setParamNum(cursor.getInt(cursor.getColumnIndexOrThrow("paramNumber")));
                changeConfig.setTarget(cursor.getString(cursor.getColumnIndexOrThrow("matchValue")));
                changeConfig.setReplaceValue(cursor.getString(cursor.getColumnIndexOrThrow("replaceValue")));
                changeConfig.setCondition(cursor.getString(cursor.getColumnIndexOrThrow("rule")));
                changeConfigs.add(changeConfig);
            }
        }
        return changeConfigs;
    }
}
