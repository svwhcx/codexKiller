package com.svwh.noenvhook.core.file;

import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.Log;
import com.svwh.noenvhook.management.HookConfigTypeEnum;

import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Member;

import top.canyie.pine.Pine;
import top.canyie.pine.callback.MethodHook;
import top.canyie.pine.utils.ReflectionHelper;

public class FileWriteHookInvocation {

    public static void hook(HookLogWriter logWriter) throws NoSuchMethodException {
        Member member = FileOutputStream.class.getDeclaredMethod("write", byte[].class, int.class, int.class);
        Pine.hook(member, new MethodHook() {
            @Override
            public void afterCall(Pine.CallFrame callFrame) throws Throwable {
                super.afterCall(callFrame);
                Field field = ReflectionHelper.findField(FileOutputStream.class, "path");
                String path = (String) field.get(callFrame.thisObject);
                if (path != null) {
                    byte[] b = (byte[]) callFrame.args[0];
                    int off = (int) callFrame.args[1];
                    int len = (int) callFrame.args[2];

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
