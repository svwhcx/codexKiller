package com.svwh.tools.apk.strategy.rewriter

import org.jf.dexlib2.dexbacked.DexBackedDexFile
import org.jf.dexlib2.dexbacked.reference.DexBackedMethodReference

/**
 * @description
 * @Author chenxin
 * @Date 2025/5/15 23:39
 */
class KillerDexBackedMethodReference(private val clazz: String, dexFile: DexBackedDexFile, methodIndex: Int) :
    DexBackedMethodReference(dexFile, methodIndex) {

    override fun getDefiningClass(): String {
        return clazz;
    }

}