package com.svwh.noenvhook.management;

import com.svwh.noenvhook.conf.HookConfig;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HookTargetResolver {

    public List<Member> resolve(ClassLoader classLoader, HookConfig hookConfig) throws Exception {
        Class<?> targetClass = classLoader.loadClass(hookConfig.getClassName());
        if ("*".equals(hookConfig.getParams())) {
            return resolveWildcardMethods(targetClass, hookConfig.getMethodName());
        }

        List<String> paramSignature = resolveParamSignature(hookConfig.getParams());
        Class<?>[] parameterTypes = new Class<?>[paramSignature.size()];
        for (int i = 0; i < paramSignature.size(); i++) {
            parameterTypes[i] = getParamClass(classLoader, paramSignature.get(i));
        }
        return Collections.singletonList(targetClass.getDeclaredMethod(hookConfig.getMethodName(), parameterTypes));
    }

    private List<Member> resolveWildcardMethods(Class<?> targetClass, String methodName) {
        List<Member> targets = new ArrayList<>();
        for (Method method : targetClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                targets.add(method);
            }
        }
        return targets;
    }

    private List<String> resolveParamSignature(String params) {
        if (params == null || params.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<String> paramSignature = new ArrayList<>();
        String[] split = params.split(",");
        for (String item : split) {
            String param = item.trim();
            if (!param.isEmpty()) {
                paramSignature.add(param);
            }
        }
        return paramSignature;
    }

    private Class<?> getParamClass(ClassLoader classLoader, String typeName) throws ClassNotFoundException {
        String normalized = normalizeTypeName(typeName);
        if (normalized.startsWith("[")) {
            return Class.forName(normalized.replace('/', '.'), false, classLoader);
        }

        switch (normalized) {
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
            case "java.lang.String":
                return String.class;
            default:
                return classLoader.loadClass(normalized);
        }
    }

    private String normalizeTypeName(String typeName) {
        if (typeName == null) {
            return "";
        }
        String normalized = typeName.trim();
        if (normalized.startsWith("L") && normalized.endsWith(";")) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        return normalized.replace('/', '.');
    }
}
