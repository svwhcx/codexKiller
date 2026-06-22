package com.svwh.tools.apk.strategy.rewriter

import android.util.Log
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.MutableMethodImplementation
import org.jf.dexlib2.builder.instruction.BuilderInstruction10x
import org.jf.dexlib2.builder.instruction.BuilderInstruction3rc
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
class MethodChangeWriter(
    private val superClazz: String,
    private val clearOnCreate: Boolean = false,
    private val injectAttachBootstrap: Boolean = false,
) : Rewriter<Method> {
    override fun rewrite(method: Method): Method {
        if (method.isApplicationOnCreate() && clearOnCreate) {
            val implementation = method.implementation ?: return method
            return method.withImplementation(emptyVoidImplementation(implementation.registerCount))
        }
        if (method.isApplicationOnCreate() || method.isAttachBaseContext() || method.name == "<init>") {
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
        if (originMethod.isAttachBaseContext() && injectAttachBootstrap) {
            i.addInstruction(0, attachBootstrapInstruction(originMethodImplementation.registerCount))
        }
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

    private fun emptyVoidImplementation(registerCount: Int): MethodImplementation {
        return MutableMethodImplementation(registerCount).apply {
            addInstruction(BuilderInstruction10x(Opcode.RETURN_VOID))
        }
    }

    private fun attachBootstrapInstruction(registerCount: Int): BuilderInstruction3rc {
        val baseContextRegister = registerCount - 1
        return BuilderInstruction3rc(
            Opcode.INVOKE_STATIC_RANGE,
            baseContextRegister,
            1,
            ImmutableMethodReference(
                KILLER_BASE_APPLICATION,
                "beforeAttachBaseContext",
                listOf("Landroid/content/Context;"),
                "V",
            ),
        )
    }

    private fun Method.withImplementation(implementation: MethodImplementation): Method {
        return ImmutableMethod(
            definingClass,
            name,
            parameters,
            returnType,
            accessFlags,
            annotations,
            implementation,
        )
    }

    private fun Method.isApplicationOnCreate(): Boolean {
        return name == "onCreate" && returnType == "V" && parameterTypes.isEmpty()
    }

    private fun Method.isAttachBaseContext(): Boolean {
        return name == "attachBaseContext" &&
            returnType == "V" &&
            parameterTypes.size == 1 &&
            parameterTypes[0].toString() == "Landroid/content/Context;"
    }

    private companion object {
        const val KILLER_BASE_APPLICATION = "Lcom/svwh/noenvhook/app/KillerBaseApplication;"
    }
}
