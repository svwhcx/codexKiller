package com.svwh.tools.apk

object MethodSignatureUtil {
    fun convertSmaliSignature(name: String): String {
        return "L$name;".replace(".", "/")
    }
}
