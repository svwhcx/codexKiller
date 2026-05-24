package com.svwh.tools.apk.strategy.rewriter

import org.jf.dexlib2.base.reference.BaseTypeReference
import org.jf.dexlib2.iface.Annotation
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.Field
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.rewriter.Rewriter

/**
 * @description
 * @Author chenxin
 * @Date 2024/11/24 20:56
 */
class SuperClassChangeWriter(private val targetClass: String,private val superClassSignature: String):
    Rewriter<ClassDef> {
    override fun rewrite(classDef: ClassDef): ClassDef {
        // 判断类签名是否是定义的App
        if (classDef.type != targetClass) {
            return classDef
        }
        return RewrittenClassDef(classDef,superClassSignature)
    }


    class RewrittenClassDef(private val classDef: ClassDef, private val superClassSignature: String): BaseTypeReference(),ClassDef {


        override fun getType(): String {
            return classDef.type
        }

        override fun getAnnotations(): MutableSet<out Annotation> {
            return classDef.annotations
        }

        override fun getAccessFlags(): Int {
            return classDef.accessFlags
        }

        override fun getSuperclass(): String {
            return superClassSignature
        }

        override fun getInterfaces(): MutableList<String> {
            return classDef.interfaces
        }

        override fun getSourceFile(): String? {
            return classDef.sourceFile
        }

        override fun getStaticFields(): MutableIterable<Field> {
            return classDef.staticFields
        }

        override fun getInstanceFields(): MutableIterable<Field> {
            return classDef.instanceFields
        }

        override fun getFields(): MutableIterable<Field> {
            return classDef.fields
        }

        override fun getDirectMethods(): MutableIterable<Method> {
            return classDef.directMethods
        }

        override fun getVirtualMethods(): MutableIterable<Method> {
            return classDef.virtualMethods
        }

        override fun getMethods(): MutableIterable<Method> {
            return classDef.methods
        }
    }


}