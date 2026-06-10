package com.svwh.tools.apk.pipline

import com.svwh.tools.apk.context.ApkProcessorContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.Deflater
import java.util.zip.ZipEntry
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
        ZipOutputStream(
            BufferedOutputStream(
                FileOutputStream(apkProcessorContext.apkModificationConfig.apkSavePath),
            ),
        ).use { zipOutputStream ->
            zipOutputStream.setLevel(Deflater.BEST_COMPRESSION)
            compileApk(zipOutputStream, apkProcessorContext)
        }
        return apkProcessorContext
    }

    /**
     * 执行停止的操作
     */
    override fun doStop() {
        TODO("Not yet implemented")
    }

    /**
     * 编译apk，执行apk打包操作
     */
    private fun compileApk(zos: ZipOutputStream, apkProcessorContext: ApkProcessorContext) {
        val zipFile = apkProcessorContext.apkZipFile
        val extraNodeNames = apkProcessorContext.extraDataNodes
            .map { node -> node.nodeName }
            .toSet()
        zipFile.entries().asSequence().forEach { sourceEntry ->
            if (sourceEntry.name in extraNodeNames || sourceEntry.isSignatureEntry()) {
                return@forEach
            }
            addFile2Apk(
                zipOutputStream = zos,
                zipEntry = sourceEntry.toOutputEntry(),
                bais = BufferedInputStream(zipFile.getInputStream(sourceEntry)),
            )
        }
        apkProcessorContext.extraDataNodes.forEach { node ->
            node.nodeDataIs.use { inputStream ->
                addFile2Apk(zos, ZipEntry(node.nodeName), inputStream)
            }
        }
    }

    /**
     * 向apk中添加一个文件
     */
    private fun addFile2Apk(
        zipOutputStream: ZipOutputStream,
        zipEntry: ZipEntry,
        bais: InputStream
    ) {
        zipOutputStream.putNextEntry(zipEntry)
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var len: Int
        while (bais.read(buffer).also { len = it } > 0) {
            zipOutputStream.write(buffer, 0, len)
        }
        zipOutputStream.closeEntry()
    }

    private fun ZipEntry.toOutputEntry(): ZipEntry {
        return ZipEntry(name).also { outputEntry ->
            outputEntry.time = time
            comment?.let { outputEntry.comment = it }
            if (method == ZipEntry.STORED) {
                outputEntry.method = ZipEntry.STORED
                outputEntry.size = size
                outputEntry.compressedSize = compressedSize
                outputEntry.crc = crc
                extra?.let { outputEntry.extra = it }
            } else {
                outputEntry.method = ZipEntry.DEFLATED
            }
        }
    }

    private fun ZipEntry.isSignatureEntry(): Boolean {
        return APK_SIGNATURE_ENTRY_REGEX.matches(name)
    }

    private companion object {
        const val DEFAULT_BUFFER_SIZE = 64 * 1024
        val APK_SIGNATURE_ENTRY_REGEX = Regex(
            """META-INF/[^/]+\.(RSA|DSA|EC|SF|MF)""",
            RegexOption.IGNORE_CASE,
        )
    }

}
