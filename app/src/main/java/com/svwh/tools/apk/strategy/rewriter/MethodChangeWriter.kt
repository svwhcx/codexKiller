package com.svwh.tools.apk.strategy.rewriter

import android.util.Log
import org.jf.dexlib2.builder.MutableMethodImplementation
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.iface.MethodImplementation
import org.jf.dexlib2.iface.reference.MethodReference
import org.jf.dexlib2.immutable.ImmutableMethod
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference
import org.jf.dexlib2.rewriter.Rewriter

/**
 * @description 更改一个方法中的某条指令
 * @Author chenxin
 * @Date 2025/5/15 22:50
 */
class MethodChangeWriter(private val superClazz:String): Rewriter<Method> {
    override fun rewrite(method: Method): Method {
        if (method.name == "onCreate" || method.name == "attachBaseContext" || method.name == "<init>"){
            val implementation = getImplementation(method)
            return ImmutableMethod(
                method.definingClass,
                method.name,
                method.parameters,
                method.returnType,
                method.accessFlags,
                method.annotations,
                implementation
            )
        }
        return method;
    }

    /**
     * 重写里面的supper class执行方法
     */
    private fun getImplementation(originMethod: Method): MethodImplementation {
        val originMethodImplementation = originMethod.implementation!!
        val i = MutableMethodImplementation(originMethodImplementation)
        val mutableIndex = mutableListOf< Int>()
        i.instructions.forEachIndexed { index, it ->
            if (it is BuilderInstruction35c){
                val reference = it.reference
                if (reference is MethodReference){
                    if (reference.definingClass == "Landroid/app/Application;"){
                        Log.i("调用父类方法", "${it.reference}")
                        mutableIndex.add(index)
                    }
                }

            }
        }
        mutableIndex.forEach {
            val builderInstruction = i.instructions[it] as BuilderInstruction35c
            val reference = builderInstruction.reference as MethodReference
            val newReference = ImmutableMethodReference(
                superClazz,
                reference.name,
                reference.parameterTypes,
                reference.returnType,
            )
            val builderInstruction35c = BuilderInstruction35c(
                builderInstruction.opcode,
                builderInstruction.registerCount,
                builderInstruction.registerC,
                builderInstruction.registerD,
                builderInstruction.registerE,
                builderInstruction.registerF,
                builderInstruction.registerG,
                newReference
            )
            i.replaceInstruction(it, builderInstruction35c)
        }

        return i;
    }
}
