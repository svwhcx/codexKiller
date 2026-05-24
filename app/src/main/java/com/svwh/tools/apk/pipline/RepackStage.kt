package com.svwh.tools.apk.pipline

import com.svwh.tools.apk.context.ApkProcessorContext
import java.io.FileOutputStream
import java.io.InputStream
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
        val fos = FileOutputStream(apkProcessorContext.apkModificationConfig.apkSavePath)
        val zipOutputStream = ZipOutputStream(fos)
        compileApk(zipOutputStream, apkProcessorContext)
        zipOutputStream.close()
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
        // 向assets目录下内置so文件。
        zipFile.entries().asSequence().forEach {
            // 单独处理资源文件
            if (it.name == "resources.arsc") {
                val zipEntry = ZipEntry(it.name)
                zipEntry.method = ZipEntry.STORED
                zipEntry.method = it.method
                zipEntry.size = it.size
                zipEntry.crc = it.crc
                addFile2Apk(zos, zipEntry, zipFile.getInputStream(it))
                return@forEach
            }
            if (apkProcessorContext.extraDataNodes.find {item -> item.nodeName == it.name} != null) {
                return@forEach
            }
            addFile2Apk(zos, ZipEntry(it.name), zipFile.getInputStream(it))
        }
        apkProcessorContext.extraDataNodes.forEach {
            addFile2Apk(zos, ZipEntry(it.nodeName), it.nodeDataIs)
            it.nodeDataIs.close()
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
        val buffer = ByteArray(1024)
        var len: Int
        while (bais.read(buffer).also { len = it } > 0) {
            zipOutputStream.write(buffer, 0, len)
        }
        zipOutputStream.closeEntry()
    }

}