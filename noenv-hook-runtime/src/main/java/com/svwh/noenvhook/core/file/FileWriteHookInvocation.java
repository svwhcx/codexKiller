package com.svwh.noenvhook.core.file;

import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.Log;
import com.svwh.noenvhook.framework.HookCallFrame;
import com.svwh.noenvhook.framework.HookCallback;
import com.svwh.noenvhook.framework.HookFramework;
import com.svwh.noenvhook.framework.ReflectionUtils;
import com.svwh.noenvhook.management.HookConfigTypeEnum;

import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Member;

public class FileWriteHookInvocation {

    public static void hook(HookFramework hookFramework, HookLogWriter logWriter) throws NoSuchMethodException {
        Member member = FileOutputStream.class.getDeclaredMethod("write", byte[].class, int.class, int.class);
        hookFramework.hook(member, new HookCallback() {
            @Override
            public void afterCall(HookCallFrame callFrame) throws Throwable {
                Field field = ReflectionUtils.findField(FileOutputStream.class, "path");
                String path = (String) field.get(callFrame.getThisObject());
                if (path != null) {
                    byte[] b = (byte[]) callFrame.getArg(0);
                    int off = (int) callFrame.getArg(1);
                    int len = (int) callFrame.getArg(2);

                    byte[] bytes = new byte[Math.min(len, 2048)];
                    String str;
                    if (len > 2048) {
                        System.arraycopy(b, off, bytes, 0, 2048);
                        str = new String(bytes) + "(最多记录 2kb 数据...)";
                    } else {
                        System.arraycopy(b, off, bytes, 0, len);
                        str = new String(bytes);
                    }
                    Log log = new Log();
                    log.setTitle("写入文件");
                    log.setType(HookConfigTypeEnum.FILE_WRITE);
                    StringBuilder sb = new StringBuilder();
                    sb.append("文件路径：").append(path).append("\n");
                    sb.append("文件内容：").append(str).append("\n");
                    log.setContent(sb.toString());
                    logWriter.save(log);
                }
            }
        });
    }
}
