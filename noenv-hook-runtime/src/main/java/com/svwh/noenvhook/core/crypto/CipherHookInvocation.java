package com.svwh.noenvhook.core.crypto;

import com.svwh.noenvhook.framework.HookCallFrame;
import com.svwh.noenvhook.framework.HookCallback;
import com.svwh.noenvhook.framework.HookFramework;
import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.Log;
import com.svwh.noenvhook.log.StackTraceCollector;
import com.svwh.noenvhook.management.HookConfigTypeEnum;
import com.svwh.noenvhook.util.StringUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import javax.crypto.Cipher;

public final class CipherHookInvocation {

    private static final int MAX_CAPTURE_BYTES = 4096;
    private static final int MAX_PREVIEW_BYTES = 512;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Map<Cipher, CipherState> STATES = Collections.synchronizedMap(new WeakHashMap<>());
    private static final ThreadLocal<Boolean> WRITING_LOG = new ThreadLocal<>();

    private CipherHookInvocation() {
    }

    public static void hook(
            HookFramework hookFramework,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector
    ) throws NoSuchMethodException {
        hookGetInstance(hookFramework, logWriter);
        hookInit(hookFramework);
        hookUpdate(hookFramework);
        hookDoFinal(hookFramework, logWriter, stackTraceCollector);
    }

    private static void hookGetInstance(HookFramework hookFramework, HookLogWriter logWriter)
            throws NoSuchMethodException {
        hookGetInstanceMethod(hookFramework, logWriter, Cipher.class.getDeclaredMethod("getInstance", String.class));
        hookGetInstanceMethod(
                hookFramework,
                logWriter,
                Cipher.class.getDeclaredMethod("getInstance", String.class, String.class)
        );
        hookGetInstanceMethod(
                hookFramework,
                logWriter,
                Cipher.class.getDeclaredMethod("getInstance", String.class, Provider.class)
        );
    }

    private static void hookGetInstanceMethod(HookFramework hookFramework, HookLogWriter logWriter, java.lang.reflect.Member member) {
        hookFramework.hook(member, new HookCallback() {
            @Override
            public void afterCall(HookCallFrame callFrame) {
                if (isWritingLog()) return;
                Object result = callFrame.getResult();
                if (result instanceof Cipher) {
                    Cipher cipher = (Cipher) result;
                    stateOf(cipher).transformation = cipher.getAlgorithm();
                    saveSimpleLog(
                            logWriter,
                            "加解密算法实例创建",
                            "transformation: " + cipher.getAlgorithm() + "\n"
                                    + "provider: " + cipher.getProvider().getName() + "\n"
                                    + "params: " + describeArgs(callFrame.getArgs()) + "\n"
                    );
                }
            }
        });
    }

    private static void hookInit(HookFramework hookFramework) throws NoSuchMethodException {
        hookInitMethod(hookFramework, Cipher.class.getDeclaredMethod("init", int.class, Key.class));
        hookInitMethod(hookFramework, Cipher.class.getDeclaredMethod("init", int.class, Key.class, SecureRandom.class));
        hookInitMethod(hookFramework, Cipher.class.getDeclaredMethod("init", int.class, Key.class, AlgorithmParameterSpec.class));
        hookInitMethod(hookFramework, Cipher.class.getDeclaredMethod("init", int.class, Key.class, AlgorithmParameterSpec.class, SecureRandom.class));
        hookInitMethod(hookFramework, Cipher.class.getDeclaredMethod("init", int.class, Key.class, AlgorithmParameters.class));
        hookInitMethod(hookFramework, Cipher.class.getDeclaredMethod("init", int.class, Key.class, AlgorithmParameters.class, SecureRandom.class));
        hookInitMethod(hookFramework, Cipher.class.getDeclaredMethod("init", int.class, Certificate.class));
        hookInitMethod(hookFramework, Cipher.class.getDeclaredMethod("init", int.class, Certificate.class, SecureRandom.class));
    }

