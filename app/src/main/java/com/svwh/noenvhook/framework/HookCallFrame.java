package com.svwh.noenvhook.framework;

import java.lang.reflect.Member;
import java.lang.reflect.Method;

public interface HookCallFrame {

    Object getThisObject();

    Object[] getArgs();

    default Object getArg(int index) {
        Object[] args = getArgs();
        if (args == null || index < 0 || index >= args.length) {
            return null;
        }
        return args[index];
    }

    default void setArg(int index, Object value) {
        Object[] args = getArgs();
        if (args != null && index >= 0 && index < args.length) {
            args[index] = value;
        }
    }

    Member getMember();

    default String getMethodName() {
        Member member = getMember();
        return member == null ? "" : member.getName();
    }

    default Class<?> getReturnType() {
        Member member = getMember();
        if (member instanceof Method) {
            return ((Method) member).getReturnType();
        }
        return void.class;
    }

    Object getResult();

    void setResult(Object result);

    Object invokeOriginal() throws Throwable;
}
