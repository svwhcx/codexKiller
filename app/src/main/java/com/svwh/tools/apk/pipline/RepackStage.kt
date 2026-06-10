package com.svwh.tools.apk.pipline

import com.svwh.tools.apk.context.ApkProcessorContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.RandomAccessFile
import java.util.zip.CRC32
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream

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
     * 执行停止的操作
     */
    override fun doStop() {
        TODO("Not yet implemented")
    }

    /**
     * 编译apk，执行apk打包操作
     */
    private fun compileApk(outputStream: OutputStream, apkProcessorContext: ApkProcessorContext) {
        val sourceApk = File(apkProcessorContext.apkPath)
        val sourceZip = readSourceZip(sourceApk)
        val extraNodeNames = apkProcessorContext.extraDataNodes
            .map { node -> node.nodeName }
            .toSet()
        val skippedNames = extraNodeNames + sourceZip.entries
            .asSequence()
            .map { entry -> entry.name }
            .filter { name -> name.isSignatureEntry() }
            .toSet()

        val centralDirectoryEntries = mutableListOf<ByteArray>()
        var outputOffset = 0L

        RandomAccessFile(sourceApk, "r").use { inputApk ->
            sourceZip.entries
                .asSequence()
                .filterNot { entry -> entry.name in skippedNames }
                .sortedBy { entry -> entry.localHeaderOffset }
                .forEach { entry ->
                    centralDirectoryEntries += entry.centralDirectoryBytesWithOffset(outputOffset)
                    outputOffset += inputApk.copyRangeTo(
                        outputStream = outputStream,
                        offset = entry.localHeaderOffset,
                        byteCount = entry.localRecordSize,
                    )
                }
        }

        apkProcessorContext.extraDataNodes.forEach { node ->
            node.nodeDataIs.use { inputStream ->
                val newEntry = writeCompressedEntry(
                    outputStream = outputStream,
                    name = node.nodeName,
                    inputStream = BufferedInputStream(inputStream),
                    localHeaderOffset = outputOffset,
                )
                outputOffset += newEntry.localRecordSize
                centralDirectoryEntries += newEntry.centralDirectoryBytes
            }
        }

        val centralDirectoryOffset = outputOffset
        centralDirectoryEntries.forEach { centralDirectoryEntry ->
            outputStream.write(centralDirectoryEntry)
            outputOffset += centralDirectoryEntry.size.toLong()
        }
        val centralDirectorySize = outputOffset - centralDirectoryOffset
        outputStream.writeEndOfCentralDirectory(
            entryCount = centralDirectoryEntries.size,
            centralDirectorySize = centralDirectorySize,
            centralDirectoryOffset = centralDirectoryOffset,
        )
    }

    private fun writeCompressedEntry(
        outputStream: OutputStream,
        name: String,
        inputStream: InputStream,
        localHeaderOffset: Long,
    ): NewZipEntry {
        val entryName = name.toByteArray(Charsets.UTF_8)
        val bytes = inputStream.readBytes()
        val crc32 = CRC32().apply { update(bytes) }.value
        val compressedBytes = ByteArrayOutputStream().use { compressedOutput ->
            val deflater = Deflater(Deflater.BEST_COMPRESSION, true)
            DeflaterOutputStream(compressedOutput, deflater).use { deflaterOutput ->
                deflaterOutput.write(bytes)
            }
            deflater.end()
            compressedOutput.toByteArray()
        }

        outputStream.writeLocalFileHeader(
            name = entryName,
            crc32 = crc32,
            compressedSize = compressedBytes.size.toLong(),
            uncompressedSize = bytes.size.toLong(),
        )
        outputStream.write(compressedBytes)

        val localRecordSize = LOCAL_FILE_HEADER_SIZE.toLong() + entryName.size + compressedBytes.size
        val centralDirectoryBytes = buildCentralDirectoryEntry(
            name = entryName,
            crc32 = crc32,
            compressedSize = compressedBytes.size.toLong(),
            uncompressedSize = bytes.size.toLong(),
            localHeaderOffset = localHeaderOffset,
        )
        return NewZipEntry(
            localRecordSize = localRecordSize,
            centralDirectoryBytes = centralDirectoryBytes,
        )
    }

    private data class NewZipEntry(
        val localRecordSize: Long,
        val centralDirectoryBytes: ByteArray,
    )

    private data class SourceZip(
        val entries: List<SourceZipEntry>,
    )

    private data class SourceZipEntry(
        val name: String,
        val localHeaderOffset: Long,
        val compressedSize: Long,
        val centralDirectoryBytes: ByteArray,
    ) {
        var localRecordSize: Long = 0L
    }

    private fun readSourceZip(file: File): SourceZip {
        RandomAccessFile(file, "r").use { apk ->
            val eocdOffset = apk.findEndOfCentralDirectory()
            val entryCount = apk.readUInt16(eocdOffset + EOCD_ENTRY_COUNT_OFFSET)
            val centralDirectoryOffset = apk.readUInt32(eocdOffset + EOCD_CENTRAL_DIRECTORY_OFFSET)

            val entries = mutableListOf<SourceZipEntry>()
            var offset = centralDirectoryOffset
            repeat(entryCount) {
                require(apk.readUInt32(offset) == CENTRAL_DIRECTORY_HEADER_SIGNATURE) {
                    "Invalid APK central directory"
                }
                val nameLength = apk.readUInt16(offset + CEN_NAME_LENGTH_OFFSET)
                val extraLength = apk.readUInt16(offset + CEN_EXTRA_LENGTH_OFFSET)
                val commentLength = apk.readUInt16(offset + CEN_COMMENT_LENGTH_OFFSET)
                val centralRecordSize = CENTRAL_DIRECTORY_HEADER_SIZE +
                    nameLength +
                    extraLength +
                    commentLength
                val centralDirectoryBytes = apk.readBytes(offset, centralRecordSize)
                val name = centralDirectoryBytes.copyOfRange(
                    CENTRAL_DIRECTORY_HEADER_SIZE,
                    CENTRAL_DIRECTORY_HEADER_SIZE + nameLength,
                ).toString(Charsets.UTF_8)
                val localHeaderOffset = centralDirectoryBytes.readUInt32(CEN_LOCAL_HEADER_OFFSET)
                val compressedSize = centralDirectoryBytes.readUInt32(CEN_COMPRESSED_SIZE_OFFSET)
                entries += SourceZipEntry(
                    name = name,
                    localHeaderOffset = localHeaderOffset,
                    compressedSize = compressedSize,
                    centralDirectoryBytes = centralDirectoryBytes,
                )
                offset += centralRecordSize.toLong()
            }

            entries.forEach { entry ->
                entry.localRecordSize = apk.calculateLocalRecordSize(
                    localHeaderOffset = entry.localHeaderOffset,
                    compressedSize = entry.compressedSize,
                )
            }
            return SourceZip(entries)
        }
    }

    private fun SourceZipEntry.centralDirectoryBytesWithOffset(localHeaderOffset: Long): ByteArray {
        return centralDirectoryBytes.copyOf().also { bytes ->
            bytes.writeUInt32(CEN_LOCAL_HEADER_OFFSET, localHeaderOffset)
        }
    }

    private fun RandomAccessFile.calculateLocalRecordSize(
        localHeaderOffset: Long,
        compressedSize: Long,
    ): Long {
        require(readUInt32(localHeaderOffset) == LOCAL_FILE_HEADER_SIGNATURE) {
            "Invalid APK local file header"
        }
        val flags = readUInt16(localHeaderOffset + LOC_FLAGS_OFFSET)
        val nameLength = readUInt16(localHeaderOffset + LOC_NAME_LENGTH_OFFSET)
        val extraLength = readUInt16(localHeaderOffset + LOC_EXTRA_LENGTH_OFFSET)
        val compressedDataOffset = localHeaderOffset +
            LOCAL_FILE_HEADER_SIZE.toLong() +
            nameLength +
            extraLength
        val descriptorSize = if ((flags and GENERAL_PURPOSE_DATA_DESCRIPTOR_FLAG) != 0) {
            val descriptorOffset = compressedDataOffset + compressedSize
            if (readUInt32(descriptorOffset) == DATA_DESCRIPTOR_SIGNATURE) {
                DATA_DESCRIPTOR_WITH_SIGNATURE_SIZE
            } else {
                DATA_DESCRIPTOR_SIZE
            }
        } else {
            0
        }
        return LOCAL_FILE_HEADER_SIZE.toLong() +
            nameLength +
            extraLength +
            compressedSize +
            descriptorSize
    }

    private fun RandomAccessFile.copyRangeTo(
        outputStream: OutputStream,
        offset: Long,
        byteCount: Long,
    ): Long {
        seek(offset)
        var remaining = byteCount
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (remaining > 0L) {
            val readCount = read(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
            if (readCount < 0) {
                error("Unexpected EOF while copying APK entry")
            }
            outputStream.write(buffer, 0, readCount)
            remaining -= readCount
        }
        return byteCount
    }

    private fun RandomAccessFile.findEndOfCentralDirectory(): Long {
        val fileSize = length()
        val tailSize = minOf(fileSize, MAX_EOCD_SEARCH_SIZE.toLong()).toInt()
        val tailOffset = fileSize - tailSize
        val tail = readBytes(tailOffset, tailSize)
        for (index in tail.size - MIN_EOCD_SIZE downTo 0) {
            if (tail.readUInt32(index) == END_OF_CENTRAL_DIRECTORY_SIGNATURE) {
                return tailOffset + index
            }
        }
        error("APK end of central directory not found")
    }

    private fun RandomAccessFile.readBytes(offset: Long, size: Int): ByteArray {
        val bytes = ByteArray(size)
        seek(offset)
        readFully(bytes)
        return bytes
    }

    private fun RandomAccessFile.readUInt16(offset: Long): Int {
        seek(offset)
        return readUnsignedByte() or (readUnsignedByte() shl 8)
    }

    private fun RandomAccessFile.readUInt32(offset: Long): Long {
        seek(offset)
        return readUnsignedByte().toLong() or
            (readUnsignedByte().toLong() shl 8) or
            (readUnsignedByte().toLong() shl 16) or
            (readUnsignedByte().toLong() shl 24)
    }

    private fun OutputStream.writeLocalFileHeader(
        name: ByteArray,
        crc32: Long,
        compressedSize: Long,
        uncompressedSize: Long,
    ) {
        writeUInt32(LOCAL_FILE_HEADER_SIGNATURE)
        writeUInt16(ZIP_VERSION_NEEDED)
        writeUInt16(GENERAL_PURPOSE_UTF8_FLAG)
        writeUInt16(DEFLATED_METHOD)
        writeUInt16(0)
        writeUInt16(0)
        writeUInt32(crc32)
        writeUInt32(compressedSize)
        writeUInt32(uncompressedSize)
        writeUInt16(name.size)
        writeUInt16(0)
        write(name)
    }

    private fun OutputStream.writeEndOfCentralDirectory(
        entryCount: Int,
        centralDirectorySize: Long,
        centralDirectoryOffset: Long,
    ) {
        writeUInt32(END_OF_CENTRAL_DIRECTORY_SIGNATURE)
        writeUInt16(0)
        writeUInt16(0)
        writeUInt16(entryCount)
        writeUInt16(entryCount)
        writeUInt32(centralDirectorySize)
        writeUInt32(centralDirectoryOffset)
        writeUInt16(0)
    }

    private fun buildCentralDirectoryEntry(
        name: ByteArray,
        crc32: Long,
        compressedSize: Long,
        uncompressedSize: Long,
        localHeaderOffset: Long,
    ): ByteArray {
        return ByteArrayOutputStream().use { outputStream ->
            outputStream.writeUInt32(CENTRAL_DIRECTORY_HEADER_SIGNATURE)
            outputStream.writeUInt16(ZIP_VERSION_NEEDED)
            outputStream.writeUInt16(ZIP_VERSION_NEEDED)
            outputStream.writeUInt16(GENERAL_PURPOSE_UTF8_FLAG)
            outputStream.writeUInt16(DEFLATED_METHOD)
            outputStream.writeUInt16(0)
            outputStream.writeUInt16(0)
            outputStream.writeUInt32(crc32)
            outputStream.writeUInt32(compressedSize)
            outputStream.writeUInt32(uncompressedSize)
            outputStream.writeUInt16(name.size)
            outputStream.writeUInt16(0)
            outputStream.writeUInt16(0)
            outputStream.writeUInt16(0)
            outputStream.writeUInt16(0)
            outputStream.writeUInt32(0)
            outputStream.writeUInt32(localHeaderOffset)
            outputStream.write(name)
            outputStream.toByteArray()
        }
    }

    private fun ByteArray.readUInt32(offset: Int): Long {
        return (this[offset].toInt() and BYTE_MASK).toLong() or
            ((this[offset + 1].toInt() and BYTE_MASK).toLong() shl 8) or
            ((this[offset + 2].toInt() and BYTE_MASK).toLong() shl 16) or
            ((this[offset + 3].toInt() and BYTE_MASK).toLong() shl 24)
    }

    private fun ByteArray.writeUInt32(offset: Int, value: Long) {
        this[offset] = (value and BYTE_MASK.toLong()).toByte()
        this[offset + 1] = ((value shr 8) and BYTE_MASK.toLong()).toByte()
        this[offset + 2] = ((value shr 16) and BYTE_MASK.toLong()).toByte()
        this[offset + 3] = ((value shr 24) and BYTE_MASK.toLong()).toByte()
    }

    private fun OutputStream.writeUInt16(value: Int) {
        write(value and BYTE_MASK)
        write((value shr 8) and BYTE_MASK)
    }

    private fun OutputStream.writeUInt32(value: Long) {
        write((value and BYTE_MASK.toLong()).toInt())
        write(((value shr 8) and BYTE_MASK.toLong()).toInt())
        write(((value shr 16) and BYTE_MASK.toLong()).toInt())
        write(((value shr 24) and BYTE_MASK.toLong()).toInt())
    }

    private fun String.isSignatureEntry(): Boolean {
        return APK_SIGNATURE_ENTRY_REGEX.matches(this)
    }

    private companion object {
        const val BYTE_MASK = 0xff
        const val DEFAULT_BUFFER_SIZE = 64 * 1024
        const val MAX_EOCD_SEARCH_SIZE = 65_557
        const val MIN_EOCD_SIZE = 22
        const val LOCAL_FILE_HEADER_SIZE = 30
        const val CENTRAL_DIRECTORY_HEADER_SIZE = 46
        const val LOCAL_FILE_HEADER_SIGNATURE = 0x04034b50L
        const val CENTRAL_DIRECTORY_HEADER_SIGNATURE = 0x02014b50L
        const val END_OF_CENTRAL_DIRECTORY_SIGNATURE = 0x06054b50L
        const val DATA_DESCRIPTOR_SIGNATURE = 0x08074b50L
        const val ZIP_VERSION_NEEDED = 20
        const val GENERAL_PURPOSE_UTF8_FLAG = 0x0800
        const val GENERAL_PURPOSE_DATA_DESCRIPTOR_FLAG = 0x0008
        const val DEFLATED_METHOD = 8
        const val DATA_DESCRIPTOR_SIZE = 12
        const val DATA_DESCRIPTOR_WITH_SIGNATURE_SIZE = 16
        const val EOCD_ENTRY_COUNT_OFFSET = 10L
        const val EOCD_CENTRAL_DIRECTORY_OFFSET = 16L
        const val LOC_FLAGS_OFFSET = 6L
        const val LOC_NAME_LENGTH_OFFSET = 26L
        const val LOC_EXTRA_LENGTH_OFFSET = 28L
        const val CEN_COMPRESSED_SIZE_OFFSET = 20
        const val CEN_NAME_LENGTH_OFFSET = 28L
        const val CEN_EXTRA_LENGTH_OFFSET = 30L
        const val CEN_COMMENT_LENGTH_OFFSET = 32L
        const val CEN_LOCAL_HEADER_OFFSET = 42
        val APK_SIGNATURE_ENTRY_REGEX = Regex(
            """META-INF/[^/]+\.(RSA|DSA|EC|SF|MF)""",
            RegexOption.IGNORE_CASE,
        )
    }
}
