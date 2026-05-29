package com.svwh.noenvhook.core.crypto;

import com.svwh.noenvhook.framework.HookCallFrame;
import com.svwh.noenvhook.framework.HookCallback;
import com.svwh.noenvhook.framework.HookFramework;
import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.Log;
import com.svwh.noenvhook.log.StackTraceCollector;
import com.svwh.noenvhook.management.HookConfigTypeEnum;
import com.svwh.noenvhook.util.StringUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Member;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Provider;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class DigestHookInvocation {

    private static final int MAX_CAPTURE_BYTES = 4096;
    private static final int MAX_PREVIEW_BYTES = 512;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Map<MessageDigest, DigestState> STATES =
            Collections.synchronizedMap(new WeakHashMap<>());
    private static final ThreadLocal<Boolean> WRITING_LOG = new ThreadLocal<>();

    private DigestHookInvocation() {
    }

    public static void hook(
            HookFramework hookFramework,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector
    ) throws NoSuchMethodException {
        hookGetInstance(hookFramework, logWriter);
        hookUpdateMethods(hookFramework);
        hookDigestMethods(hookFramework, logWriter, stackTraceCollector);
    }

    private static void hookGetInstance(HookFramework hookFramework, HookLogWriter logWriter)
            throws NoSuchMethodException {
        hookGetInstanceMethod(
                hookFramework,
                logWriter,
                MessageDigest.class.getDeclaredMethod("getInstance", String.class)
        );
        hookGetInstanceMethod(
                hookFramework,
                logWriter,
                MessageDigest.class.getDeclaredMethod("getInstance", String.class, String.class)
        );
        hookGetInstanceMethod(
                hookFramework,
                logWriter,
                MessageDigest.class.getDeclaredMethod("getInstance", String.class, Provider.class)
        );
    }

    private static void hookGetInstanceMethod(
            HookFramework hookFramework,
            HookLogWriter logWriter,
            Member member
    ) {
        hookFramework.hook(member, new HookCallback() {
            @Override
            public void afterCall(HookCallFrame callFrame) {
                if (isWritingLog()) {
                    return;
                }
                Object result = callFrame.getResult();
                if (result instanceof MessageDigest) {
                    MessageDigest digest = (MessageDigest) result;
                    stateOf(digest).algorithm = digest.getAlgorithm();
                    saveSimpleLog(
                            logWriter,
                            "摘要算法实例创建",
                            "algorithm: " + digest.getAlgorithm() + "\n"
                                    + "provider: " + digest.getProvider().getName() + "\n"
                                    + "params: " + describeArgs(callFrame.getArgs()) + "\n"
                    );
                }
            }
        });
    }

    private static void hookUpdateMethods(HookFramework hookFramework) throws NoSuchMethodException {
        hookFramework.hook(
                MessageDigest.class.getDeclaredMethod("update", byte.class),
                new HookCallback() {
                    @Override
                    public void beforeCall(HookCallFrame callFrame) {
                        MessageDigest digest = digestOf(callFrame);
                        if (digest != null) {
                            appendInput(digest, new byte[]{(Byte) callFrame.getArg(0)}, 0, 1);
                        }
                    }
                }
        );
        hookFramework.hook(
                MessageDigest.class.getDeclaredMethod("update", byte[].class),
                new HookCallback() {
                    @Override
                    public void beforeCall(HookCallFrame callFrame) {
                        MessageDigest digest = digestOf(callFrame);
                        byte[] input = (byte[]) callFrame.getArg(0);
                        if (digest != null && input != null) {
                            appendInput(digest, input, 0, input.length);
                        }
                    }
                }
        );
        hookFramework.hook(
                MessageDigest.class.getDeclaredMethod("update", byte[].class, int.class, int.class),
                new HookCallback() {
                    @Override
                    public void beforeCall(HookCallFrame callFrame) {
                        MessageDigest digest = digestOf(callFrame);
                        byte[] input = (byte[]) callFrame.getArg(0);
                        Integer offset = (Integer) callFrame.getArg(1);
                        Integer length = (Integer) callFrame.getArg(2);
                        if (digest != null && input != null && offset != null && length != null) {
                            appendInput(digest, input, offset, length);
                        }
                    }
                }
        );
        hookFramework.hook(
                MessageDigest.class.getDeclaredMethod("update", ByteBuffer.class),
                new HookCallback() {
                    @Override
                    public void beforeCall(HookCallFrame callFrame) {
                        MessageDigest digest = digestOf(callFrame);
                        ByteBuffer buffer = (ByteBuffer) callFrame.getArg(0);
                        if (digest != null && buffer != null) {
                            ByteBuffer copy = buffer.slice();
                            byte[] input = new byte[copy.remaining()];
                            copy.get(input);
                            appendInput(digest, input, 0, input.length);
                        }
                    }
                }
        );
    }

    private static void hookDigestMethods(
            HookFramework hookFramework,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector
    ) throws NoSuchMethodException {
        hookFramework.hook(
                MessageDigest.class.getDeclaredMethod("digest"),
                new HookCallback() {
                    @Override
                    public void afterCall(HookCallFrame callFrame) {
                        MessageDigest digest = digestOf(callFrame);
                        byte[] result = (byte[]) callFrame.getResult();
                        saveDigestLog(digest, null, result, logWriter, stackTraceCollector);
                    }
                }
        );
        hookFramework.hook(
                MessageDigest.class.getDeclaredMethod("digest", byte[].class),
                new HookCallback() {
                    @Override
                    public void beforeCall(HookCallFrame callFrame) {
                        MessageDigest digest = digestOf(callFrame);
                        byte[] input = (byte[]) callFrame.getArg(0);
                        if (digest != null && input != null) {
                            appendInput(digest, input, 0, input.length);
                        }
                    }

                    @Override
                    public void afterCall(HookCallFrame callFrame) {
                        MessageDigest digest = digestOf(callFrame);
                        byte[] result = (byte[]) callFrame.getResult();
                        saveDigestLog(digest, null, result, logWriter, stackTraceCollector);
                    }
                }
        );
        hookFramework.hook(
                MessageDigest.class.getDeclaredMethod("digest", byte[].class, int.class, int.class),
                new HookCallback() {
                    @Override
                    public void afterCall(HookCallFrame callFrame) {
                        MessageDigest digest = digestOf(callFrame);
                        byte[] output = (byte[]) callFrame.getArg(0);
                        Integer offset = (Integer) callFrame.getArg(1);
                        Integer written = (Integer) callFrame.getResult();
                        byte[] result = copyRange(output, offset == null ? 0 : offset, written == null ? 0 : written);
                        saveDigestLog(digest, null, result, logWriter, stackTraceCollector);
                    }
                }
        );
    }

    private static MessageDigest digestOf(HookCallFrame callFrame) {
        Object thisObject = callFrame.getThisObject();
        return thisObject instanceof MessageDigest ? (MessageDigest) thisObject : null;
    }

    private static DigestState stateOf(MessageDigest digest) {
        synchronized (STATES) {
            DigestState state = STATES.get(digest);
            if (state == null) {
                state = new DigestState(digest);
                STATES.put(digest, state);
            }
            return state;
        }
    }

    private static void appendInput(MessageDigest digest, byte[] input, int offset, int length) {
        if (isWritingLog() || length <= 0 || offset < 0 || input == null || offset >= input.length) {
            return;
        }
        int safeLength = Math.min(length, input.length - offset);
        stateOf(digest).append(input, offset, safeLength);
    }

    private static void saveDigestLog(
            MessageDigest digest,
            String note,
            byte[] result,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector
    ) {
        if (isWritingLog() || digest == null) {
            return;
        }
        DigestState state = stateOf(digest);
        try {
            WRITING_LOG.set(Boolean.TRUE);
            Log log = new Log();
            log.setType(HookConfigTypeEnum.DIGEST);
            log.setTitle("摘要算法分析 - " + state.algorithm);
            log.setTime(LocalDateTime.now().format(TIME_FORMATTER));
            log.setStackTrace(stackTraceCollector.collect());

            byte[] input = state.snapshot();
            StringBuilder content = new StringBuilder();
            content.append("algorithm: ").append(state.algorithm).append("\n");
            content.append("provider: ").append(digest.getProvider().getName()).append("\n");
            if (note != null && !note.isEmpty()) {
                content.append("note: ").append(note).append("\n");
            }
            content.append("inputLength: ").append(state.totalLength).append("\n");
            content.append("capturedInputLength: ").append(input.length).append("\n");
            content.append("inputHex: ").append(toHex(input)).append("\n");
            content.append("inputTextPreview: ").append(toTextPreview(input)).append("\n");
            content.append("resultLength: ").append(result == null ? 0 : result.length).append("\n");
            content.append("resultHex: ").append(result == null ? "" : toHex(result)).append("\n");
            log.setContent(content.toString());
            logWriter.save(log);
        } finally {
            state.reset();
            WRITING_LOG.remove();
        }
    }

    private static void saveSimpleLog(HookLogWriter logWriter, String title, String content) {
        try {
            WRITING_LOG.set(Boolean.TRUE);
            Log log = new Log();
            log.setType(HookConfigTypeEnum.DIGEST);
            log.setTitle(title);
            log.setTime(LocalDateTime.now().format(TIME_FORMATTER));
            log.setContent(content);
            logWriter.save(log);
        } finally {
            WRITING_LOG.remove();
        }
    }

    private static boolean isWritingLog() {
        return Boolean.TRUE.equals(WRITING_LOG.get());
    }

    private static byte[] copyRange(byte[] source, int offset, int length) {
        if (source == null || length <= 0 || offset < 0 || offset >= source.length) {
            return new byte[0];
        }
        int safeLength = Math.min(length, source.length - offset);
        byte[] result = new byte[safeLength];
        System.arraycopy(source, offset, result, 0, safeLength);
        return result;
    }

    private static String toHex(byte[] bytes) {
        return StringUtils.toHexString(bytes, false, false);
    }

    private static String toTextPreview(byte[] bytes) {
        int length = Math.min(bytes.length, MAX_PREVIEW_BYTES);
        String text = new String(bytes, 0, length, StandardCharsets.UTF_8);
        StringBuilder safe = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            safe.append(Character.isISOControl(ch) && ch != '\n' && ch != '\r' && ch != '\t' ? '.' : ch);
        }
        if (bytes.length > MAX_PREVIEW_BYTES) {
            safe.append("...");
        }
        return safe.toString();
    }

    private static String describeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            Object arg = args[i];
            if (arg instanceof Provider) {
                builder.append(((Provider) arg).getName());
            } else {
                builder.append(arg);
            }
        }
        return builder.toString();
    }

    private static final class DigestState {
        private final WeakReference<MessageDigest> digestRef;
        private String algorithm;
        private byte[] captured = new byte[0];
        private int totalLength;

        private DigestState(MessageDigest digest) {
            this.digestRef = new WeakReference<>(digest);
            this.algorithm = digest.getAlgorithm();
        }

        private void append(byte[] input, int offset, int length) {
            totalLength += length;
            int remain = MAX_CAPTURE_BYTES - captured.length;
            if (remain <= 0) {
                return;
            }
            int copyLength = Math.min(remain, length);
            byte[] next = new byte[captured.length + copyLength];
            System.arraycopy(captured, 0, next, 0, captured.length);
            System.arraycopy(input, offset, next, captured.length, copyLength);
            captured = next;
        }

        private byte[] snapshot() {
            byte[] result = new byte[captured.length];
            System.arraycopy(captured, 0, result, 0, captured.length);
            return result;
        }

        private void reset() {
            MessageDigest digest = digestRef.get();
            algorithm = digest == null ? algorithm : digest.getAlgorithm();
            captured = new byte[0];
            totalLength = 0;
        }
    }
}
