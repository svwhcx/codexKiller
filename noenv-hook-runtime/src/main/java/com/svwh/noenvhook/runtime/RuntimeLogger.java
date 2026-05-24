package com.svwh.noenvhook.runtime;

public interface RuntimeLogger {

    void info(String message);

    void warn(String message);

    void error(String message, Throwable throwable);
}
