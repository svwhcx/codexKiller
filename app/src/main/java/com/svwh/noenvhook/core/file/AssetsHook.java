package com.svwh.noenvhook.core.file;

import android.content.res.AssetManager;

import com.svwh.noenvhook.framework.HookFramework;
import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.Log;
import com.svwh.noenvhook.management.HookConfigTypeEnum;

import java.lang.reflect.Method;

public class AssetsHook {

    public static void hook(HookFramework hookFramework, HookLogWriter logWriter) throws NoSuchMethodException {
        Method open = AssetManager.class.getDeclaredMethod("open", String.class, int.class);
        hookFramework.hook(open, new com.svwh.noenvhook.framework.HookCallback() {
            @Override
            public void afterCall(com.svwh.noenvhook.framework.HookCallFrame callFrame) {
                String filePath = (String) callFrame.getArg(0);
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
