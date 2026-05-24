package com.svwh.killer.hook.engine.log;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

/**
 * @description
 * @Author chenxin
 * @Date 2025/4/26 21:10
 */
public class KillerDataBaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "killer_env.db";
    private static final int DATABASE_VERSION = 1;


    public KillerDataBaseHelper(Context context) {
        // 初始化上下文Db文件，使用Android/media/包名/的目录下新建一个db文件
        super(new MyContext(context, Arrays.stream(Objects.requireNonNull(context).getExternalMediaDirs()).findFirst().get().getPath()), DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "CREATE TABLE HookLog ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "title TEXT,"
                + "time TEXT,"
                + "content TEXT,"
                + "type INTEGER,"
                + "packageName TEXT,"
                + "status INTEGER,"
                + "exp TEXT,"
                + "isRead INTEGER,"
                + "stackTrace TEXT"
                + ")";
        sqLiteDatabase.execSQL(createTable);
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    static class MyContext extends ContextWrapper {

        private String contextPath;

        public MyContext(Context base, String contextPath) {
            super(base);
            this.contextPath = contextPath;
        }

        @Override
        public File getDatabasePath(String name) {
            Log.i("数据库", "name:" + name);
            return new File(contextPath + "/" + name);
        }
    }

}


