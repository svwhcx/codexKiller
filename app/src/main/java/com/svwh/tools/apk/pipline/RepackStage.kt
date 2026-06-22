package com.svwh.tools.apk.pipline

import com.svwh.tools.apk.context.ApkProcessorContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * @description
 * @Author chenxin
 * @Date 2025/7/31 23:56
 */
class RepackStage : AbstractApkProcessor() {

    init {
        nextProcessor = SignApkStage()
    }

    override fun doProcess(apkProcessorContext: ApkProcessorContext): ApkProcessorContext {
        setCurrentProcessor(this)
        BufferedOutputStream(
            FileOutputStream(apkProcessorContext.apkModificationConfig.apkSavePath),
        ).use { outputStream ->
            compileApk(outputStream, apkProcessorContext)
        }
        return apkProcessorContext
    }

    /**
     * 执行停止操作
     */
    override fun doStop() {
    }

    /**
     * 编译 APK，使用标准 ZipOutputStream 重新写出所有 entry。
     */
    private fun compileApk(outputStream: OutputStream, apkProcessorContext: ApkProcessorContext) {
        val sourceApk = File(apkProcessorContext.apkPath)
        val extraNodeNames = apkProcessorContext.extraDataNodes
            .map { node -> node.nodeName }
            .toSet()
        val writtenNames = mutableSetOf<String>()

        ZipOutputStream(outputStream).use { zipOutputStream ->
            ZipFile(sourceApk).use { zipFile ->
                zipFile.entries()
                    .asSequenceCompat()
                    .filterNot { entry -> entry.name in extraNodeNames }
                    .filterNot { entry -> entry.name.isSignatureEntry() }
                    .forEach { entry ->
                        if (!writtenNames.add(entry.name)) {
                            return@forEach
                        }
                        zipFile.getInputStream(entry).use { inputStream ->
                            zipOutputStream.writeEntry(
                                sourceEntry = entry,
                                inputStream = BufferedInputStream(inputStream),
                            )
                        }
                    }
            }

            apkProcessorContext.extraDataNodes.forEach { node ->
                if (!writtenNames.add(node.nodeName)) {
                    return@forEach
                }
                node.openInputStream().use { inputStream ->
                    zipOutputStream.writeEntry(
                        name = node.nodeName,
                        inputStream = BufferedInputStream(inputStream),
                    )
                }
            }
        }
    }

    private fun ZipOutputStream.writeEntry(
        sourceEntry: ZipEntry,
        inputStream: InputStream,
    ) {
        val entry = ZipEntry(sourceEntry.name).copyMetadataFrom(sourceEntry)
        if (sourceEntry.isDirectory) {
            putNextEntry(entry)
            closeEntry()
            return
        }
        writeEntry(
            entry = entry,
            sourceMethod = sourceEntry.method,
            inputStream = inputStream,
        )
    }

    private fun ZipOutputStream.writeEntry(
        name: String,
        inputStream: InputStream,
    ) {
        writeEntry(
            entry = ZipEntry(name).apply { time = 0L },
            sourceMethod = ZipEntry.DEFLATED,
            inputStream = inputStream,
        )
    }

    private fun ZipOutputStream.writeEntry(
        entry: ZipEntry,
        sourceMethod: Int,
        inputStream: InputStream,
    ) {
        val bytes = inputStream.readBytes()
        if (sourceMethod == ZipEntry.STORED || entry.name.shouldStoreWithoutCompression()) {
            entry.method = ZipEntry.STORED
            entry.size = bytes.size.toLong()
            entry.compressedSize = bytes.size.toLong()
            entry.crc = crc32(bytes)
        } else {
            entry.method = ZipEntry.DEFLATED
        }
        putNextEntry(entry)
        write(bytes)
        closeEntry()
    }

    private fun ZipEntry.copyMetadataFrom(sourceEntry: ZipEntry): ZipEntry {
        time = sourceEntry.time.takeIf { it >= 0L } ?: 0L
        comment = sourceEntry.comment
        return this
    }

    private fun crc32(bytes: ByteArray): Long {
        return CRC32().apply { update(bytes) }.value
    }

    private fun <T> java.util.Enumeration<T>.asSequenceCompat(): Sequence<T> {
        return generateSequence {
            if (hasMoreElements()) nextElement() else null
        }
    }

    private fun String.isSignatureEntry(): Boolean {
        return APK_SIGNATURE_ENTRY_REGEX.matches(this)
    }

    private fun String.shouldStoreWithoutCompression(): Boolean {
        return endsWith(".so", ignoreCase = true)
    }

    private companion object {
        val APK_SIGNATURE_ENTRY_REGEX = Regex(
            """META-INF/[^/]+\.(RSA|DSA|EC|SF|MF)""",
            RegexOption.IGNORE_CASE,
        )
    }
}
