package com.svwh.noenvhook.log.delete;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.svwh.noenvhook.db.KillerDataBaseHelper;
import com.svwh.noenvhook.log.query.BaseLogOperation;

/**
 * @description 删除全部的日志操作
 * @Author chenxin
 * @Date 2025/6/8 21:05
 */
public class DeleteAllLogOperation extends BaseLogOperation {
    public DeleteAllLogOperation(KillerDataBaseHelper killerDataBaseHelper) {
        super(killerDataBaseHelper);
    }

    @Override
    public Cursor operation(String[] args) {
        SQLiteDatabase sqLiteDatabase = this.killerDataBaseHelper.getWritableDatabase();
        String deleteSQL = "delete from HookLog";
        return  sqLiteDatabase.rawQuery(deleteSQL,null);

    }
}
