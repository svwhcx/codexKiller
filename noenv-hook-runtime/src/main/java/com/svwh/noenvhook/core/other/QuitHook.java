package com.svwh.noenvhook.core.other;

import android.app.Activity;

import java.lang.reflect.Member;

import top.canyie.pine.Pine;

/**
 * @description
 * @Author chenxin
 * @Date 2025/8/15 22:23
 */
public class QuitHook {

    public static void hook() throws Exception {
//        if (HdUtils.isDebug()) {
//            XposedBridge.log(HdUtils.获取TAG() + " >>start>> " + "swAppSetMonitorExit");
//        }
//        try {
//            XposedBridge.hookAllMethods(
//                    Activity.class,
//                    "finish",
//                    new XC_MethodHook() {
//                        @Override
//                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                            super.beforeHookedMethod(param);
//                            Object obj = param.thisObject;
//                            if (!(obj instanceof Activity) || ((Activity) obj).isTaskRoot()) {
//                                String data = "==========================================\n";
//                                data += "当前时间 " + TimeUtils.getNowTime() + "\n";
//                                data += "控件类名 " + obj.getClass().getName() + "\n";
//                                data += "控件内容 " + "Exit-finish" + " -> 已拦截" + "\n";
//                                data += "调用堆栈 " + ToolHook.printStackTrace() + "\n";
//                                FileUtils.writeAdd(setFolderPath + "/" + "Hook_Exit.lua", data);
//                                param.setResult(null);
//                                if (HdUtils.isDebug()) {
//                                    XposedBridge.log(HdUtils.获取TAG() + " >>end>> " + "Exit-finish");
//                                }
//                            }
//                        }
//                    }
//            );
//            XposedBridge.hookAllMethods(
//                    ActivityManager.class,
//                    "killBackgroundProcesses",
//                    new XC_MethodHook() {
//                        @Override
//                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                            super.beforeHookedMethod(param);
//                            Object obj = param.thisObject;
//                            if (!(obj instanceof Activity) || ((Activity) obj).isTaskRoot()) {
//                                String data = "==========================================\n";
//                                data += "当前时间 " + TimeUtils.getNowTime() + "\n";
//                                data += "控件类名 " + obj.getClass().getName() + "\n";
//                                data += "控件内容 " + "Exit-killBackgroundProcesses" + " -> 已拦截" + "\n";
//                                data += "调用堆栈 " + ToolHook.printStackTrace() + "\n";
//                                FileUtils.writeAdd(setFolderPath + "/" + "Hook_Exit.lua", data);
//                                param.setResult(null);
//                                if (HdUtils.isDebug()) {
//                                    XposedBridge.log(HdUtils.获取TAG() + " >>end>> " + "Exit-killBackgroundProcesses");
//                                }
//                            }
//                        }
//                    }
//            );
//            XposedBridge.hookAllMethods(
//                    System.class,
//                    "exit",
//                    new XC_MethodHook() {
//                        @Override
//                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                            super.beforeHookedMethod(param);
//                            Object obj = param.thisObject;
//                            if (!(obj instanceof Activity) || ((Activity) obj).isTaskRoot()) {
//                                String data = "==========================================\n";
//                                data += "当前时间 " + TimeUtils.getNowTime() + "\n";
//                                data += "控件类名 " + obj.getClass().getName() + "\n";
//                                data += "控件内容 " + "Exit-exit" + " -> 已拦截" + "\n";
//                                data += "调用堆栈 " + ToolHook.printStackTrace() + "\n";
//                                FileUtils.writeAdd(setFolderPath + "/" + "Hook_Exit.lua", data);
//                                param.setResult(null);
//                                if (HdUtils.isDebug()) {
//                                    XposedBridge.log(HdUtils.获取TAG() + " >>end>> " + "Exit-exit");
//                                }
//                            }
//                        }
//                    }
//            );
//            XposedBridge.hookAllMethods(
//                    Runtime.class,
//                    "exit",
//                    new XC_MethodHook() {
//                        @Override
//                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                            super.beforeHookedMethod(param);
//                            Object obj = param.thisObject;
//                            if (!(obj instanceof Activity) || ((Activity) obj).isTaskRoot()) {
//                                String data = "==========================================\n";
//                                data += "当前时间 " + TimeUtils.getNowTime() + "\n";
//                                data += "控件类名 " + obj.getClass().getName() + "\n";
//                                data += "控件内容 " + "Exit-exit" + " -> 已拦截" + "\n";
//                                data += "调用堆栈 " + ToolHook.printStackTrace() + "\n";
//                                FileUtils.writeAdd(setFolderPath + "/" + "Hook_Exit.lua", data);
//                                param.setResult(null);
//                                if (HdUtils.isDebug()) {
//                                    XposedBridge.log(HdUtils.获取TAG() + " >>end>> " + "Exit-exit");
//                                }
//                            }
//                        }
//                    }
//            );
//            XposedBridge.hookAllMethods(
//                    Process.class,
//                    "killProcess",
//                    new XC_MethodHook() {
//                        @Override
//                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                            super.beforeHookedMethod(param);
//                            Object obj = param.thisObject;
//                            if (!(obj instanceof Activity) || ((Activity) obj).isTaskRoot()) {
//                                String data = "==========================================\n";
//                                data += "当前时间 " + TimeUtils.getNowTime() + "\n";
//                                data += "控件类名 " + obj.getClass().getName() + "\n";
//                                data += "控件内容 " + "Exit-killProcess" + " -> 已拦截" + "\n";
//                                data += "调用堆栈 " + ToolHook.printStackTrace() + "\n";
//                                FileUtils.writeAdd(setFolderPath + "/" + "Hook_Exit.lua", data);
//                                param.setResult(null);
//                                if (HdUtils.isDebug()) {
//                                    XposedBridge.log(HdUtils.获取TAG() + " >>end>> " + "Exit-killProcess");
//                                }
//                            }
//                        }
//                    }
//            );
//            XposedBridge.hookAllMethods(
//                    Process.class,
//                    "killProcessQuiet",
//                    new XC_MethodHook() {
//                        @Override
//                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                            super.beforeHookedMethod(param);
//                            Object obj = param.thisObject;
//                            if (!(obj instanceof Activity) || ((Activity) obj).isTaskRoot()) {
//                                String data = "==========================================\n";
//                                data += "当前时间 " + TimeUtils.getNowTime() + "\n";
//                                data += "控件类名 " + obj.getClass().getName() + "\n";
//                                data += "控件内容 " + "Exit-killProcessQuiet" + " -> 已拦截" + "\n";
//                                data += "调用堆栈 " + ToolHook.printStackTrace() + "\n";
//                                FileUtils.writeAdd(setFolderPath + "/" + "Hook_Exit.lua", data);
//                                param.setResult(null);
//                                if (HdUtils.isDebug()) {
//                                    XposedBridge.log(HdUtils.获取TAG() + " >>end>> " + "Exit-killProcessQuiet");
//                                }
//                            }
//                        }
//                    }
//            );
//            Pine.hook()
//            Member member = Process.class.getDeclaredMethod()
//            XposedBridge.hookAllMethods(
//                    Process.class,
//                    "killProcessGroup",
//                    new XC_MethodHook() {
//                        @Override
//                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                            super.beforeHookedMethod(param);
//                            Object obj = param.thisObject;
//                            if (!(obj instanceof Activity) || ((Activity) obj).isTaskRoot()) {
//                                String data = "==========================================\n";
//                                data += "当前时间 " + TimeUtils.getNowTime() + "\n";
//                                data += "控件类名 " + obj.getClass().getName() + "\n";
//                                data += "控件内容 " + "Exit-killProcessGroup" + " -> 已拦截" + "\n";
//                                data += "调用堆栈 " + ToolHook.printStackTrace() + "\n";
//                                FileUtils.writeAdd(setFolderPath + "/" + "Hook_Exit.lua", data);
//                                param.setResult(null);
//                                if (HdUtils.isDebug()) {
//                                    XposedBridge.log(HdUtils.获取TAG() + " >>end>> " + "Exit-killProcessGroup");
//                                }
//                            }
//                        }
//                    }
//            );
//        } catch (Exception e) {
//        }
    }
}
