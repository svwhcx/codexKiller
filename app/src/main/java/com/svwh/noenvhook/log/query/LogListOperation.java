package com.svwh.noenvhook.log.query;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.svwh.noenvhook.db.KillerDataBaseHelper;

/**
 * @description
 * @Author chenxin
 * @Date 2025/5/10 23:36
 */
public class LogListOperation extends BaseLogOperation {

    public LogListOperation(KillerDataBaseHelper killerDataBaseHelper) {
        super(killerDataBaseHelper);
    }

    @Override
    public Cursor operation(String[] args) {
        SQLiteDatabase sqLiteDatabase = this.killerDataBaseHelper.getReadableDatabase();
        String querySQL = "select id,title,substring(content,1,100) as content,time,type,status from HookLog limit ? offset ?";
        String page = args[1];
        String size = args[2];
        String offset = ((Integer.parseInt(page) -1) * Integer.parseInt(size))+"";
        Log.i("Killer_Hook","分页参数：" + size + ","+offset);
        Log.i("Killer_Hook", "查询日志情况: "+page+","+size);
        return  sqLiteDatabase.rawQuery(querySQL, new String[]{size, offset});
    }
}