    private static void hookInitMethod(HookFramework hookFramework, java.lang.reflect.Member member) {
        hookFramework.hook(member, new HookCallback() {
            @Override
            public void afterCall(HookCallFrame callFrame) {
                Cipher cipher = cipherOf(callFrame);
                if (cipher == null || isWritingLog()) return;
                CipherState state = stateOf(cipher);
                state.resetData();
                state.transformation = cipher.getAlgorithm();
                Object opMode = callFrame.getArg(0);
                state.opMode = opMode instanceof Integer ? (Integer) opMode : 0;
                state.keyDescription = describeKey(callFrame.getArg(1));
                state.parameterDescription = describeInitParameters(callFrame.getArgs());
                state.ivHex = cipher.getIV() == null ? "" : toHex(cipher.getIV());
            }
        });
    }

    private static void hookUpdate(HookFramework hookFramework) throws NoSuchMethodException {
        hookByteArrayInput(hookFramework, "update", byte[].class);
        hookByteArrayInput(hookFramework, "update", byte[].class, int.class, int.class);
        hookByteArrayInput(hookFramework, "update", byte[].class, int.class, int.class, byte[].class);
        hookByteArrayInput(hookFramework, "update", byte[].class, int.class, int.class, byte[].class, int.class);
        hookByteBufferInput(hookFramework, "update");
    }

    private static void hookDoFinal(
            HookFramework hookFramework,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector
    ) throws NoSuchMethodException {
        hookFinalNoInput(hookFramework, logWriter, stackTraceCollector);
        hookFinalByteArrayInput(hookFramework, logWriter, stackTraceCollector, byte[].class);
        hookFinalByteArrayInput(hookFramework, logWriter, stackTraceCollector, byte[].class, int.class);
        hookFinalByteArrayInput(hookFramework, logWriter, stackTraceCollector, byte[].class, int.class, int.class);
        hookFinalByteArrayInput(hookFramework, logWriter, stackTraceCollector, byte[].class, int.class, int.class, byte[].class);
        hookFinalByteArrayInput(hookFramework, logWriter, stackTraceCollector, byte[].class, int.class, int.class, byte[].class, int.class);
        hookFinalByteBufferInput(hookFramework, logWriter, stackTraceCollector);
    }

    private static void hookByteArrayInput(HookFramework hookFramework, String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        hookFramework.hook(Cipher.class.getDeclaredMethod(methodName, parameterTypes), new HookCallback() {
            @Override
            public void beforeCall(HookCallFrame callFrame) {
                captureByteArrayInput(callFrame);
            }
        });
    }

    private static void hookByteBufferInput(HookFramework hookFramework, String methodName) throws NoSuchMethodException {
        hookFramework.hook(Cipher.class.getDeclaredMethod(methodName, ByteBuffer.class, ByteBuffer.class), new HookCallback() {
            @Override
            public void beforeCall(HookCallFrame callFrame) {
                Cipher cipher = cipherOf(callFrame);
                ByteBuffer input = (ByteBuffer) callFrame.getArg(0);
                if (cipher != null && input != null) {
                    appendInput(cipher, copyRemaining(input), 0, input.remaining());
                }
            }
        });
    }

    private static void hookFinalNoInput(
            HookFramework hookFramework,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector
    ) throws NoSuchMethodException {
        hookFramework.hook(Cipher.class.getDeclaredMethod("doFinal"), new HookCallback() {
            @Override
            public void afterCall(HookCallFrame callFrame) {
                saveCipherLog(cipherOf(callFrame), (byte[]) callFrame.getResult(), logWriter, stackTraceCollector);
            }
        });
    }

    private static void hookFinalByteArrayInput(
            HookFramework hookFramework,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector,
            Class<?>... parameterTypes
    ) throws NoSuchMethodException {
        hookFramework.hook(Cipher.class.getDeclaredMethod("doFinal", parameterTypes), new HookCallback() {
            private int outputOffset;

            @Override
            public void beforeCall(HookCallFrame callFrame) {
                captureByteArrayInput(callFrame);
                Object offset = parameterTypes.length >= 5 ? callFrame.getArg(4) : null;
                outputOffset = offset instanceof Integer ? (Integer) offset : 0;
            }

            @Override
            public void afterCall(HookCallFrame callFrame) {
                Object result = callFrame.getResult();
                byte[] output;
                if (result instanceof byte[]) {
                    output = (byte[]) result;
                } else {
                    byte[] outputBuffer = parameterTypes.length >= 4 ? (byte[]) callFrame.getArg(3) : null;
                    int written = result instanceof Integer ? (Integer) result : 0;
                    output = copyRange(outputBuffer, outputOffset, written);
                }
                saveCipherLog(cipherOf(callFrame), output, logWriter, stackTraceCollector);
            }
        });
    }

