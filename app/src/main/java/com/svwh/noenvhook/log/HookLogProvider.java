package com.svwh.noenvhook.log;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.svwh.noenvhook.db.KillerDataBaseHelper;
import com.svwh.noenvhook.log.delete.DeleteAllLogOperation;
import com.svwh.noenvhook.log.delete.DeleteAllTypeLogOperation;
import com.svwh.noenvhook.log.query.ILogOperation;
import com.svwh.noenvhook.log.query.LogDetailOperation;
import com.svwh.noenvhook.log.query.LogListOffsetOperation;
import com.svwh.noenvhook.log.query.LogListOperation;

/**
 * @description
 * @Author chenxin
 * @Date 2025/5/3 9:41
 */
public class HookLogProvider extends ContentProvider {

    /**
     * DataBaseHelper成员变量，防止被多次实例化导致数据出现问题
     */
    private KillerDataBaseHelper killerDataBaseHelper;

    @Override
    public boolean onCreate() {
        killerDataBaseHelper = new KillerDataBaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        // 查询日志情况。这里可以进行分页
       try {
           Log.i("Killer_Hook", "开始查询日志");
           ILogOperation iOperation = null;
           String operationType = strings[0];
           // 先拿到type
            if (LogQueryType.LOG_LIST.equals(strings[0])){
                iOperation = new LogListOperation(killerDataBaseHelper);
            }else if (LogQueryType.LOG_DETAIL.equals(strings[0])){
                iOperation = new LogDetailOperation(killerDataBaseHelper);
            }else if (LogQueryType.LOG_LIST_OFFSET.equals(strings[0])){
                iOperation = new LogListOffsetOperation(killerDataBaseHelper);
            }else if(LogQueryType.DELETE_ALL.equals(strings[0])){
                iOperation = new DeleteAllLogOperation(killerDataBaseHelper);
            }else if (LogQueryType.DELETE_TYPE_ALL.equals(operationType)){
                iOperation = new DeleteAllTypeLogOperation(killerDataBaseHelper);
            }
           return iOperation.operation(strings);
       } catch (Exception e) {
           Log.i("Killer_Hook", "查询日志失败");
           Log.e("Killer_Hook", "查询日志失败: "+e.getMessage());
           e.printStackTrace();
       }
       return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        killerDataBaseHelper.close();
    }
}
