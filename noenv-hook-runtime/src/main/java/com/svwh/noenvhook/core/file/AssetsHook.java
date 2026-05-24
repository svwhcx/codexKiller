package com.svwh.noenvhook.core.file;

import android.content.res.AssetManager;

import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.Log;
import com.svwh.noenvhook.management.HookConfigTypeEnum;

import java.lang.reflect.Method;

import top.canyie.pine.Pine;
import top.canyie.pine.callback.MethodHook;

public class AssetsHook {

    public static void hook(HookLogWriter logWriter) throws NoSuchMethodException {
        Method open = AssetManager.class.getDeclaredMethod("open", String.class, int.class);
        Pine.hook(open, new MethodHook() {
            @Override
            public void afterCall(Pine.CallFrame callFrame) throws Throwable {
                super.afterCall(callFrame);
                String filePath = (String) callFrame.args[0];
                if (filePath != null) {
                    Log log = new Log();
                    log.setType(HookConfigTypeEnum.ASSETS);
                    log.setTitle("assets资源读取");
                    StringBuilder sb = new StringBuilder();
                    sb.append("文件路径：").append(filePath).append("\n");
                    log.setContent(sb.toString());
                    logWriter.save(log);
                }
            }
        });
    }
}
