package com.svwh.noenvhook.core.file;

import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.Log;
import com.svwh.noenvhook.management.HookConfigTypeEnum;

import java.io.File;
import java.lang.reflect.Member;

import top.canyie.pine.Pine;
import top.canyie.pine.callback.MethodHook;

public class FileDeleteHookInvocation {

    public static void hook(HookLogWriter logWriter) throws NoSuchMethodException {
        Member member = File.class.getDeclaredMethod("delete");
        Pine.hook(member, new MethodHook() {
            @Override
            public void beforeCall(Pine.CallFrame callFrame) throws Throwable {
                super.beforeCall(callFrame);
                File file = (File) callFrame.thisObject;
                if (file != null) {
                    Log log = new Log();
                    log.setType(HookConfigTypeEnum.FILE_DELETE);
                    log.setTitle("文件删除");
                    StringBuilder sb = new StringBuilder();
                    sb.append("文件路径：").append(file.getAbsolutePath()).append("\n");
                    log.setContent(sb.toString());
                    logWriter.save(log);
                }
            }
        });
    }
}
