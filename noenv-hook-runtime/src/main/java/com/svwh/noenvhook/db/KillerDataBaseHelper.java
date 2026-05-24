package com.svwh.noenvhook.db;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

/**
 * 数据库操作相关
 *
 * @description
 * @Author chenxin
 * @Date 2025/4/26 21:10
 */
public class KillerDataBaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "killer_hook_no_env.db";
    private static final int DATABASE_VERSION = 1;

    public KillerDataBaseHelper(@Nullable Context context) {
//        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // 初始化上下文Db文件，使用Android/media/包名/的目录下新建一个db文件
        super(new MyContext(context, Arrays.stream(Objects.requireNonNull(context).getExternalMediaDirs()).findFirst().get().getPath()), DATABASE_NAME,
                null, DATABASE_VERSION);
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

        private final String contextPath;

        public MyContext(Context base, String contextPath) {
            super(base);
            this.contextPath = contextPath;
        }

        @Override
        public File getDatabasePath(String name) {
            return new File(contextPath + "/" + name);
        }
    }
}
