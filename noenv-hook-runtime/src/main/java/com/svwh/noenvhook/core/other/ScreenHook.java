package com.svwh.noenvhook.core.other;

import android.view.Window;

import com.svwh.noenvhook.framework.HookCallFrame;
import com.svwh.noenvhook.framework.HookCallback;
import com.svwh.noenvhook.framework.HookFramework;

import java.lang.reflect.Member;

/**
 * @description
 * @Author chenxin
 * @Date 2025/8/15 22:00
 */
public class ScreenHook {

    public static void hook(HookFramework hookFramework) throws Exception {
        Member member = Window.class.getDeclaredMethod("setFlags", int.class, int.class);

        hookFramework.hook(
                member,
                new HookCallback() {
                    @Override
                    public void beforeCall(HookCallFrame callFrame) {
                        int flag = (int) callFrame.getArg(0);
                        int setFlag = flag & (-8193);
                        callFrame.setArg(0, setFlag);
                    }
                }
        );

    }
}
