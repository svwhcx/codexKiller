package com.svwh.tools.feature.hookconfig.data

import com.svwh.tools.feature.hookconfig.domain.model.HookLogTypeOption
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HookLogTypeRegistry @Inject constructor() {
    private val types = listOf(
        HookLogTypeOption(0, "自定义 Hook"),
        HookLogTypeOption(5, "点击事件"),
        HookLogTypeOption(6, "弹窗显示"),
        HookLogTypeOption(8, "文本设置"),
        HookLogTypeOption(9, "Toast 显示"),
        HookLogTypeOption(10, "页面跳转"),
        HookLogTypeOption(16, "截屏检测"),
        HookLogTypeOption(17, "签名读取"),
        HookLogTypeOption(26, "VPN 检测"),
        HookLogTypeOption(27, "文件写入"),
        HookLogTypeOption(28, "文件读取"),
        HookLogTypeOption(29, "文件删除"),
        HookLogTypeOption(31, "Assets 读取"),
        HookLogTypeOption(32, "SP 读取"),
        HookLogTypeOption(33, "SP 写入"),
        HookLogTypeOption(34, "摘要算法"),
        HookLogTypeOption(35, "加解密算法"),
    )

    fun options(): List<HookLogTypeOption> = types

    fun titleOf(type: Int): String = types.firstOrNull { it.type == type }?.title ?: "类型 $type"
}
