package com.svwh.noenvhook.core.file;

import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.Log;
import com.svwh.noenvhook.framework.HookCallFrame;
import com.svwh.noenvhook.framework.HookCallback;
import com.svwh.noenvhook.framework.HookFramework;
import com.svwh.noenvhook.management.HookConfigTypeEnum;

import java.io.File;
import java.lang.reflect.Member;

public class FileDeleteHookInvocation {

    public static void hook(HookFramework hookFramework, HookLogWriter logWriter) throws NoSuchMethodException {
        Member member = File.class.getDeclaredMethod("delete");
        hookFramework.hook(member, new HookCallback() {
            @Override
            public void beforeCall(HookCallFrame callFrame) {
                File file = (File) callFrame.getThisObject();
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
