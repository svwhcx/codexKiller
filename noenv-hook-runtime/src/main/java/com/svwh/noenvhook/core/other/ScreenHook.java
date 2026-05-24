package com.svwh.noenvhook.core.other;

import android.view.Window;

import java.lang.reflect.Member;

import top.canyie.pine.Pine;
import top.canyie.pine.callback.MethodHook;

/**
 * @description
 * @Author chenxin
 * @Date 2025/8/15 22:00
 */
public class ScreenHook {

    public static void hook() throws Exception {
        Member member = Window.class.getDeclaredMethod("setFlags", int.class, int.class);

        Pine.hook(
                member,
                new MethodHook() {
                    @Override
                    public void beforeCall(Pine.CallFrame callFrame) throws Throwable {
                        super.beforeCall(callFrame);
                        int flag = (int) callFrame.args[0];
                        int setFlag = flag & (-8193);
                        callFrame.args[0] = setFlag;
                    }
                }
        );

    }
}
