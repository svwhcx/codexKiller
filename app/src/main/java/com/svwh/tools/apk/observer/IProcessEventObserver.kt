package com.svwh.tools.apk.observer

/**
 * apk处理过程的观察者
 * @description
 * @Author chenxin
 * @Date 2025/8/3 14:17
 */
interface IProcessEventObserver {

    fun onProcessEvent(event: ProcessEvent)
}

/**
 * 处理事件类型
 */
enum class ProcessEventType {
    START,
    PROGRESS,
    SUCCESS,
    FAIL,
    END
}

/**
 * 处理事件结果类型
 */
enum class ProcessEventResult {
    SUCCESS,
    FAIL
}

/**
 * 具体的处理事件
 */
data class ProcessEvent(val eventType: ProcessEventType, val message: String="", val time: String, val result: ProcessEventResult)