package com.svwh.noenvhook.log;

import java.util.Arrays;
import java.util.List;

public class FilteredStackTraceCollector implements StackTraceCollector {

    private final List<String> filters;

    public FilteredStackTraceCollector() {
        this(Arrays.asList("com.svwh.noenvhook", "top.canyie.pine"));
    }

    public FilteredStackTraceCollector(List<String> filters) {
        this.filters = filters;
    }

    @Override
    public String collect() {
        StringBuilder stackTraceBuilder = new StringBuilder();
        try {
            throw new RuntimeException();
        } catch (RuntimeException e) {
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                String message = stackTraceElement.toString();
                if (shouldInclude(message)) {
                    stackTraceBuilder.append("at: ").append(message).append(",");
                }
            }
        }
        return stackTraceBuilder.toString();
    }

    private boolean shouldInclude(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }
        for (String filter : filters) {
            if (message.contains(filter)) {
                return false;
            }
        }
        return true;
    }
}
