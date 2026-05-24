package com.svwh.noenvhook.log.query;

import android.database.Cursor;

import com.svwh.noenvhook.db.KillerDataBaseHelper;

/**
 * @description
 * @Author chenxin
 * @Date 2025/5/10 23:49
 */
public abstract class BaseLogOperation implements ILogOperation{

    protected KillerDataBaseHelper killerDataBaseHelper;

    public BaseLogOperation(KillerDataBaseHelper killerDataBaseHelper)
    {
        this.killerDataBaseHelper = killerDataBaseHelper;
    }

    abstract public Cursor operation(String[] args);
}
