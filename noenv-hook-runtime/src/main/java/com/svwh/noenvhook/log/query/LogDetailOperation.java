package com.svwh.noenvhook.log.query;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.svwh.noenvhook.db.KillerDataBaseHelper;

/**
 * @description
 * @Author chenxin
 * @Date 2025/5/11 0:08
 */
public class LogDetailOperation extends BaseLogOperation {

    public LogDetailOperation(KillerDataBaseHelper killerDataBaseHelper) {
        super(killerDataBaseHelper);
    }

    @Override
    public Cursor operation(String[] args) {
        SQLiteDatabase sqLiteDatabase = killerDataBaseHelper.getReadableDatabase();
        String querySQL = "select * from HookLog where id = ?";
        String id = args[1];

        Cursor res = sqLiteDatabase.rawQuery(querySQL, new String[]{id});
        // 阅读日志
        SQLiteDatabase db = killerDataBaseHelper.getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement(
                "UPDATE HookLog SET isRead=? WHERE id=?");
        stmt.bindLong(1, 1);
        stmt.bindLong(2, Long.parseLong(id));
        stmt.execute();


        return res;
    }
}
