package com.svwh.killer.hook.engine.log;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @description
 * @Author chenxin
 * @Date 2025/10/24 22:58
 */
public class HookLogService {


    private static final DateTimeFormatter dataTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static volatile KillerDataBaseHelper killerDataBaseHelper;
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public static void init(Context context){
        if (killerDataBaseHelper == null){
            synchronized (HookLogService.class){
                if (killerDataBaseHelper == null) {
                    HookLogService.context = context;
                    killerDataBaseHelper = new KillerDataBaseHelper(context);
                }
            }
        }
    }

    /**
     * 记录日志
     * @param log 日志
     */
    public static void saveLog(HookLog log){
        // 这里进行日志的写入操作，但是现在有一个问题就是。
        logStackElement(log);
        log.setTime(LocalDateTime.now().format(dataTimeFormatter));
        try (SQLiteDatabase db = killerDataBaseHelper.getWritableDatabase();) {
            db.beginTransaction();
            try {

                ContentValues values = getContentValues(log);
                db.insert("HookLog", null, values);
                android.util.Log.i("Killer_Hook", "成功插入一条日志：" + log);
                db.setTransactionSuccessful();
            }finally {
                db.endTransaction();
            }
        }catch (Exception ignored){
            android.util.Log.e("Killer_Hook","添加数据库发生了错误！");
        }
    }

    private static final List<String> logFilters = new ArrayList<>();

    public static void addFilter(String logFilter){
        logFilters.add(logFilter);
    }

    public static void addFilters(List<String> allLogFilters){
        logFilters.addAll(allLogFilters);
    }


    /**
     * 记录堆栈
     * @param hookLog 日志实体
     */
    protected static void logStackElement(HookLog hookLog){
        // 记录调用堆栈
        try{
            throw new RuntimeException();
        }catch (Exception e){
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                // 过滤noHookEnv的堆栈信息
                String msg = stackTraceElement.toString();
                if (logFilters.stream().noneMatch(msg::contains)){
                    hookLog.setStackTrace(hookLog.getStackTrace() + "at: " + msg + ",");
                }
            }
        }

    }


    private static ContentValues getContentValues(HookLog log) {
        ContentValues values = new ContentValues();
        values.put("time", log.getTime());
        values.put("title", log.getTitle());
        values.put("content", log.getContent());
        values.put("type", log.getType());
        values.put("status", log.getStatus());
        values.put("exp", log.getExp());
        values.put("isRead",0);
        values.put("packageName",context.getPackageName());
        values.put("stackTrace", log.getStackTrace());
        return values;
    }
}
