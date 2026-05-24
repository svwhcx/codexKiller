package com.svwh.noenvhook.log.delete;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;

import com.svwh.noenvhook.db.KillerDataBaseHelper;
import com.svwh.noenvhook.log.query.BaseLogOperation;

import java.util.Arrays;

/**
 * @description 删除选中的类型的日志的删除操作
 * @Author chenxin
 * @Date 2025/6/8 21:06
 */
public class DeleteAllTypeLogOperation extends BaseLogOperation {

    public DeleteAllTypeLogOperation(KillerDataBaseHelper killerDataBaseHelper) {
        super(killerDataBaseHelper);
    }

    @Override
    public Cursor operation(String[] args) {
        SQLiteDatabase sqLiteDatabase = this.killerDataBaseHelper.getWritableDatabase();
        // 有几个参数
        int length = Integer.parseInt(args[1]);
        if (length < 0){
            return new MatrixCursor(null);
        }
        String deleteSQL = "delete from HookLog where type in (";
        deleteSQL +=  String.join(",",Arrays.asList(args).subList(2, length + 2)) + ")";
        return  sqLiteDatabase.rawQuery(deleteSQL,null);
    }
}
