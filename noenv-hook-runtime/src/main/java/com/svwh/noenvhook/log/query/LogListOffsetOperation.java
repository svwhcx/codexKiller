package com.svwh.noenvhook.log.query;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.svwh.noenvhook.db.KillerDataBaseHelper;

import java.util.Arrays;

/**
 * @description
 * @Author chenxin
 * @Date 2025/6/5 23:15
 */
public class LogListOffsetOperation extends BaseLogOperation{

    public LogListOffsetOperation(KillerDataBaseHelper killerDataBaseHelper) {
        super(killerDataBaseHelper);
    }

    @Override
    public Cursor operation(String[] args) {
        SQLiteDatabase sqLiteDatabase = this.killerDataBaseHelper.getReadableDatabase();
        String offset = args[1];
        String size = args[2];
        int length = Integer.parseInt(args[3]);
        if (length < 0){
            return new MatrixCursor(null);
        }
        String querySQL = "";
        // 判断是否是搜索模式
        if (args.length > 4+ length && !args[4 + length].isEmpty()){
            String searchContent = args[length + 4];
            // 构建搜索模式下的结果
            querySQL = "select id,title,time,type,status,isRead,substr(content, max(1, instr(content, " + "'" + searchContent+ "') - 5), 100) as content from HookLog where content like" +"'"+"%"+searchContent+"%' and type in (";
        }else{
            querySQL = "select id,title,substring(content,1,100) as content,time,type,status,isRead from HookLog where type in (";
        }
        querySQL +=  String.join(",", Arrays.asList(args).subList(4, length + 4)) + ") limit ? offset ?";
        Log.i("查询语句：",querySQL);
        Log.i("Killer_Hook","分页参数：" + size + ","+offset);
        return  sqLiteDatabase.rawQuery(querySQL, new String[]{size, offset});
    }
}
