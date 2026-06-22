package com.svwh.tools.apk.strategy

import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * @description
 * @Author chenxin
 * @Date 2025/8/3 10:42
 */
data class ExtraDataNode(
    val nodeName: String,
    private val nodeData: ByteArray,
) {
    constructor(nodeName: String, nodeDataIs: InputStream) : this(
        nodeName = nodeName,
        nodeData = nodeDataIs.use { it.readBytes() },
    )

    fun openInputStream(): InputStream {
        return ByteArrayInputStream(nodeData)
    }
}