    private static void hookFinalByteBufferInput(
            HookFramework hookFramework,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector
    ) throws NoSuchMethodException {
        hookFramework.hook(Cipher.class.getDeclaredMethod("doFinal", ByteBuffer.class, ByteBuffer.class), new HookCallback() {
            private int outputPosition;

            @Override
            public void beforeCall(HookCallFrame callFrame) {
                Cipher cipher = cipherOf(callFrame);
                ByteBuffer input = (ByteBuffer) callFrame.getArg(0);
                ByteBuffer output = (ByteBuffer) callFrame.getArg(1);
                outputPosition = output == null ? 0 : output.position();
                if (cipher != null && input != null) {
                    appendInput(cipher, copyRemaining(input), 0, input.remaining());
                }
            }

            @Override
            public void afterCall(HookCallFrame callFrame) {
                ByteBuffer output = (ByteBuffer) callFrame.getArg(1);
                int written = callFrame.getResult() instanceof Integer ? (Integer) callFrame.getResult() : 0;
                saveCipherLog(cipherOf(callFrame), copyBufferRange(output, outputPosition, written), logWriter, stackTraceCollector);
            }
        });
    }

    private static void captureByteArrayInput(HookCallFrame callFrame) {
        Cipher cipher = cipherOf(callFrame);
        byte[] input = (byte[]) callFrame.getArg(0);
        if (cipher == null || input == null) return;
        int offset = callFrame.getArg(1) instanceof Integer ? (Integer) callFrame.getArg(1) : 0;
        int length = callFrame.getArg(2) instanceof Integer ? (Integer) callFrame.getArg(2) : input.length - offset;
        appendInput(cipher, input, offset, length);
    }

    private static Cipher cipherOf(HookCallFrame callFrame) {
        Object thisObject = callFrame.getThisObject();
        return thisObject instanceof Cipher ? (Cipher) thisObject : null;
    }

    private static CipherState stateOf(Cipher cipher) {
        synchronized (STATES) {
            CipherState state = STATES.get(cipher);
            if (state == null) {
                state = new CipherState(cipher);
                STATES.put(cipher, state);
            }
            return state;
        }
    }

    private static void appendInput(Cipher cipher, byte[] input, int offset, int length) {
        if (isWritingLog() || input == null || length <= 0 || offset < 0 || offset >= input.length) return;
        int safeLength = Math.min(length, input.length - offset);
        stateOf(cipher).append(input, offset, safeLength);
    }

    private static void saveCipherLog(
            Cipher cipher,
            byte[] output,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector
    ) {
        if (isWritingLog() || cipher == null) return;
        CipherState state = stateOf(cipher);
        try {
            WRITING_LOG.set(Boolean.TRUE);
            Log log = new Log();
            log.setType(HookConfigTypeEnum.CIPHER);
            log.setTitle("加解密算法分析 - " + state.transformation);
            log.setTime(LocalDateTime.now().format(TIME_FORMATTER));
            log.setStackTrace(stackTraceCollector.collect());

            byte[] input = state.snapshot();
            StringBuilder content = new StringBuilder();
            content.append("transformation: ").append(state.transformation).append("\n");
            content.append("provider: ").append(cipher.getProvider().getName()).append("\n");
            content.append("mode: ").append(opModeName(state.opMode)).append("\n");
            content.append("key: ").append(state.keyDescription).append("\n");
            content.append("params: ").append(state.parameterDescription).append("\n");
            content.append("ivHex: ").append(state.ivHex).append("\n");
            content.append("inputLength: ").append(state.totalLength).append("\n");
            content.append("capturedInputLength: ").append(input.length).append("\n");
            content.append("inputHex: ").append(toHex(input)).append("\n");
            content.append("inputTextPreview: ").append(toTextPreview(input)).append("\n");
            content.append("outputLength: ").append(output == null ? 0 : output.length).append("\n");
            content.append("outputHex: ").append(output == null ? "" : toHex(output)).append("\n");
            content.append("outputTextPreview: ").append(output == null ? "" : toTextPreview(output)).append("\n");
            log.setContent(content.toString());
            logWriter.save(log);
        } finally {
            state.resetData();
            WRITING_LOG.remove();
        }
    }

