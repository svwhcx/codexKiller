package com.svwh.noenvhook.log;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.svwh.noenvhook.app.KillerBaseApplication;
import com.svwh.noenvhook.db.KillerDataBaseHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogService implements HookLogWriter {

    private final KillerDataBaseHelper killerDataBaseHelper;
    private final StackTraceCollector stackTraceCollector;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Context context;

    public LogService() {
        this(KillerBaseApplication.context, new FilteredStackTraceCollector());
    }

    public LogService(Context context, StackTraceCollector stackTraceCollector) {
        this.context = context;
        this.killerDataBaseHelper = new KillerDataBaseHelper(context);
        this.stackTraceCollector = stackTraceCollector;
    }

    @Override
    public void save(Log log) {
        if (log.getStackTrace() == null || log.getStackTrace().isEmpty()) {
            log.setStackTrace(stackTraceCollector.collect());
        }
        log.setTime(LocalDateTime.now().format(dateTimeFormatter));
        try (SQLiteDatabase db = killerDataBaseHelper.getWritableDatabase()) {
            db.beginTransaction();
            try {
                ContentValues values = getContentValues(context, log);
                db.insert("HookLog", null, values);
                android.util.Log.i("Killer_Hook", "成功插入一条日志：" + log);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (Exception ignored) {
            android.util.Log.e("Killer_Hook", "添加数据库发生了错误");
        }
    }

    @NonNull
    private static ContentValues getContentValues(Context context, Log log) {
        ContentValues values = new ContentValues();
        values.put("time", log.getTime());
        values.put("title", log.getTitle());
        values.put("content", log.getContent());
        values.put("type", log.getType());
        values.put("status", log.getStatus());
        values.put("exp", log.getExp());
        values.put("isRead", 0);
        values.put("packageName", context == null ? "" : context.getPackageName());
        values.put("stackTrace", log.getStackTrace());
        return values;
    }

    private static final class LogServiceHolder {
        static final LogService LOG_SERVICE = new LogService();
    }

    public static LogService getInstance() {
        return LogServiceHolder.LOG_SERVICE;
    }
}
