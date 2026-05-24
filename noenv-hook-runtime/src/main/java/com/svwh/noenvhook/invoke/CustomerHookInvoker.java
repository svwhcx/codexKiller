package com.svwh.noenvhook.invoke;

import android.util.Log;

import com.svwh.noenvhook.conf.HookConfig;
import com.svwh.noenvhook.core.InterceptorInvocationAdapter;
import com.svwh.noenvhook.core.custom.ArrounHookInvocation;
import com.svwh.noenvhook.log.LogService;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import top.canyie.pine.Pine;

/**
 * @description
 * @Author chenxin
 * @Date 2025/8/10 23:08
 */
public class CustomerHookInvoker extends AbstractHookInvoker {


    private ClassLoader classLoader;
    private final HookConfig hookConfig;

    public CustomerHookInvoker(HookConfig... hookConfig) {
        super(hookConfig);
        this.hookConfig = hookConfig[0];
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void invoke() {
        try {
            if (hookConfig.getParams().equals("*")) {
                // Hook全部类型的参数
                Method[] declaredMethods = classLoader.loadClass(hookConfig.getClassName()).getDeclaredMethods();
                for (Method method : declaredMethods) {
                    if (method.getName().equals(hookConfig.getMethodName())) {
                        doRegisterHook(method, hookConfig);
                    }
                }
                return;
            }
            List<String> paramSignature = resolveParamSignature(hookConfig);
            Log.i("参数为：", paramSignature.toString());
            Class<?>[] parameterTypes = new Class<?>[paramSignature.size()];
            for (int i = 0; i < paramSignature.size(); i++) {
                parameterTypes[i] = getParamClass(classLoader, paramSignature.get(i));
            }
            Log.i("参数转换为：", Arrays.toString(parameterTypes));
            Member target = classLoader.loadClass(hookConfig.getClassName()).getDeclaredMethod(hookConfig.getMethodName(), parameterTypes);
            // 判断是 * 的情况，* 说明是所有类型的方法。
            doRegisterHook(target, hookConfig);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("注册Hook出现了问题，没有发现该方法：{}", Objects.requireNonNull(e.getMessage()));
            com.svwh.noenvhook.log.Log log = new com.svwh.noenvhook.log.Log();
            log.setTitle("Hook 注册失败：");
            log.setTime(LocalDateTime.now().toString());
            log.setContent("未找到该方法：" + hookConfig.getClassName() + "," + hookConfig.getMethodName());
            LogService.getInstance().save(log);
        }
    }

    private void doRegisterHook(Member target, HookConfig hookConfig) {
        Pine.hook(target, new InterceptorInvocationAdapter(new ArrounHookInvocation(hookConfig)));
    }


    /**
     * 解析参数的签名，转化为 . 拼接的包名格式的对象。
     *
     * @param hookConfig hook配置
     * @return
     */
    private List<String> resolveParamSignature(HookConfig hookConfig) {
        List<String> paramSignature = new ArrayList<>();
        String[] split = hookConfig.getParams().split(",");
        for (String s : split) {
            if (!s.isEmpty()) {
                paramSignature.add(s);
            }
        }
        return paramSignature;
    }


    /**
     * 获取参数类型
     *
     * @param typeName
     * @return
     */
    private Class<?> getParamClass(ClassLoader classLoader, String typeName) throws ClassNotFoundException {
        // 基本类型
        switch (typeName) {
            case "Z":
            case "boolean":
                return boolean.class;
            case "B":
            case "byte":
                return byte.class;
            case "C":
            case "char":
                return char.class;
            case "S":
            case "short":
                return short.class;
            case "I":
            case "int":
                return int.class;
            case "J":
            case "long":
                return long.class;
            case "F":
            case "float":
                return float.class;
            case "D":
            case "double":
                return double.class;
            case "V":
            case "void":
                return void.class;
            case "String":
                return String.class;

            // 对象类型，例如 Ljava/lang/String;
            default:
                return classLoader.loadClass(typeName);
        }
    }
}