    private static void saveSimpleLog(HookLogWriter logWriter, String title, String content) {
        try {
            WRITING_LOG.set(Boolean.TRUE);
            Log log = new Log();
            log.setType(HookConfigTypeEnum.CIPHER);
            log.setTitle(title);
            log.setTime(LocalDateTime.now().format(TIME_FORMATTER));
            log.setContent(content);
            logWriter.save(log);
        } finally {
            WRITING_LOG.remove();
        }
    }

    private static String describeKey(Object value) {
        if (value instanceof Key) {
            Key key = (Key) value;
            byte[] encoded = key.getEncoded();
            return "algorithm=" + key.getAlgorithm()
                    + ", format=" + key.getFormat()
                    + ", encodedLength=" + (encoded == null ? 0 : encoded.length)
                    + ", encodedHex=" + (encoded == null ? "" : toHex(limit(encoded, MAX_PREVIEW_BYTES)));
        }
        if (value instanceof Certificate) {
            Certificate certificate = (Certificate) value;
            return "certificateType=" + certificate.getType();
        }
        return String.valueOf(value);
    }

    private static String describeInitParameters(Object[] args) {
        if (args == null || args.length < 3) return "";
        StringBuilder builder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i > 2) builder.append(", ");
            Object arg = args[i];
            if (arg instanceof SecureRandom) {
                builder.append("SecureRandom");
            } else {
                builder.append(arg == null ? "null" : arg.getClass().getName()).append(": ").append(arg);
            }
        }
        return builder.toString();
    }

    private static String describeArgs(Object[] args) {
        if (args == null || args.length == 0) return "";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) builder.append(", ");
            Object arg = args[i];
            builder.append(arg instanceof Provider ? ((Provider) arg).getName() : arg);
        }
        return builder.toString();
    }

    private static String opModeName(int opMode) {
        switch (opMode) {
            case Cipher.ENCRYPT_MODE:
                return "ENCRYPT";
            case Cipher.DECRYPT_MODE:
                return "DECRYPT";
            case Cipher.WRAP_MODE:
                return "WRAP";
            case Cipher.UNWRAP_MODE:
                return "UNWRAP";
            default:
                return String.valueOf(opMode);
        }
    }

    private static byte[] copyRemaining(ByteBuffer buffer) {
        ByteBuffer copy = buffer.slice();
        byte[] bytes = new byte[copy.remaining()];
        copy.get(bytes);
        return bytes;
    }

    private static byte[] copyBufferRange(ByteBuffer buffer, int offset, int length) {
        if (buffer == null || length <= 0) return new byte[0];
        ByteBuffer copy = buffer.duplicate();
        int start = Math.max(0, Math.min(offset, copy.limit()));
        int end = Math.max(start, Math.min(start + length, copy.limit()));
        copy.position(start);
        copy.limit(end);
        byte[] bytes = new byte[copy.remaining()];
        copy.get(bytes);
        return bytes;
    }

    private static byte[] copyRange(byte[] source, int offset, int length) {
        if (source == null || length <= 0 || offset < 0 || offset >= source.length) return new byte[0];
        int safeLength = Math.min(length, source.length - offset);
        byte[] result = new byte[safeLength];
        System.arraycopy(source, offset, result, 0, safeLength);
        return result;
    }

    private static byte[] limit(byte[] source, int max) {
        return copyRange(source, 0, Math.min(source.length, max));
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
        if (bytes.length > MAX_PREVIEW_BYTES) safe.append("...");
        return safe.toString();
    }

    private static boolean isWritingLog() {
        return Boolean.TRUE.equals(WRITING_LOG.get());
    }

    private static final class CipherState {
        private String transformation;
        private int opMode;
        private String keyDescription = "";
        private String parameterDescription = "";
        private String ivHex = "";
        private byte[] captured = new byte[0];
        private int totalLength;

        private CipherState(Cipher cipher) {
            this.transformation = cipher.getAlgorithm();
        }

        private void append(byte[] input, int offset, int length) {
            totalLength += length;
            int remain = MAX_CAPTURE_BYTES - captured.length;
            if (remain <= 0) return;
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

        private void resetData() {
            captured = new byte[0];
            totalLength = 0;
        }
    }
}
